package com.learn.kafka.domain;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibraryEvent {

    private Integer libraryEventId;

    private LibraryEventType eventType;

    @NotNull
    @Valid
    private Book book;

}
