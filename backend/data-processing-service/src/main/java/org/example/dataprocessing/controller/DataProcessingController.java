package org.example.dataprocessing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/process")
public class DataProcessingController {

    @PostMapping("/map-columns")
    public ResponseEntity<String> mapColumns(@RequestBody Map<String, String> mappings) {
        // TODO: Implement CSV processing logic
        return ResponseEntity.ok("CSV mapped successfully!");
    }
}
