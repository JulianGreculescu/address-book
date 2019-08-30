package au.com.gritmed.addressbook.controler;

import au.com.gritmed.addressbook.domain.AddressBookEntry;
import au.com.gritmed.addressbook.domain.Contact;
import au.com.gritmed.addressbook.domain.ContactsResponse;
import au.com.gritmed.addressbook.exception.ExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AddressBookControllerTest {
    private static final String ADDRESS_BOOKS_INSERT =
            "INSERT INTO address_books(id, owner, first_name, last_name, phone_no) "
                    + "VALUES (hibernate_sequence.nextval, ?, ?, ?, ?)";

    private static final String BOB =
            "{\n" +
            "    \"owner\": \"Matt\",\n" +
            "    \"firstName\": \"Bob\",\n" +
            "    \"lastName\": \"The Builder\",\n" +
            "    \"phoneNo\": \"0424 320 665\"\n" +
            "}";
    private static final String BOB_NO_OWNER = BOB.replace("Matt", "");
    private static final String BOB_NO_OWNER_NO_PHONE = BOB_NO_OWNER.replace("0424 320 665", "");

    @Value("classpath:fred_contacts.json")
    private Resource _FredContacts;
    @Value("classpath:unique_contacts.json")
    private Resource _UniqueContacts;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcOperations jdbcOperations;
    @Autowired
    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient.mutate()
                .responseTimeout(Duration.ofMillis(1000000L))
                .build();
        createAddressBookEntry("Fred", "Bob", "The Builder", "0424 320 665");
        createAddressBookEntry("Fred", "Mary", "Poppins", "0410 181 523");
        createAddressBookEntry("Fred", "Jane", "Citizen", "0435 478 123");

        createAddressBookEntry("Brad", "Mary", "Poppins", "0410 181 523");
        createAddressBookEntry("Brad", "John", "Doe", "0484 123 456");
        createAddressBookEntry("Brad", "Jane", "Citizen", "0435 478 123");
    }

    @AfterEach
    void tearDown() {
        jdbcOperations.update("DELETE FROM address_books");
    }

    @Test
    void shoulRetrieveAllContactsForAddressBookOwner() throws Exception {
        EntityExchangeResult<String> result = webClient.get()
                .uri("/contacts?addressBookOwner=Fred")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult();

         String body = result.getResponseBody();
         assertNotNull(body);
         ContactsResponse contactsResponse = objectMapper.readValue(body, ContactsResponse.class);
         String indentedJsonResponse = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(contactsResponse);
         assertEquals(StreamUtils.copyToString(_FredContacts.getInputStream(), StandardCharsets.UTF_8),
                            indentedJsonResponse);
    }

    @Test
    void shoulRetrieveUniqueContactsFromAdresssBooks() throws Exception {
        EntityExchangeResult<String> result = webClient.get()
                .uri("/unique?addressBookOwners=Fred,Brad")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult();

         String body = result.getResponseBody();
         assertNotNull(body);
         List<Contact> uniqueContacts = objectMapper.readValue(body, List.class);
         String indentedJsonResponse = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(uniqueContacts);
         assertEquals(StreamUtils.copyToString(_UniqueContacts.getInputStream(), StandardCharsets.UTF_8),
                            indentedJsonResponse);
    }

    @Test
    void shouldCreateAnAddressBookEntry() throws Exception {
        webClient.post()
                .uri("/contacts")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .syncBody(BOB)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Void.class)
                .consumeWith(result -> {
                    long id = jdbcOperations.queryForObject("SELECT hibernate_sequence.currval", Long.class);
                    String expectedLocation = format("http://localhost:%s/contacts/%d", result.getUrl().getPort(), id);
                    assertEquals(expectedLocation, result.getResponseHeaders().get("Location").get(0));
                });
    }

    @Test
    void shouldNotCreateAnAddressBookEntryIfMissingOwner() throws Exception {
        EntityExchangeResult<String> result = webClient.post()
                .uri("/contacts")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .syncBody(BOB_NO_OWNER)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .returnResult();

        String body = result.getResponseBody();
        assertNotNull(body);
        ExceptionResponse response = objectMapper.readValue(body, ExceptionResponse.class);
        assertEquals("Validation Failed", response.getMessage());
        assertEquals("Address book owner cannot be empty", response.getDetails());
    }

    @Test
    void shouldNotCreateAnAddressBookEntryIfMissingOwnerAndPhone() throws Exception {
        EntityExchangeResult<String> result = webClient.post()
                .uri("/contacts")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .syncBody(BOB_NO_OWNER_NO_PHONE)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .returnResult();

        String body = result.getResponseBody();
        assertNotNull(body);
        ExceptionResponse response = objectMapper.readValue(body, ExceptionResponse.class);
        assertEquals("Validation Failed", response.getMessage());
        assertTrue(response.getDetails().contains("Address book owner cannot be empty"));
        assertTrue(response.getDetails().contains("Contact phone number cannot be empty"));
    }

    @Test
    void shouldRetrieveContactById() throws Exception {
        long existingId = jdbcOperations.queryForObject("SELECT hibernate_sequence.currval", Long.class);

        EntityExchangeResult<String> result = webClient.get()
                .uri("/contacts/" + existingId)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult();

        String body = result.getResponseBody();
        assertNotNull(body);
        AddressBookEntry response = objectMapper.readValue(body, AddressBookEntry.class);
        assertEquals(new AddressBookEntry(existingId, "Brad", "Jane", "Citizen", "0435 478 123"), response);
    }

    @Test
    void shouldReturnNotFoundForNonExistingContact() throws Exception {
        EntityExchangeResult<String> result = webClient.get()
                .uri("/contacts/123456789")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody(String.class)
                .returnResult();

        String body = result.getResponseBody();
        assertNotNull(body);
    }

    @Test
    void shouldDeleteAllAddressBookEntries() throws Exception {
        webClient.delete()
                .uri("/contacts")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();
    }

    private void createAddressBookEntry(String owner, String firstName, String lastName, String phoneNo) {
        jdbcOperations.update(ADDRESS_BOOKS_INSERT, owner, firstName, lastName, phoneNo);
    }
}
