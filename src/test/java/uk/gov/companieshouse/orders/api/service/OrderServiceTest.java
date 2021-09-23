package uk.gov.companieshouse.orders.api.service;

import org.junit.Before;
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
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

/**
 * Unit tests the {@link OrderService} class.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

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

    private static final String CHECKOUT_ID = "0002";
    private static final String ORDER_ID = "0001";

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
}
