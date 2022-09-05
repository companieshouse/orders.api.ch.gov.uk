package uk.gov.companieshouse.orders.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_AUTHORISED_USER_VALUE;
import static uk.gov.companieshouse.orders.api.util.TestConstants.ERIC_IDENTITY_VALUE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.util.CheckoutHelper;
import uk.gov.companieshouse.orders.api.util.TimestampedEntityVerifier;

@ExtendWith(MockitoExtension.class)
public class CheckoutServiceTest {

    private static final String COMPANY_NUMBER = "00006400";
    private static final String ETAG = "etag";
    private static final String LINKS_SELF = "links/self";
    private static final String LINKS_PAYMENT = "links/payment";
    private static final String KIND = "order";

    private static final String COMPANY_NAME = "company name";
    private static final String ADDRESS_LINE_1 = "address line 1";
    private static final String ADDRESS_LINE_2 = "address line 2";
    private static final String COUNTRY = "country";
    private static final String FORENAME = "forename";
    private static final String LOCALITY = "locality";
    private static final String PO_BOX = "po box";
    private static final String POSTAL_CODE = "postal code";
    private static final String REGION = "region";
    private static final String SURNAME = "surname";

    private static final int EXPECTED_TOTAL_ORDER_COST = 20;
    private static final String POSTAGE_COST = "5";
    private static final String DISCOUNT_APPLIED_1 = "0";
    private static final String ITEM_COST_1 = "5";
    private static final String CALCULATED_COST_1 = "5";
    private static final String DISCOUNT_APPLIED_2 = "10";
    private static final String ITEM_COST_2 = "5";
    private static final String CALCULATED_COST_2 = "5";
    private static final String DISCOUNT_APPLIED_3 = "0";
    private static final String ITEM_COST_3 = "5";
    private static final String CALCULATED_COST_3 = "5";

    @InjectMocks
    CheckoutService serviceUnderTest;

    @Mock
    CheckoutRepository checkoutRepository;

    @Mock
    EtagGeneratorService etagGeneratorService;

    @Mock
    LinksGeneratorService linksGeneratorService;

    @Mock
    CheckoutHelper checkoutHelper;

    @Mock
    private CheckoutSummaryBuilderFactory builderFactory;

    @Mock
    private CheckoutCriteria checkoutCriteria;

    @Mock
    private CheckoutSearchCriteria checkoutSearchCriteria;

    @Mock
    private Checkout checkoutResult;

    @Mock
    private CheckoutData checkoutData;

    @Mock
    private ActionedBy checkedOutBy;

    @Mock
    private CheckoutLinks links;

    @Mock
    private Item item;

    @Mock
    private SearchFieldMapper searchFieldMapper;

    @Mock
    private PageCriteria pageCriteria;

    @Mock
    private Page<Checkout> pages;

    @Captor
    ArgumentCaptor<Checkout> checkoutCaptor;

    private TimestampedEntityVerifier timestamps;

    @BeforeEach
    void setUp() {
        timestamps = new TimestampedEntityVerifier();
    }

