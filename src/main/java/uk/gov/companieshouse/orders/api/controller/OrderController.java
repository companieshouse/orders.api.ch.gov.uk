package uk.gov.companieshouse.orders.api.controller;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.controller.BasketController.CHECKOUT_ID_PATH_VARIABLE;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.REQUEST_ID;

import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderCriteria;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.model.OrderSearchCriteria;
import uk.gov.companieshouse.orders.api.model.OrderSearchResults;
import uk.gov.companieshouse.orders.api.model.PageCriteria;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.service.OrderService;
import uk.gov.companieshouse.orders.api.util.Log;
import uk.gov.companieshouse.orders.api.util.LoggableBuilder;

@Validated
@RestController
public class OrderController {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    public static final String ORDER_ID_PATH_VARIABLE = "id";

    /** <code>${uk.gov.companieshouse.orders.api.orders}/{id}</code> */
    public static final String GET_ORDER_URI =
            "${uk.gov.companieshouse.orders.api.orders}/{" + ORDER_ID_PATH_VARIABLE + "}";

    /** <code>${uk.gov.companieshouse.orders.api.checkouts}/{id}</code> */
    public static final String GET_CHECKOUT_URI =
        "${uk.gov.companieshouse.orders.api.checkouts}/{" + CHECKOUT_ID_PATH_VARIABLE + "}";

    public static final String ORDERS_SEARCH_URI = "${uk.gov.companieshouse.orders.api.search.orders}";

    public static final String POST_REPROCESS_ORDER_URI =
            "${uk.gov.companieshouse.orders.api.orders}/{" + ORDER_ID_PATH_VARIABLE + "}/reprocess";

    private final OrderService orderService;
    private final CheckoutService checkoutService;
    private final Log log;

    public OrderController(OrderService orderService, CheckoutService checkoutService, Log log) {
        this.orderService = orderService;
        this.checkoutService = checkoutService;
        this.log = log;
    }

    @GetMapping(GET_ORDER_URI)
    public ResponseEntity<OrderData> getOrder(final @PathVariable(ORDER_ID_PATH_VARIABLE) String id,
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
    public ResponseEntity<CheckoutData> getCheckout(final @PathVariable(CHECKOUT_ID_PATH_VARIABLE) String id,
                                                    final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
       Map<String, Object> logMap = LoggingUtils.createLogMapWithRequestId(requestId);
       LoggingUtils.logIfNotNull(logMap, LoggingUtils.CHECKOUT_ID, id);
       LOGGER.info("Retrieving checkout", logMap);
       final Checkout checkoutRetrieved = checkoutService.getCheckoutById(id)
           .orElseThrow(ResourceNotFoundException::new);
       logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
       LOGGER.info("Checkout found and returned", logMap);
       return ResponseEntity.ok().body(checkoutRetrieved.getData());
   }

    @GetMapping(ORDERS_SEARCH_URI)
    public ResponseEntity<OrderSearchResults> searchOrders(
            @RequestParam(value = "id", required = false) final String id,
            @RequestParam(value = "email", required = false) final String email,
            @RequestParam(value = "company_number", required = false) final String companyNumber,
            @RequestParam(value = "page_size", required = false) @NotNull(message = "page_size is mandatory") @Min(value = 1, message = "page_size must be greater than 0") final Integer pageSize,
            @RequestHeader(REQUEST_ID_HEADER_NAME) final String requestId) {
        LoggableBuilder loggableBuilder = LoggableBuilder.newBuilder()
                .withLogMapPut(REQUEST_ID, requestId)
                .withLogMapIfNotNullPut(LoggingUtils.ORDER_ID, id);
        log.info(loggableBuilder.withMessage("Search orders").build());
        OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria(
                OrderCriteria.newBuilder()
                        .withOrderId(id)
                        .withEmail(email)
                        .withCompanyNumber(companyNumber)
                        .build(),
                new PageCriteria(pageSize)
        );
        OrderSearchResults orderSearchResults = orderService.searchOrders(orderSearchCriteria);
        log.info(loggableBuilder.withLogMapPut(LoggingUtils.STATUS, HttpStatus.OK)
                .withMessage("Total orders found %d", orderSearchResults.getTotalOrders())
                .build());
        return ResponseEntity.ok().body(orderSearchResults);
    }

    @PostMapping(POST_REPROCESS_ORDER_URI)
    public ResponseEntity<?> reprocessOrder(@PathVariable(ORDER_ID_PATH_VARIABLE) final String id,
                                            @RequestHeader(REQUEST_ID_HEADER_NAME) final String requestId) {
        final LoggableBuilder loggableBuilder = LoggableBuilder.newBuilder()
                .withLogMapPut(REQUEST_ID, requestId)
                .withLogMapIfNotNullPut(LoggingUtils.ORDER_ID, id);
        log.info(loggableBuilder.withMessage("Reprocess order").build());
        return ResponseEntity.ok().build();
    }
}
