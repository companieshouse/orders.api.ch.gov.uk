package uk.gov.companieshouse.orders.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.REQUEST_ID_HEADER_NAME;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.orders.api.dto.BasketItemDTO;
import uk.gov.companieshouse.orders.api.dto.BasketPaymentRequestDTO;
import uk.gov.companieshouse.orders.api.dto.BasketRequestDTO;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.mapper.BasketMapper;
import uk.gov.companieshouse.orders.api.mapper.ItemMapper;
import uk.gov.companieshouse.orders.api.model.ApiError;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
import uk.gov.companieshouse.orders.api.service.ApiClientService;
import uk.gov.companieshouse.orders.api.service.BasketService;
import uk.gov.companieshouse.orders.api.service.CheckoutService;
import uk.gov.companieshouse.orders.api.service.ItemEnricher;
import uk.gov.companieshouse.orders.api.service.ItemEnrichmentException;
import uk.gov.companieshouse.orders.api.service.OrderService;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;
import uk.gov.companieshouse.orders.api.util.TimestampedEntityVerifier;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

/**
 * Partially unit tests the {@link BasketController} class.
 */
@ExtendWith(MockitoExtension.class)
class BasketControllerTest {

    private static final String USER_ID = "user_id";

    @InjectMocks
    private BasketController controllerUnderTest;

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private OrderService orderService;

    @Mock
    private BasketService basketService;

    @Mock
    private Checkout checkout;

    @Mock
    private CheckoutData checkoutData;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private EricHeaderHelper ericHeaderHelper;

    @Mock
    private Item certificateResource, certificate, document, missingImage;

    @Mock
    private ItemEnricher enricher;

    @Mock
    private Basket retrievedBasket, mappedBasket;

    @Mock
    private BasketData retrievedBasketData, mappedBasketData;

    @Mock
    private BasketMapper basketMapper;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BasketItemDTO basketResponse;

