package uk.gov.companieshouse.orders.api.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.orders.api.logging.LoggingUtils;
import uk.gov.companieshouse.orders.api.model.Item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ItemEnricher {

    private final ApiClientService apiClientService;

    public ItemEnricher(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * For each {@link Item item}, fetch the related item resource using the item's URI and map the returned entity.
     *
     * @param nonEnrichedItems A {@link List list} of non-enriched {@link Item items}.
     * @param userIdentity The user's identity.
     * @param logMap A {@link Map map} containing entries to be logged by the logging framework. Fields item_uri and
     *               company_number will be populated by this method.
     * @return A {@link List list of} {@link Item items} enriched with data fetched by each item's URI.
     */
    public List<Item> enrichItemsByIdentifiers(List<Item> nonEnrichedItems, String userIdentity, Map<String, Object> logMap) {
            List<String> processedItemUris = Collections.synchronizedList(new ArrayList<>());
            List<String> companyNumbers = Collections.synchronizedList(new ArrayList<>());
            logMap.put(LoggingUtils.ITEM_URI, processedItemUris);
            logMap.put(LoggingUtils.COMPANY_NUMBER, companyNumbers);
            return nonEnrichedItems
                    .parallelStream()
                    .map(item -> {
                        String itemUri = item.getItemUri();
                        try {
                            Item result = apiClientService.getItem(userIdentity, itemUri);
                            processedItemUris.add(itemUri);
                            companyNumbers.add(result.getCompanyNumber());
                            return result;
                        } catch (IOException e) {
                            throw new ItemEnrichmentException("Failed to enrich item: [" + itemUri + "]", e);
                        }
                    }).collect(Collectors.toList());
    }
}
