package com.learn.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.kafka.entity.LibraryEvent;
import com.learn.kafka.repository.LibraryEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.persistence.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryEventsService {

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final LibraryEventsRepository libraryEventsRepository;
    private final ObjectMapper mapper;

    @SneakyThrows
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) {
        LibraryEvent libraryEvent = mapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("Library event: {}", libraryEvent);

        if (libraryEvent.getLibraryEventId() != null && libraryEvent.getLibraryEventId() == 123) {
            throw new RecoverableDataAccessException("Temporary db issue");
        }

        switch (libraryEvent.getEventType()) {
            case NEW:
                libraryEvent.setLibraryEventId(null);
                save(libraryEvent);
                break;
            case UPDATE:
                validateLibraryEvent(libraryEvent);
                save(libraryEvent);
                break;
            default:
                log.error("Library event type is invalid: {}", libraryEvent);
        }
    }

    private void validateLibraryEvent(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null)
            throw new IllegalArgumentException("Library event id is missing");

        libraryEventsRepository.findById(libraryEvent.getLibraryEventId())
                .orElseThrow(() -> new EntityNotFoundException("Library event is not present"));
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Successfully saved library event: {}", libraryEvent);
    }

    public void handleRecovery(ConsumerRecord<Integer, String> consumerRecord) {
        Integer key = consumerRecord.key();
        String message = consumerRecord.value();

        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, message);
        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, message, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, message, result);
            }
        });
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully key: {}, value: {}, partition: {}",
                key, value, result.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error sending message key: {}, value: {}, exception: {}", key, value, ex.getMessage());
    }
}
