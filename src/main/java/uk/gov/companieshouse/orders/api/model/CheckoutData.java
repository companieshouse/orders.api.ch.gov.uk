package uk.gov.companieshouse.orders.api.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class CheckoutData extends AbstractOrderData {

    private OffsetDateTime paidAt;

    private ActionedBy checkedOutBy;

    private PaymentStatus status;

    private CheckoutLinks links;

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public ActionedBy getCheckedOutBy() {
        return checkedOutBy;
    }

    public void setCheckedOutBy(ActionedBy checkedOutBy) {
        this.checkedOutBy = checkedOutBy;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public CheckoutLinks getLinks() {
        return links;
    }

    public void setLinks(CheckoutLinks links) {
        this.links = links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CheckoutData that = (CheckoutData) o;
        return Objects.equals(paidAt, that.paidAt) && Objects.equals(checkedOutBy, that.checkedOutBy) && status == that.status && Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paidAt, checkedOutBy, status, links);
    }
}
