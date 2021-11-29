package uk.gov.companieshouse.orders.api.interceptor;

import static java.util.Arrays.asList;
import static uk.gov.companieshouse.orders.api.controller.BasketController.ADD_ITEM_URI;
import static uk.gov.companieshouse.orders.api.controller.BasketController.BASKET_URI;
import static uk.gov.companieshouse.orders.api.controller.BasketController.CHECKOUT_BASKET_URI;
import static uk.gov.companieshouse.orders.api.controller.BasketController.GET_PAYMENT_DETAILS_URI;
import static uk.gov.companieshouse.orders.api.controller.BasketController.PATCH_PAYMENT_DETAILS_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.GET_CHECKOUT_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.GET_ORDER_URI;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

@Service
public class RequestMapper implements InitializingBean {

    static final String ADD_ITEM = "addItem";
    static final String CHECKOUT_BASKET = "checkoutBasket";
    static final String GET_PAYMENT_DETAILS = "getPaymentDetails";
    static final String BASKET = "basket";
    static final String PATCH_BASKET = "basket";
    static final String PATCH_PAYMENT_DETAILS = "patchPaymentDetails";
    static final String GET_ORDER = "getOrder";
    static final String GET_CHECKOUT = "getCheckout";

    private final String addItemUri;
    private final String checkoutBasketUri;
    private final String basketUri;
    private final String getPaymentDetailsUri;
    private final String getOrderUri;
    private final String getCheckoutUri;
    private final String patchPaymentDetailsUri;

    /**
     * Represents the requests identified by this.
     */
    private List<RequestMappingInfo> knownRequests;

    public RequestMapper(
            @Value(ADD_ITEM_URI)
            final String addItemUri,
            @Value(CHECKOUT_BASKET_URI)
            final String checkoutBasketUri,
            @Value(BASKET_URI)
            final String basketUri,
            @Value(GET_PAYMENT_DETAILS_URI)
            final String getPaymentDetailsUri,
            @Value(GET_ORDER_URI)
            final String getOrderUri,
            @Value(GET_CHECKOUT_URI)
            final String getCheckoutUri,
            @Value(PATCH_PAYMENT_DETAILS_URI)
            final String patchPaymentDetailsUri) {
        this.addItemUri = addItemUri;
        this.checkoutBasketUri = checkoutBasketUri;
        this.basketUri = basketUri;
        this.getPaymentDetailsUri = getPaymentDetailsUri;
        this.getOrderUri = getOrderUri;
        this.getCheckoutUri = getCheckoutUri;
        this.patchPaymentDetailsUri = patchPaymentDetailsUri;
    }

    /**
     * Gets the request mapping found for the request provided.
     * @param request the HTTP request to be authenticated
     * @return the mapping representing the request if it is to be handled, or <code>null</code> if not
     */
    RequestMappingInfo getRequestMapping(final HttpServletRequest request) {
        for (final RequestMappingInfo mapping: knownRequests) {
            final RequestMappingInfo match = mapping.getMatchingCondition(request);
            if (match != null) {
                return match;
            }
        }
        return null; // no match found
    }

    @Override
    public void afterPropertiesSet() {

        final RequestMappingInfo addItem = RequestMappingInfo
                .paths(addItemUri)
                .methods(RequestMethod.POST)
                .mappingName(ADD_ITEM)
                .build();

        final RequestMappingInfo checkoutBasket = RequestMappingInfo
                .paths(checkoutBasketUri)
                .methods(RequestMethod.POST)
                .mappingName(CHECKOUT_BASKET)
                .build();

        final RequestMappingInfo getPaymentDetails = RequestMappingInfo
                .paths(getPaymentDetailsUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_PAYMENT_DETAILS)
                .build();

        final RequestMappingInfo getBasket = RequestMappingInfo
                .paths(basketUri)
                .methods(RequestMethod.GET)
                .mappingName(BASKET)
                .build();

        final RequestMappingInfo patchBasket = RequestMappingInfo
                .paths(basketUri)
                .methods(RequestMethod.PATCH)
                .mappingName(PATCH_BASKET)
                .build();

        final RequestMappingInfo patchPaymentDetails = RequestMappingInfo
                .paths(patchPaymentDetailsUri)
                .methods(RequestMethod.PATCH)
                .mappingName(PATCH_PAYMENT_DETAILS)
                .build();

        final RequestMappingInfo getOrder = RequestMappingInfo
                .paths(getOrderUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_ORDER)
                .build();

        final RequestMappingInfo getCheckout = RequestMappingInfo
                .paths(getCheckoutUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_CHECKOUT)
                .build();

        knownRequests = asList(
                addItem, checkoutBasket, getPaymentDetails, getBasket, patchBasket, patchPaymentDetails, getOrder, getCheckout
        );

    }
}
