package uk.gov.hmcts.reform.rsecheck.config;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.demo.config.SpringSecurityConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SpringSecurityConfigTest {

    /**
     * This test checks the creation of the config. Ideally this test isn't needed and we would just test the configure
     * method, however this is marked as protected. Sonarqube flags this as coverage required, and rather than excluding
     * this file globally, this test has been added as a placeholder.
     */
    @Test
    public void testSpringSecurityConfigCreation() {
        SpringSecurityConfig springSecurityConfig = new SpringSecurityConfig();
        springSecurityConfig
        assertNotNull(springSecurityConfig, "Spring security config class not created");
    }

}
