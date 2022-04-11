package uk.gov.companieshouse.orders.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.kafka.OrderReceivedMessageProducer;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderCriteria;
import uk.gov.companieshouse.orders.api.model.OrderSearchCriteria;
import uk.gov.companieshouse.orders.api.model.OrderSearchResults;
import uk.gov.companieshouse.orders.api.model.OrderSummary;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

/**
 * Unit tests the {@link OrderService} class.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final String CHECKOUT_ID = "0002";
    private static final String ORDER_ID = "0001";
    @InjectMocks
    private OrderService serviceUnderTest;
    @Mock
    private Checkout checkout;
    @Mock
    private CheckoutToOrderMapper mapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CheckoutRepository checkoutRepository;
    @Mock
    private LinksGeneratorService linksGeneratorService;
    @Mock
    private OrderReceivedMessageProducer ordersMessageProducer;
    @Mock
    private OrderCriteria orderCriteria;
    @Mock
    private OrderSearchCriteria orderSearchCriteria;
    @Mock
    private List<Order> searchOrdersResult;
    @Mock
    private Order orderResult;

    @Test
    void createOrderCreatesOrder() {
        // Given
        final Order order = new Order();
        order.setId(ORDER_ID);
        when(mapper.checkoutToOrder(checkout)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        // When and then
        assertThat(serviceUnderTest.createOrder(checkout), is(order));
        verify(linksGeneratorService, times(1)).generateOrderLinks(ORDER_ID);

    }

    @Test
    @DisplayName("test getCheckout returns a checkout object")
    void getCheckout() {
        Checkout checkout = new Checkout();
        checkout.setId(CHECKOUT_ID);

        when(checkoutRepository.findById(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

        Optional<Checkout> returnedCheckout = serviceUnderTest.getCheckout(CHECKOUT_ID);

        assertNotNull(returnedCheckout);
        assertEquals(CHECKOUT_ID, returnedCheckout.get().getId());
    }

    @Test
    @DisplayName("test getOrder returns a order object")
    void getOrder() {
        Order order = new Order();
        order.setId(ORDER_ID);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        Optional<Order> returnedOrder = serviceUnderTest.getOrder(ORDER_ID);

        assertNotNull(returnedOrder);
        assertEquals(ORDER_ID, returnedOrder.get().getId());
    }

    @Test
    @DisplayName("search orders returns an expected list of orders")
    void searchOrders() {
        //given
        when(orderResult.getId()).thenReturn("ORD-123-456");
        when(searchOrdersResult.get(0)).thenReturn(orderResult);
        when(orderRepository.searchOrders(anyString(), anyString(), anyString())).thenReturn(
                searchOrdersResult);
        when(orderSearchCriteria.getOrderCriteria()).thenReturn(orderCriteria);
        OrderSummary orderSummary = OrderSummary.newBuilder().withId("ORD-123-456").build();
        OrderSearchResults expected = new OrderSearchResults(1,
                Collections.singletonList(orderSummary));

        //when
        OrderSearchResults actual = serviceUnderTest.searchOrders(orderSearchCriteria);

        //then
        assertThat(actual, is(expected));
    }

}
