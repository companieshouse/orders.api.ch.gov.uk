package uk.gov.companieshouse.orders.api.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DirtiesContext
@SpringBootTest
@EmbeddedKafka
class FieldNameConverterTest {

    @Qualifier("fieldNameConverter")
    @Autowired
    private FieldNameConverter converterUnderTest;

    @Test
    void toSnakeCaseWorksAsExpected() {
        assertThat(converterUnderTest.toSnakeCase("itemCosts"), is("item_costs"));
        assertThat(converterUnderTest.toSnakeCase("item"), is("item"));
        assertThat(converterUnderTest.toSnakeCase("certIncConLast"), is("cert_inc_con_last"));
        assertThat(converterUnderTest.toSnakeCase("isPostalDelivery"), is("postal_delivery"));
        assertThat(converterUnderTest.toSnakeCase("addressLine1"), is("address_line_1"));
    }
}
