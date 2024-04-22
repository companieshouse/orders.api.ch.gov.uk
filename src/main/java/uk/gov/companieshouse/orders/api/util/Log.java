package uk.gov.companieshouse.orders.api.util;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAME_SPACE;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class Log {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public void info(Loggable loggable) {
        LOGGER.info(loggable.getMessage(), loggable.getLogMap());
    }

    public void infoRequest(Loggable loggable) {
        LOGGER.infoRequest(loggable.getRequest(), loggable.getMessage(), loggable.getLogMap());
    }
}
