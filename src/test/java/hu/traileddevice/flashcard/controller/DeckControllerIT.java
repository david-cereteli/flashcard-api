package hu.traileddevice.flashcard.controller;

import hu.traileddevice.flashcard.dto.deck.DeckCreateInput;
import hu.traileddevice.flashcard.dto.deck.DeckOfUserOutputModel;
import hu.traileddevice.flashcard.dto.deck.DeckOutputModel;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class DeckControllerIT {

    @LocalServerPort
    private int port;

    private String BASE_URL;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private List<UserCreateInput> users;
    private List<DeckCreateInput> decks;

    @BeforeEach
    void setUp() {
        BASE_URL = "http://localhost:" + port + "/deck";

        users = new ArrayList<>();
        users.add(new UserCreateInput("Rupert Terrance", "fake1@gmail.com"));
        users.add(new UserCreateInput("Geraldine Dorris", "fake2@gmail.com"));
        for (UserCreateInput user : users) {
            testRestTemplate.postForObject("http://localhost:" + port + "/user", user, UserOutputModel.class);
        }

        decks = new ArrayList<>();
        decks.add(new DeckCreateInput("Spring"));
        decks.add(new DeckCreateInput("Core Java"));
        decks.add(new DeckCreateInput("Math"));
    }

    @Test
    void findAll_databaseHas3Decks_allRetrieved() {

        // init database
        for (DeckCreateInput deck : decks) {
            testRestTemplate.postForObject(BASE_URL + "?userId=1", deck, DeckOutputModel.class);
        }

        ResponseEntity<CollectionModel<DeckOutputModel>> collectionModelResponseEntity = testRestTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, collectionModelResponseEntity.getStatusCode());
        assertEquals("application/hal+json", collectionModelResponseEntity.getHeaders().getContentType().toString());

        CollectionModel<DeckOutputModel> collectionModel = collectionModelResponseEntity.getBody();

        String receivedCollectionHref = collectionModel.getLinks().toList().get(0).getHref();
        assertEquals(BASE_URL, receivedCollectionHref);

        List<Optional<Link>> deckHrefs = collectionModel
                .getContent().stream()
                .map(deckOutputModel -> deckOutputModel.getLink("self"))
                .collect(Collectors.toList());

        for (int i = 0; i < deckHrefs.size(); i++) {
            assertEquals(BASE_URL + "/" + (i + 1), deckHrefs.get(i).get().toUri().toString());
        }

        List<DeckOutputModel> deckOutputModels = new ArrayList<>(collectionModel.getContent());
        List<String> receivedNames = deckOutputModels.stream()
                .map(DeckOutputModel::getName)
                .sorted()
                .collect(Collectors.toList());
        List<String> expectedNames = decks.stream()
                .map(DeckCreateInput::getName)
                .sorted()
                .collect(Collectors.toList());
        assertEquals(expectedNames, receivedNames);
    }

    @Test
    void save_postForDeck_returns201WithProperLinks() {
        ResponseEntity<DeckOutputModel> deckOutputModelResponseEntity = testRestTemplate
                .postForEntity(BASE_URL + "?userId=1", decks.get(0), DeckOutputModel.class);

        assertEquals(HttpStatus.CREATED, deckOutputModelResponseEntity.getStatusCode());
        assertEquals("application/hal+json", deckOutputModelResponseEntity.getHeaders().getContentType().toString());

        String locationField = BASE_URL + "/" + 1;
        assertEquals(locationField, deckOutputModelResponseEntity.getHeaders().getLocation().toString());

        assertEquals(
                locationField,
                deckOutputModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        assertEquals(
                "http://localhost:" + port + "/card?deckId=1",
                deckOutputModelResponseEntity.getBody().getLink("create-card").get().toUri().toString()
        );

        assertEquals(decks.get(0).getName(), deckOutputModelResponseEntity.getBody().getName());
    }

    @Test
    void save_emptyName_returns400WithDetailedProblem() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .postForEntity(BASE_URL + "?userId=1", new DeckCreateInput(""), Problem.class);

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Invalid field input", problem.getTitle());
        assertEquals("[name: Deck name length should be between 1-60 characters]", problem.getDetail());
    }

    @Test
    void update_existingDeck_returns200AndIsModified() {
        // init database
        testRestTemplate.postForObject(BASE_URL + "?userId=1", decks.get(0), DeckOutputModel.class);

        String deckUrl = BASE_URL + "/" + 1;

        ResponseEntity<DeckOutputModel> deckOutputModelResponseEntity = testRestTemplate
                .exchange(
                        deckUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new DeckCreateInput("updated")),
                        DeckOutputModel.class
                );

        assertEquals(HttpStatus.OK, deckOutputModelResponseEntity.getStatusCode());
        assertEquals(deckUrl, deckOutputModelResponseEntity.getHeaders().getLocation().toString());

        assertEquals(
                deckUrl,
                deckOutputModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        assertEquals(
                "http://localhost:" + port + "/card?deckId=1",
                deckOutputModelResponseEntity.getBody().getLink("create-card").get().toUri().toString()
        );

        assertEquals("updated", deckOutputModelResponseEntity.getBody().getName());
    }

    @Test
    void update_nonexistentDeckId_returns404WithDetail() {

        String deckUrl = BASE_URL + "/" + 1;

        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(
                        deckUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new DeckCreateInput("updated")),
                        Problem.class
                );

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals("No such deck id: 1", problem.getDetail());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
    }

    @Test
    void update_emptyNameField_returns400WithDetail() {

        String deckUrl = BASE_URL + "/" + 1;

        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(
                        deckUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new DeckCreateInput("")),
                        Problem.class
                );

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Invalid field input", problem.getTitle());
        assertEquals("[name: Deck name length should be between 1-60 characters]", problem.getDetail());
        assertEquals(HttpStatus.BAD_REQUEST, problem.getStatus());
    }

    @Test
    void findById_existingDeck_returns200() {
        // init database
        testRestTemplate.postForObject(BASE_URL + "?userId=1", decks.get(0), DeckOutputModel.class);

        ResponseEntity<DeckOutputModel> deckOutputModelResponseEntity = testRestTemplate
                .getForEntity(BASE_URL + "/" + 1, DeckOutputModel.class);

        assertEquals(HttpStatus.OK, deckOutputModelResponseEntity.getStatusCode());

        DeckOutputModel deckOutputModel = deckOutputModelResponseEntity.getBody();

        assertEquals(BASE_URL + "/" + 1, deckOutputModel.getLink("self").get().toUri().toString());
        assertEquals(1L, deckOutputModel.getId());
        assertEquals(decks.get(0).getName(), deckOutputModel.getName());
    }

    @Test
    void findById_nonexistentDeck_returns404WithDetail() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .getForEntity(BASE_URL + "/" + 1, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("No such deck id: 1", problem.getDetail());
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
        assertEquals("[Deck id must be positive]", problem.getDetail());
    }

    @Test
    void deleteById_existingDeck_returns200() {
        // init database
        testRestTemplate.postForObject(BASE_URL + "?userId=1", decks.get(0), DeckOutputModel.class);

        ResponseEntity<Void> voidResponseEntity = testRestTemplate
                .exchange(BASE_URL + "/" + 1, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, voidResponseEntity.getStatusCode());
    }

    @Test
    void deleteById_nonexistentDeck_returns404WithDetail() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(BASE_URL + "/" + 1, HttpMethod.DELETE, null, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("No such deck id: 1", problem.getDetail());
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
        assertEquals("[Deck id must be positive]", problem.getDetail());
    }

    @Test
    void findAllOfUser_2decksBelongToUser_retrieved() {
        // init database - add 3 decks - to 2 separate users
        testRestTemplate.postForObject(BASE_URL + "?userId=1", decks.get(0), DeckOutputModel.class);
        testRestTemplate.postForObject(BASE_URL + "?userId=1", decks.get(1), DeckOutputModel.class);
        testRestTemplate.postForObject(BASE_URL + "?userId=2", decks.get(2), DeckOutputModel.class);

        String urlForDecksOfUser1 = BASE_URL + "/user" + "/" + 1;

        ResponseEntity<CollectionModel<DeckOfUserOutputModel>> collectionModelResponseEntity = testRestTemplate.exchange(
                urlForDecksOfUser1,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, collectionModelResponseEntity.getStatusCode());

        assertEquals(
                urlForDecksOfUser1,
                collectionModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        List<DeckOfUserOutputModel> deckOfUserOutputModels = new ArrayList<>(collectionModelResponseEntity.getBody().getContent());

        for (int i = 0; i < deckOfUserOutputModels.size(); i++) {
            assertEquals(decks.get(i).getName(), deckOfUserOutputModels.get(i).getName());
            assertEquals(
                    BASE_URL + "/" + (i + 1),
                    deckOfUserOutputModels.get(i).getLink("self").get().toUri().toString()
            );
        }
    }

    @Test
    void findAllOfUser_nonexistentUser_returns404() {

        String urlForDecksOfUser1 = BASE_URL + "/user" + "/" + 3;

        ResponseEntity<Problem> problemResponseEntity = testRestTemplate.exchange(
                urlForDecksOfUser1,
                HttpMethod.GET,
                null,
                Problem.class
        );

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals("User does not exist or has no decks.", problem.getDetail());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
    }
}