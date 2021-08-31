package hu.traileddevice.flashcard.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "decks")
@Getter
@Setter
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60)
    @Size(min = 1, max = 60)
    private String name;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.REMOVE)
    private Set<Card> cards = new HashSet<>();

    public void addCard(Card cardToSave) {
        cards.add(cardToSave);
    }

    public void removeCard(Card cardToUpdate) {
        cards.remove(cardToUpdate);
    }
}
