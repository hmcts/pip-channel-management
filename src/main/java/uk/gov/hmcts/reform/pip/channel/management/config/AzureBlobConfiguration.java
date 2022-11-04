package uk.gov.hmcts.reform.pip.channel.management.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test & !integration")
public class AzureBlobConfiguration {

    @Bean
    public BlobContainerClient blobContainerClient(AzureBlobConfigurationProperties
                                                       azureBlobConfigurationProperties) {
        return new BlobContainerClientBuilder()
            .connectionString(azureBlobConfigurationProperties.getConnectionString())
            .containerName(azureBlobConfigurationProperties.getContainerName())
            .buildClient();
    }

}
