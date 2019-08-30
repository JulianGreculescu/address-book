package au.com.gritmed.addressbook.service;

import au.com.gritmed.addressbook.dao.AddressBookRepository;
import au.com.gritmed.addressbook.domain.AddressBookEntry;
import au.com.gritmed.addressbook.domain.Contact;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class AddressBookService {
    private final AddressBookRepository addressBookRepository;

    public List<Contact> getContacts(String addressBookOwner) {
        return addressBookRepository.findByOwner(addressBookOwner).stream()
                .sorted()
                .map(AddressBookEntry::getContact)
                .collect(Collectors.toList());
    }

    public Map<String, List<Contact>> getUniqueContacts(String[] addressBookOwners) {
        return addressBookRepository.findUniqueForOwners(addressBookOwners).stream()
                .collect(groupingBy(AddressBookEntry::getOwner,
                        mapping(AddressBookEntry::getContact, toList())));
    }

    public AddressBookEntry save(AddressBookEntry addressBookEntry) {
        return addressBookRepository.save(addressBookEntry);
    }

    public Optional<AddressBookEntry> getAddressBookEntry(long id) {
        return addressBookRepository.findById(id);
    }

    public void deleteAllContacts() {
        addressBookRepository.deleteAll();
    }
}
