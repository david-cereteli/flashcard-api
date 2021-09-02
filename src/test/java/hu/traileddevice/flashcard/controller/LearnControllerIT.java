package hu.traileddevice.flashcard.controller;

import hu.traileddevice.flashcard.dto.card.CardCreateInput;
import hu.traileddevice.flashcard.dto.card.CardOutputModel;
import hu.traileddevice.flashcard.dto.deck.DeckCreateInput;
import hu.traileddevice.flashcard.dto.deck.DeckOutputModel;
import hu.traileddevice.flashcard.dto.learn.AnswerOutputModel;
import hu.traileddevice.flashcard.dto.learn.QuestionOutputModel;
import hu.traileddevice.flashcard.dto.user.UserCreateInput;
import hu.traileddevice.flashcard.dto.user.UserOutputModel;
import hu.traileddevice.flashcard.model.CardTiming;
import hu.traileddevice.flashcard.repository.CardTimingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // @SpyBean breaks other test classes with BEFORE
@ActiveProfiles("test")
class LearnControllerIT {

    @LocalServerPort
    private int port;

    private String BASE_URL;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @SpyBean
    private CardTimingRepository cardTimingRepository;

    private List<UserCreateInput> users;
    private List<DeckCreateInput> decks;
    private List<CardCreateInput> cards;

    @BeforeEach
    void setUp() {
        BASE_URL = "http://localhost:" + port + "/learn";

        users = new ArrayList<>();
        users.add(new UserCreateInput("Rupert Terrance", "fake1@gmail.com"));
        testRestTemplate.postForObject("http://localhost:" + port + "/user", users.get(0), UserOutputModel.class);

        decks = new ArrayList<>();
        decks.add(new DeckCreateInput("Spring"));
        testRestTemplate.postForObject("http://localhost:" + port + "/deck?userId=1", decks.get(0), DeckOutputModel.class);
        decks.add(new DeckCreateInput("Core Java"));
        testRestTemplate.postForObject("http://localhost:" + port + "/deck?userId=1", decks.get(1), DeckOutputModel.class);

        cards = new ArrayList<>();
        cards.add(new CardCreateInput("What is Spring Framework?", "Spring is a powerful open source, loosely coupled, light weight, java based application framework meant for reducing the complexity of developing enterprise level applications."));
        cards.add(new CardCreateInput("Is HATEOAS required in REST?", "Well, it turns out it is."));
        cards.add(new CardCreateInput("Singleton?", "The scope of bean definition while using this would be single instance per IoC container."));
        for (CardCreateInput card : cards) {
            testRestTemplate.postForObject("http://localhost:" + port + "/card?deckId=1", card, CardOutputModel.class);
        }
    }

    @Test
    void getDueCardFromDeck_3CardsInDeck_getsOldestCard() {

        String requestUrl = BASE_URL + "/" + 1;

        ResponseEntity<QuestionOutputModel> questionOutputModelResponseEntity =
                testRestTemplate.getForEntity(requestUrl, QuestionOutputModel.class);

        assertEquals(HttpStatus.OK, questionOutputModelResponseEntity.getStatusCode());
        assertEquals("application/hal+json", questionOutputModelResponseEntity.getHeaders().getContentType().toString());

        QuestionOutputModel questionOutputModel = questionOutputModelResponseEntity.getBody();

        assertEquals(requestUrl, questionOutputModel.getLink("self").get().toUri().toString());
        assertEquals(cards.get(0).getFrontContent(), questionOutputModel.getFrontContent());
    }

    @Test
    void getDueCardFromDeck_firstCardReviewed1SecondBeforeMidnight_stillDueToday() {
        for (int i = 1; i <= 3; i++) { // set cards to reviewed, and due tomorrow
            CardTiming cardTiming = cardTimingRepository.findByCardId((long) i).get();
            cardTiming.setRepetitionNumber(1);
            cardTiming.setRepetitionInterval(i);// only id 1 is ready now
            cardTiming.setLastReviewDate(
                    LocalDateTime.of(
                            LocalDate.now().minusDays(1),  // yesterday
                            LocalTime.of(23, 59, 59)
                    )
            );
            cardTimingRepository.save(cardTiming);
        }

        String requestUrl = BASE_URL + "/" + 1;

        ResponseEntity<QuestionOutputModel> questionOutputModelResponseEntity =
                testRestTemplate.getForEntity(requestUrl, QuestionOutputModel.class);

        assertEquals(HttpStatus.OK, questionOutputModelResponseEntity.getStatusCode());
        assertEquals("application/hal+json", questionOutputModelResponseEntity.getHeaders().getContentType().toString());

        QuestionOutputModel questionOutputModel = questionOutputModelResponseEntity.getBody();

        assertEquals(requestUrl, questionOutputModel.getLink("self").get().toUri().toString());
        assertEquals(cards.get(0).getFrontContent(), questionOutputModel.getFrontContent());
    }

