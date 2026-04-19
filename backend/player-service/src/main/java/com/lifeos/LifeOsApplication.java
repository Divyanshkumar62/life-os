package com.lifeos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EntityScan({"com.lifeos.player.domain", "com.lifeos.player.state", "com.lifeos.quest.domain", 
    "com.lifeos.penalty.domain", "com.lifeos.progression.domain",
    "com.lifeos.project.domain", "com.lifeos.streak.domain", "com.lifeos.reward.domain",
    "com.lifeos.onboarding.domain", "com.lifeos.system.domain", "com.lifeos.event.domain", 
    "com.lifeos.voice.domain", "com.lifeos.economy.domain"})
@EnableJpaRepositories({"com.lifeos.player.repository", "com.lifeos.player.state", 
    "com.lifeos.quest.repository", "com.lifeos.penalty.repository",
    "com.lifeos.progression.repository", "com.lifeos.project.repository", "com.lifeos.streak.repository",
    "com.lifeos.reward.repository", "com.lifeos.onboarding.repository", "com.lifeos.system.repository",
    "com.lifeos.voice.repository", "com.lifeos.economy.repository"})
@ComponentScan(basePackages = "com.lifeos")
public class LifeOsApplication {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(LifeOsApplication.class, args);
    }
}
