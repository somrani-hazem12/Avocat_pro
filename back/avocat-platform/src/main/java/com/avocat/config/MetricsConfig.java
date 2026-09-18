package com.avocat.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    // ---------- Dashboard 2 : Securite et audit ----------

    @Bean
    public Counter connexionsReussiesCounter(MeterRegistry registry) {
        return Counter.builder("auth_connexions_total")
                .tag("resultat", "succes")
                .description("Nombre de connexions reussies")
                .register(registry);
    }

    @Bean
    public Counter connexionsEchoueesCounter(MeterRegistry registry) {
        return Counter.builder("auth_connexions_total")
                .tag("resultat", "echec")
                .description("Nombre d'echecs de connexion")
                .register(registry);
    }

    @Bean
    public Counter comptesBloquesCounter(MeterRegistry registry) {
        return Counter.builder("auth_comptes_bloques_total")
                .description("Nombre de comptes bloques automatiquement (5 echecs)")
                .register(registry);
    }

    @Bean
    public Counter alertesRisqueEleveCounter(MeterRegistry registry) {
        return Counter.builder("risque_alertes_total")
                .tag("niveau", "ELEVE")
                .description("Nombre d'alertes de risque critique declenchees")
                .register(registry);
    }

    // ---------- Dashboard 4 : Module IA ----------

    @Bean
    public Counter chatbotRequetesCounter(MeterRegistry registry) {
        return Counter.builder("chatbot_requetes_total")
                .description("Nombre de questions posees au chatbot juridique")
                .register(registry);
    }

    @Bean
    public Timer chatbotLatenceTimer(MeterRegistry registry) {
        return Timer.builder("chatbot_latence_seconds")
                .description("Temps de reponse du chatbot juridique")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Counter ocrDocumentsTraitesCounter(MeterRegistry registry) {
        return Counter.builder("ocr_documents_traites_total")
                .description("Nombre de documents traites par OCR")
                .register(registry);
    }

    @Bean
    public Timer ocrTraitementLatenceTimer(MeterRegistry registry) {
        return Timer.builder("ocr_traitement_latence_seconds")
                .description("Duree du traitement OCR + resume automatique")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}