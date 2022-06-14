package uk.gov.companieshouse.orders.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.orders.api.model.Order;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    @Query(value = "{ 'id': {$regex: ?0}, 'data.ordered_by.email': {$regex: ?1, $options: 'i'}, 'data.items.company_number': {$regex: ?2} }")
    List<Order> searchOrders(String id, String email, String companyNumber);

    @Query(value = "{ 'id': {$regex: ?0}, 'data.ordered_by.email': {$regex: ?1, $options: 'i'}, 'data.items.company_number': {$regex: ?2} }")
    Page<Order> searchOrders(String id, String email, String companyNumber, Pageable pageCriteria);
}
