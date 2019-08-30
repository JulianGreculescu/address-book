package au.com.gritmed.addressbook.dao;

import au.com.gritmed.addressbook.domain.AddressBookEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class AddressBookRepositoryTest {
    private static final String ADDRESS_BOOKS_INSERT =
            "INSERT INTO address_books(id, owner, first_name, last_name, phone_no) "
          + "VALUES (?, ?, ?, ?, ?)";

    @Autowired
    private AddressBookRepository addressBookRepository;
    @Autowired
    private JdbcOperations jdbcOperations;

    @BeforeEach
    void setUp() {
        createAddressBookEntry(1,"Fred", "Bob", "The Builder", "0424 320 665");
        createAddressBookEntry(2,"Fred", "Mary", "Poppins", "0410 181 523");
        createAddressBookEntry(3,"Fred", "Jane", "Citizen", "0435 478 123");

        createAddressBookEntry(4,"Brad", "Mary", "Poppins", "0410 181 523");
        createAddressBookEntry(5,"Brad", "John", "Doe", "0484 123 456");
        createAddressBookEntry(6,"Brad", "Jane", "Citizen", "0435 478 123");
    }

    @Test
    void shouldBeAbleToRetrieveOwnerEntries() {
        assertTrue(addressBookRepository.findByOwner("Alan").isEmpty());
        List<AddressBookEntry> entries = addressBookRepository.findByOwner("Fred");
        assertEquals(3, entries.size());
        List<String> expectedFirstNames = new ArrayList<String>() {{
            add("Bob");
            add("Mary");
            add("Jane");
        }};
        entries.forEach(entry -> {
            assertEquals("Fred", entry.getOwner());
            assertTrue(expectedFirstNames.remove(entry.getFirstName()));
        });
    }

    @Test
    void shouldBeAbleToRetrieveUniqueAddressBookEntries() {
        List<AddressBookEntry> entries = addressBookRepository.findUniqueForOwners(new String[] {"Fred", "Brad"});
        assertEquals(2, entries.size());
        assertTrue(entries.contains(new AddressBookEntry(1, "Fred", "Bob", "The Builder", "0424 320 665")));
        assertTrue(entries.contains(new AddressBookEntry(5,"Brad", "John", "Doe", "0484 123 456")));
    }

    private void createAddressBookEntry(long id, String owner, String firstName, String lastName, String phoneNo) {
        jdbcOperations.update(ADDRESS_BOOKS_INSERT, id, owner, firstName, lastName, phoneNo);
    }
}