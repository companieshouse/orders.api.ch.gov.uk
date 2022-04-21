package uk.gov.companieshouse.orders.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.kafka.OrderReceivedMessageProducer;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.ActionedBy;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.HRef;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderCriteria;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.model.OrderLinks;
import uk.gov.companieshouse.orders.api.model.OrderSearchCriteria;
import uk.gov.companieshouse.orders.api.model.OrderSearchResults;
import uk.gov.companieshouse.orders.api.model.OrderSummary;
import uk.gov.companieshouse.orders.api.model.Links;
import uk.gov.companieshouse.orders.api.model.PageCriteria;
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
    private Order orderResult;
    @Mock
    private OrderData orderData;
    @Mock
    private ActionedBy orderedBy;
    @Mock
    private OrderLinks links;
    @Mock
    private Item item;
    @Mock
    private SearchFieldMapper searchFieldMapper;
    @Mock
    private PageCriteria pageCriteria;

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
    @DisplayName("search orders returns an expected order with all details populated")
    void searchOrders() {
        //given
        when(orderSearchCriteria.getOrderCriteria()).thenReturn(orderCriteria);
        when(orderCriteria.getOrderId()).thenReturn("ORD-123-456");
        when(orderCriteria.getEmail()).thenReturn("demo@ch.gov.uk");
        when(orderCriteria.getCompanyNumber()).thenReturn("12345678");
        when(orderRepository.searchOrders(anyString(), anyString(), anyString())).thenReturn(
                Collections.singletonList(orderResult));
        when(orderResult.getId()).thenReturn("ORD-123-456");
        when(orderResult.getData()).thenReturn(orderData);
        when(orderData.getOrderedBy()).thenReturn(orderedBy);
        when(orderedBy.getEmail()).thenReturn("demo@ch.gov.uk");
        when(orderData.getItems()).thenReturn(Collections.singletonList(item));
        when(item.getKind()).thenReturn("item#certificate");
        when(orderResult.getCreatedAt()).thenReturn(LocalDate.of(2022, 04, 11).atStartOfDay());
        when(orderData.getLinks()).thenReturn(links);
        when(links.getSelf()).thenReturn("http");
        when(searchFieldMapper.exactMatchOrAny("ORD-123-456")).thenReturn("mapped order id");
        when(searchFieldMapper.exactMatchOrAny("12345678")).thenReturn("mapped company number");
        when(searchFieldMapper.partialMatchOrAny("demo@ch.gov.uk")).thenReturn("mapped email");

        OrderSearchResults expected = new OrderSearchResults(1,
                Collections.singletonList(
                        OrderSummary.newBuilder()
                                .withId("ORD-123-456")
                                .withEmail("demo@ch.gov.uk")
                                .withProductLine("item#certificate")
                                .withOrderDate(LocalDate.of(2022, 04, 11).atStartOfDay())
                                .withLinks(new Links(new HRef("http"), new HRef("http")))
                                .build()));

        //when
        OrderSearchResults actual = serviceUnderTest.searchOrders(orderSearchCriteria);

        //then
        verify(orderRepository).searchOrders("mapped order id",
                "mapped email",
                "mapped company number");
        assertThat(actual, is(expected));
    }

    @Test
    @DisplayName("search orders returns an order with blank details")
    void searchOrdersWithBlankDetails() {
        //given
        when(orderSearchCriteria.getOrderCriteria()).thenReturn(orderCriteria);
        when(orderCriteria.getOrderId()).thenReturn("");
        when(orderCriteria.getEmail()).thenReturn("");
        when(orderCriteria.getCompanyNumber()).thenReturn("");
        when(orderRepository.searchOrders(anyString(), anyString(), anyString())).thenReturn(
                Collections.singletonList(orderResult));
        when(searchFieldMapper.exactMatchOrAny(anyString())).thenReturn("mapped string");
        when(searchFieldMapper.partialMatchOrAny(anyString())).thenReturn("mapped string");

        OrderSummary orderSummary = OrderSummary.newBuilder().build();

        OrderSearchResults expected = new OrderSearchResults(1,
                Collections.singletonList(orderSummary));

        //when
        OrderSearchResults actual = serviceUnderTest.searchOrders(orderSearchCriteria);

        //then
        assertThat(actual, is(expected));
    }

    @Test
    @DisplayName("search orders returns a single order when page size is one")
    void searchOrdersLimitsSearchResults() {
        //given
        when(orderSearchCriteria.getOrderCriteria()).thenReturn(orderCriteria);
        when(orderSearchCriteria.getPageCriteria()).thenReturn(pageCriteria);
        when(orderCriteria.getOrderId()).thenReturn("");
        when(orderCriteria.getEmail()).thenReturn("");
        when(orderCriteria.getCompanyNumber()).thenReturn("");
        when(pageCriteria.getPageSize()).thenReturn(1);
        when(orderRepository.searchOrders(anyString(), anyString(), anyString())).thenReturn(
                Arrays.asList(orderResult, orderResult));
        when(searchFieldMapper.exactMatchOrAny(anyString())).thenReturn("mapped string");
        when(searchFieldMapper.partialMatchOrAny(anyString())).thenReturn("mapped string");

        //when
        OrderSearchResults actual = serviceUnderTest.searchOrders(orderSearchCriteria);

        //then
        assertThat(actual.getTotalOrders(), is(2));
        assertThat(actual.getOrderSummaries().size(), is(1));
    }
}
