package au.com.gritmed.addressbook.service;

import au.com.gritmed.addressbook.dao.AddressBookRepository;
import au.com.gritmed.addressbook.domain.AddressBookEntry;
import au.com.gritmed.addressbook.domain.Contact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressBookServiceTest {
    private static final String ALAN = "Alan";
    private static final List<AddressBookEntry> FRIENDS = new ArrayList<>();
    static {
        FRIENDS.add(new AddressBookEntry(1, ALAN, "Fred", "Flintstone", "0410 320 281"));
        FRIENDS.add(new AddressBookEntry(2, ALAN, "John", "Doe", "0424 320 664"));
        FRIENDS.add(new AddressBookEntry(3, ALAN, "Janet", "Citizen", "0445 128 399"));
    }

    @Mock
    private AddressBookRepository addressBookRepository;

    @InjectMocks
    private AddressBookService service;

    @Test
    void shouldSortAddressBookEntries() {
        when(addressBookRepository.findByOwner(ALAN)).thenReturn(FRIENDS);

        List<Contact> contacts = service.getContacts(ALAN);

        assertEquals(3, contacts.size());
        assertEquals(new Contact("CITIZEN Janet", "0445 128 399"), contacts.get(0));
        assertEquals(new Contact("DOE John", "0424 320 664"), contacts.get(1));
        assertEquals(new Contact("FLINTSTONE Fred", "0410 320 281"), contacts.get(2));
    }

    @Test
    void shouldBeAbleToCreateUniqueContactsMap() {
        when(addressBookRepository.findUniqueForOwners(new String[] {"Fred", "Brad"}))
                .thenReturn(new ArrayList<AddressBookEntry>() {{
                    add(new AddressBookEntry(1, "Fred", "Bob", "The Builder", "0424 320 665"));
                    add(new AddressBookEntry(5,"Brad", "John", "Doe", "0484 123 456"));
                    add(new AddressBookEntry(6,"Brad", "Janet", "Citizen", "0432 456 789"));
                }});

        Map<String, List<Contact>> uniqueContacts = service.getUniqueContacts(new String[] {"Fred", "Brad"});

        assertEquals(2, uniqueContacts.size());
        assertEquals(1, uniqueContacts.getOrDefault("Fred", new ArrayList<>()).size());
        assertEquals(new Contact("THE BUILDER Bob", "0424 320 665"), uniqueContacts.get("Fred").get(0));
        assertEquals(2, uniqueContacts.getOrDefault("Brad", new ArrayList<>()).size());
        assertTrue(uniqueContacts.get("Brad").contains(new Contact("DOE John", "0484 123 456")));
        assertTrue(uniqueContacts.get("Brad").contains(new Contact("CITIZEN Janet", "0432 456 789")));
    }

    @Test
    void shouldDelegateSavingAnAddressBookEntryToTheRepository() {
        when(addressBookRepository.save(new AddressBookEntry(0, "Fred", "Bob", "The Builder", "0424 320 665")))
                .thenReturn(new AddressBookEntry(1, "Fred", "Bob", "The Builder", "0424 320 665"));
        assertEquals(new AddressBookEntry(1, "Fred", "Bob", "The Builder", "0424 320 665"),
                service.save(new AddressBookEntry(0, "Fred", "Bob", "The Builder", "0424 320 665")));
    }

    @Test
    void shouldDelegateFetchingAnAddressBookEntryToTheRepository() {
        when(addressBookRepository.findById(123L)).thenReturn(Optional.of(
                new AddressBookEntry(1, "Fred", "Bob", "The Builder", "0424 320 665")));
        assertEquals(Optional.of(new AddressBookEntry(1, "Fred", "Bob", "The Builder", "0424 320 665")),
                service.getAddressBookEntry(123));
    }

    @Test
    void shouldDelegateDeletingAllAddressBookEntriesToTheRepository() {
        service.deleteAllContacts();
        verify(addressBookRepository).deleteAll();
    }
}