    @Test
    @DisplayName("Fetch basket containing multiple items")
    void fetchBasketWithMultipleItems() throws Exception {
        // given
        Basket basket = createBasket();
        basket.getData().setItems(Arrays.asList(certificate, document, missingImage));
        when(basketService.getBasketById(any())).thenReturn(Optional.of(basket));
        lenient().when(httpServletRequest.getHeader(eq("ERIC-Access-Token"))).thenReturn(USER_ID);
        when(enricher.enrichItemsByIdentifiers(any(), any(), any())).thenReturn(Arrays.asList(certificate, document, missingImage));

        // when
        ResponseEntity<?> responseEntity = controllerUnderTest.getBasket(httpServletRequest, "requestId");

        // then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(Arrays.asList(certificate, document, missingImage),
                ((BasketData)responseEntity.getBody()).getItems());
        verify(enricher).enrichItemsByIdentifiers(eq(Arrays.asList(certificate, document, missingImage)), eq(USER_ID), any());
    }

    @Test
    @DisplayName("Fetch basket containing multiple items returns HTTP 400 Bad Request if exception thrown handling item")
    void fetchBasketWithMultipleItemsReturnsBadRequest() throws Exception {
        // given
        Basket basket = createBasket();
        basket.getData().setItems(Arrays.asList(certificate, document, missingImage));
        when(basketService.getBasketById(any())).thenReturn(Optional.of(basket));
        lenient().when(httpServletRequest.getHeader(eq("ERIC-Access-Token"))).thenReturn(USER_ID);
        when(enricher.enrichItemsByIdentifiers(any(), any(), any())).thenThrow(ItemEnrichmentException.class);

        // when
        ResponseEntity<?> responseEntity = controllerUnderTest.getBasket(httpServletRequest, "requestId");

        // then
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        verify(enricher).enrichItemsByIdentifiers(eq(Arrays.asList(certificate, document, missingImage)), eq(USER_ID), any());
    }

    @Test
    @DisplayName("Return 200 OK for when no items are present in GET basket")
    void returnOKWhenNoItemsPresentRequestGetItems() throws Exception {

        Basket basket = createBasket();

        when(basketService.getBasketById(any())).thenReturn(Optional.of(basket));

        ResponseEntity<?> responseEntity = controllerUnderTest.getBasket(httpServletRequest, "requestId");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Patch payment details PAID status update is saved to checkout")
    void patchPaymentDetailsPaidStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Patch payment details FAILED status update is saved to checkout")
    void patchPaymentDetailsFailedStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Patch payment details PENDING status update is saved to checkout")
    void patchPaymentDetailsPendingStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Patch payment details EXPIRED status update is saved to checkout")
    void patchPaymentDetailsExpiredStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.EXPIRED);
    }

    @Test
    @DisplayName("Patch payment details IN_PROGRESS status update is saved to checkout")
    void patchPaymentDetailsInProgressStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Patch payment details NO_FUNDS status update is saved to checkout")
    void patchPaymentDetailsNoFundsStatusUpdateIsSaved() throws IOException {
        patchPaymentDetailsStatusUpdateIsSaved(PaymentStatus.NO_FUNDS);
    }

    @Test
    @DisplayName("Add item to basket returns HTTP 200 OK if item resource exists and retrieved "
            + "basket is empty")
    void addItemToBasketWhenBasketResourceIsEmpty() throws IOException {
        // given
        BasketRequestDTO basketRequest = new BasketRequestDTO();
        basketRequest.setItemUri("/path/to/item");
        when(apiClientService.getItem(any(), any())).thenReturn(certificateResource);
        when(basketService.getBasketById(any())).thenReturn(Optional.of(retrievedBasket));
        when(retrievedBasket.getData()).thenReturn(retrievedBasketData);
        when(basketMapper.addToBasketRequestDTOToBasket(any())).thenReturn(mappedBasket);
        when(mappedBasket.getData()).thenReturn(mappedBasketData);
        when(mappedBasketData.getItems()).thenReturn(Collections.singletonList(certificate));
        when(itemMapper.itemToBasketItemDTO(any())).thenReturn(basketResponse);
        when(httpServletRequest.getHeader(ERIC_IDENTITY_HEADER_NAME)).thenReturn("id");
        when(httpServletRequest.getHeader(REQUEST_ID_HEADER_NAME)).thenReturn("request_id");
        when(httpServletRequest.getHeader("ERIC-Access-Token")).thenReturn("passthrough");

        // when
        ResponseEntity<Object> actual = controllerUnderTest.addItemToBasket(basketRequest,
                httpServletRequest, "123");

        // then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(basketResponse, actual.getBody());
        verify(apiClientService).getItem("passthrough", "/path/to/item");
        verify(basketService).getBasketById("id");
        verify(basketService).saveBasket(retrievedBasket);
        verify(itemMapper).itemToBasketItemDTO(certificateResource);
    }

    @Test
    @DisplayName("Add item to basket returns HTTP 200 OK if item resource exists and no basket "
            + "resource exists for the user")
    void addItemToBasketWhenBasketResourceNonexistent() throws IOException {
        // given
        BasketRequestDTO basketRequest = new BasketRequestDTO();
        basketRequest.setItemUri("/path/to/item");
        when(apiClientService.getItem(any(), any())).thenReturn(certificateResource);
        when(basketService.getBasketById(any())).thenReturn(Optional.empty());
        when(basketMapper.addToBasketRequestDTOToBasket(any())).thenReturn(mappedBasket);
        when(itemMapper.itemToBasketItemDTO(any())).thenReturn(basketResponse);
        when(httpServletRequest.getHeader(ERIC_IDENTITY_HEADER_NAME)).thenReturn("id");
        when(httpServletRequest.getHeader(REQUEST_ID_HEADER_NAME)).thenReturn("request_id");
        when(httpServletRequest.getHeader("ERIC-Access-Token")).thenReturn("passthrough");

        // when
        ResponseEntity<Object> actual = controllerUnderTest.addItemToBasket(basketRequest,
                httpServletRequest, "123");

        // then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(basketResponse, actual.getBody());
        verify(apiClientService).getItem("passthrough", "/path/to/item");
        verify(basketService).getBasketById("id");
        verify(mappedBasket).setId("id");
        verify(basketService).saveBasket(mappedBasket);
        verify(itemMapper).itemToBasketItemDTO(certificateResource);
    }

    @Test
    @DisplayName("Add item to basket returns HTTP 400 Bad Request if ApiClientService throws an "
            + "exception when retrieving an item")
    void addItemToBasketReturnsBadRequestIfApiClientThrowsException() throws IOException {
        // given
        BasketRequestDTO basketRequest = new BasketRequestDTO();
        basketRequest.setItemUri("/path/to/item");
        when(apiClientService.getItem(any(), any())).thenThrow(IOException.class);
        when(httpServletRequest.getHeader(REQUEST_ID_HEADER_NAME)).thenReturn("request_id");
        when(httpServletRequest.getHeader("ERIC-Access-Token")).thenReturn("passthrough");

        // when
        ResponseEntity<Object> actual = controllerUnderTest.addItemToBasket(basketRequest,
                httpServletRequest, "123");

        // then
        assertEquals(HttpStatus.BAD_REQUEST, actual.getStatusCode());
        assertEquals(new ApiError(BAD_REQUEST, ErrorType.BASKET_ITEM_INVALID.getValue()), actual.getBody());
        verify(apiClientService).getItem("passthrough", "/path/to/item");
        verifyNoInteractions(basketService);
    }

    @Test
    @DisplayName("Append item to basket returns 200 OK")
    void appendItemToBasketWhenBasketResourceIsEmpty() throws IOException {
        // given
        BasketRequestDTO basketRequest = new BasketRequestDTO();
        basketRequest.setItemUri("/path/to/item");
        when(apiClientService.getItem(any(), any())).thenReturn(certificateResource);
        when(basketService.getBasketById(any())).thenReturn(Optional.of(retrievedBasket));
        when(retrievedBasket.getData()).thenReturn(retrievedBasketData);
        when(basketMapper.addToBasketRequestDTOToBasket(any())).thenReturn(mappedBasket);
        when(mappedBasket.getData()).thenReturn(mappedBasketData);
        when(mappedBasketData.getItems()).thenReturn(Collections.singletonList(certificate));
        when(itemMapper.itemToBasketItemDTO(any())).thenReturn(basketResponse);
        when(httpServletRequest.getHeader(ERIC_IDENTITY_HEADER_NAME)).thenReturn("id");
        when(httpServletRequest.getHeader(REQUEST_ID_HEADER_NAME)).thenReturn("request_id");
        when(httpServletRequest.getHeader("ERIC-Access-Token")).thenReturn("passthrough");

        // when
        ResponseEntity<Object> actual = controllerUnderTest.appendItemToBasket(basketRequest,
                httpServletRequest, "123");

        // then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(basketResponse, actual.getBody());
        verify(apiClientService).getItem("passthrough", "/path/to/item");
        verify(basketService).getBasketById("id");
        verify(basketService).saveBasket(retrievedBasket);
        verify(itemMapper).itemToBasketItemDTO(certificateResource);
    }

    @Test
    @DisplayName("Append item to basket returns 200 OK with duplicate item in basket")
    void appendItemToBasketWhenBasketResourceContainsItem() throws IOException {
        // given
        BasketRequestDTO basketRequest = new BasketRequestDTO();
        basketRequest.setItemUri("/path/to/item");
        Item savedCertificateItem = new Item();
        savedCertificateItem.setItemUri("/path/to/item");
        Item newCertificateItem = new Item();
        newCertificateItem.setItemUri("/path/to/item");
        when(apiClientService.getItem(any(), any())).thenReturn(certificateResource);
        when(basketService.getBasketById(any())).thenReturn(Optional.of(retrievedBasket));
        when(retrievedBasket.getData()).thenReturn(retrievedBasketData);
        when(retrievedBasketData.getItems()).thenReturn(Collections.singletonList(savedCertificateItem));
        when(basketMapper.addToBasketRequestDTOToBasket(any())).thenReturn(mappedBasket);
        when(mappedBasket.getData()).thenReturn(mappedBasketData);
        when(mappedBasketData.getItems()).thenReturn(Collections.singletonList(newCertificateItem));
        when(itemMapper.itemToBasketItemDTO(any())).thenReturn(basketResponse);
        when(httpServletRequest.getHeader(ERIC_IDENTITY_HEADER_NAME)).thenReturn("id");
        when(httpServletRequest.getHeader(REQUEST_ID_HEADER_NAME)).thenReturn("request_id");
        when(httpServletRequest.getHeader("ERIC-Access-Token")).thenReturn("passthrough");

        // when
        ResponseEntity<Object> actual = controllerUnderTest.appendItemToBasket(basketRequest,
                httpServletRequest, "123");

        // then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(basketResponse, actual.getBody());
        verify(apiClientService).getItem("passthrough", "/path/to/item");
        verify(basketService).getBasketById("id");
        verify(itemMapper).itemToBasketItemDTO(certificateResource);
        verify(basketService, times(0)).saveBasket(retrievedBasket);
    }

    @Test
    @DisplayName("Append item to list of basket items if no duplicate found")
    void appendItemToBasketWhenBasketResourceDoesNotContainsDuplicateItem() throws IOException {
        // given
        List<Item> persistedBasketItems = new ArrayList<>();
        persistedBasketItems.add(certificate);
        BasketRequestDTO basketRequest = new BasketRequestDTO();
        basketRequest.setItemUri("/path/to/item");
        when(apiClientService.getItem(any(), any())).thenReturn(certificateResource);
        when(basketService.getBasketById(any())).thenReturn(Optional.of(retrievedBasket));
        when(retrievedBasket.getData()).thenReturn(retrievedBasketData);
        when(retrievedBasketData.getItems()).thenReturn(persistedBasketItems);
        when(basketMapper.addToBasketRequestDTOToBasket(any())).thenReturn(mappedBasket);
        when(mappedBasket.getData()).thenReturn(mappedBasketData);
        when(mappedBasketData.getItems()).thenReturn(Collections.singletonList(document));
        when(itemMapper.itemToBasketItemDTO(any())).thenReturn(basketResponse);
        when(httpServletRequest.getHeader(ERIC_IDENTITY_HEADER_NAME)).thenReturn("id");
        when(httpServletRequest.getHeader(REQUEST_ID_HEADER_NAME)).thenReturn("request_id");
        when(httpServletRequest.getHeader("ERIC-Access-Token")).thenReturn("passthrough");

        // when
        ResponseEntity<Object> actual = controllerUnderTest.appendItemToBasket(basketRequest,
                httpServletRequest, "123");

        // then
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(basketResponse, actual.getBody());
        assertEquals(Arrays.asList(certificate, document), persistedBasketItems);
        verify(apiClientService).getItem("passthrough", "/path/to/item");
        verify(basketService).getBasketById("id");
        verify(itemMapper).itemToBasketItemDTO(certificateResource);
    }

    /**
     * Verifies that the controller has requested the checkout service to save the updated status to the checkout,
     * and made any further updates required to the checkout for that updated status.
     * @param paymentOutcome the payment status updated value
     */
    private void patchPaymentDetailsStatusUpdateIsSaved(final PaymentStatus paymentOutcome) throws IOException {

        // Given
        final String checkout_id = "123456789";
        final String payment_id = "987654321";
        final String eric_header = "EricHeader";
        final LocalDateTime paidAt = LocalDateTime.now();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ApiSdkManager.getEricPassthroughTokenHeader(), eric_header);
        request.setRequestURI("/basket/checkouts/" + checkout_id + "/payment");

        final BasketPaymentRequestDTO paymentStatusUpdate = new BasketPaymentRequestDTO();
        paymentStatusUpdate.setStatus(paymentOutcome);
        paymentStatusUpdate.setPaidAt(paidAt);
        paymentStatusUpdate.setPaymentReference(payment_id);

        final PaymentApi paymentSummary = new PaymentApi();
        paymentSummary.setStatus("paid");
        paymentSummary.setAmount("211.03");
        paymentSummary.setLinks(new HashMap<String, String>() {{
            put("resource", "/basket/checkouts/" + checkout_id + "/payment");
        }});

        when(checkoutService.getCheckoutById(checkout_id)).thenReturn(Optional.of(checkout));
        when(checkout.getData()).thenReturn(checkoutData);
        if (paymentOutcome.equals(PaymentStatus.PAID)) {
            mockCheckoutDataItems();
            when(apiClientService.getPaymentSummary("EricHeader", payment_id)).thenReturn(paymentSummary);
        }

        // When
        controllerUnderTest.patchBasketPaymentDetails(paymentStatusUpdate, request, checkout_id, "requestId");

        // Then
        verify(checkoutData).setStatus(paymentOutcome);
        if (paymentOutcome.equals(PaymentStatus.PAID)) {
            verify(checkoutData).setPaidAt(paidAt);
            verify(checkoutData).setPaymentReference(payment_id);
        }
        verify(checkoutService).saveCheckout(checkout);
    }

    private void mockCheckoutDataItems() {
        ArrayList<Item> items = new ArrayList<>();

        Item mockItem1 = new Item();
        ItemCosts mockItemCosts1 = new ItemCosts();
        mockItemCosts1.setCalculatedCost("40.43");
        ItemCosts mockItemCosts2 = new ItemCosts();
        mockItemCosts2.setCalculatedCost("30.60");
        mockItem1.setItemCosts(Arrays.asList(mockItemCosts1, mockItemCosts2));

        Item mockItem2 = new Item();
        ItemCosts mockItemCosts3 = new ItemCosts();
        mockItemCosts3.setCalculatedCost("140");
        mockItem2.setItemCosts(Arrays.asList(mockItemCosts3));

        items.add(mockItem1);
        items.add(mockItem2);

        when(checkoutData.getItems()).thenReturn(items);
    }

    private Basket createBasket() {
        TimestampedEntityVerifier timestamps = new TimestampedEntityVerifier();
        final LocalDateTime start = timestamps.start();
        final Basket basket = new Basket();
        basket.setCreatedAt(start);
        basket.setUpdatedAt(start);
        basket.setId(ERIC_IDENTITY_VALUE);
        return basket;
    }
}
