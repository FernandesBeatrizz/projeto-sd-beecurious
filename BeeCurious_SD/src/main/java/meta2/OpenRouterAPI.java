package main.java.meta2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
//import com.konghq.unirest.core.HttpResponse;
//import com.konghq.unirest.core.Unirest;
//import com.konghq.unirest.core.exceptions.UnirestException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.util.List;



/*public class OpenRouterAPI {
    public static void main(String[] args) {
        // Set your OpenRouter API key
        String apiKey = "your-api-key-here";
        String openRouterUrl = "https://openrouter.ai/api/v1/chat/completions";

        // Create the request payload
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-3.5-turbo");
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Hello, how are you?");
        payload.add("messages", new Gson().toJsonTree(new JsonObject[]{message}));

        try {
            // Make the POST request using Unirest
            HttpResponse<String> response = Unirest.post(openRouterUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(payload.toString())
                    .asString();

            // Parse the response using Gson
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(response.getBody(), JsonObject.class);

            // Extract the message content from the response
            String content = jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            // Print the response
            System.out.println("Response from OpenRouter: " + content);

        } catch (UnirestException e) {
            e.printStackTrace();
        }
    } // Fechar o método main
} */// Fechar a classe OpenRouterAPI



public class OpenRouterAPI {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final String MODEL = "nousresearch/deephermes-3-mistral-24b-preview";

    public static String gerarAnaliseContextual(String termosPesquisa, List<String> snippets) throws UnirestException {
        Gson gson = new Gson();

        // Criar mensagem
        StringBuilder messageText = new StringBuilder();
        messageText.append("Gere uma análise textual baseada nos seguintes termos de pesquisa e excertos dos resultados:\n");
        messageText.append("Termos de pesquisa: ").append(termosPesquisa).append("\n");
        messageText.append("Excerto dos resultados:\n");
        for (String snippet : snippets) {
            messageText.append("- ").append(snippet).append("\n");
        }

        // Montar JSON com GSON
        JsonObject payload = new JsonObject();
        payload.addProperty("model", MODEL);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", messageText.toString());
        messages.add(message);
        payload.add("messages", messages);

        // Enviar requisição
        HttpResponse<String> response = Unirest.post(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "https://googol.ai")
                .header("X-Title", "Googol Search")
                .body(payload.toString())
                .asString();

        JsonObject jsonResponse = gson.fromJson(response.getBody(), JsonObject.class);
        return jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }
}

