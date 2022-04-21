package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.orders.api.model.ActionedBy;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopy;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.HRef;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.model.OrderLinks;
import uk.gov.companieshouse.orders.api.model.OrderSearchResults;
import uk.gov.companieshouse.orders.api.model.OrderSummary;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
import uk.gov.companieshouse.orders.api.model.Links;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_KEY_ROLES;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS;
import static uk.gov.companieshouse.api.util.security.SecurityConstants.INTERNAL_USER_ROLE;
import static uk.gov.companieshouse.orders.api.model.CertificateType.INCORPORATION_WITH_ALL_NAME_CHANGES;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFICATE_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFIED_COPY_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.DOCUMENT;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_OAUTH2_TYPE_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_TYPE_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_PERMISSION_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_REQUEST_ID_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.WRONG_ERIC_IDENTITY_VALUE;

@DirtiesContext
@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedKafka
@ActiveProfiles("orders-search-enabled")
class OrderControllerIntegrationTest {
    private static final String ORDER_ID = "0001";
    private static final String ORDER_REFERENCE = "0001";
    private static final String CHECKOUT_ID = "0002";
    private static final String CHECKOUT_REFERENCE = "0002";
    private static final String COMPANY_STATUS_ACTIVE = "active";
    public static final String ORDERS_SEARCH_PATH = "/orders/search";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private ObjectMapper mapper;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        checkoutRepository.deleteAll();
    }

    @Test
    void getOrderSuccessfully() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/"+ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(orderData)));
    }

    @Test
    @DisplayName("Get order responds with correctly populated certificate item options")
    void getOrderCertificateItemOptionsCorrectly() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        final Certificate certificate = new Certificate();
        certificate.setKind(CERTIFICATE_KIND);
        final CertificateItemOptions options = new CertificateItemOptions();
        options.setCertificateType(INCORPORATION_WITH_ALL_NAME_CHANGES);
        options.setCompanyStatus(COMPANY_STATUS_ACTIVE);
        certificate.setItemOptions(options);
        orderData.setItems(singletonList(certificate));
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/" + ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].item_options.certificate_type",
                        is(INCORPORATION_WITH_ALL_NAME_CHANGES.getJsonName())))
                .andExpect(jsonPath("$.items[0].item_options.company_status",
                        is(COMPANY_STATUS_ACTIVE)));
    }

    @Test
    @DisplayName("Get order responds with correctly populated certified copy item options")
    void getOrderCertifiedCopyItemOptionsCorrectly() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        final CertifiedCopy copy = new CertifiedCopy();
        copy.setKind(CERTIFIED_COPY_KIND);
        final CertifiedCopyItemOptions options = new CertifiedCopyItemOptions();
        options.setFilingHistoryDocuments(singletonList(DOCUMENT));
        copy.setItemOptions(options);
        orderData.setItems(singletonList(copy));
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/" + ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_date",
                        is(DOCUMENT.getFilingHistoryDate())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_description",
                        is(DOCUMENT.getFilingHistoryDescription())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_id",
                        is(DOCUMENT.getFilingHistoryId())))
                .andExpect(jsonPath("$.items[0].item_options.filing_history_documents[0].filing_history_type",
                        is(DOCUMENT.getFilingHistoryType())));
    }

    @Test
   void respondsWithNotFoundIfOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/orders/"+ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderUnauthorisedIfUserDoesNotOwnOrder() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/"+ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, WRONG_ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCheckoutSuccessfully() throws Exception {
        final Checkout preexistingCheckout = new Checkout();
        preexistingCheckout.setId(CHECKOUT_ID);
        preexistingCheckout.setUserId(ERIC_IDENTITY_VALUE);
        final CheckoutData checkoutData = new CheckoutData();
        checkoutData.setReference(CHECKOUT_REFERENCE);
        checkoutData.setTotalOrderCost("100");
        preexistingCheckout.setData(checkoutData);
        checkoutRepository.save(preexistingCheckout);

        mockMvc.perform(get("/checkouts/"+ CHECKOUT_ID)
            .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
            .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
            .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
            .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(mapper.writeValueAsString(checkoutData)));
    }

    @DisplayName("Should find a single order when searching by order id")
    @Test
    void searchOrdersById() throws Exception {
        orderRepository.save(getOrder(ORDER_ID, "demo@ch.gov.uk", "12345678"));
        checkoutRepository.save(getCheckout(ORDER_ID));
        OrderSearchResults expected = new OrderSearchResults(1,
                Collections.singletonList(
                        OrderSummary.newBuilder()
                                .withId(ORDER_ID)
                                .withEmail("demo@ch.gov.uk")
                                .withCompanyNumber("12345678")
                                .withProductLine("item#certificate")
                                .withPaymentStatus(PaymentStatus.PAID)
                                .withOrderDate(LocalDate.of(2022, 4, 12).atStartOfDay())
                                .withLinks(new Links(new HRef("http"), new HRef("http")))
                                .build()));

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                .param("id", ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @DisplayName("Should find a single order when searching with a partial email address")
    @Test
    void searchOrdersByEmail() throws Exception {
        orderRepository.save(getOrder(ORDER_ID, "demo@ch.gov.uk", "12345678"));
        checkoutRepository.save(getCheckout(ORDER_ID));
        OrderSearchResults expected = new OrderSearchResults(1,
                Collections.singletonList(
                        OrderSummary.newBuilder()
                                .withId(ORDER_ID)
                                .withEmail("demo@ch.gov.uk")
                                .withCompanyNumber("12345678")
                                .withProductLine("item#certificate")
                                .withPaymentStatus(PaymentStatus.PAID)
                                .withOrderDate(LocalDate.of(2022, 4, 12).atStartOfDay())
                                .withLinks(new Links(new HRef("http"), new HRef("http")))
                                .build()));

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                .param("email", "demo@ch")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @DisplayName("Should find a single order when a valid company number is provided")
    @Test
    void searchOrdersByCompanyNumber() throws Exception {
        orderRepository.save(getOrder(ORDER_ID, "demo@ch.gov.uk", "12345678"));
        checkoutRepository.save(getCheckout(ORDER_ID));
        orderRepository.save(getOrder("0002", "demo2@ch.gov.uk", "23456781"));
        checkoutRepository.save(getCheckout("0002"));

        OrderSearchResults expected = new OrderSearchResults(1,
                Collections.singletonList(
                        OrderSummary.newBuilder()
                                .withId(ORDER_ID)
                                .withEmail("demo@ch.gov.uk")
                                .withCompanyNumber("12345678")
                                .withProductLine("item#certificate")
                                .withPaymentStatus(PaymentStatus.PAID)
                                .withOrderDate(LocalDate.of(2022, 4, 12).atStartOfDay())
                                .withLinks(new Links(new HRef("http"), new HRef("http")))
                                .build()));

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                .param("company_number", "12345678")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("noMatchesFixture")
    void searchReturnsNoMatches(final String displayName, final String searchField, final String searchValue) throws Exception {
        orderRepository.save(getOrder(ORDER_ID, "demo@ch.gov.uk", "12345678"));
        OrderSearchResults expected = new OrderSearchResults(0, Collections.emptyList());

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param(searchField, searchValue)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
    }

    private Order getOrder(String orderId, String email, String companyNumber) {
        final Order order = new Order();
        order.setId(orderId);
        order.setUserId(ERIC_IDENTITY_VALUE);
        order.setCreatedAt(LocalDate.of(2022, 4, 12).atStartOfDay());

        final OrderData orderData = new OrderData();
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        orderData.setOrderedBy(new ActionedBy());
        orderData.getOrderedBy().setEmail(email);
        orderData.setItems(Collections.singletonList(new Item()));
        orderData.getItems().get(0).setId("item-id-123");
        orderData.getItems().get(0).setKind("item#certificate");
        orderData.getItems().get(0).setCompanyNumber(companyNumber);
        orderData.setLinks(new OrderLinks());
        orderData.getLinks().setSelf("http");

        order.setData(orderData);

        return order;
    }

    private Checkout getCheckout(String orderId) {
        Checkout checkout = new Checkout();
        checkout.setData(new CheckoutData());
        checkout.setId(orderId);
        checkout.getData().setStatus(PaymentStatus.PAID);
        return checkout;
    }

    private static Stream<Arguments> noMatchesFixture() {
        return Stream.of(Arguments.arguments("Should not find a order when incomplete order id is provided", "id", "00"),
                Arguments.arguments("Should not find a order when a incorrect email address is provided", "email", "wrong@ch.gov.uk"),
                Arguments.arguments("Should not find a order when a incomplete company number is provided", "company_number", "345678912"));
    }
}
