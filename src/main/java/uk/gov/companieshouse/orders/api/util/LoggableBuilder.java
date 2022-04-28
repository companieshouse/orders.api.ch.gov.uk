package uk.gov.companieshouse.orders.api.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class LoggableBuilder {
    private String message;
    private final Map<String, Object> logMap = new HashMap<>();

    private LoggableBuilder() {
    }

    public static LoggableBuilder newBuilder() {
        return new LoggableBuilder();
    }

    public LoggableBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public LoggableBuilder withLogMapPut(String key, Object value) {
        this.logMap.put(key, value);
        return this;
    }

    public Loggable build() {
        return new SimpleLoggable(message, Collections.unmodifiableMap(logMap));
    }

    private static class SimpleLoggable implements Loggable {
        private final String message;
        private final Map<String, Object> logMap;

        public SimpleLoggable(final String message, final Map<String, Object> logMap) {
            this.message = message;
            this.logMap = logMap;
        }

        @Override
        public Map<String, Object> getLogMap() {
            return logMap;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
