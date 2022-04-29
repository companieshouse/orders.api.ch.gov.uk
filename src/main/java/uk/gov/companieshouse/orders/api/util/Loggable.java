package uk.gov.companieshouse.orders.api.util;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface Loggable {
    Map<String, Object> getLogMap();
    String getMessage();
    HttpServletRequest getRequest();
}
