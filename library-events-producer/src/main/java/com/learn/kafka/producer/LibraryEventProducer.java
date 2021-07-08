package com.learn.kafka.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.kafka.domain.LibraryEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryEventProducer {

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper mapper;

    @SneakyThrows
    public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent) {
        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);

        SendResult<Integer, String> result = kafkaTemplate.sendDefault(key, value).get();
        return result;
    }

    @SneakyThrows
    public void sendLibraryEvent(LibraryEvent libraryEvent) {

        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

    @SneakyThrows
    public ListenableFuture<SendResult<Integer, String>> sendLibraryEventProducerRecord(LibraryEvent libraryEvent) {

        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
        listenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
        return listenableFuture;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
        List<Header> headers = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>("library-events", null, key, value, headers);
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully key: {}, value: {}, partition: {}",
                key, value, result.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error sending message key: {}, value: {}, exception: {}", key, value, ex.getMessage());
    }

}
