package uk.gov.companieshouse.orders.api.util;

import java.util.Map;

public interface Loggable {
    Map<String, Object> getLogMap();
    String getMessage();
}
