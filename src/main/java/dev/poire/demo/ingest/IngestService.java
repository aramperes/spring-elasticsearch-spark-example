package dev.poire.demo.ingest;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class IngestService {

    private final RestHighLevelClient client;
    private final String indexNamePeople;
    private final String indexNameCities;
    private final Random random = ThreadLocalRandom.current();

    public IngestService(RestHighLevelClient client,
                         @Value("${demo.index-name.people}") String indexNamePeople,
                         @Value("${demo.index-name.cities}") String indexNameCities) {
        this.client = client;
        this.indexNamePeople = indexNamePeople;
        this.indexNameCities = indexNameCities;
    }

    public long createDocuments(int people, int cities) throws IOException {
        createCleanIndex(indexNamePeople);
        createCleanIndex(indexNameCities);
        return generateCities(people, cities);
    }

    @SneakyThrows
    private long generateCities(int people, int cities) {
        final Faker cityFaker = new Faker(random);
        final Faker nameFaker = new Faker(random);

        int placedPopulation = 0;
        final int averagePopulation = (int) ((float) people / (float) cities);
        final AtomicInteger completed = new AtomicInteger();

        for (int i = 0; i < cities; i++) {
            final int remaining = people - placedPopulation;
            final float extraPercentage = random.nextFloat() * 2 - 1F;
            final int extra = (int) (extraPercentage * (float) averagePopulation);

            final int cityPopulation = Math.min(Math.max(extra + averagePopulation, 1), remaining);
            placedPopulation += cityPopulation;

            final String cityName = cityFaker.address().cityName();
            final String cityCountry = new Faker(new Random(random.nextInt(10))).address().country();
            final String cityDescription = cityFaker.lorem().sentence(random.nextInt(10));

            final BulkRequest request = new BulkRequest();

            request.add(new IndexRequest(indexNameCities)
                    .id(cityName)
                    .source(XContentType.JSON,
                            "name", cityName,
                            "country", cityCountry,
                            "description", cityDescription));

            for (int p = 0; p < cityPopulation; p++) {
                final String personName = nameFaker.name().fullName();
                final String personBook = new Faker(new Random(random.nextInt(50))).book().title();
                request.add(new IndexRequest(indexNamePeople)
                        .source(XContentType.JSON,
                                "name", personName,
                                "favoriteBook", personBook,
                                "city", cityName));
            }

            client.bulkAsync(request, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
                @Override
                public void onResponse(BulkResponse bulkItemResponses) {
                    completed.incrementAndGet();
                    log.info("Generated city '{} ({})' with population {}", cityName, cityCountry, cityPopulation);
                }

                @Override
                public void onFailure(Exception e) {
                    completed.incrementAndGet();
                    log.info("Failed to ingest city {}", cityName);
                }
            });
        }

        while (completed.get() < cities) {
            Thread.sleep(100L);
        }

        Thread.sleep(1000L);
        return client.count(new CountRequest(indexNameCities, indexNamePeople), RequestOptions.DEFAULT).getCount();
    }

    private void createCleanIndex(String indexName) throws IOException {
        if (indexExists(indexName)) {
            log.warn("Index '{}' exists, dropping...", indexName);
            dropIndex(indexName);
        }
        log.info("Creating index '{}'...", indexName);
        createIndex(indexName);
    }

    private void createIndex(String indexName) throws IOException {
        final CreateIndexRequest request = new CreateIndexRequest(indexName)
                .mapping(getMappingJson(indexName), XContentType.JSON);
        client.indices().create(request, RequestOptions.DEFAULT);
        log.info("Index '{}' created.", indexName);
    }

    private boolean indexExists(String indexName) throws IOException {
        return client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
    }

    private void dropIndex(String indexName) throws IOException {
        client.indices().delete(new DeleteIndexRequest(indexName), RequestOptions.DEFAULT);
    }

    private String getMappingJson(String indexName) throws IOException {
        try (final InputStream is = this.getClass().getResourceAsStream(String.format("/index/%s.json", indexName))) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Unable to load index-mapping.json resource");
        }
    }
}
