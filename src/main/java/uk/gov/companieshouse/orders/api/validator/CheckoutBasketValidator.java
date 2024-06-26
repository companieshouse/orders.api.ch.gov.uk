package uk.gov.companieshouse.orders.api.validator;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.api.exception.ErrorType;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.service.ApiClientService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.orders.api.logging.LoggingUtils.APPLICATION_NAME_SPACE;

@Component
public class CheckoutBasketValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);
    private ApiClientService apiClientService;
    private DeliveryDetailsValidator deliveryDetailsValidator;

    public CheckoutBasketValidator(ApiClientService apiClientService, DeliveryDetailsValidator deliveryDetailsValidator) {
        this.apiClientService = apiClientService;
        this.deliveryDetailsValidator = deliveryDetailsValidator;
    }

    public List<String> getValidationErrors(final String passthroughHeader, final Basket basket) {
        Map<String, Object> logMap = LoggingUtils.createLogMap();
        LoggingUtils.logIfNotNull(logMap, LoggingUtils.BASKET_ID, basket.getId());
        List<String> errors = new ArrayList<>();
        List<Item> basketItems = basket.getData().getItems();
        if (basketItems.isEmpty()) {
            errors.add(ErrorType.BASKET_ITEMS_MISSING.getValue());
        }
        else {
            for (Item item : basketItems) {
                try {
                    String itemUri = item.getItemUri();
                    LoggingUtils.logIfNotNull(logMap, LoggingUtils.ITEM_URI, itemUri);
                    Item retrievedItem = apiClientService.getItem(passthroughHeader, itemUri);

                    if (Boolean.TRUE.equals(retrievedItem.isPostalDelivery())
                            && !deliveryDetailsValidator.isValid(
                                    basket.getData().getDeliveryDetails())) {
                        logMap.put(LoggingUtils.ERROR_TYPE,
                                ErrorType.DELIVERY_DETAILS_MISSING.getValue());
                        LOGGER.error(ErrorType.DELIVERY_DETAILS_MISSING.getValue(), logMap);
                        errors.add(ErrorType.DELIVERY_DETAILS_MISSING.getValue());
                    }
                } catch (Exception exception) {
                    logMap.put(LoggingUtils.EXCEPTION, exception);
                    logMap.put(LoggingUtils.ERROR_TYPE,
                            ErrorType.BASKET_ITEM_INVALID.getValue());
                    LOGGER.error(ErrorType.BASKET_ITEM_INVALID.getValue(), logMap);
                    errors.add(ErrorType.BASKET_ITEM_INVALID.getValue());
                }
            }
        }
        return errors;
    }
}
