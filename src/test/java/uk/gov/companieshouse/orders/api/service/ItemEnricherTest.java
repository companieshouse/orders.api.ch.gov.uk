package uk.gov.companieshouse.orders.api.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.model.Item;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemEnricherTest {

    private static final String PATH_TO_CERTIFICATE = "/path/to/certificate";
    private static final String PATH_TO_DOCUMENT = "/path/to/document";
    private static final String PATH_TO_MISSING_IMAGE = "/path/to/missingImage";
    private static final String COMPANY_NUMBER_CERTIFICATE = "00000001";
    private static final String COMPANY_NUMBER_DOCUMENT = "00000002";
    private static final String COMPANY_NUMBER_MISSING_IMAGE = "00000003";
    private static final String USER_ID = "user_id";

    @InjectMocks
    private ItemEnricher executionEngine;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private Item certificate;

    @Mock
    private Item document;

    @Mock
    private Item missingImage;

    @Test
    void testEnrichBasketItemsEnrichesItemsWithResourceData() throws IOException {
        // given
        when(certificate.getItemUri()).thenReturn(PATH_TO_CERTIFICATE);
        when(document.getItemUri()).thenReturn(PATH_TO_DOCUMENT);
        when(missingImage.getItemUri()).thenReturn(PATH_TO_MISSING_IMAGE);
        when(certificate.getCompanyNumber()).thenReturn(COMPANY_NUMBER_CERTIFICATE);
        when(document.getCompanyNumber()).thenReturn(COMPANY_NUMBER_DOCUMENT);
        when(missingImage.getCompanyNumber()).thenReturn(COMPANY_NUMBER_MISSING_IMAGE);
        when(apiClientService.getItem(any(), eq(PATH_TO_CERTIFICATE))).thenReturn(certificate);
        when(apiClientService.getItem(any(), eq(PATH_TO_DOCUMENT))).thenReturn(document);
        when(apiClientService.getItem(any(), eq(PATH_TO_MISSING_IMAGE))).thenReturn(missingImage);
        Map<String, Object> logMap = new HashMap<>();

        // when
        List<Item> result = executionEngine.enrichItemsByIdentifiers(Arrays.asList(certificate, document, missingImage), USER_ID, logMap);

        // then
        assertEquals(Arrays.asList(certificate, document, missingImage), result);
        assertTrue(logMap.containsKey(LoggingUtils.ITEM_URI));
        assertTrue(logMap.containsKey(LoggingUtils.COMPANY_NUMBER));
        verify(apiClientService).getItem(USER_ID, PATH_TO_CERTIFICATE);
        verify(apiClientService).getItem(USER_ID, PATH_TO_DOCUMENT);
        verify(apiClientService).getItem(USER_ID, PATH_TO_MISSING_IMAGE);
    }

    @Test
    @DisplayName("Fetch basket containing multiple items returns HTTP 400 Bad Request if exception thrown handling item")
    void fetchBasketWithMultipleItemsReturnsBadRequest() throws Exception {
        // given
        when(certificate.getItemUri()).thenReturn(PATH_TO_CERTIFICATE);
        when(document.getItemUri()).thenReturn(PATH_TO_DOCUMENT);
        when(missingImage.getItemUri()).thenReturn(PATH_TO_MISSING_IMAGE);
        when(certificate.getCompanyNumber()).thenReturn(COMPANY_NUMBER_CERTIFICATE);
        when(document.getCompanyNumber()).thenReturn(COMPANY_NUMBER_DOCUMENT);
        when(apiClientService.getItem(any(), eq(PATH_TO_CERTIFICATE))).thenReturn(certificate);
        when(apiClientService.getItem(any(), eq(PATH_TO_DOCUMENT))).thenReturn(document);
        when(apiClientService.getItem(any(), eq(PATH_TO_MISSING_IMAGE))).thenThrow(ApiErrorResponseException.class);
        Map<String, Object> logMap = new HashMap<>();

        // when
        Executable executable = () -> executionEngine.enrichItemsByIdentifiers(Arrays.asList(certificate, document, missingImage), USER_ID, logMap);

        // then
        ItemEnrichmentException enrichmentException = assertThrows(ItemEnrichmentException.class, executable);
        assertEquals("Failed to enrich item: [" + PATH_TO_MISSING_IMAGE + "]", enrichmentException.getMessage());
        verify(apiClientService).getItem(USER_ID, PATH_TO_CERTIFICATE);
        verify(apiClientService).getItem(USER_ID, PATH_TO_DOCUMENT);
        verify(apiClientService).getItem(USER_ID, PATH_TO_MISSING_IMAGE);
    }
}
