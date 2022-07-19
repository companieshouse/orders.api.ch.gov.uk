package uk.gov.companieshouse.orders.api.mapper;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.orders.api.dto.BasketRequestDTO;
import uk.gov.companieshouse.orders.api.model.Basket;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(BasketMapperTest.Config.class)
public class BasketMapperTest {

    private static final String ITEM_URI = "/orderable/certificate/12345678";

    @Configuration
    @ComponentScan(basePackageClasses = BasketMapperTest.class)
    static class Config {}

    @Autowired
    private BasketMapper basketMapper;

    @Test
    public void testAddToBasketRequestDTOToBasket(){
        final BasketRequestDTO dto = new BasketRequestDTO();
        dto.setItemUri(ITEM_URI);

        Basket item = basketMapper.addToBasketRequestDTOToBasket(dto);

        assertThat(item.getData(), is(notNullValue()));
        assertEquals(ITEM_URI, item.getData().getItems().get(0).getItemUri());
    }
}
