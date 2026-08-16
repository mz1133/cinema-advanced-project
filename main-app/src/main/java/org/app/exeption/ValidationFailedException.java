package org.app.exeption;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
@Getter
@AllArgsConstructor
public class ValidationFailedException extends RuntimeException {

    private final Map<String,String> errorsMessages;



}
