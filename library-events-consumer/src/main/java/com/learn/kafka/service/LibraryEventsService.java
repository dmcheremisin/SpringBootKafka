package com.learn.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.kafka.entity.LibraryEvent;
import com.learn.kafka.repository.LibraryEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryEventsService {

    private final LibraryEventsRepository libraryEventsRepository;
    private final ObjectMapper mapper;

    @SneakyThrows
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) {
        LibraryEvent libraryEvent = mapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("Library event: {}", libraryEvent);

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
}
