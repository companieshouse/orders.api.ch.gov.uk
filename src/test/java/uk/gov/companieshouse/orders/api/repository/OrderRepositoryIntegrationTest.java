package uk.gov.companieshouse.orders.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import uk.gov.companieshouse.orders.api.model.ActionedBy;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.Order;
import uk.gov.companieshouse.orders.api.model.OrderData;

@DataMongoTest
public class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @DisplayName("repository returns an order when only order id specified")
    @Test
    void testSearchOrdersId() {
        // given
        Order order = new Order();
        order.setId("ORD-123-456");
        order.setData(new OrderData());
        order.getData().setOrderedBy(new ActionedBy());
        order.getData().getOrderedBy().setEmail("demo@ch.gov.uk");
        order.getData().setItems(Collections.singletonList(new Item()));
        order.getData().getItems().get(0).setCompanyNumber("12345678");
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
        Order order = new Order();
        order.setId("ORD-123-456");
        order.setData(new OrderData());
        order.getData().setOrderedBy(new ActionedBy());
        order.getData().getOrderedBy().setEmail("demo@ch.gov.uk");
        order.getData().setItems(Collections.singletonList(new Item()));
        order.getData().getItems().get(0).setCompanyNumber("12345678");
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
        Order order = new Order();
        order.setId("ORD-123-456");
        order.setData(new OrderData());
        order.getData().setOrderedBy(new ActionedBy());
        order.getData().getOrderedBy().setEmail("demo@ch.gov.uk");
        order.getData().setItems(Collections.singletonList(new Item()));
        order.getData().getItems().get(0).setCompanyNumber("12345678");
        orderRepository.save(order);

        // when
        List<Order> orders = orderRepository.searchOrders(".*", "^.*ch\\.gov\\.uk\\.*$", ".*");

        // then
        assertEquals(1, orders.size());
        assertEquals(order.getId(), orders.get(0).getId());
        assertEquals(order.getData().getOrderedBy().getEmail(), orders.get(0).getData().getOrderedBy().getEmail());
    }
}
