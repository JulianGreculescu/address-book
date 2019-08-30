package au.com.gritmed.addressbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressBookEntryTest {
    @Test
    void shouldCmpareLastNamesFirst() {
        assertTrue(new AddressBookEntry(0, "Fred", "John", "Doe", "0424 320 665" )
                .compareTo(new AddressBookEntry(1, "Tom", "John", "Citizen", "0424 320 668")) > 0);
    }

    @Test
    void shouldCmpareFirstNameWhenTheSameLastName() {
        assertTrue(new AddressBookEntry(0, "Fred", "John", "Doe", "0424 320 665")
                .compareTo(new AddressBookEntry(1, "Tom", "Janet", "Doe", "0424 320 668")) > 0);
    }

    @Test
    void shouldCmpareFirstCaseInsensitive() {
        assertEquals(0, new AddressBookEntry(0, "Fred", "John", "Doe", "0424 320 663")
                .compareTo(new AddressBookEntry(1, "Tom", "JOHN", "Doe", "0424 320 668")));
    }
}
