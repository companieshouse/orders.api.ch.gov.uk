package uk.gov.companieshouse.orders.api.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import uk.gov.companieshouse.orders.api.converter.EnumToStringConverterFactory;
import uk.gov.companieshouse.orders.api.converter.StringToEnumConverterFactory;

@Configuration
public class MongoConfig {
    /**
     * _class maps to the model class in mongoDB (e.g. _class : uk.gov.companieshouse.items.orders.api.model.CertificateItem)
     * when using spring data mongo it by default adds a _class key to your collection to be able to
     * handle inheritance. But if your domain model is simple and flat, you can remove it by overriding
     * the default MappingMongoConverter.
     * Copied from items.orders.api.ch.gov.uk
     */

    @Bean
    public MappingMongoConverter mappingMongoConverter(final MongoDatabaseFactory factory,
                                                       final MongoMappingContext context) {
        final DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        final MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);

        // Don't save _class to mongo
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));

        final List<ConverterFactory<?, ?>> converters = new ArrayList<>();
        converters.add(new StringToEnumConverterFactory());
        converters.add(new EnumToStringConverterFactory());
        mappingConverter.setCustomConversions(new MongoCustomConversions(converters));

        return mappingConverter;
    }
}
