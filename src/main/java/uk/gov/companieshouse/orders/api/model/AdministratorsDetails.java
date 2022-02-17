package uk.gov.companieshouse.orders.api.model;

public class AdministratorsDetails {
    private Boolean includeBasicInformation;

    public Boolean getIncludeBasicInformation() {
        return includeBasicInformation;
    }

    public void setIncludeBasicInformation(Boolean includeBasicInformation) {
        this.includeBasicInformation = includeBasicInformation;
    }
}