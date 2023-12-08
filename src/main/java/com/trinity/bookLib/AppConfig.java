package com.trinity.bookLib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class AppConfig {
    private final BookRepository bookRepository;

    @Autowired
    public AppConfig(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Bean
    public CommandLineRunner initializeDB() {
        return args -> {
            // Add your sample product initialization logic here
            bookRepository.save(new Book("Book 1", "Author 1", "ISBN 1", LocalDate.of(2023, 1, 1)));
            bookRepository.save(new Book("Book 2", "Author 2", "ISBN 2", LocalDate.of(2023, 2, 1)));
            bookRepository.save(new Book("Book 3", "Author 3", "ISBN 3", LocalDate.of(2023, 3, 1)));
        };
    }
}