    @Test
    void getDueCardFromDeck_0CardsInDeck_returns404WithDetail() {

        String requestUrl = BASE_URL + "/" + 2;

        ResponseEntity<Problem> problemResponseEntity =
                testRestTemplate.getForEntity(requestUrl, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("No due cards exist", problem.getDetail());
    }

    @Test
    void getDueCardFromDeck_allCardsDueTomorrow_returns404WithDetail() {
        for (int i = 1; i <= 3; i++) { // set cards to reviewed, and due tomorrow
            CardTiming cardTiming = cardTimingRepository.findByCardId((long) i).get();
            cardTiming.setRepetitionNumber(1);
            cardTimingRepository.save(cardTiming);
        }

        String requestUrl = BASE_URL + "/" + 1;

        ResponseEntity<Problem> problemResponseEntity =
                testRestTemplate.getForEntity(requestUrl, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("No due cards exist", problem.getDetail());
    }

    @Test
    void getDueCardFromDeck_invalidInputId_returns400WithDetail() {

        String requestUrl = BASE_URL + "/" + 0;

        ResponseEntity<Problem> problemResponseEntity =
                testRestTemplate.getForEntity(requestUrl, Problem.class);

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Input constraint violation", problem.getTitle());
        assertEquals(HttpStatus.BAD_REQUEST, problem.getStatus());
        assertEquals("[Deck id must be positive]", problem.getDetail());
    }

    @Test
    void getSolutionToQuestion_existingCard_returns200() {
        String requestUrl = BASE_URL + "/solution/" + 1;
        ResponseEntity<AnswerOutputModel> answerOutputModelResponseEntity = testRestTemplate
                .getForEntity(requestUrl, AnswerOutputModel.class);

        assertEquals(HttpStatus.OK, answerOutputModelResponseEntity.getStatusCode());

        AnswerOutputModel answerOutputModel = answerOutputModelResponseEntity.getBody();

        assertEquals(requestUrl, answerOutputModel.getLink("self").get().toUri().toString());
        assertEquals(1L, answerOutputModel.getId());
        assertEquals(cards.get(0).getBackContent(), answerOutputModel.getBackContent());
    }

    @Test
    void getSolutionToQuestion_nonexistentCard_returns404() {
        String requestUrl = BASE_URL + "/solution/" + 4;
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .getForEntity(requestUrl, Problem.class);

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
        assertEquals("Unable to find card with id: 4", problem.getDetail());
    }

    @Test
    void getSolutionToQuestion_invalidId_returns400() {
        String requestUrl = BASE_URL + "/solution/" + 0;
        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .getForEntity(requestUrl, Problem.class);

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Input constraint violation", problem.getTitle());
        assertEquals(HttpStatus.BAD_REQUEST, problem.getStatus());
        assertEquals("[Card id must be positive]", problem.getDetail());
    }

    @Test
    void updateTimings_setSuccessOnExistingCard_returnsCardWithDueDateAndLinks() {

        String requestUrl = BASE_URL + "/" + 1 + "?difficulty=SUCCESS_EASY";

        ResponseEntity<CardOutputModel> cardOutputModelResponseEntity = testRestTemplate
                .exchange(
                        requestUrl,
                        HttpMethod.PUT,
                        null,
                        CardOutputModel.class
                );

        assertEquals(HttpStatus.OK, cardOutputModelResponseEntity.getStatusCode());
        assertEquals("http://localhost:" + port + "/card" + "/" + 1,
                cardOutputModelResponseEntity.getHeaders().getLocation().toString());

        assertEquals(
                requestUrl,
                cardOutputModelResponseEntity.getBody().getLink("self").get().toUri().toString()
        );

        assertEquals(
                "http://localhost:" + port + "/learn" + "/" + 1,
                cardOutputModelResponseEntity.getBody().getLink("get-next-due-card").get().toUri().toString()
        );

        assertEquals(LocalDate.now().plusDays(1), cardOutputModelResponseEntity.getBody().getDueDate());
    }

    @Test
    void updateTimings_missingParameter_returns400WithDetail() {

        String requestUrl = BASE_URL + "/" + 1;

        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(
                        requestUrl,
                        HttpMethod.PUT,
                        null,
                        Problem.class
                );

        assertEquals(HttpStatus.BAD_REQUEST, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Missing input", problem.getTitle());
        assertEquals("Parameter 'difficulty' is missing.", problem.getDetail());
        assertEquals(HttpStatus.BAD_REQUEST, problem.getStatus());
    }

    @Test
    void updateTimings_nonExistentCard_returns404WithDetail() {

        String requestUrl = BASE_URL + "/" + 4 + "?difficulty=SUCCESS_EASY";

        ResponseEntity<Problem> problemResponseEntity = testRestTemplate
                .exchange(
                        requestUrl,
                        HttpMethod.PUT,
                        null,
                        Problem.class
                );

        assertEquals(HttpStatus.NOT_FOUND, problemResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_PROBLEM_JSON, problemResponseEntity.getHeaders().getContentType());

        Problem problem = problemResponseEntity.getBody();

        assertEquals("Queried data does not exist", problem.getTitle());
        assertEquals("Unable to find card with id: 4", problem.getDetail());
        assertEquals(HttpStatus.NOT_FOUND, problem.getStatus());
    }

    @Test
    void updateTimings_setEasyOnNonReviewedCard_cardTimingDataIsCorrect() {
        ArgumentCaptor<CardTiming> cardTimingArgument = ArgumentCaptor.forClass(CardTiming.class);

        String requestUrl = BASE_URL + "/" + 1 + "?difficulty=SUCCESS_EASY";
        testRestTemplate.put(requestUrl, null);

        verify(cardTimingRepository, times(4)) // include all 3 @BeforeEach calls!
                .save(cardTimingArgument.capture());

        CardTiming cardTiming = cardTimingArgument.getValue(); // this returns the last `.save()` call's argument
        assertEquals(1L, cardTiming.getId());
        assertEquals(2.6, cardTiming.getEasinessFactor()); // increased by .1 from 2.5
        assertEquals(1, cardTiming.getRepetitionNumber());
        assertEquals(1, cardTiming.getRepetitionInterval());

    }

    @Test
    void updateTimings_setEasyOnOnceReviewedCard_cardTimingDataIsCorrect() {
        CardTiming cardTiming = cardTimingRepository.findByCardId(1L).get();
        cardTiming.setLastReviewDate(LocalDateTime.now().minusDays(1)); // set last review to yesterday (not necessary here)
        cardTiming.setRepetitionNumber(1);
        cardTiming.setEasinessFactor(2.6);
        cardTiming.setRepetitionInterval(1);
        cardTimingRepository.save(cardTiming);

        ArgumentCaptor<CardTiming> cardTimingArgument = ArgumentCaptor.forClass(CardTiming.class);

        String requestUrl = BASE_URL + "/" + 1 + "?difficulty=SUCCESS_EASY";
        testRestTemplate.put(requestUrl, null);

        verify(cardTimingRepository, times(5)) // include all 3 @BeforeEach calls, and the 2 calls here
                .save(cardTimingArgument.capture());

        cardTiming = cardTimingArgument.getValue(); // this returns the last `.save()` call's argument
        assertEquals(1L, cardTiming.getId());
        assertEquals(2.7, cardTiming.getEasinessFactor()); // increased by .1 from 2.6
        assertEquals(2, cardTiming.getRepetitionNumber()); // increased by 1 from 1
        assertEquals(6, cardTiming.getRepetitionInterval()); // increased according to algorithm from 1
    }

    @Test
    void updateTimings_failOnceEasyCard_cardTimingDataIsCorrect() {
        CardTiming cardTiming = cardTimingRepository.findByCardId(1L).get();
        cardTiming.setLastReviewDate(LocalDateTime.now().minusDays(1)); // set last review to yesterday (not necessary here)
        cardTiming.setRepetitionNumber(1);
        cardTiming.setEasinessFactor(2.6);
        cardTiming.setRepetitionInterval(1);
        cardTimingRepository.save(cardTiming);

        ArgumentCaptor<CardTiming> cardTimingArgument = ArgumentCaptor.forClass(CardTiming.class);

        String requestUrl = BASE_URL + "/" + 1 + "?difficulty=BLACKOUT";
        testRestTemplate.put(requestUrl, null);

        verify(cardTimingRepository, times(5)) // include all 3 @BeforeEach calls, and the 2 calls here
                .save(cardTimingArgument.capture());

        cardTiming = cardTimingArgument.getValue(); // this returns the last `.save()` call's argument
        assertEquals(1L, cardTiming.getId());
        assertEquals(1.8000000000000003, cardTiming.getEasinessFactor()); // set according to algorithm from 2.6
        assertEquals(0, cardTiming.getRepetitionNumber()); // reset to default
        assertEquals(1, cardTiming.getRepetitionInterval()); // reset to default
    }

}