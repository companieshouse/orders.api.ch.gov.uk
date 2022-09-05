package uk.gov.companieshouse.orders.api.mapper;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsRequestDTO;
import uk.gov.companieshouse.orders.api.dto.AddDeliveryDetailsResponseDTO;
import uk.gov.companieshouse.orders.api.dto.DeliveryDetailsDTO;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(DeliveryDetailsMapperTest.Config.class)
public class DeliveryDetailsMapperTest {

    private static final String COMPANY_NAME = "company name";
    private static final String ADDRESS_LINE_1 = "address line 1";
    private static final String ADDRESS_LINE_2 = "address line 2";
    private static final String COUNTRY = "country";
    private static final String FORENAME = "forename";
    private static final String LOCALITY = "locality";
    private static final String PO_BOX = "po box";
    private static final String POSTAL_CODE = "postal code";
    private static final String REGION = "region";
    private static final String SURNAME = "surname";

    @Configuration
    @ComponentScan(basePackageClasses =  DeliveryDetailsMapperTest.class)
    static class Config{}

    @Autowired
    private DeliveryDetailsMapper deliveryDetailsMapper;

    @Test
    public void testDeliveryDetailsDTOToDeliveryDetails(){

        final AddDeliveryDetailsRequestDTO dto = new AddDeliveryDetailsRequestDTO();
        DeliveryDetailsDTO deliveryDetailsDTO = new DeliveryDetailsDTO();
        deliveryDetailsDTO.setCompanyName(COMPANY_NAME);
        deliveryDetailsDTO.setAddressLine1(ADDRESS_LINE_1);
        deliveryDetailsDTO.setAddressLine2(ADDRESS_LINE_2);
        deliveryDetailsDTO.setCountry(COUNTRY);
        deliveryDetailsDTO.setForename(FORENAME);
        deliveryDetailsDTO.setLocality(LOCALITY);
        deliveryDetailsDTO.setPoBox(PO_BOX);
        deliveryDetailsDTO.setPostalCode(POSTAL_CODE);
        deliveryDetailsDTO.setRegion(REGION);
        deliveryDetailsDTO.setSurname(SURNAME);
        dto.setDeliveryDetails(deliveryDetailsDTO);

        DeliveryDetails deliveryDetails = deliveryDetailsMapper.addToDeliveryDetailsRequestDTOToDeliveryDetails(dto);

        assertEquals(COMPANY_NAME, deliveryDetails.getCompanyName());
        assertEquals(ADDRESS_LINE_1, deliveryDetails.getAddressLine1());
        assertEquals(ADDRESS_LINE_2, deliveryDetails.getAddressLine2());
        assertEquals(COUNTRY, deliveryDetails.getCountry());
        assertEquals(FORENAME, deliveryDetails.getForename());
        assertEquals(LOCALITY, deliveryDetails.getLocality());
        assertEquals(PO_BOX, deliveryDetails.getPoBox());
        assertEquals(POSTAL_CODE, deliveryDetails.getPostalCode());
        assertEquals(REGION, deliveryDetails.getRegion());
        assertEquals(SURNAME, deliveryDetails.getSurname());
    }

    @Test
    public void testDeliveryDetailsToDeliveryDetailsDTO(){

        final DeliveryDetails deliveryDetails = new DeliveryDetails();

        deliveryDetails.setCompanyName(COMPANY_NAME);
        deliveryDetails.setAddressLine1(ADDRESS_LINE_1);
        deliveryDetails.setAddressLine2(ADDRESS_LINE_2);
        deliveryDetails.setCountry(COUNTRY);
        deliveryDetails.setForename(FORENAME);
        deliveryDetails.setLocality(LOCALITY);
        deliveryDetails.setPoBox(PO_BOX);
        deliveryDetails.setPostalCode(POSTAL_CODE);
        deliveryDetails.setRegion(REGION);
        deliveryDetails.setSurname(SURNAME);

        AddDeliveryDetailsResponseDTO addDeliveryDetailsResponseDTO = deliveryDetailsMapper.deliveryDetailsToAddToDeliveryDetailsDTO(deliveryDetails);

        assertEquals(COMPANY_NAME, addDeliveryDetailsResponseDTO.getCompanyName());
        assertEquals(ADDRESS_LINE_1, addDeliveryDetailsResponseDTO.getAddressLine1());
        assertEquals(ADDRESS_LINE_2, addDeliveryDetailsResponseDTO.getAddressLine2());
        assertEquals(COUNTRY, addDeliveryDetailsResponseDTO.getCountry());
        assertEquals(FORENAME, addDeliveryDetailsResponseDTO.getForename());
        assertEquals(LOCALITY, addDeliveryDetailsResponseDTO.getLocality());
        assertEquals(PO_BOX, addDeliveryDetailsResponseDTO.getPoBox());
        assertEquals(POSTAL_CODE, addDeliveryDetailsResponseDTO.getPostalCode());
        assertEquals(REGION, addDeliveryDetailsResponseDTO.getRegion());
        assertEquals(SURNAME, addDeliveryDetailsResponseDTO.getSurname());
    }
}
