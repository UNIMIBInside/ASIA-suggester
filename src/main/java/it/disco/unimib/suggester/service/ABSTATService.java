package it.disco.unimib.suggester.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.disco.unimib.suggester.Configuration.ConfigProperties;
import it.disco.unimib.suggester.model.Dataset;
import it.disco.unimib.suggester.model.Suggestion;
import lombok.*;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class ABSTATService {

    private final Gson gson = new Gson();
    private final OkHttpClient client;
    @Getter
    @Setter
    private boolean test = false;
    @Getter
    @Setter
    private List<String> preferredSummaries;
    private ConfigProperties properties;
    private static Pattern notAlphanumeric = Pattern.compile("[^a-z0-9]");
    private static Pattern spaces = Pattern.compile("\\s+");

    public ABSTATService(ConfigProperties properties, OkHttpClient client) {
        this.properties = properties;
        this.client = client;
    }

    private static String stringPreprocessing(String str) {
        Matcher m = notAlphanumeric.matcher(str);
        str = m.replaceAll(" ");
        m = spaces.matcher(str);
        str = m.replaceAll(" ");
        String[] words = str.split(" ");
        for (int i = 1; i < words.length; i++)
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
        return str;
    }

    private static String filterURI(String URI) {
        if (URI.startsWith("http")) {
            int slashIdx = URI.lastIndexOf('/');
            int hashIdx = URI.lastIndexOf('#');
            int colonIdx = URI.lastIndexOf(':');
            URI = URI.substring(Math.max(slashIdx, Math.max(hashIdx, colonIdx)));
        }
        return URI;
    }


    // This function prettifies the json response.
    private static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    private String abstatSuggestions(@NonNull String keyword, @NonNull Position position) throws IOException {
        String url = properties.getSummarizer().getFullSuggestEndpoint();

        HttpUrl.Builder urlBuilder = requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (!Objects.equals(keyword, "")) {
            urlBuilder.addQueryParameter("qString", keyword);
            urlBuilder.addQueryParameter("qPosition", position.getValue());
            urlBuilder.addQueryParameter("rows", "15");
            urlBuilder.addQueryParameter("start", "0");

            if (!isEmpty(preferredSummaries)) // check nullity and emptiness
                urlBuilder.addQueryParameter("dataset", String.join(",", preferredSummaries));
            return getString(urlBuilder);

        }
        return "";
    }

    private List<Suggestion> abstatListSuggestions(String keyword, boolean filter, Position position) {
        keyword = filterURI(keyword);
        if (filter) keyword = stringPreprocessing(keyword);

        try {
            String suggestions = this.abstatSuggestions(keyword, position);
            return gson.fromJson(suggestions, Suggestions.class).getSuggestions();
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    Datasets abstatListSummaries() throws IOException {
        String url = properties.getSummarizer().getFullDatasetsEndpoint();
        HttpUrl.Builder urlBuilder = requireNonNull(HttpUrl.parse(url)).newBuilder();
        String strDatasets = getString(urlBuilder);

        return gson.fromJson(strDatasets, Datasets.class);

    }

    private String getString(HttpUrl.Builder urlBuilder) throws IOException {
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder().url(url).get().build();
        Response response = client.newCall(request).execute();
        assert response.body() != null;
        String bodyString = response.body().string();
        if (test) System.out.println(prettify(bodyString));
        return bodyString;
    }


    List<Suggestion> propertySuggestions(String keyword, boolean filter) {
        return abstatListSuggestions(keyword, filter, Position.PRED);
    }

    List<Suggestion> typeSuggestions(String keyword, boolean filter) {
        return abstatListSuggestions(keyword, filter, Position.SUBJ);
    }


    List<Suggestion> propertySuggestionsMultipleKeywords(@NonNull List<String> keywords, boolean filter) {
        return listSuggestionsMultipleKeywords(keywords, filter, Position.PRED);
    }

    List<Suggestion> typeSuggestionsMultipleKeywords(@NonNull List<String> keywords, boolean filter) {
        return listSuggestionsMultipleKeywords(keywords, filter, Position.SUBJ);
    }


    private List<Suggestion> listSuggestionsMultipleKeywords(@NonNull List<String> keywords, boolean filter, Position position) {
        return keywords.stream()
                .map(k -> abstatListSuggestions(k, filter, position))
                .filter(suggestions -> !isEmpty(suggestions))
                .flatMap(Collection::stream)
                .distinct()
                .sorted(Comparator.comparing(Suggestion::getOccurrence))
                .collect(Collectors.toList());
    }

    enum Position {
        PRED("pred"), SUBJ("subj"), OBJ("obj");
        private String value;

        Position(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class Datasets {
        private List<Dataset> datasets;

        List<String> getDatasetsNames() {
            return isEmpty(datasets) ?
                    Collections.emptyList() : datasets.stream().map(Dataset::getDatasetName).collect(Collectors.toList());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Suggestions {
        private List<Suggestion> suggestions = new ArrayList<>();
    }

}