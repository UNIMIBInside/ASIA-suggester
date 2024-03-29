package it.disco.unimib.suggester.configuration;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "suggester")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class ConfigProperties {

    @NotNull
    private Translator translator;
    @NotNull
    private Summarizer abstat;

    @NotNull
    private Summarizer lov;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Validated
    public static class Summarizer {
        @NotNull
        private String mainEndpoint;
        @NotNull
        private String suggestEndpoint;
        @NotNull
        private String summariesEndpoint;

        private String authEndpoint;
        private String authUsername;
        private String authPassword;
        private String username;
        private String password;

        public String getFullSuggestEndpoint() {
            return mainEndpoint + "/" + suggestEndpoint;
        }

        public String getFullSummariesEndpoint() {
            return mainEndpoint + "/" + summariesEndpoint;
        }

        public String getAuthEndpoint() {
            return mainEndpoint + "/" + authEndpoint;
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Validated
    public static class Translator {

        private String subscriptionKey;
        @NotNull
        private String mainEndpoint;
        @NotNull
        private String detectEndpoint;
        @NotNull
        private String translateEndpoint;
        @NotNull
        private String lookupEndpoint;

        private Double translatedWordThreshold = 0.0;

        private Double translatedPhrasesThreshold = 0.0;

        public String getFullDetectEndpoint() {
            return mainEndpoint + "/" + detectEndpoint;
        }

        public String getFullTranslateEndpoint() {
            return mainEndpoint + "/" + translateEndpoint;
        }

        public String getFullLookupEndpoint() {
            return mainEndpoint + "/" + lookupEndpoint;
        }
    }
}

