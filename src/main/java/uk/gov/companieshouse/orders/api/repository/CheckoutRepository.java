package uk.gov.companieshouse.orders.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import uk.gov.companieshouse.orders.api.model.Checkout;

@Repository
public interface CheckoutRepository extends MongoRepository<Checkout, String> {

    @Query(value = "{ 'id': {$regex: ?0}, 'data.checked_out_by.email': {$regex: ?1, $options: 'i'}, 'data.items.company_number': {$regex: ?2} }")
    Page<Checkout> searchCheckouts(String id, String email, String companyNumber, Pageable pageCriteria);
}
