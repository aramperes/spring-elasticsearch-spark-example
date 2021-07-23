package dev.poire.demo.spark;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;
import static org.elasticsearch.spark.rdd.api.java.JavaEsSpark.esRDD;

@Service
@Slf4j
public class SparkService implements Serializable {

    private final transient JavaSparkContext context;
    private final ObjectMapper objectMapper;

    public SparkService(JavaSparkContext context, ObjectMapper objectMapper) {
        this.context = context;
        this.objectMapper = objectMapper;
    }

    public long countCities() {
        return esRDD(context, "cities").count();
    }

    public List<String> getCityNames() {
        final SparkSession session = new SparkSession(context.sc());
        return session.read().format("es")
                .option("pushdown", true)
                .load("cities")
                .select("name")
                .sort(col("name").asc())
                .collectAsList()
                .stream()
                .map(r -> r.<String>getAs("name"))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> populationByCity() {
        final SparkSession session = new SparkSession(context.sc());
        final Dataset<Row> PEOPLE = session.read().format("es").option("pushdown", true).load("people");
        final Dataset<Row> CITIES = session.read().format("es").option("pushdown", true).load("cities");

        return PEOPLE
                .groupBy("city")
                .agg(count("*").alias("population"))
                .join(CITIES)
                .where(PEOPLE.col("city").equalTo(CITIES.col("name")))
                .select(PEOPLE.col("city"), CITIES.col("country"), col("population"))
                .sort(desc("population"))
                .collectAsList()
                .stream()
                .map(row -> {
                    final Map<String, Object> map = new HashMap<>();
                    map.put("city", row.getAs("city"));
                    map.put("country", row.getAs("country"));
                    map.put("population", row.getAs("population"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> populationByCountry() {
        final SparkSession session = new SparkSession(context.sc());
        final Dataset<Row> PEOPLE = session.read().format("es").option("pushdown", true).load("people");
        final Dataset<Row> CITIES = session.read().format("es").option("pushdown", true).load("cities");

        return PEOPLE.join(CITIES)
                .where(PEOPLE.col("city").equalTo(CITIES.col("name")))
                .groupBy(CITIES.col("country").alias("country"))
                .agg(count("*").alias("population"))
                .sort(desc("population"))
                .collectAsList()
                .stream()
                .map(row -> {
                    final Map<String, Object> map = new HashMap<>();
                    map.put("country", row.getAs("country"));
                    map.put("population", row.getAs("population"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map> freeFormQuery(String sql) {
        final String[] lines = sql.split("\n\n");

        final SparkSession session = new SparkSession(context.sc());
        for (int i = 0; i < lines.length - 1; i++) {
            session.sql(lines[i]);
        }
        return session.sql(lines[lines.length - 1])
                .toJSON()
                .collectAsList()
                .stream()
                .map(o -> {
                    try {
                        return objectMapper.readValue(o, Map.class);
                    } catch (JsonProcessingException processingException) {
                        processingException.printStackTrace();
                        return new HashMap<>();
                    }
                })
                .collect(Collectors.toList());
    }
}
