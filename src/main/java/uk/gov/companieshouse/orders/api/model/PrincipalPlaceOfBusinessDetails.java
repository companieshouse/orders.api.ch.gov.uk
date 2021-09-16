package uk.gov.companieshouse.orders.api.model;

public class PrincipalPlaceOfBusinessDetails {
    private IncludeAddressRecordsType includeAddressRecordsType;
    private Boolean includeDates;

    public IncludeAddressRecordsType getIncludeAddressRecordsType() {
        return includeAddressRecordsType;
    }

    public void setIncludeAddressRecordsType(IncludeAddressRecordsType includeAddressRecordsType) {
        this.includeAddressRecordsType = includeAddressRecordsType;
    }

    public Boolean getIncludeDates() {
        return includeDates;
    }

    public void setIncludeDates(Boolean includeDates) {
        this.includeDates = includeDates;
    }
}
