package uk.gov.hmcts.reform.pip.channel.management.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.pip.channel.management.Application;
import uk.gov.hmcts.reform.pip.channel.management.config.AzureBlobTestConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, AzureBlobTestConfiguration.class})
@ActiveProfiles(profiles = "test")
@EnableConfigurationProperties(ThirdPartyApi.class)
@TestPropertySource("classpath:application-test.yaml")
class ThirdPartyServiceTest {

    @Autowired
    private ThirdPartyApi thirdPartyApi;

    @Test
    void testGetCourtelValue() {
        assertEquals("testCourtelApi", thirdPartyApi.getCourtel(), "Values should match");
    }
}
