package com.example.demo.configs;

import com.eventstore.dbclient.EventStoreDBClient;
import com.eventstore.dbclient.EventStoreDBClientSettings;
import com.eventstore.dbclient.EventStoreDBConnectionString;
import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventStoreDB {

    @Bean
    public EventStoreDBClient eventStoreDBClient() {
        EventStoreDBClientSettings settings = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
        System.out.println("fares");
        return EventStoreDBClient.create(settings);
    }

    @Bean
    public Gson gsonMapper() {
        return new Gson();
    }
}
