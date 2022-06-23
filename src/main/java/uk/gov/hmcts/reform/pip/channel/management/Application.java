package uk.gov.hmcts.reform.pip.channel.management;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uk.gov.hmcts.reform.pip.channel.management.utils.ThirdPartyApi;

@EnableConfigurationProperties(ThirdPartyApi.class)
@SpringBootApplication
@Slf4j
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        log.info("IN THE ROOT: " + System.getenv("TENANT_ID"));
        SpringApplication.run(Application.class, args);
    }
}
