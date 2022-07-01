package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_KEY_ROLES;
import static uk.gov.companieshouse.api.util.security.SecurityConstants.INTERNAL_USER_ROLE;
import static uk.gov.companieshouse.orders.api.controller.BasketController.CHECKOUT_ID_PATH_VARIABLE;
import static uk.gov.companieshouse.orders.api.controller.OrderController.ORDER_ID_PATH_VARIABLE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.*;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.WRONG_ERIC_IDENTITY_VALUE;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.web.util.UrlPathHelper;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

/**
 * Unit/integration tests the {@link UserAuthorisationInterceptor} class.
 */
@SpringBootTest
@EmbeddedKafka
class UserAuthorisationInterceptorTests {

    @Autowired
    private UserAuthorisationInterceptor interceptorUnderTest;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object handler;

    @MockBean
    private CheckoutRepository checkoutRepository;

    @Mock
    private Checkout checkout;

    @MockBean
    private OrderRepository orderRepository;

    @Mock
    private Order order;

    @MockBean
    private SecurityManager securityManager;

    @Test
    @DisplayName("preHandle accepts a request it has not been configured to authorise")
    void preHandleAcceptsUnknownRequest() {

        // Given
        givenRequest(DELETE, "/unknown");

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts patch payment details request that has the required headers")
    void preHandleAcceptsAuthorisedPatchPaymentDetailsRequest() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/1234/payment");
        givenRequestHasInternalUserRole();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle rejects patch payment details request that lacks the required headers")
    void preHandleRejectsUnauthorisedPatchPaymentDetailsRequest() {

        // Given
        givenRequest(PATCH, "/basket/checkouts/1234/payment");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle accepts get payment details user request that has the required headers")
    void preHandleAcceptsAuthorisedUserGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);
        givenGetCheckoutIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle rejects get payment details internal API request that does not have internal user role")
    void preHandleRejectsUnauthorisedInternalApiGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestDoesNotHaveInternalUserRole();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects get payment details user request that lacks the required headers")
    void preHandleRejectsUnauthorisedUserGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasSignedInUser(WRONG_ERIC_IDENTITY_VALUE);
        givenGetCheckoutIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle errors clearly if URI path variables are not present in get payment details request")
    void preHandleErrorsClearlyIfUriPathVariablesNotPresentInGetPaymentDetailsRequest() {

        // Given
        givenRequest(GET, "/basket/checkouts/1234/payment");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);

        // When and then
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> interceptorUnderTest.preHandle(request, response, handler));
        assertEquals("No URI template path variables found in the request!", exception.getMessage());
    }

    @Test
    @DisplayName("preHandle accepts get order user request that has the required headers")
    void preHandleAcceptsAuthorisedUserGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);
        givenGetOrderOrderIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsAccepted();
    }

    @DisplayName("Authorisation for orders/search endpoint succeeds if caller is has correct permissions")
    @Test
    void ordersSearchValidAuthorisation() {
        when(securityManager.checkPermission()).thenReturn(true);
        givenRequest(GET, "/orders/search");

        boolean actual = interceptorUnderTest.preHandle(request, response, handler);

        assertThat(actual, is(true));
    }

    @DisplayName("Authentication for orders/search endpoint false if caller has incorrect permissions")
    @Test
    void ordersSearchInvalidAuthorisation() {
        when(securityManager.checkPermission()).thenReturn(false);
        givenRequest(GET, "/orders/search");

        boolean actual = interceptorUnderTest.preHandle(request, response, handler);

        assertThat(actual, is(false));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("apiGetRequestFixtures")
    void preHandleAcceptsAuthorisedInternalApiGetRequest(final String displayName, final String uri) {

        // Given
        givenRequest(GET, uri);
        givenRequestHasInternalUserRole();

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get checkout request authorised by security manager")
    void preHandleAcceptsGetCheckoutRequestAuthorisedBySecurityManager() {
        // Given
        when(securityManager.checkPermission()).thenReturn(true);
        givenRequest(GET, "/checkouts/1234");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);
        givenGetCheckoutIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get checkout request authorised by user ownership")
    void preHandleAcceptsGetCheckoutRequestAuthorisedByOwnership() {
        // Given
        when(securityManager.checkPermission()).thenReturn(false);
        givenRequest(GET, "/checkouts/1234");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);
        givenGetCheckoutIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle accepts get checkout request authorised with internal user role")
    void preHandleAcceptsGetCheckoutRequestAuthorisedAsInternalUser() {
        // Given
        when(securityManager.checkPermission()).thenReturn(false);
        givenRequest(GET, "/checkouts/1234");
        givenRequestHasInternalUserRole();
        givenGetCheckoutIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsAccepted();
    }

    @Test
    @DisplayName("preHandle rejects get checkout request if neither authorised by security manager nor as resource owner")
    void preHandleRejectsGetCheckoutRequestIfNotResourceOwner() {
        // Given
        when(securityManager.checkPermission()).thenReturn(false);
        givenRequest(GET, "/checkouts/1234");
        givenRequestHasSignedInUser(WRONG_ERIC_IDENTITY_VALUE);
        givenGetCheckoutIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects get order internal API request that lacks the required headers")
    void preHandleRejectsUnauthorisedInternalApiGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestDoesNotHaveInternalUserRole();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects get order user request that lacks the required headers")
    void preHandleRejectsUnauthorisedUserGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasSignedInUser(WRONG_ERIC_IDENTITY_VALUE);
        givenGetOrderOrderIdPathVariableIsPopulated(ERIC_IDENTITY_VALUE);

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle errors clearly if URI path variables are not present in get order request")
    void preHandleErrorsClearlyIfUriPathVariablesNotPresentInGetOrderRequest() {

        // Given
        givenRequest(GET, "/orders/1234");
        givenRequestHasSignedInUser(ERIC_IDENTITY_VALUE);

        // When and then
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> interceptorUnderTest.preHandle(request, response, handler));
        assertEquals("No URI template path variables found in the request!", exception.getMessage());
    }

    @Test
    @DisplayName("preHandle rejects post reprocess order request that lacks the required headers")
    void preHandleRejectsUnauthorisedPostReprocessOrderRequest() {

        // Given
        givenRequest(POST, "/orders/1234/reprocess");

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle rejects post reprocess order request that does not have internal user role")
    void preHandleRejectsPostReprocessOrderRequestWithoutInternalUserRole() {

        // Given
        givenRequest(POST, "/orders/1234/reprocess");
        givenRequestDoesNotHaveInternalUserRole();

        // When and then
        thenRequestIsRejected();
    }

    @Test
    @DisplayName("preHandle accepts post reprocess order request that has internal user role")
    void preHandleAcceptsAuthorisedPostReprocessOrderRequest() {

        // Given
        givenRequest(POST, "/orders/1234/reprocess");
        givenRequestHasInternalUserRole();

        // When and then
        thenRequestIsAccepted();
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
        when(request.getAttribute(UrlPathHelper.class.getName() + ".PATH")).thenReturn(uri);
    }

    /**
     * Sets up the request with the checkout ID path variable as Spring does.
     * @param checkoutOwnerId the user ID value on the retrieved checkout
     */
    private void givenGetCheckoutIdPathVariableIsPopulated(final String checkoutOwnerId) {
        givenPathVariable(CHECKOUT_ID_PATH_VARIABLE, "1");
        when(checkoutRepository.findById("1")).thenReturn(Optional.of(checkout));
        when(checkout.getUserId()).thenReturn(checkoutOwnerId);
    }

    /**
     * Sets up the request with the order ID path variable as Spring does.
     * @param orderOwnerId the user ID value on the retrieved order
     */
    private void givenGetOrderOrderIdPathVariableIsPopulated(final String orderOwnerId) {
        givenPathVariable(ORDER_ID_PATH_VARIABLE, "1");
        when(orderRepository.findById("1")).thenReturn(Optional.of(order));
        when(order.getUserId()).thenReturn(orderOwnerId);
    }

    /**
     * Sets up the request with the named path variable as Spring does.
     * @param name the name of the path variable
     * @param value the value of the path variable
     */
    private void givenPathVariable(final String name, final String value) {
        final Map<String, String> uriPathVariables = singletonMap(name, value);
        when(request.getAttribute(URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(uriPathVariables);
    }

    /**
     * Sets up request with required header values to represent a signed in user.
     * @param userId the user ID in the request's <code>ERIC-Identity</code> header
     */
    private void givenRequestHasSignedInUser(final String userId) {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(OAUTH2_IDENTITY_TYPE);
        when(request.getHeader(ERIC_IDENTITY)).thenReturn(userId);
    }

    /**
     * Sets up the request for an API client with an internal user role.
     */
    private void givenRequestHasInternalUserRole() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(API_KEY_IDENTITY_TYPE);
        when(request.getHeader(ERIC_AUTHORISED_KEY_ROLES)).thenReturn(INTERNAL_USER_ROLE);
    }

    /**
     * Sets up the request for an API client with no internal user role.
     */
    private void givenRequestDoesNotHaveInternalUserRole() {
        when(request.getHeader(ERIC_IDENTITY_TYPE)).thenReturn(API_KEY_IDENTITY_TYPE);
        when(request.getHeader(ERIC_AUTHORISED_KEY_ROLES)).thenReturn(null);
    }

    /**
     * Verifies that the authorisation interceptor does not reject the request as unauthorised.
     */
    private void thenRequestIsAccepted() {
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(true));
        verify(response, never()).setStatus(anyInt());
    }

    /**
     * Verifies that the authorisation interceptor blocks the request as unauthorised.
     */
    private void thenRequestIsRejected() {
        assertThat(interceptorUnderTest.preHandle(request, response, handler), is(false));
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    private static Stream<Arguments> apiGetRequestFixtures() {
        return Stream.of(arguments("preHandle accepts get payment details internal API request that has the required headers", "/basket/checkouts/1234/payment"),
                arguments("preHandle accepts get order internal API request that has the required headers", "/orders/1234"),
                arguments("preHandle accepts get checkout internal API request that has the required headers", "/checkouts/1234"));
    }
}
