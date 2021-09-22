package uk.gov.companieshouse.orders.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.service.OrderService;

import java.util.Map;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.REQUEST_ID_HEADER_NAME;

@RestController
public class OrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final OrderService orderService;

    public static final String ID_PATH_VARIABLE = "id";

    /** <code>${uk.gov.companieshouse.orders.api.orders}/{id}</code> */
    public static final String GET_ORDER_URI =
            "${uk.gov.companieshouse.orders.api.orders}/{" + ID_PATH_VARIABLE + "}";

    /** <code>${uk.gov.companieshouse.orders.api.checkouts}/{id}</code> */
    public static final String GET_CHECKOUT_URI =
        "${uk.gov.companieshouse.orders.api.checkouts}/{" + ID_PATH_VARIABLE + "}";

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(GET_ORDER_URI)
    public ResponseEntity<OrderData> getOrder(final @PathVariable(ID_PATH_VARIABLE) String id,
                                              final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.ORDER_ID, id);
        LOGGER.info("Retrieving order", logMap);
        final Order orderRetrieved = orderService.getOrder(id)
                .orElseThrow(ResourceNotFoundException::new);
        logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
        LOGGER.info("Order found and returned", logMap);
        return ResponseEntity.ok().body(orderRetrieved.getData());
    }

   @GetMapping(GET_CHECKOUT_URI)
    public ResponseEntity<CheckoutData> getCheckout(final @PathVariable(ID_PATH_VARIABLE) String id,
                                                    final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
       Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
       LoggingUtils.logIfNotNull(logMap, LoggingUtils.CHECKOUT_ID, id);
       final Checkout checkoutRetrieved = orderService.getCheckout(id)
           .orElseThrow(ResourceNotFoundException::new);
       logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
       LOGGER.info("Checkout found and returned", logMap);
       return ResponseEntity.ok().body(checkoutRetrieved.getData());
   }
}
