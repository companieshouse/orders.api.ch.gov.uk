package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.OAUTH2_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_INVALID_TYPE_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

import java.util.stream.Stream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class UserAuthenticationInterceptorTests {

    @Autowired
    private UserAuthenticationInterceptor interceptorUnderTest;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @MockBean
    private SecurityManager securityManager;

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
        postToUriExpectingRejection("/basket/items");
    }

    @Test
    @DisplayName("preHandle rejects checkout basket request that lacks required headers")
    void preHandleRejectsUnauthenticatedCheckoutBasketRequest() {
        postToUriExpectingRejection("/basket/checkouts");
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

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("unauthenticatedRequestFixtures")
    void preHandleRejectsUnauthenticatedRequest(final String displayName, String uri) {

        // Given
        givenRequest(GET, uri);

        // When and then
        thenRequestIsRejected();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("signedInPostRequestFixtures")
    void preHandleAcceptsPostRequestForSignedInUsers(String displayName, String uri) {
        // Given
        givenRequest(POST, uri);
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts remove basket item request from a user with OAuth2 authentication")
    void preHandleAcceptsPostRequestForSignedInUsers() {
        // Given
        givenRequest(PUT, "/basket/items/remove");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("signedInGetRequestFixtures")
    void preHandleAcceptsGetRequestForSignedInUsers(String displayName, String uri) {
        // Given
        givenRequest(GET, uri);
        givenRequestHasSignedInUser();

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
    @DisplayName("preHandle accepts get checkout request that has signed in user headers")
    void preHandleAcceptsSignedInUserGetCheckoutRequest() {
        // Given
        when(securityManager.checkIdentity()).thenReturn(true);
        givenRequest(GET, "/checkouts/1234");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsAccepted();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("authenticatedRequestFixtures")
    void preHandleAcceptsAuthenticatedApiRequest(String displayName, String uriPath) {
        // Given
        when(securityManager.checkIdentity()).thenReturn(true);
        givenRequest(GET, uriPath);
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

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("hasAdminAuthenticationFixtures")
    void ordersSearchValidIdentity(String displayName, String endpoint, boolean isValid) {
        when(securityManager.checkIdentity()).thenReturn(isValid);
        givenRequest(GET, endpoint);

        boolean actual = interceptorUnderTest.preHandle(request, response, handler);

        assertThat(actual, is(isValid));
    }

    @Test
    @DisplayName("preHandle rejects post reprocess order request that is unauthenticated")
    void preHandleRejectsUnauthenticatedPostReprocessOrderRequest() {
        postToUriExpectingRejection("/orders/1234/reprocess");
    }

    @Test
    @DisplayName("preHandle rejects post reprocess order request that is from an authenticated user")
    void preHandleRejectsAuthenticatedUserPostReprocessOrderRequest() {

        // Given
        givenRequest(POST, "/orders/1234/reprocess");
        givenRequestHasSignedInUser();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle accepts post reprocess order request that is from an internal API")
    void preHandleAcceptsAuthenticatedInternalApiPostReprocessOrderRequestRequest() {

        // Given
        givenRequest(POST, "/basket/checkouts/1234/payment");
        givenRequestHasAuthenticatedApi();

        // When and then
        thenRequestIsAccepted();
    }

    /**
     * Sets up a POST request to the URI provided, expects the request to be rejected
     * @param uri the request URI
     */
    private void postToUriExpectingRejection(final String uri) {

        // Given
        givenRequest(POST, uri);

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

    private static Stream<Arguments> authenticatedRequestFixtures() {
        return Stream.of(arguments("preHandle accepts get checkout request that has authenticated API headers", "/checkouts/1234"),
                arguments("preHandle accepts get order request that has authenticated API headers", "/orders/1234"),
                arguments("preHandle accepts get order item request that has authenticated API headers", "/orders/1234/items/5678"),
                arguments("preHandle accepts get payment details request that has authenticated "
                        + "API headers", "/basket/checkouts/1234/payment"));
    }

    private static Stream<Arguments> unauthenticatedRequestFixtures() {
        return Stream.of(arguments("preHandle rejects get payment details request that lacks required headers", "/basket/checkouts/1234/payment"),
                arguments("preHandle rejects get basket request that lacks required headers", "/basket"),
                arguments("preHandle rejects get order request that lacks required headers", "/orders/1234"),
                arguments("preHandle rejects get order item request that lacks required headers", "/orders/1234/items/5678"),
                arguments("preHandle rejects get basket links request that lacks requires "
                        + "headers", "/basket/links"));
    }

    private static Stream<Arguments> signedInPostRequestFixtures() {
        return Stream.of(arguments("preHandle accepts add item request that has the required headers", "/basket/items"),
                arguments("preHandle accepts checkout basket request that has the required "
                        + "headers", "/basket/checkouts"),
                arguments("preHandle accepts append item request that has the required headers",
                        "/basket/items/append"));
    }

    private static Stream<Arguments> signedInGetRequestFixtures() {
        return Stream.of(arguments("preHandle accepts get payment details request that has signed in user headers", "/basket/checkouts/1234/payment"),
                arguments("preHandle accepts get order request that has signed in user headers", "/orders/1234"),
                arguments("preHandle accepts get order item request that has signed in user headers", "/orders/1234/items/5678"),
                arguments("preHandle accepts get basket links request from a user with OAuth2 authentication", "/basket/links"));
    }

    private static Stream<Arguments> hasAdminAuthenticationFixtures() {
        return Stream.of(arguments("Authentication for orders/search endpoint succeeds if caller identity is valid", "/checkouts/search", true),
                arguments("Authentication for orders/search endpoint fails if caller identity is invalid", "/checkouts/search", false),
                arguments("Authentication for get order item endpoint succeeds if caller identity is valid", "/orders/1234/items/5678", true),
                arguments("Authentication for get order item endpoint fails if caller identity is"
                        + " invalid", "/orders/1234/items/5678", false),
                arguments("Authentication for get checkout item endpoint succeeds if caller "
                        + "identity is valid", "/checkouts/1234/items/5678", true),
                arguments("Authentication for get checkout item endpoint fails if caller identity "
                        + "is invalid", "/checkouts/1234/items/5678", false));
    }
}
