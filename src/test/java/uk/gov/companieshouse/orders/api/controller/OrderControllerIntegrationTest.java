package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.orders.api.dto.PatchOrderedItemDTO;
import uk.gov.companieshouse.orders.api.exceptionhandler.ConstraintValidationError;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopy;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.CheckoutSearchResults;
import uk.gov.companieshouse.orders.api.model.CheckoutSummary;
import uk.gov.companieshouse.orders.api.model.HRef;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemStatus;
import uk.gov.companieshouse.orders.api.model.Links;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;
import uk.gov.companieshouse.orders.api.util.StubHelper;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_KEY_ROLES;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS;
import static uk.gov.companieshouse.api.util.security.SecurityConstants.INTERNAL_USER_ROLE;
import static uk.gov.companieshouse.orders.api.model.CertificateType.INCORPORATION_WITH_ALL_NAME_CHANGES;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_AUTHORISED_ROLES;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.OAUTH2_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFICATE_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFIED_COPY_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.DOCUMENT;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_ACCESS_TOKEN;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_API_KEY_TYPE_VALUE;
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
@ActiveProfiles({"orders-search-enabled", "orders-search-multibasket-enabled"})
class OrderControllerIntegrationTest {
    private static final String ORDER_ID = "0001";
    private static final String ORDER_REFERENCE = "0001";
    private static final String CHECKOUT_ID = "0002";
    private static final String CHECKOUT_REFERENCE = "0002";
    private static final String COMPANY_STATUS_ACTIVE = "active";
    public static final String ORDERS_SEARCH_PATH = "/checkouts/search";
    private static final String PAGE_SIZE_PARAM = "page_size";
    private static final String PAGE_SIZE_VALUE = "1";
    private static final String ERIC_AUTHORISED_KEY_PRIVILEGES = "ERIC-Authorised-Key-Privileges";
    private static final String STATUS_SATISFIED = "satisfied";
    private static final String DIGITAL_DOCUMENT_LOCATION = "/digital/document/location";

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

