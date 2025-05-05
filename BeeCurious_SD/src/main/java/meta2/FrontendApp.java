package main.java.meta2;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FrontendApp {
    public FrontendApp() {
    }
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FrontendApp.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setLogStartupInfo(false);
        app.run(args);
    }
}