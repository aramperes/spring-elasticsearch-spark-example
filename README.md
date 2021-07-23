# spring-elasticsearch-spark-example

Playing with Spring Boot REST, Spark, Elasticsearch, and Docker.

## Getting Started

Requires:
- Java 8 (would support 11 if the Bitnami image was built with it).
- Maven 3
- Modern versions of Docker & Docker Compose

1. Build the JAR:

```
mvn package -DskipTests
```

2. Build and spin-up the containers

```
docker-compose up -d -build
```

3. Attach logs to demo app

```
docker-compose logs -f demo
```

> Note: if you see errors in the logs that it cannot connect to the cluster,
> try
>
> ```
> docker-compose restart demo
> ```

4. Attach logs to Elasticsearch (to monitor queries)

```
docker-compose logs -f elasticsearch
```

5. Visit http://localhost:8081/swagger-ui.html.
   Create sample data using the `/ingest` endpoint.

6. After the data is done generating, try the other `/query` endpoints.

7. Sample `freeForm` query:

```
CREATE TEMPORARY VIEW PEOPLE USING org.elasticsearch.spark.sql OPTIONS (resource 'people');

CREATE TEMPORARY VIEW CITIES USING org.elasticsearch.spark.sql OPTIONS (resource 'cities');

SELECT PEOPLE.favoriteBook book, CITIES.country country, COUNT(*) readers
FROM PEOPLE
JOIN CITIES ON PEOPLE.city = CITIES.name
GROUP BY PEOPLE.favoriteBook, CITIES.country
ORDER BY readers DESC
LIMIT 100;
```
