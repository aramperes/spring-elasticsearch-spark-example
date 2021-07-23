package dev.poire.demo.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class City {
    private String name;
    private String country;
    private String description;
}
