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


@Controller
public class GreetingController {
    private final BackendRMIcliente backend;

    public GreetingController() throws NotBoundException, RemoteException {
        this.backend=new BackendRMIcliente();
    }

    @GetMapping("/")
    public String index (Model model) {
        model.addAttribute("message", "Bem-vindo");
        return "index";
    }

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

        } catch (Exception e) {
            model.addAttribute("results", new ArrayList<>());
        }
        return "search_results";
    }


    @GetMapping("/pesquisarURL")
    public String linkForm() {
        return "pesquisarURL"; // Mostra o formulário para introduzir URL
    }

    @PostMapping("/indexarURL")
    public String indexarURL(@RequestParam("url") String url, Model model) {
        try {
            // Envia a URL para o backend para ser indexada
            String urlIndexada = backend.indexarURL(url);

            // Mensagem de sucesso
            model.addAttribute("mensagem", "URL indexada com sucesso: " + urlIndexada);
            return "redirect:/results_url?url=" + url;
        } catch (Exception e) {
            // Caso haja erro, mostra uma mensagem de erro
            model.addAttribute("mensagem", "Erro ao indexar a URL: " + e.getMessage());
            return "pesquisarURL"; // Retorna ao formulário com mensagem de erro
        }
    }

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




}