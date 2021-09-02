package hu.traileddevice.flashcard.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldErrorOutput {

    @Schema(description = "Field where the error occurred", example = "name")
    private String field;

    @Schema(description = "Details of the error", example = "Invalid input")
    private String errorMessage;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(field);
        sb.append(": ");
        sb.append(errorMessage);
        return sb.toString();
    }
}
