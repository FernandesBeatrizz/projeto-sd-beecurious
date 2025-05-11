package main.java.meta2;


import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import main.java.search.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;



/**
 * Controlador responsável pelas interações entre o frontend e o backend da aplicação.
 */
@Controller
public class GreetingController {
    private final BackendRMIcliente backend;
    private static final Logger log = LoggerFactory.getLogger(GreetingController.class);
    //private OpenRouterAPI openRouterAPI;


    /**
     * Construtor do controlador. Inicializa o cliente backend para a comunicação via RMI.
     *
     * @throws NotBoundException Se o serviço não estiver disponível no registo RMI.
     * @throws RemoteException Se ocorrer uma falha de comunicação com o backend via RMI.
     */
    public GreetingController() throws NotBoundException, RemoteException {
        this.backend=new BackendRMIcliente();
    }


    /**
     * Metodo para exibir a página inicial da aplicação.
     *
     * @param model Modelo que será usado para passar atributos para a view.
     * @return O nome da view para renderizar a página inicial.
     */
    @GetMapping("/")
    public String index (Model model) {
        model.addAttribute("message", "Bem-vindo");
        return "index";
    }




    // ----- Pesquisar páginas ---------------------------------------------------------------------------------------------//
    //----------------------------------------------------------------------------------------------------------------------//

    //DEPOIS VER OQ METER AQUI
    @GetMapping("/pesquisarURL")
    public String linkForm() {
        return "pesquisarURL";
    }


    /**
     * Metodo para realizar a pesquisa de uma palavra.
     *
     * @param query O termo de pesquisa.
     * @param page O número da página para a paginação dos resultados.
     * @param model Modelo que será usado para passar atributos para a view.
     */
    @GetMapping("/search")
    public String search(@RequestParam("q") String query,
                         @RequestParam(value = "page", defaultValue = "1") int page,
                         Model model) {
        try {
            List<String[]> results = backend.search(query);
            int resultsPerPage = 10;
            int total = results.size();
            int totalPages = (int) Math.ceil((double) total / resultsPerPage);

            int start = (page - 1) * resultsPerPage;
            int end = Math.min(start + resultsPerPage, total);
            List<String[]> paginated = results.subList(start, end);

            model.addAttribute("query", query);
            model.addAttribute("results", paginated);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            /*model.addAttribute("totalResults", total);

            // Gerar análise contextualizada com OpenAI
            List<String> snippets = new ArrayList<>();
            for (String[] result : paginated) {
                snippets.add(result[2]); // Supondo que o excerto esteja no índice 2
            }
            String analise = OpenRouterAPI.gerarAnaliseContextual(query, snippets);
            model.addAttribute("analise", analise);*/

        } catch (Exception e) {
            model.addAttribute("results", new ArrayList<>());
        }
        return "search_results";
    }


    /**
     * Exibe os resultados de pesquisa de links que apontam para uma URL específica.
     *
     * @param url A URL a ser pesquisada.
     * @param model Modelo que será usado para passar atributos para a view.
     */
    @GetMapping("/results_url")
    public String linkResult(@RequestParam("url") String url, Model model) {
        try {
            // Verifica se há páginas que apontam para esta URL
            List<String> ligacoes = backend.consultarLinks(url);
            // Determina se encontrou páginas apontando para a URL
            boolean foiIndexado = ligacoes != null && !ligacoes.isEmpty();
            // Adiciona os atributos necessários para exibir na página de resultados
            model.addAttribute("encontrado", foiIndexado);
            model.addAttribute("url", url);
            if (foiIndexado) {
                // Não temos título nem excerto porque o Gateway não fornece isso diretamente
                model.addAttribute("titulo", "Título não disponível");
                model.addAttribute("excerto", "Excerto não disponível");
            }
            // Adiciona os links encontrados (se houver) na resposta
            model.addAttribute("ligacoes", ligacoes);
        } catch (Exception e) {
            // Caso ocorra algum erro durante o processo, mostra uma mensagem de erro
            model.addAttribute("encontrado", false);
            model.addAttribute("url", url);
            model.addAttribute("mensagem", "Erro ao indexar a URL: " + e.getMessage());
        }
        return "results_url";
    }




    // ----- STOP WORDS ----------------------------------------------------------------------------------------------------//
    //----------------------------------------------------------------------------------------------------------------------//
    /**
     * Exibe as stop words.
     *
     * @param model Modelo que será usado para passar atributos para a view.
     */
    @GetMapping("/stopwords")
    public String verStopWords(Model model) {
        try {
            String stopWords = backend.getStopWords();
            model.addAttribute("stopwords", stopWords);
        } catch (Exception e) {
            model.addAttribute("stopwords", "Erro ao obter stop words: " + e.getMessage());
        }
        return "checkStopwords.html";
    }



    // ----- INDEXAR URL ---------------------------------------------------------------------------------------------------//
    //----------------------------------------------------------------------------------------------------------------------//
    /**
     * Exibe o sítio para enviar uma URL para indexação.
     *
     * @param model Modelo que será usado para passar atributos para a view.
     */
    @GetMapping("/indexarURL")
    public String indexarURLForm(Model model) {
        return "indexarURL"; // Exibe o formulário de indexação de URL
    }


