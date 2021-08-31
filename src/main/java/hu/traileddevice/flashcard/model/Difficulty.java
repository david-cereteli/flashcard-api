package hu.traileddevice.flashcard.model;

public enum Difficulty {
    BLACKOUT(0),       // Complete failure to recall the information
    FAIL_FAMILIAR(1),  // Incorrect response, but upon seeing the correct answer it felt familiar
    FAIL_EASY(2),      // Incorrect response, but upon seeing the correct answer it seemed easy to remember.
    SUCCESS_HARD(3),   // Correct response, but required significant difficulty to recall.
    SUCCESS_MEDIUM(4), // Correct response, after some hesitation.
    SUCCESS_EASY(5);    // Correct response with perfect recall.

    private final int value;

    Difficulty(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
