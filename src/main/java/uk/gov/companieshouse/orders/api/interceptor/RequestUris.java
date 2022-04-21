package uk.gov.companieshouse.orders.api.interceptor;

import static uk.gov.companieshouse.orders.api.controller.BasketController.ADD_ITEM_URI;
import static uk.gov.companieshouse.orders.api.controller.BasketController.BASKET_URI;
import static uk.gov.companieshouse.orders.api.controller.BasketController.CHECKOUT_BASKET_URI;
import static uk.gov.companieshouse.orders.api.controller.BasketController.GET_PAYMENT_DETAILS_URI;
import static uk.gov.companieshouse.orders.api.controller.BasketController.PATCH_PAYMENT_DETAILS_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.GET_CHECKOUT_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.GET_ORDER_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.ORDERS_SEARCH_URI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

@Configuration
class RequestUris {
    static final String ADD_ITEM = "addItem";
    static final String CHECKOUT_BASKET = "checkoutBasket";
    static final String GET_PAYMENT_DETAILS = "getPaymentDetails";
    static final String BASKET = "basket";
    static final String PATCH_BASKET = "basket";
    static final String PATCH_PAYMENT_DETAILS = "patchPaymentDetails";
    static final String GET_ORDER = "getOrder";
    static final String SEARCH = "searchOrders";
    static final String GET_CHECKOUT = "getCheckout";

    @Value(ADD_ITEM_URI)
    private String addItemUri;
    @Value(CHECKOUT_BASKET_URI)
    private String checkoutBasketUri;
    @Value(BASKET_URI)
    private String basketUri;
    @Value(GET_PAYMENT_DETAILS_URI)
    private String getPaymentDetailsUri;
    @Value(GET_ORDER_URI)
    private String getOrderUri;
    @Value(ORDERS_SEARCH_URI)
    private String searchUri;
    @Value(GET_CHECKOUT_URI)
    private String getCheckoutUri;
    @Value(PATCH_PAYMENT_DETAILS_URI)
    private String patchPaymentDetailsUri;

    @Bean
    List<RequestMappingInfo> requestMappingInfoList() {
        List<RequestMappingInfo> knownRequests = new ArrayList<>();

        knownRequests.add(RequestMappingInfo
                .paths(addItemUri)
                .methods(RequestMethod.POST)
                .mappingName(ADD_ITEM)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(checkoutBasketUri)
                .methods(RequestMethod.POST)
                .mappingName(CHECKOUT_BASKET)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(getPaymentDetailsUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_PAYMENT_DETAILS)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(basketUri)
                .methods(RequestMethod.GET)
                .mappingName(BASKET)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(basketUri)
                .methods(RequestMethod.PATCH)
                .mappingName(PATCH_BASKET)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(patchPaymentDetailsUri)
                .methods(RequestMethod.PATCH)
                .mappingName(PATCH_PAYMENT_DETAILS)
                .build());

        // Note: SEARCH [/orders/search] must rank higher than GET_ORDER [/orders/{id}] so that
        // it is mapped correctly.
        knownRequests.add(RequestMappingInfo
                .paths(searchUri)
                .methods(RequestMethod.GET)
                .mappingName(SEARCH)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(getOrderUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_ORDER)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(getCheckoutUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_CHECKOUT)
                .build());

        return Collections.unmodifiableList(knownRequests);
    }
}