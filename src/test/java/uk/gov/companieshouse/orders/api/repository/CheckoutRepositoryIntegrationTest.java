package uk.gov.companieshouse.orders.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.util.StubHelper;

@DataMongoTest
class CheckoutRepositoryIntegrationTest {

    @Autowired
    private CheckoutRepository checkoutRepository;

    @DisplayName("repository returns an order when only checkout id specified")
    @Test
    void testSearchCheckoutsById() {
        // given
        Checkout checkout = StubHelper.getCheckout("ORD-123-456", "demo@ch.gov.uk", "12345678");
        checkoutRepository.save(checkout);

        // when
        Page<Checkout> checkouts = checkoutRepository.searchCheckouts("^ORD-123-456$", ".*", ".*", PageRequest.ofSize(2));
        List<Checkout> checkoutList = checkouts.toList();

        // then
        assertEquals(1, checkoutList.size());
        assertEquals(checkout.getId(), checkoutList.get(0).getId());
        assertEquals(checkout.getData().getCheckedOutBy().getEmail(), checkoutList.get(0).getData().getCheckedOutBy().getEmail());
        assertEquals(checkout.getData().getItems().get(0).getCompanyNumber(), checkoutList.get(0).getData().getItems().get(0).getCompanyNumber());
    }

    @DisplayName("repository returns not found")
    @Test
    void testSearchCheckoutsNotFound() {
        // given
        Checkout checkout = StubHelper.getCheckout("ORD-123-456", "demo@ch.gov.uk", "12345678");
        checkoutRepository.save(checkout);

        // when
        Page<Checkout> checkouts = checkoutRepository.searchCheckouts("^ORD-123-455$", ".*", ".*", PageRequest.ofSize(2));
        List<Checkout> checkoutList = checkouts.toList();

        // then
        assertTrue(checkoutList.isEmpty());
    }

    @DisplayName("repository returns a checkout when only email specified")
    @Test
    void testSearchCheckoutsEmail() {
        // given
        Checkout checkout = StubHelper.getCheckout("ORD-123-456", "demo@ch.gov.uk", "12345678");
        checkoutRepository.save(checkout);

        // when
        Page<Checkout> checkouts = checkoutRepository.searchCheckouts(".*", "ch\\.gov\\.uk", ".*", PageRequest.ofSize(2));
        List<Checkout> checkoutList = checkouts.toList();

        // then
        assertEquals(1, checkoutList.size());
        assertEquals(checkout.getId(), checkoutList.get(0).getId());
        assertEquals(checkout.getData().getCheckedOutBy().getEmail(), checkoutList.get(0).getData().getCheckedOutBy().getEmail());
    }

    @DisplayName("repository returns a checkout when only email specified and ignore case")
    @Test
    void testSearchCheckoutsEmailIgnoresCase() {
        // given
        Checkout checkout = StubHelper.getCheckout("ORD-123-456", "Demo@Ch.gov.uk", "12345678");
        checkoutRepository.save(checkout);

        // when
        Page<Checkout> checkouts = checkoutRepository.searchCheckouts(".*", "deMo@ch.GOV.uk", ".*", PageRequest.ofSize(2));
        List<Checkout> checkoutList = checkouts.toList();

        // then
        assertEquals(1, checkoutList.size());
        assertEquals(checkout.getId(), checkoutList.get(0).getId());
        assertEquals(checkout.getData().getCheckedOutBy().getEmail(), checkoutList.get(0).getData().getCheckedOutBy().getEmail());
    }

    @Test
    @DisplayName("repository returns one checkout if page size of 1 specified")
    void testSearchCheckoutsWithPageSize() {
        // given
        Checkout firstCheckout = StubHelper.getCheckout("ORD-123-456", "demo1@ch.gov.uk", "12345678", LocalDate.of(2021, 1, 1).atStartOfDay());
        Checkout secondCheckout = StubHelper.getCheckout("ORD-654-321", "demo2@ch.gov.uk", "87654321", LocalDate.of(2022, 1, 1).atStartOfDay());
        Checkout thirdCheckout = StubHelper.getCheckout("ORD-987-654", "demo3@ch.gov.uk", "87654321", LocalDate.of(2022, 1, 1).atStartOfDay());
        checkoutRepository.saveAll(Arrays.asList(firstCheckout, secondCheckout, thirdCheckout));

        // when
        Page<Checkout> actual = checkoutRepository.searchCheckouts("", "", "", PageRequest.of(0, 1, Sort.by("created_at").descending().and(Sort.by("_id"))));

        // then
        assertEquals(3, actual.getTotalElements());
        assertEquals(3, actual.getTotalPages());
        assertEquals(secondCheckout.getId(), actual.stream().findFirst().get().getId());
        assertEquals(secondCheckout.getData().getCheckedOutBy().getEmail(), actual.stream().findFirst().get().getData().getCheckedOutBy().getEmail());
        assertEquals(secondCheckout.getData().getItems().get(0).getCompanyNumber(), actual.stream().findFirst().get().getData().getItems().get(0).getCompanyNumber());
    }
}
