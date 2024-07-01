package uk.gov.companieshouse.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import uk.gov.companieshouse.orders.api.model.PaymentStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class BasketPaymentRequestDTO {

    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("status")
    private PaymentStatus status;

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    @Override
    public String toString() { return new Gson().toJson(this); }

}
