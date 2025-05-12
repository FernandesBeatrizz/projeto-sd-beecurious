package main.java.meta2;

import com.google.gson.*;
import com.mashape.unirest.http.*;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.List;


/**
 * Classe responsável por interagir com a API OpenRouter.
 * Utiliza a API OpenRouter para gerar análises contextuais baseadas em pesquisas realizadas na web.
 * A classe se conecta à API utilizando o modelo GPT para processar consultas e gerar respostas.
 */
public class OpenRouterAPI {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";


    /**
     * Gera uma análise contextual baseada na pesquisa realizada e nos resultados fornecidos.
     * Este método envia uma consulta à API OpenRouter e recebe uma resposta contextualizada
     * com base nos resultados da pesquisa fornecidos.
     *
     * @param query A string que representa o termo de pesquisa.
     * @param resultados Lista de strings contendo os resultados (ou excertos) que serão utilizados
     * para gerar a análise.
     * @return A análise contextualizada gerada pela API OpenRouter.
     * @throws UnirestException Se ocorrer um erro durante a comunicação com a API.
     */
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

