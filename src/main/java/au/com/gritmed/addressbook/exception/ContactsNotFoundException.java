package au.com.gritmed.addressbook.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ContactsNotFoundException extends RuntimeException {
    public ContactsNotFoundException(String message) {
        super(message);
    }
}
