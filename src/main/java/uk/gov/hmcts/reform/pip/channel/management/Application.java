package uk.gov.hmcts.reform.pip.channel.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uk.gov.hmcts.reform.pip.channel.management.config.AzureBlobConfigurationProperties;

@EnableConfigurationProperties(AzureBlobConfigurationProperties.class)
@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, it's not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
