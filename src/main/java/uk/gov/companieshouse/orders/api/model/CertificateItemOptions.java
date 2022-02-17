package uk.gov.companieshouse.orders.api.model;

public class CertificateItemOptions extends DeliveryItemOptions {

    private CertificateType certificateType;

    private DirectorOrSecretaryDetails directorDetails;

    private Boolean includeCompanyObjectsInformation;

    private Boolean includeEmailCopy;

    private Boolean includeGoodStandingInformation;

    private RegisteredOfficeAddressDetails registeredOfficeAddressDetails;

    private DirectorOrSecretaryDetails secretaryDetails;

    private DesignatedMemberDetails designatedMemberDetails;

    private MemberDetails memberDetails;

    private GeneralPartnerDetails generalPartnerDetails;

    private LimitedPartnerDetails limitedPartnerDetails;

    private PrincipalPlaceOfBusinessDetails principalPlaceOfBusinessDetails;

    private Boolean includeGeneralNatureOfBusinessInformation;

    private String companyType;

    private LiquidatorsDetails liquidatorsDetails;

    private AdministratorsDetails administratorsDetails;

    private String companyStatus;

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }


    public DirectorOrSecretaryDetails getDirectorDetails() {
        return directorDetails;
    }

    public void setDirectorDetails(DirectorOrSecretaryDetails directorOrSecretaryDetails) {
        this.directorDetails = directorOrSecretaryDetails;
    }

    public Boolean getIncludeCompanyObjectsInformation() {
        return includeCompanyObjectsInformation;
    }

    public void setIncludeCompanyObjectsInformation(Boolean includeCompanyObjectsInformation) {
        this.includeCompanyObjectsInformation = includeCompanyObjectsInformation;
    }

    public Boolean getIncludeEmailCopy() {
        return includeEmailCopy;
    }

    public void setIncludeEmailCopy(Boolean includeEmailCopy) {
        this.includeEmailCopy = includeEmailCopy;
    }

    public Boolean getIncludeGoodStandingInformation() {
        return includeGoodStandingInformation;
    }

    public void setIncludeGoodStandingInformation(Boolean includeGoodStandingInformation) {
        this.includeGoodStandingInformation = includeGoodStandingInformation;
    }

    public RegisteredOfficeAddressDetails getRegisteredOfficeAddressDetails() {
        return registeredOfficeAddressDetails;
    }

    public void setRegisteredOfficeAddressDetails(RegisteredOfficeAddressDetails registeredOfficeAddressDetails) {
        this.registeredOfficeAddressDetails = registeredOfficeAddressDetails;
    }

    public DirectorOrSecretaryDetails getSecretaryDetails() {
        return secretaryDetails;
    }

    public void setSecretaryDetails(DirectorOrSecretaryDetails secretaryDetails) {
        this.secretaryDetails = secretaryDetails;
    }

    public DesignatedMemberDetails getDesignatedMemberDetails() {
        return designatedMemberDetails;
    }

    public void setDesignatedMemberDetails(DesignatedMemberDetails designatedMemberDetails) {
        this.designatedMemberDetails = designatedMemberDetails;
    }

    public MemberDetails getMemberDetails() {
        return memberDetails;
    }

    public void setMemberDetails(MemberDetails memberDetails) {
        this.memberDetails = memberDetails;
    }

    public GeneralPartnerDetails getGeneralPartnerDetails() {
        return generalPartnerDetails;
    }

    public void setGeneralPartnerDetails(GeneralPartnerDetails generalPartnerDetails) {
        this.generalPartnerDetails = generalPartnerDetails;
    }

    public LimitedPartnerDetails getLimitedPartnerDetails() {
        return limitedPartnerDetails;
    }

    public void setLimitedPartnerDetails(LimitedPartnerDetails limitedPartnerDetails) {
        this.limitedPartnerDetails = limitedPartnerDetails;
    }

    public PrincipalPlaceOfBusinessDetails getPrincipalPlaceOfBusinessDetails() {
        return principalPlaceOfBusinessDetails;
    }

    public void setPrincipalPlaceOfBusinessDetails(PrincipalPlaceOfBusinessDetails principalPlaceOfBusinessDetails) {
        this.principalPlaceOfBusinessDetails = principalPlaceOfBusinessDetails;
    }

    public Boolean getIncludeGeneralNatureOfBusinessInformation() {
        return includeGeneralNatureOfBusinessInformation;
    }

    public void setIncludeGeneralNatureOfBusinessInformation(Boolean includeGeneralNatureOfBusinessInformation) {
        this.includeGeneralNatureOfBusinessInformation = includeGeneralNatureOfBusinessInformation;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public LiquidatorsDetails getLiquidatorsDetails() {
        return liquidatorsDetails;
    }

    public void setLiquidatorsDetails(LiquidatorsDetails liquidatorsDetails) {
        this.liquidatorsDetails = liquidatorsDetails;
    }

    public AdministratorsDetails getAdministratorsDetails() {
        return administratorsDetails;
    }

    public void setAdministratorsDetails(AdministratorsDetails administratorsDetails) {
        this.administratorsDetails = administratorsDetails;
    }

    public String getCompanyStatus() {
        return companyStatus;
    }

    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }
}