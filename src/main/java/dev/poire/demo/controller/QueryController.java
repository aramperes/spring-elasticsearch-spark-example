package dev.poire.demo.controller;

import dev.poire.demo.spark.SparkService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "query", produces = MediaType.APPLICATION_JSON_VALUE)
public class QueryController {

    private final SparkService sparkService;

    public QueryController(SparkService sparkService) {
        this.sparkService = sparkService;
    }

    @GetMapping("/cityNames")
    public List<String> cityNames() throws IOException {
        return sparkService.getCityNames();
    }

    @GetMapping("/countCities")
    public long countCities() throws IOException {
        return sparkService.countCities();
    }

    @GetMapping("/populationByCity")
    public List<Map<String, Object>> populationByCity() throws IOException {
        return sparkService.populationByCity();
    }

    @GetMapping("/populationByCountry")
    public List<Map<String, Object>> populationByCountry() throws IOException {
        return sparkService.populationByCountry();
    }

    @PostMapping("/freeForm")
    public List<Map> freeForm(@RequestBody String sql) throws IOException {
        return sparkService.freeFormQuery(sql);
    }
}
