package uk.gov.companieshouse.orders.api.config;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import uk.gov.companieshouse.api.interceptor.CRUDAuthenticationInterceptor;
import uk.gov.companieshouse.orders.api.interceptor.OrdersSearchEndpointFeatureToggle;
import uk.gov.companieshouse.orders.api.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.orders.api.interceptor.UserAuthenticationInterceptor;
import uk.gov.companieshouse.orders.api.interceptor.UserAuthorisationInterceptor;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    ApplicationConfig config;

    @Mock
    private LoggingInterceptor loggingInterceptor;
    @Mock
    private UserAuthenticationInterceptor authenticationInterceptor;
    @Mock
    private UserAuthorisationInterceptor authorisationInterceptor;
    @Mock
    private OrdersSearchEndpointFeatureToggle featureToggleInterceptor;
    private final String healthcheckUri = "healthcheck";
    private final String paymentDetailsUri = "payment-details";
    private final String ordersSearchUri = "orders-search-uri";

    @BeforeEach
    void setup() {
        config = Mockito.spy(new ApplicationConfig(loggingInterceptor, authenticationInterceptor,
                authorisationInterceptor, featureToggleInterceptor, healthcheckUri, paymentDetailsUri, ordersSearchUri));
    }

    @Test
    void addInterceptors() {
        final CRUDAuthenticationInterceptor crudPermissionInterceptor = Mockito
                .mock(CRUDAuthenticationInterceptor.class);
        final CRUDAuthenticationInterceptor crudPermissionInterceptorSkipPatch = Mockito
                .mock(CRUDAuthenticationInterceptor.class);

        when(config.crudPermissionInterceptor()).thenReturn(crudPermissionInterceptor);
        when(config.crudPermissionInterceptorPaymentDetails()).thenReturn(crudPermissionInterceptorSkipPatch);

        InterceptorRegistry registry = Mockito.mock(InterceptorRegistry.class);

        InterceptorRegistration loggingInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(loggingInterceptorRegistration).when(registry).addInterceptor(loggingInterceptor);

        InterceptorRegistration featureToggleInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(featureToggleInterceptorRegistration).when(registry).addInterceptor(featureToggleInterceptor);

        InterceptorRegistration authenticationInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(authenticationInterceptorRegistration).when(registry).addInterceptor(authenticationInterceptor);

        InterceptorRegistration authorisationInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(authorisationInterceptorRegistration).when(registry).addInterceptor(authorisationInterceptor);

        InterceptorRegistration crudPermissionInterceptorRegistration = Mockito.mock(InterceptorRegistration.class);
        doReturn(crudPermissionInterceptorRegistration).when(registry).addInterceptor(crudPermissionInterceptor);

        InterceptorRegistration crudPermissionInterceptorPaymentDetailsRegistration = Mockito
                .mock(InterceptorRegistration.class);
        doReturn(crudPermissionInterceptorPaymentDetailsRegistration).when(registry)
                .addInterceptor(crudPermissionInterceptorSkipPatch);

        config.addInterceptors(registry);

        verify(authenticationInterceptorRegistration).excludePathPatterns(healthcheckUri);
        verify(authorisationInterceptorRegistration).excludePathPatterns(healthcheckUri);
        verify(featureToggleInterceptorRegistration).addPathPatterns(ordersSearchUri);
        verify(crudPermissionInterceptorRegistration).excludePathPatterns(paymentDetailsUri, healthcheckUri);
        verify(crudPermissionInterceptorPaymentDetailsRegistration).addPathPatterns(paymentDetailsUri);

        InOrder inOrder = Mockito.inOrder(registry);
        inOrder.verify(registry).addInterceptor(loggingInterceptor);
        inOrder.verify(registry).addInterceptor(featureToggleInterceptor);
        inOrder.verify(registry).addInterceptor(authenticationInterceptor);
        inOrder.verify(registry).addInterceptor(authorisationInterceptor);
        inOrder.verify(registry).addInterceptor(crudPermissionInterceptor);
        inOrder.verify(registry).addInterceptor(crudPermissionInterceptorSkipPatch);
    }
}
