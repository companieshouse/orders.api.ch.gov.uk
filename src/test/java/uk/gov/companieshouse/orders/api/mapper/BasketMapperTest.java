package uk.gov.companieshouse.orders.api.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.orders.api.dto.AddToBasketRequestDTO;
import uk.gov.companieshouse.orders.api.model.Basket;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
        final AddToBasketRequestDTO dto = new AddToBasketRequestDTO();
        dto.setItemUri(ITEM_URI);

        Basket item = basketMapper.addToBasketRequestDTOToBasket(dto);

        assertThat(item.getData(), is(notNullValue()));
        assertEquals(ITEM_URI, item.getData().getItems().get(0).getItemUri());
    }
}
