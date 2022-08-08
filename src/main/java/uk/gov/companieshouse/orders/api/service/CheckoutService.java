package uk.gov.companieshouse.orders.api.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.orders.api.model.*;
import uk.gov.companieshouse.orders.api.repository.CheckoutRepository;
import uk.gov.companieshouse.orders.api.util.CheckoutHelper;

@Service
public class CheckoutService {

    private final CheckoutRepository checkoutRepository;
    private final EtagGeneratorService etagGeneratorService;
    private final LinksGeneratorService linksGeneratorService;
    private final CheckoutHelper checkoutHelper;
    private final SearchFieldMapper searchFieldMapper;

    public CheckoutService(CheckoutRepository checkoutRepository,
                           EtagGeneratorService etagGeneratorService,
                           LinksGeneratorService linksGeneratorService,
                           CheckoutHelper checkoutHelper,
                           SearchFieldMapper searchFieldMapper) {
        this.checkoutRepository = checkoutRepository;
        this.etagGeneratorService = etagGeneratorService;
        this.linksGeneratorService = linksGeneratorService;
        this.checkoutHelper = checkoutHelper;
        this.searchFieldMapper = searchFieldMapper;
    }

    private String autoGenerateId() {
        SecureRandom random = new SecureRandom();
        byte[] values = new byte[4];
        random.nextBytes(values);
        String rand = String.format("%04d", random.nextInt(9999));
        String time = String.format("%08d", Calendar.getInstance().getTimeInMillis() / 100000L);
        String rawId = rand + time;
        String[] tranId = rawId.split("(?<=\\G.{6})");
        return "ORD-" + String.join("-", tranId);
    }

    public Checkout createCheckout(List<Item> itemsList, String userId, String email,
            DeliveryDetails deliveryDetails) {
        final LocalDateTime now = LocalDateTime.now();
        String checkoutId = autoGenerateId();

        Checkout checkout = new Checkout();
        checkout.setId(checkoutId);
        checkout.setUserId(userId);
        checkout.setCreatedAt(now);
        checkout.setUpdatedAt(now);

        ActionedBy actionedBy = new ActionedBy();
        actionedBy.setId(userId);
        actionedBy.setEmail(email);
        checkout.getData().setCheckedOutBy(actionedBy);
        checkout.getData().setStatus(PaymentStatus.PENDING);
        checkout.getData().setEtag(etagGeneratorService.generateEtag());
        checkout.getData().setLinks(linksGeneratorService.generateCheckoutLinks(checkoutId));
        checkout.getData().getItems().addAll(itemsList);
        checkout.getData().setReference(checkoutId);
        checkout.getData().setKind("order");
        checkout.getData().setDeliveryDetails(deliveryDetails);
        String totalOrderCostStr = checkoutHelper.calculateTotalOrderCostForCheckout(checkout) + "";
        checkout.getData().setTotalOrderCost(totalOrderCostStr);

        return checkoutRepository.save(checkout);
    }

    public Optional<Checkout> getCheckoutById(String id) {
        return checkoutRepository.findById(id);
    }

    /**
     * Returns a result consisting of order summaries corresponding to the supplied search
     * criteria.
     *
     * @param checkoutSearchCriteria to find existing orders
     * @return OrderSearchResults matching the supplied criteria
     */
    public CheckoutSearchResults searchCheckouts(CheckoutSearchCriteria checkoutSearchCriteria) {
        CheckoutCriteria checkoutCriteria = checkoutSearchCriteria.getCheckoutCriteria();
        Page<Checkout> checkoutPages = checkoutRepository.searchCheckouts(
                searchFieldMapper.exactMatchOrAny(checkoutCriteria.getCheckoutId()),
                searchFieldMapper.partialMatchOrAny(checkoutCriteria.getEmail()),
                searchFieldMapper.exactMatchOrAny(checkoutCriteria.getCompanyNumber()),
                PageRequest.of(0, checkoutSearchCriteria.getPageCriteria().getPageSize(), Sort.by("created_at").descending().and(Sort.by("_id")))); //TODO: refactor into mapper implementation
        List<Checkout> checkouts = checkoutPages.toList();

        return new CheckoutSearchResults(checkoutPages.getTotalElements(),
                checkouts.stream().map(
                        checkout -> CheckoutSummary.newBuilder()
                                .withId(checkout.getId())
                                .withEmail(
                                        Optional.ofNullable(checkout.getData())
                                                .map(CheckoutData::getCheckedOutBy)
                                                .map(ActionedBy::getEmail)
                                                .orElse(null))
                                .withCompanyNumber(Optional.ofNullable(checkout.getData())
                                        .map(CheckoutData::getItems)
                                        .flatMap(items -> items.stream().findFirst())
                                        .map(Item::getCompanyNumber)
                                        .orElse(null))
                                .withProductLine(
                                        Optional.ofNullable(checkout.getData())
                                                .map(CheckoutData::getItems)
                                                .flatMap(items -> items.stream().findFirst())
                                                .map(Item::getKind)
                                                .orElse(null))
                                .withCheckoutDate(checkout.getCreatedAt())
                                .withPaymentStatus(Optional.ofNullable(checkout.getData())
                                        .map(CheckoutData::getStatus)
                                        .orElse(null))
                                .withLinks(
                                        Optional.ofNullable(checkout.getData())
                                                .map(CheckoutData::getLinks)
                                                .map(CheckoutLinks::getSelf)
                                                .map(self -> new Links(new HRef(self),
                                                        new HRef(self)))
                                                .orElse(null))
                                .build()
                ).collect(Collectors.toList()));
    }

    /**
     * Saves the checkout, assumed to have been updated, to the database.
     * @param updatedCheckout the certificate item to save
     * @return the latest checkout state resulting from the save
     */
    public Checkout saveCheckout(final Checkout updatedCheckout) {
        final LocalDateTime now = LocalDateTime.now();
        updatedCheckout.setUpdatedAt(now);
        updatedCheckout.getData().setEtag(etagGeneratorService.generateEtag());
        return checkoutRepository.save(updatedCheckout);
    }
}
