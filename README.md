# Trade Processing - Practical coding task

- **Author** - Matt Biggin
- **Submission Date** - 8th March 2023

## REST API Endpoints

### Core API

#### POST /api/v1/enrich
```
curl --request POST --data-binary @src/test/resources/trade.csv --header 'Content-Type: text/csv' --header 'Accept: text/csv' http://localhost:8080/api/v1/enrich
```
Enriches the trade data with product names. Where product name is not available the product name
"Missing Product Name" is used and a warning issued to the logs. For each trade set only one
notification of missing product names is given. Validation is provided for trade dates and when not
of the form YYYYMMDD the trade is ignored and an error issued to the logs.

`--data-binary` is used in preference to `--data` to avoid `curl` stripping carriage returns. See below for details:

https://stackoverflow.com/questions/3872427/how-to-send-line-break-with-curl

_Sample output:_

```
date,product_name,currency,price
20160101,Treasury Bills Domestic,EUR,10.0
20160101,Corporate Bonds Domestic,EUR,20.1
20160101,REPO Domestic,EUR,30.34
20160101,Missing Product Name,EUR,35.34
```

### Additional API

Endpoints are also provided to manage the product static data after initial service
initialisation. The following endpoints allow the product list to be viewed and products
added, changed and removed.

#### GET /api/v1/products
```
curl --request GET http://localhost:8080/api/v1/products
```
Lists the current configured product static data.

_Sample output:_

```
product_id,product_name
1,Treasury Bills Domestic
2,Corporate Bonds Domestic
3,REPO Domestic
4,Interest rate swaps International
5,OTC Index Option
6,Currency Options
7,Reverse Repos International
8,REPO International
9,766A_CORP BD
10,766B_CORP BD
```

#### PUT /api/v1/products?product_id=ID&product_name=NAME
```
curl --request PUT http://localhost:8080/api/v1/products\?product_id\=1\&product_name\=Credit%20Default%20Swaps
```
Adds a new product to the static data. Should the product already exist the call will fail with
an appropriate error returned (`product_id already present`).

#### PATCH /api/v1/products?product_id=ID&product_name=NAME
```
curl --request PATCH http://localhost:8080/api/v1/products\?product_id\=1\&product_name\=Treasury%20Bills%20International
```
Updates existing product static data. Should the product not exist a warning is returned (`product_id not found`).

#### DELETE /api/v1/products?product_id=ID
```
curl --request POST --data-binary @trade.csv --header 'Content-Type: text/csv' --header 'Accept: text/csv' http://server.com/api/v1/enrich
```
Removes existing product static data. Should the product not exist an error is returned (`product_id not found`).

## Implementation Notes

### CSV Parsing
Consideration was given to using a 3rd party library (OpenCSV or Apache Commons CSVParser) for CSV parsing, all of which
would have been more resilient to malformations but I decided to take a simple approach instead by splitting on commas and
validating the number of columns that produces. The reasons for this decision were:

1. The addition of an external library introduces the potential for additional object creation and performance issues with very large sets of trades.
2. This service is not available to the internet and therefore the input can be tightly constrained. If not well-formed then the file can be rejected.
3. Memory performance and speed of execution were a critical factor and this led to a stream based processing approach. The adoption of a 3rd party library would have complicated the implementation without more time to assess the impact of the library.

### Large Sets of Trades / Products
The service is optimised for large sets of trade data rather than a large number of products. My working assumption being that
trade volumes and likely much higher than the number of products. As such the product static data is held in-memory and an
appropriate heap size would need to be selected to handle the volume.

A stream based approach is adopted for trade processing to minimise the memory requirements whilst processing uploads trade
data files. I saw this as the major use case and optimised accordingly.

Should the assumption that heap size could not be scaled to the product static data prove to be incorrect then the data structure
used to store the product static could be modified to use a `WeakHashMap` to act as a LRU cache but this would cause the product static data to
need to be reloaded on cache misses and would have a performance penalty. Before implementation full consideration would need to be given
to how to handle this scenario.

### `product_id` using String
There was no requirement specified whether the `product_id` was an int or a String. The sample data indicates an int but for speed of lookup
I decided to keep it as a String. Only in the `GET /api/v1/products` API is this impactful as it has an affect on the ordering of the output.

Also worth noting that the `price` is also treated as a String. No mathematical operations were required on this data and
therefore it remains as a String.

### Usage of `ConcurrentHashMap` in `Products`
A `ConcurrentHashMap` is used because I decided to include the `/api/v1/products` endpoints which could modify the products list concurrently. I saw
that as a valid change of implementation to ensure no issues with iterating over a collection that has been concurrently modified.

### Missing products are logged once
When a trade references a missing product that is logged once per missing product, rather than for each trade that is referencing it. I
envisaged the situation where many trades might reference a single missing product and that identifying the missing product was preferable
to filling up the logs with the same error.

### Readability over cleverness
I have specifically avoided trying to show off every language feature, design pattern or framework in this implementation. I have
tried to keep the implementation clean and easy to follow. I've focused on the single-responsibility principle and writing clean, extensible code. Where
appropriate I have injected dependencies in order to allow for better separation of concerns and testability.

### Test coverage
I've written both unit and API endpoint tests and achieved a good test coverage. The coverage percentage was not the goal, my
intention was to ensure that the code I wrote worked. I specifically did not target 100% coverage (though I did get close).

## Further development

### Input file validation
There are many issues with the input files that could cause issues:
- Missing header rows
- Usage of alternative delimiters
- Quoted string with commas included
- Invalid currency codes
- Invalid prices

This is where 3rd party libraries start to make sense, to be able to handle variations in the inputs. This service
is not resilient to these malformations currently.

### Load testing
Before usage in production this service would need to be performance tested under load, and the memory
usage monitored to ensure that it will work for all envisaged data set sizes.

### Concurrency testing
There should be no issue with multiple trade files being processed concurrently, but more testing is needed to determine
whether the inclusion of the `/api/v1/products` endpoints make sense in a production environment. They introduce the possibility
that the underlying static changes during processing and that may not be a valid use-case.