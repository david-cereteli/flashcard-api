package hu.traileddevice.flashcard.exception;

import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(QueriedDataDoesNotExistException.class)
    public ResponseEntity<Problem> handleQueriedDataDoesNotExistException(Exception e) {

        Problem problem = Problem.create()
                .withType(URI.create("no-result"))
                .withTitle("Queried data does not exist")
                .withDetail(e.getMessage())
                .withStatus(HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler({DuplicateFrontContentException.class, DuplicateEmailException.class})
    public ResponseEntity<Problem> handleDuplicateFrontContentException(Exception e) {

        Problem problem = Problem.create()
                .withType(URI.create("constraint-violation"))
                .withTitle("Input constraint violation")
                .withDetail(e.getMessage())
                .withStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class) // repository structure violation
    public ResponseEntity<Problem> handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            //errors.add(violation.getRootBeanClass().getSimpleName() + "'s " +
            //        violation.getPropertyPath() + ": " + violation.getMessage());
            errors.add(violation.getMessage());
        }

        Problem problem = Problem.create()
                .withType(URI.create("constraint-violation"))
                .withTitle("Input constraint violation")
                .withDetail(errors.toString())
                .withStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // field errors on object passed in with @Valid
    public ResponseEntity<Problem> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        List<FieldErrorOutput> fieldErrors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> new FieldErrorOutput(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        Problem problem = Problem.create()
                .withType(URI.create("invalid-field-input"))
                .withTitle("Invalid field input")
                .withDetail(fieldErrors.toString())
                .withStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class) // controller parameter type mismatch
    public ResponseEntity<Problem> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {

        Problem problem = Problem.create()
                .withType(URI.create("invalid-input"))
                .withTitle("Invalid input")
                .withDetail(e.getName() + " should be of type " + e.getRequiredType().getSimpleName())
                .withStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class) // controller parameter missing
    public ResponseEntity<Problem> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {

        Problem problem = Problem.create()
                .withType(URI.create("missing-parameter"))
                .withTitle("Missing input")
                .withDetail(String.format("Parameter '%s' is missing.", e.getParameterName()))
                .withStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class) // invalid JSON
    public ResponseEntity<Problem> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {

        Problem problem = Problem.create()
                .withType(URI.create("invalid-json"))
                .withTitle("Input JSON is not valid")
                .withDetail(e.getRootCause().getMessage())
                .withStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class) // invalid HttpMethod on URI
    public ResponseEntity<Problem> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        StringJoiner errorMessage =
                new StringJoiner(", ",
                        e.getMethod() + " method is not supported for this request. Supported methods are: ",
                        ".");
        e.getSupportedHttpMethods().forEach(httpMethod -> errorMessage.add(httpMethod.toString()));

        Problem problem = Problem.create()
                .withType(URI.create("method-not-allowed"))
                .withTitle("Method Not Allowed")
                .withDetail(errorMessage.toString())
                .withStatus(HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

}
