package hu.traileddevice.flashcard.controller;

import hu.traileddevice.flashcard.dto.user.UserCreateInput;
import hu.traileddevice.flashcard.dto.user.UserOutputModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class UserControllerIT {

    @LocalServerPort
    private int port;

    private String BASE_URL;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private List<UserCreateInput> users;

    @BeforeEach
    void setUp() {
        BASE_URL = "http://localhost:" + port + "/user";

        users = new ArrayList<>();
        users.add(new UserCreateInput("Rupert Terrance", "fake1@gmail.com"));
        users.add(new UserCreateInput("Geraldine Dorris", "fake2@gmail.com"));
        users.add(new UserCreateInput("Lyndon Anderson", "fake3@gmail.com"));
    }

    @Test
    void findAll_databaseHas3Users_allRetrieved() {

        // init database
        for (UserCreateInput user : users) {
            testRestTemplate.postForObject(BASE_URL, user, UserOutputModel.class);
        }

        ResponseEntity<CollectionModel<UserOutputModel>> userOutputModelsResponseEntity = testRestTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, userOutputModelsResponseEntity.getStatusCode());
        assertEquals("application/hal+json", userOutputModelsResponseEntity.getHeaders().getContentType().toString());

        CollectionModel<UserOutputModel> collectionModel = userOutputModelsResponseEntity.getBody();

        String receivedCollectionHref = collectionModel.getLinks().toList().get(0).getHref();
        assertEquals(BASE_URL, receivedCollectionHref);

        List<Optional<Link>> userHrefs = collectionModel
                .getContent().stream()
                .map(userOutputModel -> userOutputModel.getLink("self"))
                .collect(Collectors.toList());

        for (int i = 0; i < userHrefs.size(); i++) {
            assertEquals(BASE_URL + "/" + (i + 1), userHrefs.get(i).get().toUri().toString());
        }

        Collection<UserOutputModel> content = userOutputModelsResponseEntity.getBody().getContent();
        List<String> receivedNames = content.stream()
                .map(UserOutputModel::getName)
                .sorted()
                .collect(Collectors.toList());
        List<String> expectedNames = users.stream()
                .map(UserCreateInput::getName)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(expectedNames, receivedNames);
    }

    @Test
    void save_postForUser_returns201WithProperLinks() {
        ResponseEntity<UserOutputModel> userOutputModelResponseEntity = testRestTemplate
                .postForEntity(BASE_URL, users.get(0), UserOutputModel.class);

        assertEquals(HttpStatus.CREATED, userOutputModelResponseEntity.getStatusCode());
        assertEquals("application/hal+json", userOutputModelResponseEntity.getHeaders().getContentType().toString());

        String locationField = BASE_URL + "/" + 1;
        assertEquals(locationField, userOutputModelResponseEntity.getHeaders().getLocation().toString());

        assertEquals(
                locationField,
                userOutputModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        assertEquals(
                "http://localhost:" + port + "/deck?userId=1",
                userOutputModelResponseEntity.getBody().getLink("create-deck").get().toUri().toString()
        );

        assertEquals(users.get(0).getName(), userOutputModelResponseEntity.getBody().getName());
    }

    @Test
    void save_emptyName_returns400WithDetailedProblem() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .postForEntity(BASE_URL, new UserCreateInput("", "fakenew@gmail.com"), Problem.class);

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Invalid field input", problem.getTitle());
        assertEquals("[name: Username length should be between 1-128 characters]", problem.getDetail());
    }

    @Test
    void save_nonGmailEmail_returns400WithDetailedProblem() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .postForEntity(BASE_URL, new UserCreateInput("aName", "fake@notgmail.com"), Problem.class);

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Invalid field input", problem.getTitle());
        assertEquals("[email: Please provide a valid Google mail address]", problem.getDetail());
    }

    @Test
    void update_existingUser_returns200AndIsModified() {
        // init database
        testRestTemplate.postForObject(BASE_URL, users.get(0), UserOutputModel.class);

        String userUrl = BASE_URL + "/" + 1;

        ResponseEntity<UserOutputModel> userOutputModelResponseEntity = testRestTemplate
                .exchange(
                        userUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new UserCreateInput("updated", "fakeupdated@gmail.com")),
                        UserOutputModel.class
                );

        assertEquals(HttpStatus.OK, userOutputModelResponseEntity.getStatusCode());
        assertEquals(userUrl, userOutputModelResponseEntity.getHeaders().getLocation().toString());

        assertEquals(
                userUrl,
                userOutputModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        assertEquals(
                "http://localhost:" + port + "/deck?userId=1",
                userOutputModelResponseEntity.getBody().getLink("create-deck").get().toUri().toString()
        );

        assertEquals("updated", userOutputModelResponseEntity.getBody().getName());
    }

    @Test
    void update_nonexistentUserId_returns404WithDetail() {

        String userUrl = BASE_URL + "/" + 1;

        ResponseEntity<Problem> userOutputModelResponseEntity = testRestTemplate
                .exchange(
                        userUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new UserCreateInput("updated", "fakeupdated@gmail.com")),
                        Problem.class
                );

        assertEquals(HttpStatus.NOT_FOUND, userOutputModelResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, userOutputModelResponseEntity.getHeaders().getContentType());

        Problem problem = userOutputModelResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals("No such user id: 1", problem.getDetail());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
    }

    @Test
    void update_emptyNameField_returns400WithDetail() {

        String userUrl = BASE_URL + "/" + 1;

        ResponseEntity<Problem> userOutputModelResponseEntity = testRestTemplate
                .exchange(
                        userUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new UserCreateInput("", "fakeupdated@gmail.com")),
                        Problem.class
                );

        assertEquals(HttpStatus.BAD_REQUEST, userOutputModelResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, userOutputModelResponseEntity.getHeaders().getContentType());

        Problem problem = userOutputModelResponseEntity.getBody();

        assertEquals("Invalid field input", problem.getTitle());
        assertEquals("[name: Username length should be between 1-128 characters]", problem.getDetail());
        assertEquals(HttpStatus.BAD_REQUEST, problem.getStatus());
    }

    @Test
    void findById_existingUser_returns200() {
        // init database
        testRestTemplate.postForObject(BASE_URL, users.get(0), UserOutputModel.class);

        ResponseEntity<UserOutputModel> userOutputModelResponseEntity = testRestTemplate
                .getForEntity(BASE_URL + "/" + 1, UserOutputModel.class);

        assertEquals(HttpStatus.OK, userOutputModelResponseEntity.getStatusCode());

        UserOutputModel userOutputModel = userOutputModelResponseEntity.getBody();

        assertEquals(BASE_URL + "/" + 1, userOutputModel.getLink("self").get().toUri().toString());
        assertEquals(1L, userOutputModel.getId());
        assertEquals(users.get(0).getName(), userOutputModel.getName());
    }

    @Test
    void findById_nonexistentUser_returns404WithDetail() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .getForEntity(BASE_URL + "/" + 1, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("No such user id: 1", problem.getDetail());
    }

    @Test
    void findById_invalidId_returns400WithDetail() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .getForEntity(BASE_URL + "/" + 0, Problem.class);

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Input constraint violation", problem.getTitle());
        assertEquals(HttpStatus.BAD_REQUEST, problem.getStatus());
        assertEquals("[User id must be positive]", problem.getDetail());
    }

    @Test
    void deleteById_existingUser_returns200() {
        // init database
        testRestTemplate.postForObject(BASE_URL, users.get(0), UserOutputModel.class);

        ResponseEntity<Void> voidResponseEntity = testRestTemplate
                .exchange(BASE_URL + "/" + 1, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, voidResponseEntity.getStatusCode());
    }

    @Test
    void deleteById_nonexistentUser_returns404WithDetail() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(BASE_URL + "/" + 1, HttpMethod.DELETE, null, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("No such user id: 1", problem.getDetail());
    }

    @Test
    void deleteById_invalidId_returns400WithDetail() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(BASE_URL + "/" + 0, HttpMethod.DELETE, null, Problem.class);

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Input constraint violation", problem.getTitle());
        assertEquals(HttpStatus.BAD_REQUEST, problem.getStatus());
        assertEquals("[User id must be positive]", problem.getDetail());
    }
}