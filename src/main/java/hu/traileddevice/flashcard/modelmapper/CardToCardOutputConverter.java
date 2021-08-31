package hu.traileddevice.flashcard.modelmapper;

import hu.traileddevice.flashcard.dto.card.CardOutput;
import hu.traileddevice.flashcard.model.Card;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

public class CardToCardOutputConverter implements Converter<Card, CardOutput> {
    @Override
    public CardOutput convert(MappingContext<Card, CardOutput> mappingContext) {
        Card source = mappingContext.getSource();
        CardOutput result = new CardOutput();

        result.setId(source.getId());
        result.setDeckId(source.getDeck().getId());
        result.setFrontContent(source.getFrontContent());
        result.setBackContent(source.getBackContent());
        result.setDueDate(source.getCardTiming().getLastReviewDate().toLocalDate());

        return result;
    }
}
