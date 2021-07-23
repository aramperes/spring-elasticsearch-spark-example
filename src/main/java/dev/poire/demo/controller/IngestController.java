package dev.poire.demo.controller;

import dev.poire.demo.ingest.IngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping(value = "ingest", produces = MediaType.APPLICATION_JSON_VALUE)
public class IngestController {

    private final IngestService ingestService;

    public IngestController(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping("")
    @Operation(summary = "Create data in ElasticSearch")
    public long createDocuments(@RequestParam(value = "people", defaultValue = "1000")
                                @Parameter(name = "people", description = "People dataset size") int people,
                                @RequestParam(value = "cities", defaultValue = "10")
                                @Parameter(name = "cities", description = "City dataset size") int cities) throws IOException {
        if (people < 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "People must be at least 1000.");
        }
        if (cities < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cities must be at least 1.");
        }
        if (people < cities) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Must have more people than cities.");
        }
        return ingestService.createDocuments(people, cities);
    }
}
