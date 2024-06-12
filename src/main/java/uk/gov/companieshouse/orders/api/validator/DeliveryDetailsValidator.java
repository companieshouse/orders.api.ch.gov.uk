package uk.gov.companieshouse.orders.api.validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeliveryDetailsValidator {

    private static final String ADDRESS_LINE_1_VALIDATION = "delivery_details.address_line_1 may not be blank";
    private static final String COUNTRY_VALIDATION = "delivery_details.country may not be blank";
    private static final String FORENAME_VALIDATION = "delivery_details.forename may not be blank";
    private static final String SURNAME_VALIDATION = "delivery_details.surname may not be blank";
    private static final String LOCALITY_VALIDATION = "delivery_details.locality may not be blank";
    private static final String POSTAL_CODE_VALIDATION = "Postcode or Region is required";

    public DeliveryDetailsValidator() { }

    public List<String> getValidationErrors(final AddDeliveryDetailsRequestDTO addDeliveryDetailsRequestDTO) {
        List<String> errors = new ArrayList<>();

        String addressLine1 = addDeliveryDetailsRequestDTO.getDeliveryDetails().getAddressLine1();
        String country = addDeliveryDetailsRequestDTO.getDeliveryDetails().getCountry();
        String forename = addDeliveryDetailsRequestDTO.getDeliveryDetails().getForename();
        String surname = addDeliveryDetailsRequestDTO.getDeliveryDetails().getSurname();
        String locality = addDeliveryDetailsRequestDTO.getDeliveryDetails().getLocality();
        String postalCode = addDeliveryDetailsRequestDTO.getDeliveryDetails().getPostalCode();
        String region = addDeliveryDetailsRequestDTO.getDeliveryDetails().getRegion();

        if(StringUtils.isBlank(postalCode) && StringUtils.isBlank(region)) {
            errors.add(POSTAL_CODE_VALIDATION);
        }

        if (StringUtils.isBlank(addressLine1)) {
            errors.add(ADDRESS_LINE_1_VALIDATION);
        }

        if (StringUtils.isBlank(country)) {
            errors.add(COUNTRY_VALIDATION);
        }

        if (StringUtils.isBlank(forename)) {
            errors.add(FORENAME_VALIDATION);
        }

        if (StringUtils.isBlank(surname)) {
            errors.add(SURNAME_VALIDATION);
        }

        if (StringUtils.isBlank(locality)) {
            errors.add(LOCALITY_VALIDATION);
        }

        return errors;
    }

    public boolean isValid(final DeliveryDetails deliveryDetails) {
        return (deliveryDetails != null &&
                StringUtils.isNotBlank(deliveryDetails.getAddressLine1()) &&
                StringUtils.isNotBlank(deliveryDetails.getForename()) &&
                StringUtils.isNotBlank(deliveryDetails.getSurname()) &&
                StringUtils.isNotBlank(deliveryDetails.getCountry()) &&
                StringUtils.isNotBlank(deliveryDetails.getLocality()) &&
                (StringUtils.isNotBlank(deliveryDetails.getPostalCode()) ||
                        StringUtils.isNotBlank(deliveryDetails.getRegion())));
    }
}
