package main.java.meta2;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/*
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import com.example.servingwebcontent.beans.Number;
import com.example.servingwebcontent.forms.Project;
import com.example.servingwebcontent.thedata.Employee;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;*/

/*deixar estes por enquanto*/
import main.java.search.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


/*import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;*/

@Controller
public class GreetingController {
    private static GatewayINTER gateway;

    public GreetingController() {
        try {
            Registry registry = LocateRegistry.getRegistry(8183);
            gateway = (GatewayINTER) registry.lookup("Gateway");
        } catch (Exception e) {
            System.err.println("Couldn't find Gateway!");
            // Handle exception appropriately
        }
    }

    /*


    @Resource(name = "requestScopedNumberGenerator")
    private Number nRequest;

    @Resource(name = "sessionScopedNumberGenerator")
    private Number nSession;

    @Resource(name = "applicationScopedNumberGenerator")
    private Number nApplication;

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number requestScopedNumberGenerator() {
        return new Number();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number sessionScopedNumberGenerator() {
        return new Number();
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Number applicationScopedNumberGenerator() {
        return new Number();
    }*/

    @GetMapping("/")
    public String index (Model model) {
        model.addAttribute("message", "Bem-vindo");
        return "index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false) String query,
                         @RequestParam(name = "page", defaultValue = "1") int page,
                         Model model) {
        if (query == null || query.trim().isEmpty()) {
            return "index"; // redireciona para a página principal se a query estiver vazia
        }

        try {
            int resultsPerPage = 10;

            // Usa o método searchWord que já tens na Gateway
            List<String[]> allResults = gateway.searchWord(query); // lista de [url, título, snippet]

            int totalResults = allResults.size();
            int totalPages = (int) Math.ceil((double) totalResults / resultsPerPage);

            int start = (page - 1) * resultsPerPage;
            int end = Math.min(start + resultsPerPage, totalResults);
            List<String[]> paginatedResults = allResults.subList(start, end);

            model.addAttribute("query", query);
            model.addAttribute("results", paginatedResults);
            model.addAttribute("totalResults", totalResults);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", page);

            return "search_results";
        } catch (Exception e) {
            model.addAttribute("query", query);
            model.addAttribute("results", new ArrayList<>());
            model.addAttribute("totalResults", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("currentPage", 1);
            return "search_results";
        }
    }


    @GetMapping("/link")
    public String linkForm() {
        return "link"; // Mostra o formulário para introduzir URL
    }

    @GetMapping("/link_result")
    public String linkResult(@RequestParam("url") String url, Model model) {
        try {
            // Verifica se há páginas que apontam para esta URL
            List<String> ligacoes = gateway.obterPaginasApontamPara(url);

            boolean foiIndexado = ligacoes != null && !ligacoes.isEmpty();

            model.addAttribute("encontrado", foiIndexado);
            model.addAttribute("url", url);

            if (foiIndexado) {
                // Não temos título nem excerto porque o Gateway não fornece isso diretamente
                model.addAttribute("titulo", "Título não disponível");
                model.addAttribute("excerto", "Excerto não disponível");
            }

        } catch (Exception e) {
            model.addAttribute("encontrado", false);
            model.addAttribute("url", url);
        }

        return "link_result";
    }






/*
    @GetMapping("/givemeatable")
    public String atable(Model model) {
        Employee [] theEmployees = { new Employee(1, "José", "9199999", 1890), new Employee(2, "Marisa", "9488444", 2120), new Employee(3, "Hélio", "93434444", 2500)};
        List<Employee> le = new ArrayList<>();
        Collections.addAll(le, theEmployees);
        model.addAttribute("emp", le);
        return "table";
    }

    // from https://attacomsian.com/blog/spring-boot-thymeleaf-form-handling and https://github.com/attacomsian/code-examples
    @GetMapping("/create-project")
    public String createProjectForm(Model model) {

        model.addAttribute("project", new Project());
        return "create-project";
    }

    @PostMapping("/save-project")
    public String saveProjectSubmission(@ModelAttribute Project project) {

        // TODO: save project in DB here

        return "result";
    }

    @GetMapping("/counters")
    public String counters(Model model) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession(true);
        Integer counter = (Integer) session.getAttribute("counter");
        int c;
        if (counter == null)
            c = 1;
        else
            c = counter + 1;
        session.setAttribute("counter", c);
        model.addAttribute("sessioncounter", c);
        model.addAttribute("requestcounter2", this.nRequest.next());
        model.addAttribute("sessioncounter2", this.nSession.next());
        model.addAttribute("applicationcounter2", this.nApplication.next());
        return "counter";
    }*/

}