package uk.gov.companieshouse.orders.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.util.OrderHelper;

@DataMongoTest
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @DisplayName("repository returns an order when only order id specified")
    @Test
    void testSearchOrdersId() {
        // given
        Order order = OrderHelper.getOrder("ORD-123-456", "demo@ch.gov.uk", "12345678");
        orderRepository.save(order);

        // when
        List<Order> orders = orderRepository.searchOrders("^ORD-123-456$", ".*", ".*");

        // then
        assertEquals(1, orders.size());
        assertEquals(order.getId(), orders.get(0).getId());
        assertEquals(order.getData().getOrderedBy().getEmail(), orders.get(0).getData().getOrderedBy().getEmail());
        assertEquals(order.getData().getItems().get(0).getCompanyNumber(), orders.get(0).getData().getItems().get(0).getCompanyNumber());
    }

    @DisplayName("repository returns not found")
    @Test
    void testSearchOrdersNotFound() {
        // given
        Order order = OrderHelper.getOrder("ORD-123-456", "demo@ch.gov.uk", "12345678");
        orderRepository.save(order);

        // when
        List<Order> orders = orderRepository.searchOrders("^ORD-123-455$", ".*", ".*");

        // then
        assertEquals(0, orders.size());
    }

    @DisplayName("repository returns an order when only email specified")
    @Test
    void testSearchOrdersEmail() {
        // given
        Order order = OrderHelper.getOrder("ORD-123-456", "demo@ch.gov.uk", "12345678");
        orderRepository.save(order);

        // when
        List<Order> orders = orderRepository.searchOrders(".*", "ch\\.gov\\.uk", ".*");

        // then
        assertEquals(1, orders.size());
        assertEquals(order.getId(), orders.get(0).getId());
        assertEquals(order.getData().getOrderedBy().getEmail(), orders.get(0).getData().getOrderedBy().getEmail());
    }

    @Test
    @DisplayName("repository returns one order if page size of 1 specified")
    void testSearchOrdersWithPageSize() {
        // given
        Order firstOrder = OrderHelper.getOrder("ORD-123-456", "demo1@ch.gov.uk", "12345678", LocalDate.of(2021, 1, 1).atStartOfDay());
        Order secondOrder = OrderHelper.getOrder("ORD-654-321", "demo2@ch.gov.uk", "87654321", LocalDate.of(2022, 1, 1).atStartOfDay());
        Order thirdOrder = OrderHelper.getOrder("ORD-987-654", "demo3@ch.gov.uk", "87654321", LocalDate.of(2022, 1, 1).atStartOfDay());
        orderRepository.saveAll(Arrays.asList(firstOrder, secondOrder, thirdOrder));

        // when
        Page<Order> actual = orderRepository.searchOrders("", "", "", PageRequest.of(0, 1, Sort.by("data.ordered_at").descending().and(Sort.by("_id"))));

        // then
        assertEquals(3, actual.getTotalElements());
        assertEquals(3, actual.getTotalPages());
        assertEquals(secondOrder.getId(), actual.stream().findFirst().get().getId());
        assertEquals(secondOrder.getData().getOrderedBy().getEmail(), actual.stream().findFirst().get().getData().getOrderedBy().getEmail());
        assertEquals(secondOrder.getData().getItems().get(0).getCompanyNumber(), actual.stream().findFirst().get().getData().getItems().get(0).getCompanyNumber());
    }
}
