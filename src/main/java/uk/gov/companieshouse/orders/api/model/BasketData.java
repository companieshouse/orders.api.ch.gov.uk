package uk.gov.companieshouse.orders.api.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BasketData {
    private DeliveryDetails deliveryDetails;

    private String etag;

    private List<Item> items = new ArrayList<>();

    private String kind;

    private BasketLinks links;

    private String totalBasketCost;

    private boolean enrolled;


    public DeliveryDetails getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(DeliveryDetails deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public BasketLinks getLinks() {
        return links;
    }

    public void setLinks(BasketLinks links) {
        this.links = links;
    }

    public String getTotalBasketCost() {
        return totalBasketCost;
    }

    public void setTotalBasketCost(String totalBasketCost) {
        this.totalBasketCost = totalBasketCost;
    }

    public boolean isEnrolled() {
        return enrolled;
    }

    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BasketData that = (BasketData) o;
        return enrolled == that.enrolled
                && Objects.equals(deliveryDetails, that.deliveryDetails)
                && Objects.equals(etag, that.etag)
                && Objects.equals(items, that.items)
                && Objects.equals(kind, that.kind)
                && Objects.equals(links, that.links)
                && Objects.equals(totalBasketCost, that.totalBasketCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deliveryDetails, etag, items, kind, links, totalBasketCost, enrolled);
    }
}
