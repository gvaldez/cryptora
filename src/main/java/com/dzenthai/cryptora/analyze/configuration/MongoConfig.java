package com.dzenthai.cryptora.analyze.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
public class MongoConfig {

    private final ApplicationContext applicationContext;

    public MongoConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        MappingMongoConverter mappingMongoConverter =
                applicationContext.getBean(MappingMongoConverter.class);
        mappingMongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }
}