        mockMvc.perform(get("/orders/" + ORDER_ID)
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
        mockMvc.perform(get("/orders/" + ORDER_ID)
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

        mockMvc.perform(get("/orders/" + ORDER_ID)
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

        mockMvc.perform(get("/checkouts/" + CHECKOUT_ID)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(checkoutData)));
    }

    @DisplayName("Should find a single checkout when searching by order id")
    @Test
    void searchCheckoutsById() throws Exception {
        checkoutRepository.save(StubHelper.getCheckout(CHECKOUT_ID, "demo@ch.gov.uk", "12345678", PaymentStatus.PAID));
        CheckoutSearchResults expected = new CheckoutSearchResults(1,
                Collections.singletonList(
                        CheckoutSummary.newBuilder()
                                .withId(CHECKOUT_ID)
                                .withEmail("demo@ch.gov.uk")
                                .withPaymentStatus(PaymentStatus.PAID)
                                .withCheckoutDate(LocalDate.of(2022, 4, 12).atStartOfDay())
                                .withLinks(new Links(new HRef("http"), new HRef("http")))
                                .build()));

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param("id", CHECKOUT_ID)
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_USER_ROLE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected), true));
    }

    @DisplayName("Should find a single checkout when searching with a partial email address")
    @Test
    void searchCheckoutsByEmail() throws Exception {
        checkoutRepository.save(StubHelper.getCheckout(CHECKOUT_ID, "demo@ch.gov.uk", "12345678", PaymentStatus.PAID));
        CheckoutSearchResults expected = new CheckoutSearchResults(1,
                Collections.singletonList(
                        CheckoutSummary.newBuilder()
                                .withId(CHECKOUT_ID)
                                .withEmail("demo@ch.gov.uk")
                                .withPaymentStatus(PaymentStatus.PAID)
                                .withCheckoutDate(LocalDate.of(2022, 4, 12).atStartOfDay())
                                .withLinks(new Links(new HRef("http"), new HRef("http")))
                                .build()));

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param("email", "demo@ch")
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_USER_ROLE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected), true));
    }

    @DisplayName("Should find a single checkout when a valid company number is provided")
    @Test
    void searchCheckoutsByCompanyNumber() throws Exception {
        checkoutRepository.save(StubHelper.getCheckout(ORDER_ID, "demo@ch.gov.uk", "12345678", PaymentStatus.PAID));
        checkoutRepository.save(StubHelper.getCheckout(CHECKOUT_ID, "demo2@ch.gov.uk", "23456781", PaymentStatus.PAID));

        CheckoutSearchResults expected = new CheckoutSearchResults(1,
                Collections.singletonList(
                        CheckoutSummary.newBuilder()
                                .withId(ORDER_ID)
                                .withEmail("demo@ch.gov.uk")
                                .withPaymentStatus(PaymentStatus.PAID)
                                .withCheckoutDate(LocalDate.of(2022, 4, 12).atStartOfDay())
                                .withLinks(new Links(new HRef("http"), new HRef("http")))
                                .build()));

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .param("company_number", "12345678")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_USER_ROLE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected), true));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("noMatchesFixture")
    void searchReturnsNoMatches(final String displayName, final String searchField, final String searchValue) throws Exception {
        checkoutRepository.save(StubHelper.getCheckout(CHECKOUT_ID, "demo@ch.gov.uk", "12345678"));
        CheckoutSearchResults expected = new CheckoutSearchResults(0, Collections.emptyList());

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .param(searchField, searchValue)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_USER_ROLE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected), true));
    }

    @DisplayName("Should return a page containing a single order when page_size is one")
    @Test
    void limitSearchResultsToPageSize() throws Exception {
        checkoutRepository.save(StubHelper.getCheckout(ORDER_ID, "demo@ch.gov.uk", "12345678", PaymentStatus.PAID));
        checkoutRepository.save(StubHelper.getCheckout(CHECKOUT_ID, "demo2@ch.gov.uk", "23456781", PaymentStatus.PAID));

        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "*")
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_orders", is(2)))
                .andExpect(jsonPath("$.order_summaries.*", hasSize(1)));
    }


    @DisplayName("Should return HTTP 400 Bad Request if page size less than 1")
    @Test
    void returnBadRequestPageSize0() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(ORDERS_SEARCH_PATH)
                    .param("page_size", "0")
                    .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                    .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                    .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "*")
                    .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        ConstraintValidationError constraintValidationError =
                (new ObjectMapper()).readValue(mvcResult.getResponse().getContentAsString(), ConstraintValidationError.class);
        Assertions.assertEquals(constraintValidationError.getStatus(), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals(constraintValidationError.getConstraintErrors().get(0), "page_size must be greater than 0");
    }

    @DisplayName("Should return HTTP 400 Bad Request if query parameter page_size is absent")
    @Test
    void returnBadRequestIfPageSizeAbsent() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, "*")
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        ConstraintValidationError constraintValidationError =
                (new ObjectMapper()).readValue(mvcResult.getResponse().getContentAsString(), ConstraintValidationError.class);
        Assertions.assertEquals(constraintValidationError.getStatus(), HttpStatus.BAD_REQUEST);
        Assertions.assertEquals(constraintValidationError.getConstraintErrors().get(0), "page_size is mandatory");
    }

    @DisplayName("Order search fails to authenticate caller when identity not provided")
    @Test
    void ordersSearchFailsAuthenticationNullIdentity() throws Exception {
        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Order search fails to authenticate caller when identity type not provided")
    @Test
    void ordersSearchFailsAuthenticationNullIdentityType() throws Exception {
        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Order search fails to authorise caller when identity type is oauth2 and authorised roles does not contain permission /admin/chs-order-investigator")
    @Test
    void ordersSearchFailsOauth2Authorisation() throws Exception {
        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                        .header(ERIC_IDENTITY_TYPE, OAUTH2_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_ROLES, "permission-a permission-b")
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Order search correctly authorises caller when identity type is oauth2 and authorised roles contain permission /admin/chs-order-investigation")
    @Test
    void ordersSearchWithOauth2AuthorisationSucceeds() throws Exception {
        mockMvc.perform(get(ORDERS_SEARCH_PATH)
                        .param(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY, ERIC_IDENTITY_VALUE)
                        .header(ERIC_IDENTITY_TYPE, OAUTH2_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_ROLES, "permission-a /admin/chs-order-investigation permission-b")
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @DisplayName("Unauthenticated reprocess order request is unauthorised")
    @Test
    void reprocessOrderWithoutAuthIsUnauthorised() throws Exception {
        mockMvc.perform(post("/orders/" + ORDER_ID + "/reprocess")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Authenticated user reprocess order request is unauthorised")
    @Test
    void reprocessOrderWithUserAuthIsUnauthorised() throws Exception {
        mockMvc.perform(post("/orders/" + ORDER_ID + "/reprocess")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Authenticated internal API reprocess order request is OK")
    @Test
    void reprocessOrderWithInternalApiAuthIsOK() throws Exception {
        orderRepository.save(StubHelper.getOrder(ORDER_ID, "demo@ch.gov.uk", "12345678"));
        mockMvc.perform(reprocessOrderWithRequiredCredentials())
                .andExpect(status().isOk());
    }

    @DisplayName("Reprocess order confirms successful reprocessing")
    @Test
    void reprocessOrderConfirmsSuccessfulReprocessing() throws Exception {
        orderRepository.save(StubHelper.getOrder(ORDER_ID, "demo@ch.gov.uk", "12345678"));
        mockMvc.perform(reprocessOrderWithRequiredCredentials())
                .andExpect(status().isOk())
                .andExpect(content().string(new StringContains("Order number 0001 reprocessed.")));
    }

    @DisplayName("Reprocess order reports missing order and checkout")
    @Test
    void reprocessOrderReportsMissingOrderAndCheckout() throws Exception {
        mockMvc.perform(reprocessOrderWithRequiredCredentials())
                .andExpect(status().isConflict())
                .andExpect(content().string(new StringContains("No order number 0001 found. Is order number correct? ***")));
    }

    @DisplayName("Reprocess order reports missing order and payment status")
    @Test
    void reprocessOrderReportsMissingOrderAndPaymentStatus() throws Exception {
        checkoutRepository.save(StubHelper.getCheckout(ORDER_ID, "demo@ch.gov.uk", "12345678", PaymentStatus.FAILED));
        mockMvc.perform(reprocessOrderWithRequiredCredentials())
                .andExpect(status().isConflict())
                .andExpect(content().string(new StringContains("No order number 0001 found. Payment status was FAILED. ***")));
    }

    @DisplayName("Get an order item")
    @Test
    void getOrderItem() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        preexistingOrder.setData(orderData);
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        Item expectedItem = StubHelper.getOrderItem("CCD-123456-123456", "item#certified-copy",
                "12345678");
        orderData.setItems(Arrays.asList(
                StubHelper.getOrderItem("MID-123456-123456", "item#missing-image-delivery",
                        "12345678"),
                expectedItem,
                StubHelper.getOrderItem("CRT-123456-123456", "item#certificate",
                        "12345678")));
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/" + ORDER_ID + "/items/CCD-123456-123456")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedItem)));
    }

    @DisplayName("Get order item returns HTTP 404 Not Found if no matching item ID")
    @Test
    void getOrderItemReturnsNotFoundIfNoMatchingItemID() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        preexistingOrder.setData(orderData);
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        Item expectedItem = StubHelper.getOrderItem("CCD-123456-123456", "item#certified-copy",
                "12345678");
        orderData.setItems(Arrays.asList(
                StubHelper.getOrderItem("MID-123456-123456", "item#missing-image-delivery",
                        "12345678"),
                expectedItem,
                StubHelper.getOrderItem("CRT-123456-123456", "item#certificate",
                        "12345678")));
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/orders/" + ORDER_ID + "/items/NONEXISTENT")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Patch an order item")
    @Test
    void patchOrderItem() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        preexistingOrder.setData(orderData);
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        Item expectedItem = StubHelper.getOrderItem("CCD-123456-123456", "item#certified-copy",
                "12345678");
        orderData.setItems(Arrays.asList(
                StubHelper.getOrderItem("MID-123456-123456", "item#missing-image-delivery",
                        "12345678"),
                expectedItem,
                StubHelper.getOrderItem("CRT-123456-123456", "item#certificate",
                        "12345678")));
        orderRepository.save(preexistingOrder);

        PatchOrderedItemDTO patchOrderedItemDTO = new PatchOrderedItemDTO();
        patchOrderedItemDTO.setStatus(ItemStatus.SATISFIED);
        patchOrderedItemDTO.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION);

        mockMvc.perform(patch("/orders/" + ORDER_ID + "/items/CCD-123456-123456")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.UPDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patchOrderedItemDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.digital_document_location").value(DIGITAL_DOCUMENT_LOCATION))
                .andExpect(jsonPath("$.status").value(STATUS_SATISFIED));
    }

    @DisplayName("Patch an order item returns HTTP 404 Not Found if no matching item ID")
    @Test
    void patchOrderItemReturnsNotFoundIfNoMatchingItemID() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final OrderData orderData = new OrderData();
        preexistingOrder.setData(orderData);
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        Item expectedItem = StubHelper.getOrderItem("CCD-123456-123456", "item#certified-copy",
                "12345678");
        orderData.setItems(Arrays.asList(
                StubHelper.getOrderItem("MID-123456-123456", "item#missing-image-delivery",
                        "12345678"),
                expectedItem,
                StubHelper.getOrderItem("CRT-123456-123456", "item#certificate",
                        "12345678")));
        orderRepository.save(preexistingOrder);

        PatchOrderedItemDTO patchOrderedItemDTO = new PatchOrderedItemDTO();
        patchOrderedItemDTO.setStatus(ItemStatus.SATISFIED);
        patchOrderedItemDTO.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION);

        mockMvc.perform(patch("/orders/" + ORDER_ID + "/items/NONEXISTENT")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.UPDATE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(patchOrderedItemDTO)))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Get a checkout item")
    @Test
    void getCheckoutItem() throws Exception {
        final Checkout preexistingCheckout = new Checkout();
        preexistingCheckout.setId(CHECKOUT_ID);
        preexistingCheckout.setUserId(ERIC_IDENTITY_VALUE);
        final CheckoutData checkoutData = new CheckoutData();
        preexistingCheckout.setData(checkoutData);
        checkoutData.setReference(ORDER_REFERENCE);
        checkoutData.setTotalOrderCost("100");
        Item expectedItem = StubHelper.getOrderItem("CCD-123456-123456", "item#certified-copy",
                "12345678");
        checkoutData.setItems(Arrays.asList(
                StubHelper.getOrderItem("MID-123456-123456", "item#missing-image-delivery",
                        "12345678"),
                expectedItem,
                StubHelper.getOrderItem("CRT-123456-123456", "item#certificate",
                        "12345678")));
        checkoutRepository.save(preexistingCheckout);

        final CheckoutData expectedCheckoutData = new CheckoutData();
        expectedCheckoutData.setItems(Collections.singletonList(expectedItem));

        mockMvc.perform(get("/checkouts/" + CHECKOUT_ID + "/items/CCD-123456-123456")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_USER_ROLE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedCheckoutData)));
    }

    @DisplayName("Get checkout item returns HTTP 404 Not Found if no matching item ID")
    @Test
    void getCheckoutItemReturnsNotFoundIfNoMatchingItemID() throws Exception {
        final Checkout preexistingOrder = new Checkout();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        final CheckoutData orderData = new CheckoutData();
        preexistingOrder.setData(orderData);
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        Item expectedItem = StubHelper.getOrderItem("CCD-123456-123456", "item#certified-copy",
                "12345678");
        orderData.setItems(Arrays.asList(
                StubHelper.getOrderItem("MID-123456-123456", "item#missing-image-delivery",
                        "12345678"),
                expectedItem,
                StubHelper.getOrderItem("CRT-123456-123456", "item#certificate",
                        "12345678")));
        checkoutRepository.save(preexistingOrder);

        mockMvc.perform(get("/checkouts/" + ORDER_ID + "/items/NONEXISTENT")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_USER_ROLE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Get checkout item returns HTTP 404 Not Found if no matching checkout ID")
    @Test
    void getCheckoutItemReturnsNotFoundIfNoMatchingCheckoutID() throws Exception {
        mockMvc.perform(get("/checkouts/" + ORDER_ID + "/items/NONEXISTENT")
                        .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                        .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                        .header(ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_USER_ROLE)
                        .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                        .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private MockHttpServletRequestBuilder reprocessOrderWithRequiredCredentials() {
        return post("/orders/" + ORDER_ID + "/reprocess")
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_API_KEY_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_KEY_ROLES, INTERNAL_USER_ROLE)
                .header(ApiSdkManager.getEricPassthroughTokenHeader(), ERIC_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON);
    }

    static Stream<Arguments> noMatchesFixture() {
        return Stream.of(Arguments.arguments("Should not find a order when incomplete order id is provided", "id", "00"),
                Arguments.arguments("Should not find a order when a incorrect email address is provided", "email", "wrong@ch.gov.uk"),
                Arguments.arguments("Should not find a order when a incomplete company number is provided", "company_number", "345678912"));
    }
}
