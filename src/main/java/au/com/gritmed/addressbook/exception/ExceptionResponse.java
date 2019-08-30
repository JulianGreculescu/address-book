package au.com.gritmed.addressbook.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class ExceptionResponse {
    private final Date timestamp;
    private final String message;
    private final String details;
}
