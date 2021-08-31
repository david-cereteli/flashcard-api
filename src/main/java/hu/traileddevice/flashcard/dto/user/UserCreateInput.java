package hu.traileddevice.flashcard.dto.user;

import hu.traileddevice.flashcard.validation.GmailValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateInput {

    @Schema(description = "Name of the user", example = "John Doe")
    @Size(min = 1, max = 128, message = "Username length should be between 1-128 characters")
    private String name;

    @Schema(description = "A Google mail address", example = "test@gmail.com")
    @GmailValidator
    @NotNull
    private String email;
}
