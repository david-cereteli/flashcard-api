package hu.traileddevice.flashcard;

import hu.traileddevice.flashcard.modelmapper.CardToCardOutputConverter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FlashcardApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashcardApplication.class, args);
	}

	@Bean
	public ModelMapper getModelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.addConverter(new CardToCardOutputConverter());
		return modelMapper;
	}

	@Bean
	public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
		return new OpenAPI()
				.components(new Components())
				.info(new Info()
								.title("Flashcard REST API")
								.version(appVersion)
								.description("Operations for creating and memorizing flashcards")
				);
	}
}
