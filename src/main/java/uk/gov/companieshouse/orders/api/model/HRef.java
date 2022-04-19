package uk.gov.companieshouse.orders.api.model;

import java.util.Objects;

public class HRef {
    private final String link;

    public HRef(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HRef hRef = (HRef) o;
        return Objects.equals(link, hRef.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}
