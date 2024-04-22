package uk.gov.companieshouse.orders.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.api.util.security.EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS;
import static uk.gov.companieshouse.api.util.security.SecurityConstants.INTERNAL_USER_ROLE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.API_KEY_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY_TYPE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.REQUEST_ID_HEADER_NAME;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_PERMISSION_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.TOKEN_REQUEST_ID_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.orders.api.config.AbstractMongoConfig;
import uk.gov.companieshouse.orders.api.model.CheckoutSearchResults;
import uk.gov.companieshouse.orders.api.model.CheckoutSummary;
import uk.gov.companieshouse.orders.api.model.HRef;
import uk.gov.companieshouse.orders.api.model.Links;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.util.StubHelper;
import uk.gov.companieshouse.orders.api.util.TestConstants;

@Testcontainers
@DirtiesContext
@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedKafka
@ActiveProfiles({"orders-search-enabled", "orders-search-multibasket-disabled"})
class OrdersSearchMultibasketDisabledIntegrationTest extends AbstractMongoConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mongoDBContainer.start();
    }

    @AfterEach
    void tearDown() {
        checkoutRepository.deleteAll();
    }

    @DisplayName("Should find a single checkout when searching by order id")
    @Test
    void searchCheckoutsById() throws Exception {
        checkoutRepository.save(
                StubHelper.getCheckout(TestConstants.CHECKOUT_ID, "demo@ch.gov.uk", "12345678",
                        PaymentStatus.PAID));
        CheckoutSearchResults expected = new CheckoutSearchResults(1,
                Collections.singletonList(
                        CheckoutSummary.newBuilder()
                                       .withId(TestConstants.CHECKOUT_ID)
                                       .withEmail("demo@ch.gov.uk")
                                       .withCompanyNumber("12345678")
                                       .withProductLine("item#certificate")
                                       .withPaymentStatus(PaymentStatus.PAID)
                                       .withCheckoutDate(LocalDate.of(2022, 4, 12).atStartOfDay())
                                       .withLinks(new Links(new HRef("http"), new HRef("http")))
                                       .build()));

        mockMvc.perform(get(TestConstants.CHECKOUT_SEARCH_PATH)
                       .param(TestConstants.CHECKOUT_SEARCH_ID_PARAM, TestConstants.CHECKOUT_ID)
                       .param(TestConstants.CHECKOUT_SEARCH_PAGE_SIZE_PARAM, "1")
                       .header(REQUEST_ID_HEADER_NAME, TOKEN_REQUEST_ID_VALUE)
                       .header(ERIC_IDENTITY_TYPE, API_KEY_IDENTITY_TYPE)
                       .header(TestConstants.ERIC_AUTHORISED_KEY_PRIVILEGES, INTERNAL_USER_ROLE)
                       .header(ERIC_IDENTITY_HEADER_NAME, ERIC_IDENTITY_VALUE)
                       .header(ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(TOKEN_PERMISSION_VALUE, Permission.Value.READ))
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(mapper.writeValueAsString(expected), true));
    }
}
