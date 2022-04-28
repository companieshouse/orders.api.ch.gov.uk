package uk.gov.companieshouse.orders.api.util;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class Logger {
    private static final uk.gov.companieshouse.logging.Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    public void info(final String message, final Map<String, Object> logMap) {
        LOGGER.info(message, logMap);
    }
}
