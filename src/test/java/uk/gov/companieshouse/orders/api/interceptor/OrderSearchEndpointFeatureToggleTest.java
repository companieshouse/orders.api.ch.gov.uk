package uk.gov.companieshouse.orders.api.interceptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.orders.api.config.FeatureOptions;

@ExtendWith(MockitoExtension.class)
class OrderSearchEndpointFeatureToggleTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Object handler;
    @Mock
    private FeatureOptions featureOptions;

    @InjectMocks
    private OrdersSearchEndpointFeatureToggle featureToggle;

    @Test
    @DisplayName("pre-handle should set http status not found when feature flag disabled")
    void testFeatureFlagDisabled() {
        when(featureOptions.isOrdersSearchEndpointEnabled()).thenReturn(false);
        assertThat(featureToggle.preHandle(request,response,handler), is(false));
        verify(response).setStatus(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("pre-handle should return true when toggle enabled")
    void testFeatureFlagEnabled() {
        when(featureOptions.isOrdersSearchEndpointEnabled()).thenReturn(true);
        assertThat(featureToggle.preHandle(request,response,handler), is(true));
        verifyNoInteractions(response);
    }
}
