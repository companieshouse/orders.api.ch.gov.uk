package uk.gov.companieshouse.orders.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.orders.api.model.ActionedBy;
import uk.gov.companieshouse.orders.api.model.Certificate;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopy;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;
import uk.gov.companieshouse.orders.api.model.OrderLinks;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.repository.OrderRepository;

import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS;
import static uk.gov.companieshouse.orders.api.model.CertificateType.INCORPORATION_WITH_ALL_NAME_CHANGES;
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
class OrderControllerIntegrationTest {
    private static final String ORDER_ID = "0001";
    private static final String ORDER_REFERENCE = "0001";
    private static final String CHECKOUT_ID = "0002";
    private static final String CHECKOUT_REFERENCE = "0002";
    private static final String COMPANY_STATUS_ACTIVE = "active";

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

    @Test
    void searchOrders() throws Exception {
        final Order preexistingOrder = new Order();
        preexistingOrder.setId(ORDER_ID);
        preexistingOrder.setUserId(ERIC_IDENTITY_VALUE);
        preexistingOrder.setCreatedAt(LocalDate.of(2022, 04, 12).atStartOfDay());
        final OrderData orderData = new OrderData();
        orderData.setReference(ORDER_REFERENCE);
        orderData.setTotalOrderCost("100");
        orderData.setOrderedBy(new ActionedBy());
        orderData.getOrderedBy().setEmail("demo@ch.gov.uk");
        orderData.setItems(Collections.singletonList(new Item()));
        orderData.getItems().get(0).setKind("item#certificate");
        orderData.setLinks(new OrderLinks());
        orderData.getLinks().setSelf("http");
        preexistingOrder.setData(orderData);
        orderRepository.save(preexistingOrder);

        mockMvc.perform(get("/search/orders")
                //.param("id", ORDER_ID)
                .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                .header(ERIC_IDENTITY_TYPE_HEADER_NAME, ERIC_IDENTITY_OAUTH2_TYPE_VALUE)
                .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(orderData)));
    }
}
