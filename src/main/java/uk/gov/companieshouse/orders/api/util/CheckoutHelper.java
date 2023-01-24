package uk.gov.companieshouse.orders.api.util;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.model.Checkout;
import uk.gov.companieshouse.orders.api.model.CheckoutData;
import uk.gov.companieshouse.orders.api.model.Item;
import uk.gov.companieshouse.orders.api.model.ItemCosts;

import java.util.List;

@Component
public class CheckoutHelper {
    /**
     * Calculates `total_order_cost` of the checkout object
     * @param checkout checkout object
     * @return total order cost
     */
    public int calculateTotalOrderCostForCheckout(Checkout checkout) {
        CheckoutData checkoutData = checkout.getData();
        List<Item> items = checkoutData.getItems();
        int totalOrderCost = 0;
        for (Item item : items) {
            List<ItemCosts> itemCosts = item.getItemCosts();
            int totalCalculatedCosts = 0;
            if (itemCosts != null) {
                for (ItemCosts itemCost : itemCosts) {
                    totalCalculatedCosts += Integer.parseInt(itemCost.getCalculatedCost());
                }
                totalOrderCost += totalCalculatedCosts + Integer.parseInt(item.getPostageCost());
            }
        }

        // TODO BI-12341 Remove temporary hack.
        // return totalOrderCost;
        return 0;
    }
}