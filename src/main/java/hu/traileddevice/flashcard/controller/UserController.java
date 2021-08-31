package hu.traileddevice.flashcard.controller;

import hu.traileddevice.flashcard.dto.user.UserCreateInput;
import hu.traileddevice.flashcard.dto.user.UserOutputModel;
import hu.traileddevice.flashcard.dto.user.UserUpdateInput;
import hu.traileddevice.flashcard.hateoas.user.UserOutputModelAssembler;
import hu.traileddevice.flashcard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/user")
@Tag(name = "User CRUD", description = "Create, read, update, and delete users.")
@Validated
public class UserController {

    private final UserService userService;
    private final UserOutputModelAssembler userOutputModelAssembler;

    public UserController(UserService userService, UserOutputModelAssembler userOutputModelAssembler) {
        this.userService = userService;
        this.userOutputModelAssembler = userOutputModelAssembler;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = UserOutputModel.class)))
            })
    public CollectionModel<UserOutputModel> findAll() {
        return userOutputModelAssembler.toCollectionModel(userService.findAll());
    }

    @PostMapping
    @Operation(summary = "Create a user", description = "Add a new user to the database",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = UserOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<UserOutputModel> save(@Valid @RequestBody UserCreateInput userCreateInput) {
        UserOutputModel userOutputModel = userOutputModelAssembler.toModel(userService.save(userCreateInput));
        return ResponseEntity
                .created(userOutputModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(userOutputModel);
    }

    @PutMapping(value = "/{id}")
    @Operation(summary = "Update user by id", description = "Update user by id in the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = UserOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<UserOutputModel> update(
            @Parameter(description = "Id of the user", example = "1")
            @Min(value = 1, message = "User id must be positive")
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateInput userUpdateInput
    ) {
        UserOutputModel userOutputModel = userOutputModelAssembler.toModel(userService.update(id, userUpdateInput));
        return ResponseEntity
                .ok()
                .header("location",
                        userOutputModel.getRequiredLink(IanaLinkRelations.SELF).toUri().toString())
                .body(userOutputModel);
    }

    @GetMapping(value = "/{id}")
    @Operation(summary = "Find user by id", description = "Retrieve user by id from the database",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = UserOutputModel.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid user id",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public UserOutputModel findById(
            @Parameter(description = "Id of the user", example = "1")
            @Min(value = 1, message = "User id must be positive")
            @PathVariable Long id
    ) {
        return userOutputModelAssembler.toModel(userService.findById(id));
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete user by id", description = "Remove user by id from the database",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User removed"),
                    @ApiResponse(responseCode = "400", description = "Invalid user id",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "404", description = "User not found",
                            content = @Content(mediaType = "application/problem+json"))
            })
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Id of the user", example = "1")
            @Min(value = 1, message = "User id must be positive")
            @PathVariable Long id
    ) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
