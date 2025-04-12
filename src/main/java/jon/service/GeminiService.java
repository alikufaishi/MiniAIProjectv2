package jon.service;

import jon.dto.GeminiContentDTO;
import jon.dto.GeminiResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    // Her defineres vores API-nøgle som vi har fået gratis fra Gemini
    @Value("AIzaSyAumJSG6IdP8U7m_sfHAxZ1PpVtP2_ONuc")
    private String apiKey;

    // Forklaring fra Chat-GPT: Spring Boot’s reaktive HTTP-klient, som bruges til at sende HTTP-forespørgsler
    // og modtage svar – ligesom Postman, men i kode.
    private final WebClient webClient;

    // Her injicerer vi et webclient objekt.
    // Det kræver at vi har WebClientConfig som definerer et Bean
    public GeminiService(WebClient webClient) {
        this.webClient = webClient;
    }

    // Vi opretter en arraylist som indeholder maps af key (String) og value (objekter) som er samtalen.
    private List<GeminiContentDTO> conversation = new ArrayList<>();

    // Indledningsvis er der tale om første besked i samtalen
    private boolean isFirstMessage = true;

    // Hovedmetoden som tager imod brugerens besked og kalder API'en
    public Mono<String> generateText(String userPrompt) {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        // Kun første gang bliver robotten "opdraget" til at opføre sig på en bestemt måde
        // Bemærk at det er med role-user key-value fordi gratis Gemini ikke understøtter role-system
        // Beskeden tilføjes til vores samtale-array
        if (isFirstMessage) {

            String systemPrompt = "Du er en hjælpsom og vidende bogassistent. Du anbefaler bøger ud fra mine interesser, genrer og behov. Giv mig herefter altid 3 anbefalinger, angiv hvor lang bogen er og hvornår den er fra.";
            conversation.add(new GeminiContentDTO(
                    "user",
                    List.of(new GeminiContentDTO.Part(systemPrompt))
            ));
            isFirstMessage = false;
        }

        // De efterfølgende beskeder er kun brugerens inputbeskeder (uden systemPrompt) og gemmes også i arraylisten
        // Her er role: user
        conversation.add(new GeminiContentDTO(
                "user",
                List.of(new GeminiContentDTO.Part(userPrompt))
        ));

        // Her er den Map af key-value pairs som sendes til AI'en (fra vores arraylist)
        Map<String, Object> requestBody = Map.of("contents", conversation);

        // Svarene fra API'en kommer her
        return webClient.post()
                .uri(endpoint)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponseDTO.class)
                .map(response -> {
                    try {
                        String reply = response.getCandidates().get(0).getContent().getParts().get(0).getText();

                        // API'ens svar bliver også gemt i vores samtale, men dens key-value par er role: model
                        conversation.add(new GeminiContentDTO(
                                "model",
                                List.of(new GeminiContentDTO.Part(reply))
                        ));

                        return reply;
                    } catch (Exception e) {
                        return "Fejl ved parsing af svar: " + e.getMessage();
                    }
                });
    }
}

