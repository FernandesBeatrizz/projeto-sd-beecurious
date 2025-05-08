/*package main.java.meta2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.konghq.unirest.core.HttpResponse;
import com.konghq.unirest.core.Unirest;
import com.konghq.unirest.core.exceptions.UnirestException;


public class OpenRouterAPI {
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
} // Fechar a classe OpenRouterAPI*/
