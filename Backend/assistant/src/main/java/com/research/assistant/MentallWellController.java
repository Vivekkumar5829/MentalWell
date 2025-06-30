package com.research.assistant;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class MentallWellController {

    private final MentalWellService researchService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> processContent(@RequestBody MentalWellRequest request) {
        String result = researchService.processContent(request);

        Map<String, String> response = new HashMap<>();

        if (result == null || result.isBlank()) {
            response.put("reply", "⚠️ Could not generate a reply. Try again.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("reply", result);
        return ResponseEntity.ok(response);
    }
}
