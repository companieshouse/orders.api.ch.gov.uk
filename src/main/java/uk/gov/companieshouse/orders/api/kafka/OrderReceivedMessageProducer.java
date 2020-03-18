package uk.gov.companieshouse.orders.api.kafka;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.orders.OrderReceived;

import java.util.concurrent.ExecutionException;

import static uk.gov.companieshouse.orders.api.OrdersApiApplication.APPLICATION_NAMESPACE;

@Service
public class OrderReceivedMessageProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    private final OrdersMessageFactory ordersAvroSerializer;
    private final OrdersKafkaProducer ordersKafkaProducer;

    public OrderReceivedMessageProducer(final OrdersMessageFactory avroSerializer, final OrdersKafkaProducer kafkaMessageProducer) {
        this.ordersAvroSerializer = avroSerializer;
        this.ordersKafkaProducer = kafkaMessageProducer;
    }

    /**
     * Sends order-received message to CHKafkaProducer
     * @param orderReceived order-received object
     * @throws SerializationException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void sendMessage(final OrderReceived orderReceived)
            throws SerializationException, ExecutionException, InterruptedException {
        LOGGER.trace("Sending message to kafka producer");
        Message message = ordersAvroSerializer.createMessage(orderReceived);
        ordersKafkaProducer.sendMessage(message);
    }
}
