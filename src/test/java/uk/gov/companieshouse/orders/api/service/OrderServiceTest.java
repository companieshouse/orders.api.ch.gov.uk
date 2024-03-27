package uk.gov.companieshouse.orders.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.mongodb.MongoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.OrderReceived;
import uk.gov.companieshouse.orders.api.dto.PatchOrderedItemDTO;
import uk.gov.companieshouse.orders.api.exception.MongoOperationException;
import uk.gov.companieshouse.orders.api.kafka.OrderReceivedMessageProducer;
import uk.gov.companieshouse.orders.api.mapper.CheckoutToOrderMapper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemStatus;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
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
    private OrderReceivedMessageProducer ordersMessageProducer;
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
    private Order order;
    @Mock
    private OrderData orderData;
    @Mock
    private Item midItem;
    @Mock
    private Item certifiedCopyItem;
    @Mock
    private Item certificateItem;

    @BeforeEach
    void setup() {
        serviceUnderTest.setOrderEndpoint("endpoint");
    }

    @Test
    void createOrderCreatesOrder() {
        // Given
        final Order order = new Order();
        order.setId(ORDER_ID);
        when(mapper.checkoutToOrder(checkout)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);

        // When and then
        assertThat(serviceUnderTest.createOrder(checkout), is(order));
        OrderReceived expectedOrderReceived = new OrderReceived("endpoint/"+ORDER_ID, 0);
        verify(linksGeneratorService, times(1)).generateOrderLinks(ORDER_ID);
        verify(ordersMessageProducer).sendMessage(ORDER_ID, expectedOrderReceived);
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
    @DisplayName("Fetch order item")
    void getOrderItem() {
        // given
        when(order.getData()).thenReturn(orderData);
        when(orderData.getItems()).thenReturn(Arrays.asList(midItem, certifiedCopyItem, certificateItem));
        when(midItem.getId()).thenReturn("MID-123456-123456");
        when(certifiedCopyItem.getId()).thenReturn("CCD-123456-123456");
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // when
        Optional<Item> actual = serviceUnderTest.getOrderItem(ORDER_ID, "CCD-123456-123456");

        // then
        assertEquals(certifiedCopyItem, actual.get());
    }

    @Test
    @DisplayName("Fetch order item returns Optional.empty if no matching item found")
    void getOrderItemReturnsEmptyOptionalIfNoMatchingItemFound() {
        // given
        when(order.getData()).thenReturn(orderData);
        when(orderData.getItems()).thenReturn(Arrays.asList(midItem, certifiedCopyItem, certificateItem));
        when(midItem.getId()).thenReturn("MID-123456-123456");
        when(certifiedCopyItem.getId()).thenReturn("CCD-123456-123456");
        when(certificateItem.getId()).thenReturn("CRT-123456-123456");
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        // when
        Optional<Item> actual = serviceUnderTest.getOrderItem(ORDER_ID, "UNKNOWN");

        // then
        assertEquals(Optional.empty(), actual);
    }

    @Test
    @DisplayName("Fetch order item returns Optional.empty if no matching order found")
    void getOrderItemReturnsEmptyOptionalIfNoMatchingOrderFound() {
        // given
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        // when
        Optional<Item> actual = serviceUnderTest.getOrderItem(ORDER_ID, "UNKNOWN");

        // then
        assertEquals(Optional.empty(), actual);
    }


    @Test
    @DisplayName("Patch order item")
    void patchOrderItem() {
        // given
        Item item = new Item();
        orderData.setItems(Collections.singletonList(new Item()));
        item.setId("CCD-123456-123456");
        item.setKind("item#certified-copy");
        item.setCompanyNumber("00000000");
        item.setStatus(ItemStatus.UNKNOWN);

        when(order.getData()).thenReturn(orderData);
        when(orderData.getItems()).thenReturn(Collections.singletonList(item));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        PatchOrderedItemDTO patchOrderedItemDTO = new PatchOrderedItemDTO();
        patchOrderedItemDTO.setDigitalDocumentLocation("/updated/document/location");
        patchOrderedItemDTO.setStatus(ItemStatus.SATISFIED);

        // when
        Optional<Item> actual = serviceUnderTest.patchOrderItem(ORDER_ID, "CCD-123456-123456" , patchOrderedItemDTO);

        // then
        assertEquals(item, actual.get());
        assertEquals(ItemStatus.SATISFIED, actual.get().getStatus());
        assertEquals("/updated/document/location", actual.get().getDigitalDocumentLocation());
    }

    @Test
    @DisplayName("Patch order item throws MongoException")
    void patchOrderItemMongoException() {
        // given
        when(order.getData()).thenReturn(orderData);
        when(orderData.getItems()).thenReturn(Arrays.asList(midItem, certifiedCopyItem, certificateItem));
        when(midItem.getId()).thenReturn("MID-123456-123456");
        when(certifiedCopyItem.getId()).thenReturn("CCD-123456-123456");
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenThrow(new MongoException("Simulated MongoException"));

        PatchOrderedItemDTO patchOrderedItemDTO = new PatchOrderedItemDTO();
        patchOrderedItemDTO.setDigitalDocumentLocation("/updated/document/location");
        patchOrderedItemDTO.setStatus(ItemStatus.SATISFIED);

        // Call the service method and expect a MongoOperationException
        assertThrows(MongoOperationException.class, () ->
            serviceUnderTest.patchOrderItem(ORDER_ID, "CCD-123456-123456" , patchOrderedItemDTO)
        );
    }
}
