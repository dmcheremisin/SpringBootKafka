package com.learn.kafka.consumer;

import com.learn.kafka.service.LibraryEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryEventsConsumerAutoCommit {

    private final LibraryEventsService libraryEventsService;

    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
        log.info("ConsumerRecord: {}", consumerRecord);
        libraryEventsService.processLibraryEvent(consumerRecord);
    }
}
