package com.trinity.bookLib;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class BookControllerTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private JdbcClient jdbcClient;

    private List<Long> getIds() {
        return jdbcClient.sql("SELECT id FROM book")
                .query(Long.class)
                .list();
    }

    private Book getBook(Long id) {
        return jdbcClient.sql("SELECT * FROM book WHERE id = ?")
                .param(id)
                .query(Book.class)
                .single();
    }

    @ParameterizedTest(name = "Product ID: {0}")
    @MethodSource("getIds")
    void getBookThatExists(Long id) {
        client.get()
                .uri("/books/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id);
    }

    @Test
    void getBookThatDoesNotExist() {
        List<Long> bookIds = getIds();
        assertThat(bookIds).doesNotContain(999L);
        System.out.println("There are " + bookIds.size() + " books in the database.");
        client.get()
                .uri("/books/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllBooks() {
        List<Long> bookIds = getIds();
        System.out.println("There are " + bookIds.size() + " books in the database.");
        client.get()
                .uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Book.class).hasSize(3)
                .consumeWith(System.out::println);
    }

    @Test
    void deleteBook() {
        List<Long> ids = getIds();
        System.out.println("There are " + ids.size() + " books in the database.");
        if (ids.isEmpty()) {
            System.out.println("No ids found");
            return;
        }

        // given:
        client.get()
                .uri("/books/{id}", ids.get(0))
                .exchange()
                .expectStatus().isOk();

        // when:
        client.delete()
                .uri("/books/{id}", ids.get(0))
                .exchange()
                .expectStatus().isNoContent();

        // then:
        client.get()
                .uri("/books/{id}", ids.get(0))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void insertBook() {
        List<Long> bookIds = getIds();
        assertThat(bookIds).doesNotContain(999L);
        System.out.println("There are " + bookIds.size() + " books in the database.");
        Book book = new Book("Book 4", "Author 4", "ISBN 4", LocalDate.of(2023, 4, 1));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String expectedDate = LocalDate.of(2023, 4, 1).format(dateFormatter);
        client.post()
                .uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(book), Book.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.title").isEqualTo("Book 4")
                .jsonPath("$.author").isEqualTo("Author 4")
                .jsonPath("$.isbn").isEqualTo("ISBN 4")
                .jsonPath("$.publicationDate").isEqualTo(expectedDate);
    }
}
