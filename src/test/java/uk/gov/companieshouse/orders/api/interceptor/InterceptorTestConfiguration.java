package uk.gov.companieshouse.orders.api.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@ExtendWith(MockitoExtension.class)
public class InterceptorTestConfiguration {
    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Bean
    public HttpServletRequest httpServletRequest() {
        return httpServletRequest;
    }

    @Bean
    public HttpServletResponse httpServletResponse() {
        return httpServletResponse;
    }
}
