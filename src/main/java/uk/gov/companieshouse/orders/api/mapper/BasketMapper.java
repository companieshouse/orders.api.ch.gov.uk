package uk.gov.companieshouse.orders.api.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.orders.api.dto.BasketRequestDTO;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.Item;

import java.util.Arrays;

@Mapper(componentModel = "spring")
public interface BasketMapper {
    Basket addToBasketRequestDTOToBasket(BasketRequestDTO basketRequestDTO);

    @AfterMapping
    default void fillBasket(BasketRequestDTO basketRequestDTO, @MappingTarget Basket basket) {
        Item item = new Item();
        item.setItemUri(basketRequestDTO.getItemUri());
        basket.getData().setItems(Arrays.asList(item));
    }
}
