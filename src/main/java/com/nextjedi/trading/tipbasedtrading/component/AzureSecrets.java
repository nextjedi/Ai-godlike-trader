package com.nextjedi.trading.tipbasedtrading.component;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AzureSecrets {
    private final SecretClient secretClient;
    public AzureSecrets(@Value("${azure.keyvault.uri}") String keyVaultUri) {
        secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }
    public String getSecret(String secretName) {
        KeyVaultSecret secret = secretClient.getSecret(secretName);
        return secret.getValue();
    }
}
