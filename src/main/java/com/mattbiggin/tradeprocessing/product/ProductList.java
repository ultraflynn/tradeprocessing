package com.mattbiggin.tradeprocessing.product;

import java.util.Map;

public record ProductList(Map<String, String> products, boolean sortable) {
}
