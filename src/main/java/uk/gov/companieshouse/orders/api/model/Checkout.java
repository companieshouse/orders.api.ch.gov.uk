package uk.gov.companieshouse.orders.api.model;

import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "checkout")
public class Checkout extends AbstractOrder {

    private CheckoutData data = new CheckoutData();

    public CheckoutData getData() {
        return data;
    }

    public void setData(CheckoutData data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Checkout checkout = (Checkout) o;
        return Objects.equals(data, checkout.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
