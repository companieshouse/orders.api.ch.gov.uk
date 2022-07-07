package uk.gov.companieshouse.orders.api.service;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

import com.mongodb.MongoException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.OrderReceived;
import uk.gov.companieshouse.orders.api.exception.ForbiddenException;
import uk.gov.companieshouse.orders.api.exception.MongoOperationException;
import uk.gov.companieshouse.orders.api.kafka.OrderReceivedMessageProducer;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    private final CheckoutToOrderMapper mapper;
    private final CheckoutRepository checkoutRepository;
    private final OrderRepository orderRepository;
    private final LinksGeneratorService linksGeneratorService;
    private final OrderReceivedMessageProducer ordersMessageProducer;
    private final SearchFieldMapper searchFieldMapper;

    @Value("${uk.gov.companieshouse.orders.api.orders}")
    private String orderEndpoint;

    public OrderService(final CheckoutToOrderMapper mapper, final CheckoutRepository checkoutRepository,
                        final OrderRepository orderRepository, OrderReceivedMessageProducer producer,
                        final LinksGeneratorService linksGeneratorService,
                        final SearchFieldMapper searchFieldMapper) {
        this.mapper = mapper;
        this.checkoutRepository = checkoutRepository;
        this.orderRepository = orderRepository;
        this.ordersMessageProducer = producer;
        this.linksGeneratorService = linksGeneratorService;
        this.searchFieldMapper = searchFieldMapper;
    }

    /**
     * Used to create an order from a checkout object once payment has been successful.
     * @param checkout the user's checkout object
     * @return the resulting order
     */
    public Order createOrder(final Checkout checkout) {
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.CHECKOUT_ID, checkout.getId());
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.USER_ID, checkout.getUserId());
        LOGGER.info("Creating order", logMap);
        final Order mappedOrder = mapper.checkoutToOrder(checkout);
        setCreationDateTimes(mappedOrder);
        mappedOrder.getData().setLinks(linksGeneratorService.generateOrderLinks(mappedOrder.getId()));

        LoggingUtils.logIfNotNull(logMap, LoggingUtils.ORDER_ID, mappedOrder.getId());
        final Optional<Order> order = orderRepository.findById(mappedOrder.getId());
        order.ifPresent(
            o -> {
                   final String message = "Order ID " + o.getId() + " already exists. Will not update.";
                   LOGGER.error(message, logMap);
                   throw new ForbiddenException(message);
            }
        );

        Order savedOrder;
        try {
            savedOrder = orderRepository.save(mappedOrder);
        } catch (MongoException ex) {
            String errorMessage = String.format("Failed to save order with id %s", mappedOrder.getId());
            LOGGER.error(errorMessage, ex, logMap);
            throw new MongoOperationException(errorMessage, ex);
        }

        LOGGER.info("Publishing notification to Kafka 'order-received' topic for order - " + mappedOrder.getId(), logMap);
        sendOrderReceivedMessage(mappedOrder.getId());

        return savedOrder;
    }

    public Optional<Order> getOrder(String id) {
        return orderRepository.findById(id);
    }

    public Optional<Checkout> getCheckout(String id) {
        return checkoutRepository.findById(id);
    }

    /**
     * Resends a message to the Kafka 'order-received' topic for an existing order.
     * @param order - the order to be reprocessed
     */
    public void reprocessOrder(final Order order) {
        final String orderId = order.getId();
        final Map<String, Object> logMap = LoggingUtils.createLogMap();
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.ORDER_ID, orderId);
        LOGGER.info("REPUBLISHING notification to Kafka 'order-received' topic for order - " + orderId + ".", logMap);
        sendOrderReceivedMessage(order.getId());
    }

    /**
     * Sends a message to Kafka topic 'order-received'
     * @param orderId order id
     */
    private void sendOrderReceivedMessage(String orderId) {
        String orderURI = orderEndpoint + "/" + orderId;
        OrderReceived orderReceived = new OrderReceived();
        orderReceived.setOrderUri(orderURI);
        ordersMessageProducer.sendMessage(orderId, orderReceived);
    }

    /**
     * Sets the created at and updated at date time 'timestamps' to now.
     * @param order the order to be 'timestamped'
     */
    void setCreationDateTimes(final Order order) {
        final LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.getData().setOrderedAt(now);
    }

    void setOrderEndpoint(String orderEndpoint) {
        this.orderEndpoint = orderEndpoint;
    }
}
