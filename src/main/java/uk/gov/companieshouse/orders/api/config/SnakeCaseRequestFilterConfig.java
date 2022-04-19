package uk.gov.companieshouse.orders.api.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.text.CaseUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Provides a request filter bean that converts request parameters from snake case into camel case.
 * <p>
 * Note: Spring uses reflection [not Jackson] to unmarshal request parameters into a Java object; so
 * to implement the Companies House standard of using snake case for serialised data in request
 * parameters, conversion of the request parameter names into camel case is required.
 */
@Configuration
public class SnakeCaseRequestFilterConfig {

    @Bean
    public OncePerRequestFilter snakeCaseRequestFilter() {

        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                final Map<String, String[]> parameters = new ConcurrentHashMap<>();

                for (String param : request.getParameterMap().keySet()) {
                    String camelCaseParam = CaseUtils.toCamelCase(param, false, '_');

                    parameters.put(camelCaseParam, request.getParameterValues(param));
                    parameters.put(param, request.getParameterValues(param));
                }

                filterChain.doFilter(new HttpServletRequestWrapper(request) {
                    @Override
                    public String getParameter(String name) {
                        return parameters.containsKey(name) ? parameters.get(name)[0] : null;
                    }

                    @Override
                    public Enumeration<String> getParameterNames() {
                        return Collections.enumeration(parameters.keySet());
                    }

                    @Override
                    public String[] getParameterValues(String name) {
                        return parameters.get(name);
                    }

                    @Override
                    public Map<String, String[]> getParameterMap() {
                        return parameters;
                    }
                }, response);
            }
        };
    }
}
