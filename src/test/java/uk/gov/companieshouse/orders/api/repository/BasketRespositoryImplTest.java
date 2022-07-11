package uk.gov.companieshouse.orders.api.repository;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.model.DeliveryDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BasketRespositoryImplTest {

    @InjectMocks
    private BasketRepositoryImpl repositoryUnderTest;

    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    Basket basket;

    @Mock
    BasketData basketData;

    @Mock
    DeliveryDetails deliveryDetails;

    @Captor
    private ArgumentCaptor<Update> updateCaptor;

    @Test
    public void clearBasketDataByIdVerifyFindAndModifyCalledOnce() {
        when(mongoTemplate.findOne(any(), any())).thenReturn(basket);
        when(basket.getData()).thenReturn(basketData);
        when(basketData.getDeliveryDetails()).thenReturn(deliveryDetails);
        when(basketData.isEnrolled()).thenReturn(true);
        repositoryUnderTest.clearBasketDataById("ID");
        verify(mongoTemplate, times(1)).findOne(any(Query.class), eq(Basket.class));
        verify(mongoTemplate, times(1)).findAndModify(any(Query.class), updateCaptor.capture(), eq(Basket.class));
        assertEquals(expectedBasketData(deliveryDetails),
                updateCaptor.getValue().getUpdateObject().get("$set", Document.class).get("data"));
        assertNotNull(updateCaptor.getValue().getUpdateObject().get("$set", Document.class).get("updated_at"));
    }

    private BasketData expectedBasketData(DeliveryDetails deliveryDetails) {
        BasketData expectedBasketData = new BasketData();
        expectedBasketData.setEnrolled(true);
        expectedBasketData.setDeliveryDetails(deliveryDetails);
        return expectedBasketData;
    }
}
