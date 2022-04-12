package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.OAUTH2_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_INVALID_TYPE_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UrlPathHelper;

/**
 * Unit/integration tests the {@link UserAuthenticationInterceptor} class.
 */
@DirtiesContext
@SpringBootTest
@EmbeddedKafka
public class UserAuthenticationInterceptorTests {

    @Autowired
    private UserAuthenticationInterceptor interceptorUnderTest;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @Test
    @DisplayName("preHandle accepts a request it has not been configured to authenticate")
    void preHandleAcceptsUnknownRequest() {

        // Given
        givenRequest(DELETE, "/unknown");

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle rejects add item request that lacks required headers")
    void preHandleRejectsUnauthenticatedAddItemRequest() {

        // Given
        givenRequest(POST, "/basket/items");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects checkout basket request that lacks required headers")
    void preHandleRejectsUnauthenticatedCheckoutBasketRequest() {

        // Given
        givenRequest(POST, "/basket/checkouts");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects get payment details request that lacks required headers")
    void preHandleRejectsUnauthenticatedGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects get basket request that lacks required headers")
    void preHandleRejectsUnauthenticatedGetBasketRequest() {

        // Given
        givenRequest(GET, "/basket");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects patch basket request that lacks required headers")
    void preHandleRejectsUnauthenticatedPatchBasketRequest() {

        // Given
        givenRequest(PATCH, "/basket");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects patch payment details request that lacks required headers")
    void preHandleRejectsUnauthenticatedPatchPaymentDetailsRequest() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/1234/payment");

        // When and then
        thenRequestIsRejected();
    }
    @Test
    @DisplayName("preHandle rejects get order request that lacks required headers")
    void preHandleRejectsUnauthenticatedGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects search request that lacks required headers")
    void preHandleRejectsUnauthenticatedSearchRequest() {

        // Given
        givenRequest(GET, "/orders/search");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle accepts add item request that has the required headers")
    void preHandleAcceptsAuthenticatedAddItemRequest() {

        // Given
        givenRequest(POST, "/basket/items");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts checkout basket request that has the required headers")
    void preHandleAcceptsAuthenticatedCheckoutBasketRequest() {

        // Given
        givenRequest(POST, "/basket/checkouts");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get payment details request that has signed in user headers")
    void preHandleAcceptsSignedInUserGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get payment details request that has authenticated API headers")
    void preHandleAcceptsAuthenticatedApiGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasAuthenticatedApi();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts patch basket request that has the required headers")
    void preHandleAcceptsAuthenticatedPatchBasketRequest() {

        // Given
        givenRequest(PATCH, "/basket");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts patch payment details request that has the required headers")
    void preHandleAcceptsAuthenticatedPatchPaymentDetailsRequest() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/1234/payment");
        givenRequestHasAuthenticatedApi();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get order request that has signed in user headers")
    void preHandleAcceptsSignedInUserGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get order request that has authenticated API headers")
    void preHandleAcceptsAuthenticatedApiGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasAuthenticatedApi();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get checkout request that has signed in user headers")
    void preHandleAcceptsSignedInUserGetCheckoutRequest() {

        // Given
        givenRequest(GET, "/checkouts/1234");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get checkout request that has authenticated API headers")
    void preHandleAcceptsAuthenticatedApiGetCheckoutRequest() {

        // Given
        givenRequest(GET, "/checkouts/1234");
        givenRequestHasAuthenticatedApi();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle rejects request from which identity header value is missing for single permissible auth type request")
    void preHandleRejectsMissingIdentityHeaderForSinglePermissibleAuthTypeRequest() {

        // Given
        givenRequest(POST, "/basket/items");
        givenRequestHasSignedInUserIdentityTypeOnly();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects request with incorrect identity type for single permissible auth type request")
    void preHandleRejectsInvalidIdentityTypeForSinglePermissibleAuthTypeRequest() {

        // Given
        givenRequest(POST, "/basket/items");
        givenRequestHasInvalidIdentityType();

        // When and then
        thenRequestIsRejected();
    }


    @Test
    @DisplayName("preHandle rejects request from which identity header value is missing for multiple permissible auth type request")
    void preHandleRejectsMissingIdentityHeaderForMultiplePermissibleAuthTypeRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasAuthenticatedApiIdentityTypeOnly();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects request with incorrect identity type for multiple permissible auth type request")
    void preHandleRejectsInvalidIdentityTypeForMultiplePermissibleAuthTypeRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasInvalidIdentityType();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects request with blank identity value")
    void preHandleRejectsBlankIdentityValue() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasBlankSignedInUserIdentity();

        // When and then
        thenRequestIsRejected();
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
    }

    /**
     * Sets up request with required header values to represent a signed in user.
     */
   private void givenRequestHasSignedInUser() {
       when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(OAUTH2_IDENTITY_TYPE);
       when(request.getHeader(ERIC_IDENTITY)).thenReturn(ERIC_IDENTITY_VALUE);
   }

    /**
     * Sets up request with required header values to represent an API client.
     */
    private void givenRequestHasAuthenticatedApi() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(API_KEY_IDENTITY_TYPE);
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(ERIC_IDENTITY_VALUE);
    }

    /**
     * Sets up request with an API client identity type, but no identity.
     */
    private void givenRequestHasAuthenticatedApiIdentityTypeOnly() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(API_KEY_IDENTITY_TYPE);
    }

    /**
     * Sets up request with a signed in user identity type, but no identity.
     */
    private void givenRequestHasSignedInUserIdentityTypeOnly() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(OAUTH2_IDENTITY_TYPE);
    }

    /**
     * Sets up request with an identity, but an invalid identity type.
     */
    private void givenRequestHasInvalidIdentityType() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(ERIC_IDENTITY_INVALID_TYPE_VALUE);
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(ERIC_IDENTITY_VALUE);
    }

    /**
     * Sets up request with required header values to represent a signed in user, but with a blank (empty string)
     * identity header value.
     */
    private void givenRequestHasBlankSignedInUserIdentity() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(OAUTH2_IDENTITY_TYPE);
        when(request.getHeader(ERIC_IDENTITY)).thenReturn("");
    }

    /**
     * Verifies that the authentication interceptor does not reject the request as unauthorised.
     */
    private void thenRequestIsAccepted() {
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(true));
        verify(response, never()).setStatus(anyInt());
    }

    /**
     * Verifies that the authentication interceptor blocks the request as unauthorised.
     */
    private void thenRequestIsRejected() {
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(false));
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }

}
