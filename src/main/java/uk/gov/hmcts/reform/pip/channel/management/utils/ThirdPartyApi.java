package uk.gov.hmcts.reform.pip.channel.management.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "third-party-api")
@Data
public class ThirdPartyApi {

    private String courtel;
}