    /**
     * Realiza a indexação de uma URL fornecida.
     *
     * @param url A URL a ser indexada.
     * @param model Modelo que será usado para passar atributos para a view.
     */
    @PostMapping("/indexarURL")
    public String indexarURL(@RequestParam("url") String url, Model model) {
        try {
            // Envia a URL para o backend para ser indexada
            String urlIndexada = backend.indexarURL(url);
            // Mensagem de sucesso
            model.addAttribute("mensagem", "URL indexada com sucesso: " + urlIndexada);
            return "indexarURL";
        } catch (Exception e) {
            // Caso haja erro, mostra uma mensagem de erro
            model.addAttribute("mensagem", "Erro ao indexar a URL: " + e.getMessage());
            return "indexarURL"; // Retorna ao formulário com mensagem de erro
        }
    }





    // ----- CONTROLLER PARA O HACKER NEWS ---------------------------------------------------------------------------------//
    //----------------------------------------------------------------------------------------------------------------------//

    /**
     * Obtém as top stories do Hacker News, podendo realizar uma pesquisa nas histórias.
     *
     * @param search Termos de pesquisa para filtrar as stories.
     */
    @GetMapping("/hackernewstopstories")
    @ResponseBody
    private List<HackerNews> hackerNewsTopStories(@RequestParam(name="search", required = false) String search) {
        String topStoriesEndpoint = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";

        RestTemplate restTemplate = new RestTemplate();
        List hackerNewsNewTopStories = restTemplate.getForObject(topStoriesEndpoint, List.class);

        assert hackerNewsNewTopStories != null;
        log.info("hackerNewsNewStories: " + hackerNewsNewTopStories);
        log.info("hackerNewsNewStories: " + hackerNewsNewTopStories.size()); // Up to 500 top stories

        List<HackerNews> hackerNewsItemRecordList = new ArrayList<>();
        for (int i = 0; i <= 5; i++) { // Iterate only through 50 of them...
            Integer storyId = (Integer) hackerNewsNewTopStories.get(i);

            String storyItemDetailsEndpoint = String.format("https://hacker-news.firebaseio.com/v0/item/%s.json?print=pretty", storyId);
            HackerNews hackerNewsItemRecord = restTemplate.getForObject(storyItemDetailsEndpoint, HackerNews.class);

            if (hackerNewsItemRecord == null) {
                log.error("Item " + storyId + " is null");
                continue;
            }

            log.info("hackerNewsNewStories (details of " + storyId + "): " + hackerNewsItemRecord);

            if (search != null) {
                log.info("search: " + search);
                List<String> searchTermsList = List.of(search.toLowerCase().split(" "));
                if (searchTermsList.stream().anyMatch(hackerNewsItemRecord.title().toLowerCase()::contains))
                    hackerNewsItemRecordList.add(hackerNewsItemRecord);
            } else {
                System.out.println("No search terms");
                hackerNewsItemRecordList.add(hackerNewsItemRecord);
            }
        }

        return hackerNewsItemRecordList;
    }


    /**
     * Solicita a indexação das top stories do Hacker News, baseando-se na pesquisa feita.
     *
     * @param search Termos de pesquisa para filtrar as histórias.
     * @param confirm Confirmação para a indexação.
     * @param model Modelo que será usado para passar atributos para a view.
     */
    @GetMapping("/solicitarIndexacaoTopStories")
    public String indexarTopStories(@RequestParam("search") String search,
                                    @RequestParam("confirm") String confirm,
                                    Model model) {
        if (!"sim".equalsIgnoreCase(confirm)) {
            model.addAttribute("mensagem", "Indexação cancelada.");
            return "TopStoriesResultado";
        }

        try {
            List<HackerNews> stories = hackerNewsTopStories(search); // Usa o mesmo método de antes

            int count = 0;
            for (HackerNews story : stories) {
                if (story.url() != null && !story.url().isEmpty()) {
                    backend.indexarURL(story.url());  // Envia para indexação
                    count++;
                }
            }

            model.addAttribute("mensagem", "Foram indexadas " + count + " histórias do Hacker News.");
        } catch (Exception e) {
            model.addAttribute("mensagem", "Erro ao indexar histórias: " + e.getMessage());
        }

        return "TopStoriesResultado";
    }



    // ----- OPEN ROUTER AI-------------------------------------------------------------------------------------------------//
    //----------------------------------------------------------------------------------------------------------------------//
    @GetMapping("/chat_completions")
    public String gerarAnaliseComIA(@RequestParam("search") String search, Model model) {
        try {
            List<String[]> resultados = backend.search(search);

            // Gerar excertos em formato simples para IA
            List<String> linhas = new ArrayList<>();
            for (String[] r : resultados) {
                linhas.add(r[1] + " - " + r[0]); // título + URL
            }

            // Chamada à API da OpenAI
            String analise = OpenRouterAPI.gerarAnaliseContextual(search, linhas);
            //model.addAttribute("query", search);
            model.addAttribute("analise", analise);
        } catch (Exception e) {
            model.addAttribute("analise", "Erro ao gerar análise: " + e.getMessage());
        }
        return "chat_completions";
    }

}