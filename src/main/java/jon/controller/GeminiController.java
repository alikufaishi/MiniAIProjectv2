package jon.controller;

import jakarta.servlet.http.HttpSession;
import jon.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    // Ali: slettet de her fordi man får direkte adgang via front ent server
    /*
    @GetMapping("/testing")
    public String index() {
        return "home/index";
    }

    @GetMapping("/bookhelper")
    public String bookkeeper() {
        return "/bookhelperjjj.html";
    }
    */

    // Ali: Deaktiveret denne fordi lavet en ny som finder brugernavn også
    /*
    @GetMapping("/generate")
    // Requestparam er den inputtekst som brugeren har skrevet i API-kaldet
    public ResponseEntity<String> generate(@RequestParam String prompt) {
        // Block her betyder at man venter på svaret fra API-kaldet
        String result = geminiService.generateText(prompt).block();
        return ResponseEntity.ok(result);
    }
    */

    @GetMapping("/generate")
    // Requestparam er den inputtekst som brugeren har skrevet i API-kaldet
    // Jeg har tilføjet HttpSession for at hente username fra sessionen
    public ResponseEntity<String> generate(@RequestParam String prompt,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        // Hent brugernavnet fra Authentication
        String username = userDetails.getUsername();

        if (username == null) {
            return ResponseEntity.status(401).body("Bruger ikke logget ind");
        }

        // Block her betyder at man venter på svaret fra API-kaldet
        String result = geminiService.generateText(prompt, username).block();

        return ResponseEntity.ok(result);
    }
}
