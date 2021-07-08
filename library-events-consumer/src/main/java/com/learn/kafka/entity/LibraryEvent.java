package com.learn.kafka.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class LibraryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer libraryEventId;

    @Enumerated(EnumType.STRING)
    private LibraryEventType eventType;

    @OneToOne(mappedBy = "libraryEvent", cascade = {CascadeType.ALL})
    private Book book;

}
