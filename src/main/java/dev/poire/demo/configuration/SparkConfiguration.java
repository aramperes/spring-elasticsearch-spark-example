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
    @Value("${demo.spark.elastic.nodes}")
    private String elasticNodes;
    @Value("${demo.spark.elastic.port}")
    private String elasticPort;
    @Value("${demo.spark.jars}")
    private String sparkJars;

    @Bean
    public SparkConf sparkConf() {
        return new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("pushdown", "true")
                .set("es.nodes", elasticNodes)
                .set("es.port", elasticPort)
                .set("spark.jars", sparkJars);
    }

    @Bean
    public JavaSparkContext javaSparkContext(SparkConf conf) {
        return new JavaSparkContext(conf);
    }
}
