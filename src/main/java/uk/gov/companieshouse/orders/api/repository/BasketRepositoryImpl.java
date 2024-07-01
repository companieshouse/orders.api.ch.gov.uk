package uk.gov.companieshouse.orders.api.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.time.OffsetDateTime;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;


public class BasketRepositoryImpl implements BasketRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public BasketRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Basket clearBasketDataById(String id) {
        Query query = new Query().addCriteria(where("_id").is(id));

        Basket basket = mongoTemplate.findOne(query, Basket.class);
        BasketData basketData = basket.getData();
        DeliveryDetails deliveryDetails = basketData.getDeliveryDetails();
        BasketData newBasketData = new BasketData();
        newBasketData.setDeliveryDetails(deliveryDetails);
        newBasketData.setEnrolled(basketData.isEnrolled());

        Update update = new Update();
        update.set("data", newBasketData);
        update.set("updated_at", OffsetDateTime.now());

        return mongoTemplate.findAndModify(query, update, Basket.class);
    }

    @Override
    public Basket removeBasketDataItemByUri(String id, String uri) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        update.pull("data.items", Query.query(Criteria.where("item_uri").is(uri)));
        update.set("updated_at", OffsetDateTime.now());

        return mongoTemplate.findAndModify(query, update, Basket.class);
    }
}
