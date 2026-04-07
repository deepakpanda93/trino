/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.server.security.oauth2;

import io.trino.server.security.oauth2.OAuth2ServerConfigProvider.OAuth2ServerConfig;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TestOAuth2AuthorizationServerMetadataResource
{
    @Test
    void testMetadataWithAllEndpoints()
    {
        OAuth2Config config = new OAuth2Config()
                .setIssuer("https://issuer.example.com")
                .setClientId("client-id")
                .setClientSecret("client-secret");

        OAuth2ServerConfig serverConfig = new OAuth2ServerConfig(
                Optional.of("https://issuer.example.com/access"),
                URI.create("https://issuer.example.com/authorize"),
                URI.create("https://issuer.example.com/token"),
                URI.create("https://issuer.example.com/jwks"),
                Optional.of(URI.create("https://issuer.example.com/userinfo")),
                Optional.of(URI.create("https://issuer.example.com/logout")));

        OAuth2AuthorizationServerMetadataResource resource = new OAuth2AuthorizationServerMetadataResource(config, () -> serverConfig);

        Map<String, Object> metadata = resource.getMetadata();

        assertThat(metadata)
                .containsEntry("issuer", "https://issuer.example.com")
                .containsEntry("authorization_endpoint", "https://issuer.example.com/authorize")
                .containsEntry("token_endpoint", "https://issuer.example.com/token")
                .containsEntry("jwks_uri", "https://issuer.example.com/jwks")
                .containsEntry("scopes_supported", config.getScopes())
                .containsEntry("response_types_supported", "code")
                .containsEntry("userinfo_endpoint", "https://issuer.example.com/userinfo")
                .containsEntry("end_session_endpoint", "https://issuer.example.com/logout")
                .containsEntry("access_token_issuer", "https://issuer.example.com/access");
    }

    @Test
    void testMetadataWithMinimalEndpoints()
    {
        OAuth2Config config = new OAuth2Config()
                .setIssuer("https://issuer.example.com")
                .setClientId("client-id")
                .setClientSecret("client-secret");

        OAuth2ServerConfig serverConfig = new OAuth2ServerConfig(
                Optional.empty(),
                URI.create("https://issuer.example.com/authorize"),
                URI.create("https://issuer.example.com/token"),
                URI.create("https://issuer.example.com/jwks"),
                Optional.empty(),
                Optional.empty());

        OAuth2AuthorizationServerMetadataResource resource = new OAuth2AuthorizationServerMetadataResource(config, () -> serverConfig);

        Map<String, Object> metadata = resource.getMetadata();

        assertThat(metadata)
                .containsEntry("issuer", "https://issuer.example.com")
                .containsEntry("authorization_endpoint", "https://issuer.example.com/authorize")
                .containsEntry("token_endpoint", "https://issuer.example.com/token")
                .containsEntry("jwks_uri", "https://issuer.example.com/jwks")
                .containsEntry("scopes_supported", config.getScopes())
                .containsEntry("response_types_supported", "code")
                .doesNotContainKey("userinfo_endpoint")
                .doesNotContainKey("end_session_endpoint")
                .doesNotContainKey("access_token_issuer");
    }
}
