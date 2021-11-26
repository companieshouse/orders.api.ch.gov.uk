package uk.gov.companieshouse.orders.api.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFICATE_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.CERTIFIED_COPY_KIND;
import static uk.gov.companieshouse.orders.api.util.TestConstants.MISSING_IMAGE_DELIVERY_KIND;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.orders.api.model.CertificateItemOptions;
import uk.gov.companieshouse.orders.api.model.CertifiedCopyItemOptions;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.MissingImageDeliveryItemOptions;

/**
 * Unit tests the {@link OrderItemOptionsReader} component.
 */
@ExtendWith(MockitoExtension.class)
class OrderItemOptionsReaderTest {

    private static final String UNKNOWN_KIND = "item#unknown";
    private static final String UNUSED_ORDER_TYPE_NAME = "'order' or 'checkout', value unused in this test.";

    @InjectMocks
    private OrderItemOptionsReader readerUnderTest;

    @Mock
    private Document orderDocument;

    @Mock
    private List<Item> items;

    @Mock
    private Item certificateItem;

    @Mock
    private Item certifiedCopyItem;

    @Mock
    private Item missingImageDeliveryItem;

    @Mock
    private Document orderDataDocument;

    @Mock
    private List<Document> itemDocuments;

    @Mock
    private Document itemDocument;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Document optionsDocument;

    @Mock
    private CertificateItemOptions certificateItemOptions;

    @Mock
    private CertifiedCopyItemOptions certifiedCopyItemOptions;

    @Mock
    private MissingImageDeliveryItemOptions missingImageDeliveryItemOptions;

    @Test
    @DisplayName("readOrderItemsOptions() updates certificate item options correctly")
    void readOrderItemsOptionsUpdatesCertificateItemOptionsCorrectly() throws IOException {

        // Given
        when(items.size()).thenReturn(1);
        when(items.get(0)).thenReturn(certificateItem);

        when(orderDocument.get("data", Document.class)).thenReturn(orderDataDocument);
        when(orderDataDocument.get("items", List.class)).thenReturn(itemDocuments);
        when(itemDocuments.get(0)).thenReturn(itemDocument);
        when(itemDocument.get("item_options", Document.class)).thenReturn(optionsDocument);
        when(optionsDocument.toJson()).thenReturn("{}");
        when(certificateItem.getKind()).thenReturn(CERTIFICATE_KIND);
        when(mapper.readValue("{}", CertificateItemOptions.class)).thenReturn(certificateItemOptions);

        // When
        readerUnderTest.readOrderItemsOptions(items, orderDocument, UNUSED_ORDER_TYPE_NAME);

        // Then
        verify(items).get(0);
        verify(certificateItem).getKind();
        verify(mapper).readValue("{}", CertificateItemOptions.class);
        verify(certificateItem).setItemOptions(certificateItemOptions);
    }

    @Test
    @DisplayName("readOrderItemsOptions() updates certified copy item options correctly")
    void readOrderItemsOptionsUpdatesCertifiedCopyItemOptionsCorrectly() throws IOException {

        // Given
        when(items.size()).thenReturn(1);
        when(items.get(0)).thenReturn(certifiedCopyItem);

        when(orderDocument.get("data", Document.class)).thenReturn(orderDataDocument);
        when(orderDataDocument.get("items", List.class)).thenReturn(itemDocuments);
        when(itemDocuments.get(0)).thenReturn(itemDocument);
        when(itemDocument.get("item_options", Document.class)).thenReturn(optionsDocument);
        when(optionsDocument.toJson()).thenReturn("{}");
        when(certifiedCopyItem.getKind()).thenReturn(CERTIFIED_COPY_KIND);
        when(mapper.readValue("{}", CertifiedCopyItemOptions.class)).thenReturn(certifiedCopyItemOptions);

        // When
        readerUnderTest.readOrderItemsOptions(items, orderDocument, UNUSED_ORDER_TYPE_NAME);

        // Then
        verify(items).get(0);
        verify(certifiedCopyItem).getKind();
        verify(mapper).readValue("{}", CertifiedCopyItemOptions.class);
        verify(certifiedCopyItem).setItemOptions(certifiedCopyItemOptions);
    }

