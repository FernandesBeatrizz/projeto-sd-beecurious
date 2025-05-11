package main.java.meta2;

/*import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
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



/*public class OpenRouterAPI {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final String MODEL = "nousresearch/deephermes-3-mistral-24b-preview";


    public static String gerarAnaliseContextual(String termosPesquisa, List<String> snippets) throws UnirestException {
        Gson gson = new Gson();

        // Criar mensagem
        StringBuilder messageText = new StringBuilder();
        messageText.append("Baseado na pesquisa por: ").append(termosPesquisa).append("\n");
        messageText.append("E nos seguintes excertos de páginas:\n");
        for (String snippet : snippets) {
            messageText.append("- ").append(snippet).append("\n");
        }
        messageText.append("Gere uma análise breve e contextual sobre o conteúdo encontrado.");


        // Montar JSON com GSON
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", messageText.toString());

        JsonObject payload = new JsonObject();
        payload.addProperty("model", MODEL);
        payload.add("messages", gson.toJsonTree(new JsonObject[]{message}));


            // Fazer a requisição HTTP
        HttpResponse<String> response = Unirest.post(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                //.header("HTTP-Referer", "https://googol.ai")
                //.header("X-Title", "Googol Search")
                .body(payload.toString())
                .asString();

            // Log para debug
        JsonObject jsonResponse = gson.fromJson(response.getBody(), JsonObject.class);
        return jsonResponse.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

    }
}*/

/*import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class OpenRouterAPI {
    private static final String API_KEY = "<OPENROUTER_API_KEY>";
    private static final String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";

    public static String gerarAnaliseContextual(String termoPesquisa, List<String> citacoes) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Construção do prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("Com base no termo de pesquisa: \"").append(termoPesquisa).append("\".\n");
        prompt.append("E nas seguintes citações curtas:\n");
        for (String citacao : citacoes) {
            prompt.append("- ").append(citacao).append("\n");
        }
        prompt.append("Gere uma análise contextualizada e breve dos resultados.");

        JSONObject message = new JSONObject()
                .put("role", "user")
                .put("content", prompt.toString());

        JSONArray messages = new JSONArray().put(message);

        JSONObject requestBodyJson = new JSONObject()
                .put("model", "microsoft/phi-4-reasoning-plus:free")
                .put("messages", messages);

        RequestBody body = RequestBody.create(
                requestBodyJson.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("HTTP-Referer", "<YOUR_SITE_URL>")
                .addHeader("X-Title", "<YOUR_SITE_NAME>")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Erro na chamada da API: " + response);

            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }
}*/





import com.google.gson.*;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.List;

public class OpenRouterAPI {
    private static final String API_KEY = "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";
        private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    public static String gerarAnaliseContextual(String query, List<String> resultados) throws UnirestException {
        JsonArray messages = new JsonArray();

        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", "Você é um assistente que analisa pesquisas da web e produz uma análise contextualizada.");
        messages.add(system);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");

        StringBuilder content = new StringBuilder("Termo pesquisado: " + query + "\nResultados:\n");
        for (String resultado : resultados) {
            content.append("- ").append(resultado).append("\n");
        }

        user.addProperty("content", content.toString());
        messages.add(user);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "microsoft/phi-4-reasoning-plus:free");
        requestBody.add("messages", messages);

        HttpResponse<String> response = Unirest.post(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .asString();

        JsonObject resposta = JsonParser.parseString(response.getBody()).getAsJsonObject();
        return resposta.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }
}

