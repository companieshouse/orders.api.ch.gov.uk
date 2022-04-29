package uk.gov.companieshouse.orders.api.util;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAMESPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class Log {
    private static final uk.gov.companieshouse.logging.Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    public void info(Loggable loggable) {
        LOGGER.info(loggable.getMessage(), loggable.getLogMap());
    }

    public void infoRequest(Loggable loggable) {
        LOGGER.infoRequest(loggable.getRequest(), loggable.getMessage(), loggable.getLogMap());
    }
}
