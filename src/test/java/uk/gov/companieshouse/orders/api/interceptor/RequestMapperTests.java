package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.ADD_ITEM;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.APPEND_BASKET_ITEM;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.BASKET;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.CHECKOUT_BASKET;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.GET_CHECKOUT;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.GET_CHECKOUT_ITEM;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.GET_ORDER;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.GET_ORDER_ITEM;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.GET_PAYMENT_DETAILS;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.PATCH_PAYMENT_DETAILS;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.SEARCH;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.GET_BASKET_LINKS;
import static uk.gov.companieshouse.orders.api.interceptor.RequestUris.REMOVE_BASKET_ITEM;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.RequestPath;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockHttpServletMapping;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.UrlPathHelper;

/**
 * Unit/integration tests the {@link RequestMapper} class.
 */
@DirtiesContext
@SpringBootTest
@EmbeddedKafka
class RequestMapperTests {

    @Autowired
    private RequestMapper requestMapperUnderTest;

    @Mock
    private HttpServletRequest request;

    @Test
    @DisplayName("getRequestMappingInfo gets the add item request mapping")
    void getRequestMappingInfoGetsAddItem() {

        String servletPath = "/basket/items";
        // Given
        givenRequest(POST, servletPath);

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(ADD_ITEM));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the checkout basket request mapping")
    void getRequestMappingInfoGetsCheckoutBasket() {

        String servletPath = "/basket/checkouts";
        // Given
        givenRequest(POST, servletPath);

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(CHECKOUT_BASKET));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get payment details request mapping")
    void getRequestMappingInfoGetsGetPaymentDetails() {

        // Given
        givenRequest(GET, "/basket/checkouts/{checkoutId}/payment");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(GET_PAYMENT_DETAILS));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get basket request mapping")
    void getRequestMappingInfoGetsGetBasket() {

        // Given
        givenRequest(GET, "/basket");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(BASKET));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the patch basket request mapping")
    void getRequestMappingInfoGetsPatchBasket() {

        // Given
        givenRequest(PATCH, "/basket");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(BASKET));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the patch payment details request mapping")
    void getRequestMappingInfoGetsPatchPaymentDetails() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/{id}/payment");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(PATCH_PAYMENT_DETAILS));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get order request mapping")
    void getRequestMappingInfoGetsGetOrder() {

        // Given
        givenRequest(GET, "/orders/{id}");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(GET_ORDER));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get order item request mapping")
    void getRequestMappingInfoGetsGetOrderItem() {

        // Given
        givenRequest(GET, "/orders/{orderId}/items/{itemId}");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(GET_ORDER_ITEM));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the search request mapping")
    void getRequestMappingInfoGetsSearch() {

        // Given
        givenRequest(GET, "/checkouts/search");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(SEARCH));
    }

    @Test
    @DisplayName("getRequestMappingInfo gets the get checkout request mapping")
    void getRequestMappingInfoGetsGetCheckout() {

        // Given
        givenRequest(GET, "/checkouts/{id}");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request).getName(), is(GET_CHECKOUT));
    }

    @Test
    @DisplayName("getRequestMappingInfo returns null where no mapping found")
    void getRequestMappingInfoReturnsNullWhereNoMappingFound() {

        // Given
        givenRequest(DELETE, "/unknown/uri");

        // When and then
        assertThat(requestMapperUnderTest.getRequestMapping(request), is(nullValue()));
    }

    @Test
    @DisplayName("getRequestMappingInfo returns the get basket links mapping")
    void getBasketLinks() {
        // Given
        givenRequest(GET, "/basket/links");

        // When
        String actual = requestMapperUnderTest.getRequestMapping(request).getName();

        // Then
        assertThat(actual, is(GET_BASKET_LINKS));
    }

    @Test
    @DisplayName("getRequestMappingInfo returns the remove basket item mapping")
    void removeBasketItem() {

        // Given
        givenRequest(PUT, "/basket/items/remove");

        // When
        String actual = requestMapperUnderTest.getRequestMapping(request).getName();

        // Then
        assertThat(actual, is(REMOVE_BASKET_ITEM));
    }

    @Test
    @DisplayName("getRequestMappingInfo returns the append item to basket mapping")
    void appendItemToBasket() {
        // Given
        givenRequest(POST, "/basket/items/append");

        // When
        String actual = requestMapperUnderTest.getRequestMapping(request).getName();

        // Then
        assertThat(actual, is(APPEND_BASKET_ITEM));
    }

    @Test
    @DisplayName("Return mapping for getCheckoutItem endpoint")
    void getCheckoutItem() {
        // given
        givenRequest(GET, "/checkouts/{checkoutId}/items/{itemId}");

        // when
        String actual = requestMapperUnderTest.getRequestMapping(request).getName();

        // then
        assertThat(actual, is(GET_CHECKOUT_ITEM));
    }

    /**
     * Sets up request givens.
     * @param method the HTTP request method
     * @param uri the request URI
     */
    private void givenRequest(final HttpMethod method, final String uri) {
        when(request.getMethod()).thenReturn(method.name());
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getContextPath()).thenReturn("");
        when(request.getServletPath()).thenReturn("");
        when(request.getAttribute( UrlPathHelper.class.getName() + ".PATH")).thenReturn(uri);
        when(request.getHttpServletMapping()).thenReturn(new MockHttpServletMapping("", "", "", null));
        RequestPath requestPath = ServletRequestPathUtils.parseAndCache(request);
        when(request.getServletPath()).thenReturn(uri);
        when(request.getAttribute(ServletRequestPathUtils.class.getName() + ".PATH")).thenReturn(requestPath);

    }
}
