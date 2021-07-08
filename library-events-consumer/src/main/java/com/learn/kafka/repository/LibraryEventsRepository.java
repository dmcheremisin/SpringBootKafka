package com.learn.kafka.repository;

import com.learn.kafka.entity.LibraryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryEventsRepository extends JpaRepository<LibraryEvent, Integer> {
}
