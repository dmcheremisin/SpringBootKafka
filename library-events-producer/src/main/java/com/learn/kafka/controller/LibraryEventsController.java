package com.learn.kafka.controller;

import com.learn.kafka.domain.LibraryEvent;
import com.learn.kafka.domain.LibraryEventType;
import com.learn.kafka.producer.LibraryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1/libraryEvent")
@RequiredArgsConstructor
public class LibraryEventsController {

    private final LibraryEventProducer libraryEventProducer;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryEvent postLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) {
        log.info("Received event type: {}", libraryEvent);
        libraryEvent.setEventType(LibraryEventType.NEW);
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return libraryEvent;
    }

    @PostMapping("/synchronous")
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryEvent postLibraryEventSync(@RequestBody LibraryEvent libraryEvent) {
        log.info("Received event type: {}", libraryEvent);
        libraryEvent.setEventType(LibraryEventType.NEW);
        SendResult<Integer, String> result = libraryEventProducer.sendLibraryEventSynchronous(libraryEvent);
        log.info("Result: {}", result);
        return libraryEvent;
    }

    @PostMapping("/producerRecord")
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryEvent postLibraryEventProducerRecord(@RequestBody LibraryEvent libraryEvent) {
        log.info("Received event type: {}", libraryEvent);
        libraryEvent.setEventType(LibraryEventType.NEW);
        libraryEventProducer.sendLibraryEventProducerRecord(libraryEvent);
        return libraryEvent;
    }

    @PutMapping
    public ResponseEntity<?> putLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) {
        log.info("Received event type: {}", libraryEvent);
        if (libraryEvent.getLibraryEventId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide libraryEventId");
        }

        libraryEvent.setEventType(LibraryEventType.UPDATE);
        libraryEventProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.OK).body(libraryEvent);
    }
}
