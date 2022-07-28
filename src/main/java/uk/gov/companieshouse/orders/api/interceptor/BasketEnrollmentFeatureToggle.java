package uk.gov.companieshouse.orders.api.interceptor;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.orders.api.model.Basket;
import uk.gov.companieshouse.orders.api.service.BasketService;
import uk.gov.companieshouse.orders.api.util.EricHeaderHelper;

@Component
public class BasketEnrollmentFeatureToggle implements HandlerInterceptor {

    private final BasketService basketService;

    public BasketEnrollmentFeatureToggle(BasketService basketService) {
        this.basketService = basketService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        Optional<Basket> basket = basketService.getBasketById(EricHeaderHelper.getIdentity(request));
        if (!basket.isPresent() || !basket.get().getData().isEnrolled()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return false;
        } else {
            return true;
        }
    }
}
