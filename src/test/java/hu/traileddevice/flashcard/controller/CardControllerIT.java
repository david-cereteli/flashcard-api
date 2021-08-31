package hu.traileddevice.flashcard.controller;

import hu.traileddevice.flashcard.dto.card.CardCreateInput;
import hu.traileddevice.flashcard.dto.card.CardOfDeckOutputModel;
import hu.traileddevice.flashcard.dto.card.CardOutputModel;
import hu.traileddevice.flashcard.dto.deck.DeckCreateInput;
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
class CardControllerIT {

    @LocalServerPort
    private int port;

    private String BASE_URL;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private List<UserCreateInput> users;
    private List<DeckCreateInput> decks;
    private List<CardCreateInput> cards;

    @BeforeEach
    void setUp() {
        BASE_URL = "http://localhost:" + port + "/card";

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
        for (DeckCreateInput deck : decks) {
            testRestTemplate.postForObject("http://localhost:" + port + "/deck?userId=1", deck, DeckOutputModel.class);
        }

        cards = new ArrayList<>();
        cards.add(new CardCreateInput("What is Spring Framework?", "Spring is a powerful open source, loosely coupled, light weight, java based application framework meant for reducing the complexity of developing enterprise level applications."));
        cards.add(new CardCreateInput("Is HATEOAS required in REST?", "Well, it turns out it is."));
        cards.add(new CardCreateInput("Singleton?", "The scope of bean definition while using this would be single instance per IoC container."));
    }

    @Test
    void findAll_databaseHas3Cards_allRetrieved() {

        // init database
        for (CardCreateInput card : cards) {
            testRestTemplate.postForObject(BASE_URL + "?deckId=1", card, CardOutputModel.class);
        }

        ResponseEntity<CollectionModel<CardOutputModel>> collectionModelResponseEntity = testRestTemplate.exchange(
                BASE_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(HttpStatus.OK, collectionModelResponseEntity.getStatusCode());
        assertEquals("application/hal+json", collectionModelResponseEntity.getHeaders().getContentType().toString());

        CollectionModel<CardOutputModel> collectionModel = collectionModelResponseEntity.getBody();

        String receivedCollectionHref = collectionModel.getLinks().toList().get(0).getHref();
        assertEquals(BASE_URL, receivedCollectionHref);

        List<Optional<Link>> cardHrefs = collectionModel
                .getContent().stream()
                .map(cardOutputModel -> cardOutputModel.getLink("self"))
                .collect(Collectors.toList());

        for (int i = 0; i < cardHrefs.size(); i++) {
            assertEquals(BASE_URL + "/" + (i + 1), cardHrefs.get(i).get().toUri().toString());
        }

        List<CardOutputModel> cardOutputModels = new ArrayList<>(collectionModel.getContent());
        List<String> receivedNames = cardOutputModels.stream()
                .map(CardOutputModel::getFrontContent)
                .collect(Collectors.toList());
        List<String> expectedNames = cards.stream()
                .map(CardCreateInput::getFrontContent)
                .collect(Collectors.toList());
        assertEquals(expectedNames, receivedNames);
    }

    @Test
    void save_postForCard_returns201WithProperLinks() {
        ResponseEntity<CardOutputModel> cardOutputModelResponseEntity = testRestTemplate
                .postForEntity(BASE_URL + "?deckId=1", cards.get(0), CardOutputModel.class);

        assertEquals(HttpStatus.CREATED, cardOutputModelResponseEntity.getStatusCode());
        assertEquals("application/hal+json", cardOutputModelResponseEntity.getHeaders().getContentType().toString());

        String locationField = BASE_URL + "/" + 1;
        assertEquals(locationField, cardOutputModelResponseEntity.getHeaders().getLocation().toString());

        assertEquals(
                locationField,
                cardOutputModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        assertEquals(cards.get(0).getFrontContent(), cardOutputModelResponseEntity.getBody().getFrontContent());
    }

    @Test
    void save_nullFrontContent_returns400WithDetailedProblem() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .postForEntity(BASE_URL + "?deckId=1", new CardCreateInput(null, "backContent"), Problem.class);

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Invalid field input", problem.getTitle());
        assertEquals("[frontContent: Front content must not be null.]", problem.getDetail());
    }

    @Test
    void update_existingCard_returns200AndIsModified() {
        // init database
        testRestTemplate.postForObject(BASE_URL + "?deckId=1", cards.get(0), CardOutputModel.class);

        String cardUrl = BASE_URL + "/" + 1;

        ResponseEntity<CardOutputModel> cardOutputModelResponseEntity = testRestTemplate
                .exchange(
                        cardUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new CardCreateInput("updatedFront", "updatedBack")),
                        CardOutputModel.class
                );

        assertEquals(HttpStatus.OK, cardOutputModelResponseEntity.getStatusCode());
        assertEquals(cardUrl, cardOutputModelResponseEntity.getHeaders().getLocation().toString());

        assertEquals(
                cardUrl,
                cardOutputModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        assertEquals("updatedFront", cardOutputModelResponseEntity.getBody().getFrontContent());
    }

    @Test
    void update_nonexistentCardId_returns404WithDetail() {

        String cardUrl = BASE_URL + "/" + 4;

        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(
                        cardUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new CardCreateInput("updatedFront", "updatedBack")),
                        Problem.class
                );

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals("No such card id: 4", problem.getDetail());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
    }

