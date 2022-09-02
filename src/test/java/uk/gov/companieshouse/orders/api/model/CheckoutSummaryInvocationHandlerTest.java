package uk.gov.companieshouse.orders.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.config.FeatureOptions;

@ExtendWith(MockitoExtension.class)
public class CheckoutSummaryInvocationHandlerTest {

    @Mock
    private FeatureOptions featureOptions;

    @Mock
    private CheckoutSummary.Builder builder;

    @Mock
    private CheckoutSummary summary;

    @Mock
    private Object proxy;

    @InjectMocks
    private CheckoutSummaryInvocationHandler invocationHandler;

    @Test
    @DisplayName("Allow method invocation if method name neither withCompanyNumber nor "
            + "withProductLine")
    void testInvokeOtherMethodOnBuilder() throws Throwable {
        // given
        when(builder.withEmail(any())).thenReturn(builder);

        // when
        Object actual = invocationHandler.invoke(proxy,
                builder.getClass().getMethod("withEmail", String.class), new Object[]{
                        "demo@ch.gov.uk"
                });

        // then
        assertEquals(proxy, actual);
        verify(builder).withEmail("demo@ch.gov.uk");
    }

    @Test
    @DisplayName("Allow method invocation if method name withCompanyNumber and multi item"
            + " basket disabled")
    void testInvokeWithCompanyNumberMultiItemBasketDisabled() throws Throwable {
        // given
        when(builder.withCompanyNumber(any())).thenReturn(builder);

        // when
        Object actual = invocationHandler.invoke(proxy,
                builder.getClass().getMethod("withCompanyNumber", String.class), new Object[]{
                        "12345678"
                });

        // then
        assertEquals(proxy, actual);
        verify(builder).withCompanyNumber("12345678");
    }

    @Test
    @DisplayName("Allow method invocation if method name withProductLine and multi item"
            + " basket disabled")
    void testInvokeWithProductLineMultiItemBasketDisabled() throws Throwable {
        // given
        when(builder.withProductLine(any())).thenReturn(builder);

        // when
        Object actual = invocationHandler.invoke(proxy,
                builder.getClass().getMethod("withProductLine", String.class), new Object[]{
                        "Certificate"
                });

        // then
        assertEquals(proxy, actual);
        verify(builder).withProductLine("Certificate");
    }

    @Test
    @DisplayName("Deny method invocation if method name withCompanyNumber and multi item"
            + " basket enabled")
    void testSkipInvocationOfWithCompanyNumberIfMultiItemBasketEnabled() throws Throwable {
        // given
        when(featureOptions.isMultiItemBasketSearchEnabled()).thenReturn(true);

        // when
        Object actual = invocationHandler.invoke(proxy,
                builder.getClass().getMethod("withCompanyNumber", String.class), new Object[]{
                        "12345678"
                });

        // then
        assertEquals(proxy, actual);
        verifyNoInteractions(builder);
    }

    @Test
    @DisplayName("Deny method invocation if method name withProductLine and multi item"
            + " basket enabled")
    void testSkipInvocationOfWithProductLineIfMultiItemBasketEnabled() throws Throwable {
        // given
        when(featureOptions.isMultiItemBasketSearchEnabled()).thenReturn(true);

        // when
        Object actual = invocationHandler.invoke(proxy,
                builder.getClass().getMethod("withProductLine", String.class), new Object[]{
                        "Certificate"
                });

        // then
        assertEquals(proxy, actual);
        verifyNoInteractions(builder);
    }

    @Test
    @DisplayName("Forward return value if build method invoked")
    void testForwardReturnValueIfBuildInvoked() throws Throwable {
        // given
        when(builder.build()).thenReturn(summary);

        // when
        Object actual = invocationHandler.invoke(proxy,
                builder.getClass().getMethod("build"), null);

        // then
        assertEquals(summary, actual);
        verify(builder).build();
    }

    @Test
    @DisplayName("Throw RuntimeException if InvocationTargetException thrown")
    void testThrowRuntimeExceptionIfInvocationTargetExceptionThrown() {
        // given
        when(builder.build()).thenThrow(RuntimeException.class);

        // when
        Executable actual = () -> invocationHandler.invoke(proxy, builder.getClass().getMethod(
                "build"), null);

        // then
        assertThrows(RuntimeException.class, actual);
    }
}
