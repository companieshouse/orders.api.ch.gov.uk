package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;
import java.time.OffsetDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "basket")
public class Basket implements TimestampedEntity {
    @Id
    private String id;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private BasketData data = new BasketData();


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BasketData getData() {
        return data;
    }

    public void setData(BasketData data) {
        this.data = data;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
