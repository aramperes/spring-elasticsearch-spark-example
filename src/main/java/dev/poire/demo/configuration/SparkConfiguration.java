package dev.poire.demo.configuration;

import lombok.Getter;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SparkConfiguration {
    @Value("${demo.spark.app-name}")
    private String appName;
    @Value("${demo.spark.uri}")
    private String master;

    @Bean
    public SparkConf sparkConf() {
        return new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("pushdown", "true")
                .set("es.nodes", "elasticsearch")
                .set("es.port", "9200")
                .set("spark.jars", "/opt/app-original.jar")
                ;
    }

    @Bean
    public JavaSparkContext javaSparkContext(SparkConf conf) {
        return new JavaSparkContext(conf);
    }
}
