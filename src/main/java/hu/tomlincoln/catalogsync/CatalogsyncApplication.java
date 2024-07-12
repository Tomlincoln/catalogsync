package hu.tomlincoln.catalogsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class CatalogsyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogsyncApplication.class, args);
    }

}
