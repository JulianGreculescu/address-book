package au.com.gritmed.addressbook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;

import static java.lang.String.format;

@Entity(name = "addressBooks")
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"owner", "firstName", "lastName", "phoneNo"})})
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class AddressBookEntry implements Comparable<AddressBookEntry> {
    private static final String NAME_TEMPLATE = "%s %s";

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty(message = "Address book owner cannot be empty")
    private String owner;
    @NotEmpty(message = "Contact first name cannot be empty")
    private String firstName;
    @NotEmpty(message = "Contact last name cannot be empty")
    private String lastName;
    @NotEmpty(message = "Contact phone number cannot be empty")
    private String phoneNo;

    public AddressBookEntry() {
    }

    @JsonIgnore
    public Contact getContact() {
        return new Contact(format(NAME_TEMPLATE, lastName.toUpperCase(), firstName), phoneNo);
    }

    @Override
    public int compareTo(AddressBookEntry other) {
        int result = lastName.compareToIgnoreCase(other.lastName);

        return result == 0 ? firstName.compareToIgnoreCase(other.firstName) : result;
    }
}
