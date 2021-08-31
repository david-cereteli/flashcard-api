package hu.traileddevice.flashcard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "timings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardTiming {

    private static final double DEFAULT_EASINESS = 2.5;

    @Id
    private Long id; // the name is just for the entity, the field uses card_id

    private int repetitionNumber;

    private double easinessFactor;

    private int repetitionInterval;

    private LocalDateTime lastReviewDate;

    @OneToOne
    @MapsId // use Card's id as primary key
    @JoinColumn(name = "card_id")
    private Card card;

    public CardTiming(Card card) {
        this.card = card;
        this.setEasinessFactor(DEFAULT_EASINESS);
        this.setRepetitionInterval(1);
        this.setRepetitionNumber(0);
        this.setLastReviewDate(LocalDateTime.now());
    }
}
