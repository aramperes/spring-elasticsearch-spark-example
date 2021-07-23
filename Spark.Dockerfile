FROM docker.io/bitnami/spark:3

# Add Elasticsearch JAR
RUN curl -s https://repo.maven.apache.org/maven2/org/elasticsearch/elasticsearch-spark-30_2.12/7.13.4/elasticsearch-spark-30_2.12-7.13.4.jar --output /opt/bitnami/spark/jars/elasticsearch-spark-30_2.12-7.13.4.jar

# Add app JAR
COPY target/app.jar.original /opt/app-lib.jar