    @Test
    void update_emptyNameField_returns400WithDetail() {

        String cardUrl = BASE_URL + "/" + 1;

        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(
                        cardUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new CardCreateInput("", "updatedBackContent")),
                        Problem.class
                );

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Invalid field input", problem.getTitle());
        assertEquals("[frontContent: Length of text on the front of the card should be between 1-750 characters]", problem.getDetail());
        assertEquals(HttpStatus.BAD_REQUEST, problem.getStatus());
    }

    @Test
    void findById_existingCard_returns200() {
        // init database
        testRestTemplate.postForObject(BASE_URL + "?deckId=1", cards.get(0), CardOutputModel.class);

        ResponseEntity<CardOutputModel> cardOutputModelResponseEntity = testRestTemplate
                .getForEntity(BASE_URL + "/" + 1, CardOutputModel.class);

        assertEquals(HttpStatus.OK, cardOutputModelResponseEntity.getStatusCode());

        CardOutputModel cardOutputModel = cardOutputModelResponseEntity.getBody();

        assertEquals(BASE_URL + "/" + 1, cardOutputModel.getLink("self").get().toUri().toString());
        assertEquals(1L, cardOutputModel.getId());
        assertEquals(cards.get(0).getFrontContent(), cardOutputModel.getFrontContent());
    }

    @Test
    void findById_nonexistentCard_returns404WithDetail() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .getForEntity(BASE_URL + "/" + 1, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("No such card id: 1", problem.getDetail());
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
        assertEquals("[Card id must be positive]", problem.getDetail());
    }

    @Test
    void deleteById_existingCard_returns200() {
        // init database
        testRestTemplate.postForObject(BASE_URL + "?deckId=1", cards.get(0), CardOutputModel.class);

        ResponseEntity<Void> voidResponseEntity = testRestTemplate
                .exchange(BASE_URL + "/" + 1, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, voidResponseEntity.getStatusCode());
    }

    @Test
    void deleteById_nonexistentCard_returns404WithDetail() {
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(BASE_URL + "/" + 1, HttpMethod.DELETE, null, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("No such card id: 1", problem.getDetail());
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
        assertEquals("[Card id must be positive]", problem.getDetail());
    }

    @Test
    void findAllOfDeck_2cardsBelongToDeck_retrieved() {
        // init database - add 3 cards - to 2 separate decks
        testRestTemplate.postForObject(BASE_URL + "?deckId=1", cards.get(0), CardOutputModel.class);
        testRestTemplate.postForObject(BASE_URL + "?deckId=1", cards.get(1), CardOutputModel.class);
        testRestTemplate.postForObject(BASE_URL + "?deckId=2", cards.get(2), CardOutputModel.class);

        String urlForCardsOfDeck = BASE_URL + "/deck" + "/" + 1;

        ResponseEntity<CollectionModel<CardOfDeckOutputModel>> collectionModelResponseEntity = testRestTemplate.exchange(
                urlForCardsOfDeck,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, collectionModelResponseEntity.getStatusCode());

        assertEquals(
                urlForCardsOfDeck,
                collectionModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        List<CardOfDeckOutputModel> cardOfDeckOutputModels = new ArrayList<>(collectionModelResponseEntity.getBody().getContent());

        for (int i = 0; i < cardOfDeckOutputModels.size(); i++) {
            assertEquals(cards.get(i).getFrontContent(), cardOfDeckOutputModels.get(i).getFrontContent());
            assertEquals(
                    BASE_URL + "/" + (i + 1),
                    cardOfDeckOutputModels.get(i).getLink("self").get().toUri().toString()
            );
        }
    }

    @Test
    void findAllOfDeck_nonexistentDeck_returns404() {

        String urlForCardsOfDeck = BASE_URL + "/deck" + "/" + 4;

        ResponseEntity<Problem> collectionModelResponseEntity = testRestTemplate.exchange(
                urlForCardsOfDeck,
                HttpMethod.GET,
                null,
                Problem.class
        );

        assertEquals(HttpStatus.NOT_FOUND, collectionModelResponseEntity.getStatusCode());

        Problem problem = collectionModelResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals("Deck does not exist or is empty.", problem.getDetail());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
    }
}