package uk.gov.companieshouse.orders.api.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.WritingConverter;

import java.time.OffsetDateTime;
import java.util.Date;

@WritingConverter
public class OffsetDateTimeWriteConverterFactory implements ConverterFactory<OffsetDateTime, Date> {

    @Override
    public <T extends Date> Converter<OffsetDateTime, T> getConverter(Class<T> targetType) {
        return new OffsetDateTimeWriteConverter<>(targetType);
    }

    private final class OffsetDateTimeWriteConverter<T extends Date> implements Converter<OffsetDateTime, T> {
        private Class<T> targetType;

        public OffsetDateTimeWriteConverter(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T convert(OffsetDateTime source) {
            return targetType.cast(Date.from(source.toInstant()));
        }
    }
}