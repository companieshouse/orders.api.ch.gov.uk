package uk.gov.companieshouse.orders.api.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@ReadingConverter
public class OffsetDateTimeReadConverterFactory implements ConverterFactory<Date, OffsetDateTime> {

    @Override
    public <T extends OffsetDateTime> Converter<Date, T> getConverter(Class<T> targetType) {
        return new OffsetDateTimeReadConverter<>(targetType);
    }

    private final class OffsetDateTimeReadConverter<T extends OffsetDateTime> implements Converter<Date, T> {
        private Class<T> targetType;

        public OffsetDateTimeReadConverter(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(Date date) { return targetType.cast(date.toInstant().atOffset(ZoneOffset.UTC)); }
    }
}
