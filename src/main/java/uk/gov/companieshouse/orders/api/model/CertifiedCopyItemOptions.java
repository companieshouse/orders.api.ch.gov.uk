package uk.gov.companieshouse.orders.api.model;

import java.util.List;

public class CertifiedCopyItemOptions extends DeliveryItemOptions {

    private List<FilingHistoryDocument> filingHistoryDocuments;

    public List<FilingHistoryDocument> getFilingHistoryDocuments() {
        return filingHistoryDocuments;
    }

    public void setFilingHistoryDocuments(List<FilingHistoryDocument> filingHistoryDocuments) {
        this.filingHistoryDocuments = filingHistoryDocuments;
    }
}
