package uk.gov.companieshouse.orders.api.interceptor;

import static uk.gov.companieshouse.orders.api.controller.BasketController.*;
import static uk.gov.companieshouse.orders.api.controller.OrderController.GET_CHECKOUT_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.GET_ORDER_ITEM_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.GET_ORDER_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.CHECKOUTS_SEARCH_URI;
import static uk.gov.companieshouse.orders.api.controller.OrderController.POST_REPROCESS_ORDER_URI;

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
    static final String GET_ORDER_ITEM = "getOrderItem";
    static final String SEARCH = "searchCheckouts";
    static final String GET_CHECKOUT = "getCheckout";
    static final String POST_REPROCESS_ORDER = "postReprocessOrder";
    static final String GET_BASKET_LINKS = "getBasketLinks";
    static final String REMOVE_BASKET_ITEM = "putRemoveBasketItem";
    static final String APPEND_BASKET_ITEM = "postAppendBasketItem";

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
    @Value(GET_ORDER_ITEM_URI)
    private String getOrderItemUri;
    @Value(CHECKOUTS_SEARCH_URI)
    private String searchUri;
    @Value(GET_CHECKOUT_URI)
    private String getCheckoutUri;
    @Value(PATCH_PAYMENT_DETAILS_URI)
    private String patchPaymentDetailsUri;
    @Value(POST_REPROCESS_ORDER_URI)
    private String postReprocessOrderUri;
    @Value(GET_BASKET_LINKS_URI)
    private String getBasketLinksUri;
    @Value(REMOVE_ITEM_URI)
    private String putRemoveBasketItemUri;
    @Value(APPEND_ITEM_URI)
    private String postAppendBasketItemUri;

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

        // Note: SEARCH [/checkouts/search] must rank higher than GET_ORDER [/orders/{id}] so that
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
                .paths(getOrderItemUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_ORDER_ITEM)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(getCheckoutUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_CHECKOUT)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(postReprocessOrderUri)
                .methods(RequestMethod.POST)
                .mappingName(POST_REPROCESS_ORDER)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(getBasketLinksUri)
                .methods(RequestMethod.GET)
                .mappingName(GET_BASKET_LINKS)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(putRemoveBasketItemUri)
                .methods(RequestMethod.PUT)
                .mappingName(REMOVE_BASKET_ITEM)
                .build());

        knownRequests.add(RequestMappingInfo
                .paths(postAppendBasketItemUri)
                .methods(RequestMethod.POST)
                .mappingName(APPEND_BASKET_ITEM)
                .build());

        return Collections.unmodifiableList(knownRequests);
    }
}