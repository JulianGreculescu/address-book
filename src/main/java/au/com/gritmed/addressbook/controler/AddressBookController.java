package au.com.gritmed.addressbook.controler;

import au.com.gritmed.addressbook.domain.AddressBookEntry;
import au.com.gritmed.addressbook.domain.Contact;
import au.com.gritmed.addressbook.domain.ContactsResponse;
import au.com.gritmed.addressbook.exception.ContactsNotFoundException;
import au.com.gritmed.addressbook.service.AddressBookService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@RestController
@AllArgsConstructor
public class AddressBookController {
    private final AddressBookService addressBookService;


    @PostMapping("/contacts")
    public ResponseEntity<AddressBookEntry> createContact(@Valid @RequestBody AddressBookEntry addressBookEntry) {
        AddressBookEntry savedEntry = addressBookService.save(addressBookEntry);

        // CREATED
        // /contacts/{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedEntry.getId()).toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/contacts/{id}")
    @ResponseBody
    public AddressBookEntry retrieveContact(@PathVariable long id) {
        Optional<AddressBookEntry> addressBookEntry = addressBookService.getAddressBookEntry(id);
        if (!addressBookEntry.isPresent()) {
            throw new ContactsNotFoundException("There is no address book entry with id = " + id);
        }
        return addressBookEntry.get();
    }

    @DeleteMapping("/contacts")
    public ResponseEntity<String> deleteUser() {
        addressBookService.deleteAllContacts();
        return ResponseEntity.ok("All contacts deleted");
    }

    @GetMapping("/contacts")
    @ResponseBody
    public ContactsResponse retrieveAddressBookContacts(@RequestParam(name = "addressBookOwner") String addressBookOwner) {
        List<Contact> contacts = addressBookService.getContacts(addressBookOwner);
        if (contacts.isEmpty()) {
            throw new ContactsNotFoundException(addressBookOwner + " has no contacts");
        }
        return new ContactsResponse(addressBookOwner, contacts);
    }

    @GetMapping("/unique")
    @ResponseBody
    public List<ContactsResponse> retrieveUniqueContacts(@RequestParam(name = "addressBookOwners") String[] addressBookOwners) {
        Map<String, List<Contact>> uniqueContacts = addressBookService.getUniqueContacts(addressBookOwners);
        if (uniqueContacts.isEmpty()) {
            throw new ContactsNotFoundException("No unique contacts found for following addess books: "
                    + Arrays.stream(addressBookOwners).collect(joining(", ")));
        }
        return uniqueContacts.entrySet().stream().map(entry -> new ContactsResponse(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }
}
