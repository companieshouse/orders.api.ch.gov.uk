package uk.gov.companieshouse.orders.api.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.model.BasketData;
import uk.gov.companieshouse.orders.api.service.BasketService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.orders.api.util.EricHeaderHelper.ERIC_IDENTITY;

@ExtendWith(MockitoExtension.class)
class BasketEnrollmentFeatureToggleTest {
    @Mock
    private BasketService basketService;

    @Mock
    private Basket basket;

    @Mock
    private BasketData basketData;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private BasketEnrollmentFeatureToggle basketEnrollmentFeatureToggle;

    @Test
    void preHandleShouldReturnFalseWithStatusNotFoundIfBasketNonExistent() throws Exception {
        when(basketService.getBasketById(any())).thenReturn(Optional.empty());
        when(request.getHeader(any())).thenReturn("id");

        boolean actual = basketEnrollmentFeatureToggle.preHandle(request, response, mock(Object.class));

        assertFalse(actual);
        verify(request).getHeader(ERIC_IDENTITY);
        verify(basketService).getBasketById("id");
        verify(response).setStatus(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void preHandleShouldReturnFalseWithStatusNotFoundIfUserDisenrolled() throws Exception {
        when(basket.getData()).thenReturn(basketData);
        when(request.getHeader(any())).thenReturn("id");
        when(basketService.getBasketById(any())).thenReturn(Optional.of(basket));

        boolean actual = basketEnrollmentFeatureToggle.preHandle(request, response, mock(Object.class));

        assertFalse(actual);
        verify(request).getHeader(ERIC_IDENTITY);
        verify(basketService).getBasketById("id");
        verify(response).setStatus(HttpStatus.NOT_FOUND.value());
        verify(basketData).isEnrolled();
    }

    @Test
    void preHandleShouldReturnTrue() throws Exception {
        when(basket.getData()).thenReturn(basketData);
        when(request.getHeader(any())).thenReturn("id");
        when(basketService.getBasketById(any())).thenReturn(Optional.of(basket));
        when(basketData.isEnrolled()).thenReturn(true);

        boolean actual = basketEnrollmentFeatureToggle.preHandle(request, response, mock(Object.class));

        assertTrue(actual);
        verify(request).getHeader(ERIC_IDENTITY);
        verify(basketService).getBasketById("id");
        verify(basketData).isEnrolled();
        verifyNoInteractions(response);
    }
}
