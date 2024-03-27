package uk.gov.companieshouse.orders.api.controller;

import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.companieshouse.orders.api.OrdersApiApplication.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.controller.BasketController.CHECKOUT_ID_PATH_VARIABLE;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.REQUEST_ID;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.createLogMapWithRequestId;
import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.logIfNotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.dto.PatchOrderedItemDTO;
import uk.gov.companieshouse.orders.api.exception.ResourceNotFoundException;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutCriteria;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.CheckoutSearchCriteria;
import uk.gov.companieshouse.orders.api.model.CheckoutSearchResults;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
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

    public static final String ORDER_ITEM_URI = "/orders/{id}/items/{itemId}";
    public static final String GET_CHECKOUT_ITEM_URI = "/checkouts/{id}/items/{itemId}";

    /** <code>${uk.gov.companieshouse.orders.api.checkouts}/{id}</code> */
    public static final String GET_CHECKOUT_URI =
        "${uk.gov.companieshouse.orders.api.checkouts}/{" + CHECKOUT_ID_PATH_VARIABLE + "}";

    public static final String CHECKOUTS_SEARCH_URI = "${uk.gov.companieshouse.orders.api.search.checkouts}";

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
        Map<String, Object> logMap = createLogMapWithRequestId(requestId);
        logIfNotNull(logMap, LoggingUtils.ORDER_ID, id);
        LOGGER.info("Retrieving order", logMap);
        final Order orderRetrieved = orderService.getOrder(id)
                .orElseThrow(ResourceNotFoundException::new);
        logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
        LOGGER.info("Order found and returned", logMap);
        return ResponseEntity.ok().body(orderRetrieved.getData());
    }

    @GetMapping(ORDER_ITEM_URI)
    public ResponseEntity<Item> getOrderItem(final @PathVariable("id") String orderId,
                                             final @PathVariable("itemId") String itemId,
                                             final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        Map<String, Object> logMap = createLogMapWithRequestId(requestId);
        logIfNotNull(logMap, LoggingUtils.ORDER_ID, orderId);
        logIfNotNull(logMap, LoggingUtils.ITEM_ID, itemId);
        LOGGER.info("Retrieving order item", logMap);
        final Item item = orderService.getOrderItem(orderId, itemId)
                                                 .orElseThrow(ResourceNotFoundException::new);
        logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
        LOGGER.info("Order item found and returned", logMap);
        return ResponseEntity.ok().body(item);
    }

    @PatchMapping(ORDER_ITEM_URI)
    public ResponseEntity<Item> patchOrderItem(final @PathVariable("id") String orderId,
                                               final @PathVariable("itemId") String itemId,
                                               final @RequestBody PatchOrderedItemDTO patchOrderedItemDTO,
                                               final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        Map<String, Object> logMap = createLogMapWithRequestId(requestId);
        logIfNotNull(logMap, LoggingUtils.ORDER_ID, orderId);
        logIfNotNull(logMap, LoggingUtils.ITEM_ID, itemId);
        LOGGER.info("Patching order item", logMap);

        final Item patchedItem = orderService.patchOrderItem(orderId, itemId, patchOrderedItemDTO)
            .orElseThrow(ResourceNotFoundException::new);

        return ResponseEntity.ok().body(patchedItem);
    }

    @GetMapping(GET_CHECKOUT_ITEM_URI)
    public ResponseEntity<CheckoutData> getCheckoutItem(final @PathVariable("id") String checkoutId,
            final @PathVariable("itemId") String itemId,
            final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
        Map<String, Object> logMap = createLogMapWithRequestId(requestId);
        logIfNotNull(logMap, LoggingUtils.ORDER_ID, checkoutId);
        logIfNotNull(logMap, LoggingUtils.ITEM_ID, itemId);
        LOGGER.info("Retrieving checkout with item", logMap);
        final Checkout checkout = this.checkoutService.getCheckoutItem(checkoutId, itemId)
                                 .orElseThrow(ResourceNotFoundException::new);
        logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
        LOGGER.info("Checkout with item found and returned", logMap);
        return ResponseEntity.ok().body(checkout.getData());
    }

    @GetMapping(GET_CHECKOUT_URI)
    public ResponseEntity<CheckoutData> getCheckout(final @PathVariable(CHECKOUT_ID_PATH_VARIABLE) String id,
                                                    final @RequestHeader(REQUEST_ID_HEADER_NAME) String requestId) {
       Map<String, Object> logMap = createLogMapWithRequestId(requestId);
       logIfNotNull(logMap, LoggingUtils.CHECKOUT_ID, id);
       LOGGER.info("Retrieving checkout", logMap);
       final Checkout checkoutRetrieved = checkoutService.getCheckoutById(id)
           .orElseThrow(ResourceNotFoundException::new);
       logMap.put(LoggingUtils.STATUS, HttpStatus.OK);
       LOGGER.info("Checkout found and returned", logMap);
       return ResponseEntity.ok().body(checkoutRetrieved.getData());
   }

    @GetMapping(CHECKOUTS_SEARCH_URI)
    public ResponseEntity<CheckoutSearchResults> searchCheckouts(
            @RequestParam(value = "id", required = false) final String id,
            @RequestParam(value = "email", required = false) final String email,
            @RequestParam(value = "company_number", required = false) final String companyNumber,
            @RequestParam(value = "page_size", required = false) @NotNull(message = "page_size is mandatory") @Min(value = 1, message = "page_size must be greater than 0") final Integer pageSize,
            @RequestHeader(REQUEST_ID_HEADER_NAME) final String requestId) {
        LoggableBuilder loggableBuilder = LoggableBuilder.newBuilder()
                .withLogMapPut(REQUEST_ID, requestId)
                .withLogMapIfNotNullPut(LoggingUtils.ORDER_ID, id);
        log.info(loggableBuilder.withMessage("Search checkouts").build());
        CheckoutSearchCriteria checkoutSearchCriteria = new CheckoutSearchCriteria(
                CheckoutCriteria.newBuilder()
                        .withOrderId(id)
                        .withEmail(email)
                        .withCompanyNumber(companyNumber)
                        .build(),
                new PageCriteria(pageSize)
        );
        CheckoutSearchResults checkoutSearchResults = checkoutService.searchCheckouts(checkoutSearchCriteria);
        log.info(loggableBuilder.withLogMapPut(LoggingUtils.STATUS, HttpStatus.OK)
                .withMessage("Total checkouts found %d", checkoutSearchResults.getTotalOrders())
                .build());
        return ResponseEntity.ok().body(checkoutSearchResults);
    }

    @PostMapping(POST_REPROCESS_ORDER_URI)
    public ResponseEntity<String> reprocessOrder(@PathVariable(ORDER_ID_PATH_VARIABLE) final String id,
                                                 @RequestHeader(REQUEST_ID_HEADER_NAME) final String requestId) {
        final Map<String, Object> logMap = createLogMapWithRequestId(requestId);
        logIfNotNull(logMap, LoggingUtils.ORDER_ID, id);
        LOGGER.info("Reprocess order", logMap);

        final Optional<Order> order = orderService.getOrder(id);
        if (order.isPresent()) {
            orderService.reprocessOrder(order.get());
            final String confirmation = LocalDateTime.now() + ": Order number " + id + " reprocessed.";
            LOGGER.info(confirmation, logMap);
            return ResponseEntity.ok().body("\n" + confirmation + "\n");
        } else {
            final String error = buildMissingOrderFeedback(id);
            LOGGER.error(error, logMap);
            return ResponseEntity.status(CONFLICT).body("\n" + error + "\n");
        }
    }

    private String buildMissingOrderFeedback(final String orderId) {
        final Optional<Checkout> checkout = checkoutService.getCheckoutById(orderId);
        return  "*** " + LocalDateTime.now() + ": No order number " + orderId + " found. "
                + (checkout.map(c -> "Payment status was " + c.getData().getStatus() + ".")
                           .orElse("Is order number correct?")) + " ***";
    }

}
