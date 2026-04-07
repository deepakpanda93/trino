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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.trino.server.security.ResourceSecurity;
import io.trino.server.security.oauth2.OAuth2ServerConfigProvider.OAuth2ServerConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.Map;

import static io.trino.server.security.ResourceSecurity.AccessType.PUBLIC;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.Objects.requireNonNull;

@Path("/.well-known/oauth-authorization-server")
@ResourceSecurity(PUBLIC)
public class OAuth2AuthorizationServerMetadataResource
{
    private final OAuth2Config config;
    private final OAuth2ServerConfigProvider serverConfigProvider;

    @Inject
    public OAuth2AuthorizationServerMetadataResource(OAuth2Config config, OAuth2ServerConfigProvider serverConfigProvider)
    {
        this.config = requireNonNull(config, "config is null");
        this.serverConfigProvider = requireNonNull(serverConfigProvider, "serverConfigProvider is null");
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Map<String, Object> getMetadata()
    {
        OAuth2ServerConfig serverConfig = serverConfigProvider.get();

        ImmutableMap.Builder<String, Object> metadata = ImmutableMap.builder();
        metadata.put("issuer", config.getIssuer());
        metadata.put("authorization_endpoint", serverConfig.authUrl().toString());
        metadata.put("token_endpoint", serverConfig.tokenUrl().toString());
        metadata.put("jwks_uri", serverConfig.jwksUrl().toString());
        metadata.put("scopes_supported", config.getScopes());
        metadata.put("response_types_supported", "code");
        serverConfig.userinfoUrl().ifPresent(uri -> metadata.put("userinfo_endpoint", uri.toString()));
        serverConfig.endSessionUrl().ifPresent(uri -> metadata.put("end_session_endpoint", uri.toString()));
        serverConfig.accessTokenIssuer().ifPresent(issuer -> metadata.put("access_token_issuer", issuer));

        return metadata.buildOrThrow();
    }
}
