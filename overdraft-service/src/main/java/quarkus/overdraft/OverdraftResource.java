package quarkus.overdraft;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.reactive.messaging.*;
import quarkus.overdraft.events.OverdraftLimitUpdate;
import quarkus.overdraft.events.Overdrawn;
import quarkus.overdraft.model.AccountOverdraft;
import quarkus.overdraft.model.CustomerOverdraft;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Path("/overdraft")
public class OverdraftResource {

    @Inject
    Tracer tracer; //OpenTracing tracer
    private final Map<Long, CustomerOverdraft> customerOverdrafts = new HashMap<>();

    @Incoming("account-overdrawn")
    @Outgoing("customer-overdrafts")
    public Message<Overdrawn> overdraftNotification(Message<Overdrawn> message) {
        log.info("Received account overdrawn");
        Overdrawn overdrawnPayload = message.getPayload();

        CustomerOverdraft customerOverdraft = customerOverdrafts.get(overdrawnPayload.customerNumber);

        if (customerOverdraft == null) {
            customerOverdraft = new CustomerOverdraft();
            customerOverdraft.customerNumber = overdrawnPayload.customerNumber;

            customerOverdrafts.put(overdrawnPayload.customerNumber, customerOverdraft);
        }

        AccountOverdraft accountOverdraft = customerOverdraft.accountOverdrafts.get(overdrawnPayload.accountNumber);
        if (accountOverdraft == null) {
            accountOverdraft = new AccountOverdraft();
            accountOverdraft.accountNumber = overdrawnPayload.accountNumber;

            customerOverdraft.accountOverdrafts.put(overdrawnPayload.accountNumber, accountOverdraft);
        }

        customerOverdraft.totalOverdrawnEvents++;
        accountOverdraft.currentOverdraft = overdrawnPayload.overdraftLimit;
        accountOverdraft.numberOverdrawnEvents++;
        message = message.addMetadata(customerOverdraft);
        RecordHeaders headers = new RecordHeaders();
        //Verifies there is IncomingKafkaRecordMetadata in the metadata; otherwise doesn’t handle traces
        if (message.getMetadata(IncomingKafkaRecordMetadata.class).isPresent()) {
            //we build the span for this operation
            Span span = tracer.buildSpan("process-overdraft-fee")
                    //Makes the new span a child of the span extracted from the Kafka message on the next line
                    .asChildOf(
                            //context from the incoming message headers
                            TracingKafkaUtils.extractSpanContext(
                                    message.getMetadata(IncomingKafkaRecordMetadata
                                            .class).get().getHeaders(),
                                    tracer))
                    .start();
            //the scope of the span is autoclosable
            try (Scope scope = tracer.activateSpan(span)) {
                //Retrieves the current span context, and injects it into RecordHeaders
                TracingKafkaUtils.inject(span.context(), headers, tracer);
            } finally {
                span.finish();
            }
        }
        //In addition to the metadata about the customer overdraft, also attaches metadata for
        //OutgoingKafkaRecordMetadata containing the trace headers
        OutgoingKafkaRecordMetadata<Object> kafkaMetadata =
                OutgoingKafkaRecordMetadata.builder()
                        .withHeaders(headers)
                        .build();
        log.info("Notifying customer overdraft");
        return message.addMetadata(kafkaMetadata);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public List<AccountOverdraft> retrieveAllAccountOverdrafts() {
        return customerOverdrafts.values()
            .stream()
            .flatMap(co -> co.accountOverdrafts.values().stream())
            .collect(Collectors.toList());
    }

    @Inject
    @Channel("overdraft-update")
    Emitter<OverdraftLimitUpdate> emitter;

    @PUT
    @Path("/{accountNumber}")
    public void updateAccountOverdraft(@PathParam("accountNumber") Long accountNumber, BigDecimal amount) {
        OverdraftLimitUpdate updateEvent = new OverdraftLimitUpdate();
        updateEvent.accountNumber = accountNumber;
        updateEvent.newOverdraftLimit = amount;

        emitter.send(updateEvent);
    }
}