    @Test
    void createCheckoutPopulatesCreatedAndUpdated() {
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        timestamps.start();

        serviceUnderTest.createCheckout(Collections.singletonList(new Certificate()),
                ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        timestamps.end();

        timestamps.verifyCreationTimestampsWithinExecutionInterval(checkout());
    }

    @Test
    void createCheckoutPopulatesAndSavesItem() {
        Certificate certificate = new Certificate();
        certificate.setCompanyNumber(COMPANY_NUMBER);
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        serviceUnderTest.createCheckout(Collections.singletonList(certificate), ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        assertEquals(1, checkout().getData().getItems().size());
        assertEquals(ERIC_IDENTITY_VALUE, checkout().getUserId());
        assertEquals(COMPANY_NUMBER, checkout().getData().getItems().get(0).getCompanyNumber());
        assertEquals(checkout().getId(), checkout().getData().getReference());
        assertEquals(KIND, checkout().getData().getKind());
    }

    @Test
    void createCheckoutPopulatesAndSavesCheckedOutBy() {
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());

        serviceUnderTest.createCheckout(Collections.singletonList(new Certificate()),
                ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        assertThat(checkout().getData().getCheckedOutBy().getId(), is(ERIC_IDENTITY_VALUE));
        assertThat(checkout().getData().getCheckedOutBy().getEmail(), is(ERIC_AUTHORISED_USER_VALUE));
    }

    @Test
    void createCheckoutPopulatesAndSavesDeliveryDetails() {
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());
        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setCompanyName(COMPANY_NAME);
        deliveryDetails.setAddressLine1(ADDRESS_LINE_1);
        deliveryDetails.setAddressLine2(ADDRESS_LINE_2);
        deliveryDetails.setCountry(COUNTRY);
        deliveryDetails.setForename(FORENAME);
        deliveryDetails.setLocality(LOCALITY);
        deliveryDetails.setPoBox(PO_BOX);
        deliveryDetails.setPostalCode(POSTAL_CODE);
        deliveryDetails.setRegion(REGION);
        deliveryDetails.setSurname(SURNAME);

        serviceUnderTest.createCheckout(Collections.singletonList(new Certificate()),
                ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, deliveryDetails);
        verify(checkoutRepository).save(checkoutCaptor.capture());

        DeliveryDetails createdDeliveryDetails = checkout().getData().getDeliveryDetails();
        assertThat(createdDeliveryDetails.getCompanyName(), is(COMPANY_NAME));
        assertThat(createdDeliveryDetails.getAddressLine1(), is(ADDRESS_LINE_1));
        assertThat(createdDeliveryDetails.getAddressLine2(), is(ADDRESS_LINE_2));
        assertThat(createdDeliveryDetails.getCountry(), is(COUNTRY));
        assertThat(createdDeliveryDetails.getForename(), is(FORENAME));
        assertThat(createdDeliveryDetails.getLocality(), is(LOCALITY));
        assertThat(createdDeliveryDetails.getPoBox(), is(PO_BOX));
        assertThat(createdDeliveryDetails.getPostalCode(), is(POSTAL_CODE));
        assertThat(createdDeliveryDetails.getRegion(), is(REGION));
        assertThat(createdDeliveryDetails.getSurname(), is(SURNAME));
    }

    @Test
    void createCheckoutPopulatesAndSavesEtagAndLinks() {
        CheckoutLinks checkoutLinks = new CheckoutLinks();
        checkoutLinks.setSelf(LINKS_SELF);
        checkoutLinks.setPayment(LINKS_PAYMENT);

        when(checkoutRepository.save(any(Checkout.class))).thenReturn(new Checkout());
        when(etagGeneratorService.generateEtag()).thenReturn(ETAG);
        when(linksGeneratorService.generateCheckoutLinks(any(String.class))).thenReturn(checkoutLinks);

        serviceUnderTest.createCheckout(Collections.singletonList(new Certificate()),
                ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        verify(etagGeneratorService, times(1)).generateEtag();
        verify(linksGeneratorService, times(1)).generateCheckoutLinks(any(String.class));
        assertEquals(LINKS_SELF, checkout().getData().getLinks().getSelf());
        assertEquals(LINKS_PAYMENT, checkout().getData().getLinks().getPayment());
        assertEquals(ETAG, checkout().getData().getEtag());
    }

    @Test
    @DisplayName("createCheckout populates `total order cost` correctly")
    void createCheckoutPopulatesTotalOrderCost() {
        Item certificateItem = createCertificateItem();
        doCallRealMethod().when(checkoutHelper).calculateTotalOrderCostForCheckout(any());
        serviceUnderTest.createCheckout(Collections.singletonList(certificateItem),
                ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());

        assertThat(checkout().getData().getTotalOrderCost(), is(EXPECTED_TOTAL_ORDER_COST + ""));
    }

    @Test
    @DisplayName("createCheckout populates `id` in the format ORD-######-######")
    void createCheckoutPopulatesIdCorrectly() {
        Item certificateItem = createCertificateItem();
        serviceUnderTest.createCheckout(Collections.singletonList(certificateItem),
                ERIC_IDENTITY_VALUE,
                ERIC_AUTHORISED_USER_VALUE, new DeliveryDetails());
        verify(checkoutRepository).save(checkoutCaptor.capture());
        assertTrue(checkout().getId().matches("^ORD-\\d{6}-\\d{6}$")); ;
    }

    @Test
    @DisplayName("saveCheckout saves updated checkout correctly")
    void saveCheckoutSavesUpdatedCheckout() {

        // Given
        final Checkout checkout = new Checkout();
        checkout.setCreatedAt(LocalDateTime.now());
        checkout.getData().setStatus(PaymentStatus.PAID);
        when(etagGeneratorService.generateEtag()).thenReturn(ETAG);

        timestamps.start();

        // When
        serviceUnderTest.saveCheckout(checkout);

        timestamps.end();

        // Then
        verify(etagGeneratorService, times(1)).generateEtag();
        verify(checkoutRepository).save(checkoutCaptor.capture());
        assertThat(checkout().getData().getEtag(), is(ETAG));
        timestamps.verifyUpdatedAtTimestampWithinExecutionInterval(checkout());
    }

    @Test
    @DisplayName("search checkouts returns an expected checkout with all details populated")
    void searchCheckouts() {
        //given
        when(checkoutSearchCriteria.getCheckoutCriteria()).thenReturn(checkoutCriteria);
        when(checkoutSearchCriteria.getPageCriteria()).thenReturn(pageCriteria);
        when(checkoutCriteria.getCheckoutId()).thenReturn("ORD-123-456");
        when(checkoutCriteria.getEmail()).thenReturn("demo@ch.gov.uk");
        when(checkoutCriteria.getCompanyNumber()).thenReturn("12345678");
        when(pageCriteria.getPageSize()).thenReturn(1);
        when(checkoutRepository.searchCheckouts(anyString(), anyString(), anyString(), eq(PageRequest.of(0, 1, Sort.by("created_at").descending().and(Sort.by("_id")))))).thenReturn(pages);
        when(pages.getTotalElements()).thenReturn(42L);
        when(pages.toList()).thenReturn(Collections.singletonList(checkoutResult));
        when(checkoutResult.getId()).thenReturn("ORD-123-456");
        when(checkoutResult.getData()).thenReturn(checkoutData);
        when(checkoutData.getCheckedOutBy()).thenReturn(checkedOutBy);
        when(checkedOutBy.getEmail()).thenReturn("demo@ch.gov.uk");
        when(checkoutData.getItems()).thenReturn(Collections.singletonList(item));
        when(item.getKind()).thenReturn("item#certificate");
        when(checkoutResult.getCreatedAt()).thenReturn(LocalDate.of(2022, 04, 11).atStartOfDay());
        when(checkoutData.getLinks()).thenReturn(links);
        when(links.getSelf()).thenReturn("http");
        when(searchFieldMapper.exactMatchOrAny("ORD-123-456")).thenReturn("mapped checkout id");
        when(searchFieldMapper.exactMatchOrAny("12345678")).thenReturn("mapped company number");
        when(searchFieldMapper.partialMatchOrAny("demo@ch.gov.uk")).thenReturn("mapped email");
        when(builderFactory.newCheckoutSummaryBuilder()).thenReturn(CheckoutSummary.newBuilder());

        CheckoutSearchResults expected = new CheckoutSearchResults(42L,
                Collections.singletonList(
                        CheckoutSummary.newBuilder()
                                .withId("ORD-123-456")
                                .withEmail("demo@ch.gov.uk")
                                .withProductLine("item#certificate")
                                .withCheckoutDate(LocalDate.of(2022, 04, 11).atStartOfDay())
                                .withLinks(new Links(new HRef("http"), new HRef("http")))
                                .build()));

        //when
        CheckoutSearchResults actual = serviceUnderTest.searchCheckouts(checkoutSearchCriteria);

        //then
        verify(checkoutRepository).searchCheckouts("mapped checkout id",
                "mapped email",
                "mapped company number",
                PageRequest.of(0, 1, Sort.by("created_at").descending().and(Sort.by("_id"))));
        assertThat(actual, is(expected));
    }

    @Test
    @DisplayName("search orders returns an order with blank details")
    void searchCheckoutsWithBlankDetails() {
        //given
        when(checkoutSearchCriteria.getCheckoutCriteria()).thenReturn(checkoutCriteria);
        when(checkoutSearchCriteria.getPageCriteria()).thenReturn(pageCriteria);
        when(checkoutCriteria.getCheckoutId()).thenReturn("");
        when(checkoutCriteria.getEmail()).thenReturn("");
        when(checkoutCriteria.getCompanyNumber()).thenReturn("");
        when(pageCriteria.getPageSize()).thenReturn(1);
        when(checkoutRepository.searchCheckouts(anyString(), anyString(), anyString(), eq(PageRequest.of(0, 1, Sort.by("created_at").descending().and(Sort.by("_id")))))).thenReturn(pages);
        when(pages.getTotalElements()).thenReturn(42L);
        when(pages.toList()).thenReturn(Collections.singletonList(checkoutResult));
        when(searchFieldMapper.exactMatchOrAny(anyString())).thenReturn("mapped string");
        when(searchFieldMapper.partialMatchOrAny(anyString())).thenReturn("mapped string");
        when(builderFactory.newCheckoutSummaryBuilder()).thenReturn(CheckoutSummary.newBuilder());

        CheckoutSummary orderSummary = CheckoutSummary.newBuilder().build();

        CheckoutSearchResults expected = new CheckoutSearchResults(42L,
                Collections.singletonList(orderSummary));

        //when
        CheckoutSearchResults actual = serviceUnderTest.searchCheckouts(checkoutSearchCriteria);

        //then
        assertThat(actual, is(expected));
    }

    @Test
    @DisplayName("search orders returns a single order when page size is one")
    void searchCheckoutsLimitsSearchResults() {
        //given
        when(checkoutSearchCriteria.getCheckoutCriteria()).thenReturn(checkoutCriteria);
        when(checkoutSearchCriteria.getPageCriteria()).thenReturn(pageCriteria);
        when(checkoutCriteria.getCheckoutId()).thenReturn("");
        when(checkoutCriteria.getEmail()).thenReturn("");
        when(checkoutCriteria.getCompanyNumber()).thenReturn("");
        when(pageCriteria.getPageSize()).thenReturn(1);
        when(checkoutRepository.searchCheckouts(anyString(), anyString(), anyString(), eq(PageRequest.of(0, 1, Sort.by("created_at").descending().and(Sort.by("_id")))))).thenReturn(pages);
        when(pages.getTotalElements()).thenReturn(42L);
        when(pages.toList()).thenReturn(Collections.singletonList(checkoutResult));
        when(searchFieldMapper.exactMatchOrAny(anyString())).thenReturn("mapped string");
        when(searchFieldMapper.partialMatchOrAny(anyString())).thenReturn("mapped string");
        when(builderFactory.newCheckoutSummaryBuilder()).thenReturn(CheckoutSummary.newBuilder());

        //when
        CheckoutSearchResults actual = serviceUnderTest.searchCheckouts(checkoutSearchCriteria);

        //then
        assertThat(actual.getTotalOrders(), is(42L));
        assertThat(actual.getOrderSummaries().size(), is(1));
    }

    /**
     * @return the captured {@link Checkout}.
     */
    private Checkout checkout() {
        return checkoutCaptor.getValue();
    }

    private CheckoutData createCheckoutData(){
        List<Item> items = new ArrayList<>();
        items.add(createCertificateItem());
        CheckoutData checkoutData = new CheckoutData();
        checkoutData.setItems(items);

        return checkoutData;
    }

    private Item createCertificateItem(){
        List<ItemCosts> itemCosts = new ArrayList<>();
        ItemCosts itemCosts1 = new ItemCosts();
        itemCosts1.setDiscountApplied(DISCOUNT_APPLIED_1);
        itemCosts1.setItemCost(ITEM_COST_1);
        itemCosts1.setCalculatedCost(CALCULATED_COST_1);
        itemCosts.add(itemCosts1);
        ItemCosts itemCosts2 = new ItemCosts();
        itemCosts2.setDiscountApplied(DISCOUNT_APPLIED_2);
        itemCosts2.setItemCost(ITEM_COST_2);
        itemCosts2.setCalculatedCost(CALCULATED_COST_2);
        itemCosts.add(itemCosts2);
        ItemCosts itemCosts3 = new ItemCosts();
        itemCosts3.setDiscountApplied(DISCOUNT_APPLIED_3);
        itemCosts3.setItemCost(ITEM_COST_3);
        itemCosts3.setCalculatedCost(CALCULATED_COST_3);
        itemCosts.add(itemCosts3);

        Item item = new Item();
        item.setPostageCost(POSTAGE_COST);
        item.setItemCosts(itemCosts);

        return item;
    }
}
