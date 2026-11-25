package co.istad.application;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.io.File;

@SpringBootApplication(scanBasePackages = "co.istad")
@EntityScan(basePackages = "co.istad")
@EnableJpaRepositories(basePackages = "co.istad")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class Application {

    public static void main(String[] args) {
        String envDir = new File("docker-config/.env").exists() ? "docker-config" : ".";

        Dotenv dotenv = Dotenv.configure()
                .directory(envDir)
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(Application.class, args);
    }

}
