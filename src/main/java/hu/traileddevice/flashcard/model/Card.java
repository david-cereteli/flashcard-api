package hu.traileddevice.flashcard.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Deck deck;

    @Column(columnDefinition = "VARCHAR(750)", unique = true)
    @NotNull
    @Size(min = 1, max = 750)
    private String frontContent;

    @Column(columnDefinition = "TEXT")
    @Lob
    @NotNull
    @Size(min = 1, max = 3000)
    @Type(type = "org.hibernate.type.TextType")
    private String backContent;

    @OneToOne(mappedBy = "card", orphanRemoval = true)
    private CardTiming cardTiming;
}