    @Test
    @DisplayName("readOrderItemsOptions() updates missing image delivery item options correctly")
    void readOrderItemsOptionsUpdatesMissingImageDeliveryItemOptionsCorrectly() throws IOException {

        // Given
        when(items.size()).thenReturn(1);
        when(items.get(0)).thenReturn(missingImageDeliveryItem);

        when(orderDocument.get("data", Document.class)).thenReturn(orderDataDocument);
        when(orderDataDocument.get("items", List.class)).thenReturn(itemDocuments);
        when(itemDocuments.get(0)).thenReturn(itemDocument);
        when(itemDocument.get("item_options", Document.class)).thenReturn(optionsDocument);
        when(optionsDocument.toJson()).thenReturn("{}");
        when(missingImageDeliveryItem.getKind()).thenReturn(MISSING_IMAGE_DELIVERY_KIND);
        when(mapper.readValue("{}", MissingImageDeliveryItemOptions.class)).thenReturn(missingImageDeliveryItemOptions);

        // When
        readerUnderTest.readOrderItemsOptions(items, orderDocument, UNUSED_ORDER_TYPE_NAME);

        // Then
        verify(items).get(0);
        verify(missingImageDeliveryItem).getKind();
        verify(mapper).readValue("{}", MissingImageDeliveryItemOptions.class);
        verify(missingImageDeliveryItem).setItemOptions(missingImageDeliveryItemOptions);
    }

    @Test
    @DisplayName("readOrderItemsOptions() copes with missing item options")
    void readOrderItemsOptionsCopesWithMissingItemOptions() throws IOException {

        // Given
        when(items.size()).thenReturn(1);
        when(items.get(0)).thenReturn(certificateItem);

        when(orderDocument.get("data", Document.class)).thenReturn(orderDataDocument);
        when(orderDataDocument.get("items", List.class)).thenReturn(itemDocuments);
        when(itemDocuments.get(0)).thenReturn(itemDocument);
        when(itemDocument.get("item_options", Document.class)).thenReturn(null);

        // When
        readerUnderTest.readOrderItemsOptions(items, orderDocument, UNUSED_ORDER_TYPE_NAME);

        // Then
        verify(items).get(0);
        verify(mapper, never()).readValue(anyString(), ArgumentMatchers.eq(CertificateItemOptions.class));
    }

    @Test
    @DisplayName("readOrderItemsOptions() propagates mapper IOException as an IllegalStateException")
    void readOrderItemsOptionsPropagatesMapperIOExceptionAsIllegalStateException() throws IOException {

        // Given
        when(items.size()).thenReturn(1);
        when(items.get(0)).thenReturn(certificateItem);

        when(orderDocument.get("data", Document.class)).thenReturn(orderDataDocument);
        when(orderDataDocument.get("items", List.class)).thenReturn(itemDocuments);
        when(itemDocuments.get(0)).thenReturn(itemDocument);
        when(itemDocument.get("item_options", Document.class)).thenReturn(optionsDocument);
        when(optionsDocument.toJson()).thenReturn("{}");
        when(certificateItem.getKind()).thenReturn(CERTIFICATE_KIND);
        when(mapper.readValue("{}", CertificateItemOptions.class)).thenThrow(new IllegalStateException("Test message"));

        // When and then
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class,
                        () -> readerUnderTest.readOrderItemsOptions(items, orderDocument, UNUSED_ORDER_TYPE_NAME));
        assertThat(exception.getMessage(), is("Test message"));

        // Then
        verify(items).get(0);
        verify(certificateItem).getKind();
        verify(mapper).readValue("{}", CertificateItemOptions.class);
        verify(certificateItem, never()).setItemOptions(certificateItemOptions);

    }

    @Test
    @DisplayName("readOrderItemsOptions() throws IllegalStateException if no checkout document found on event")
    void readOrderItemsOptionsThrowsIllegalStateExceptionIfNoCheckoutDocumentFoundOnEvent() {
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class,
                        () -> readerUnderTest.readOrderItemsOptions(items, null, "checkout"));
        assertThat(exception.getMessage(), is("No checkout document found on event."));
    }

    @Test
    @DisplayName("readOrderItemsOptions() throws IllegalStateException if no order document found on event")
    void readOrderItemsOptionsThrowsIllegalStateExceptionIfNoOrderDocumentFoundOnEvent() {
        final IllegalStateException exception =
                assertThrows(IllegalStateException.class,
                        () -> readerUnderTest.readOrderItemsOptions(items, null, "order"));
        assertThat(exception.getMessage(), is("No order document found on event."));
    }

    @Test
    @DisplayName("getType() infers item options class type correctly from kind")
    void getTypeInfersItemOptionsClassCorrectlyFromKind() {
        assertEquals(CertificateItemOptions.class, readerUnderTest.getType(CERTIFICATE_KIND).getOptionsType());
        assertEquals(CertifiedCopyItemOptions.class, readerUnderTest.getType(CERTIFIED_COPY_KIND).getOptionsType());
    }

    @Test
    @DisplayName("getType() throws IllegalArgumentException for unknown kind")
    void getTypeThrowsIllegalArgumentExceptionForUnknownKind() {
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> readerUnderTest.getType(UNKNOWN_KIND));
        assertEquals("'" + UNKNOWN_KIND + "' is not a known kind!", exception.getMessage());
    }

}
