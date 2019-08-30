package au.com.gritmed.addressbook.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ContactsResponse {
    private final String addressBookOwner;
    private final List<Contact> contacts;
}
