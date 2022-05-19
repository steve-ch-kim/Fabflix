package com.github.klefstad_teaching.cs122b.gateway.config;

import com.github.klefstad_teaching.cs122b.core.security.JWTAuthenticationFilter;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.net.URI;
import java.net.URISyntaxException;

@ConstructorBinding
@ConfigurationProperties(prefix = "gateway")
public class GatewayServiceConfig
{
    private final URI  idm;
    private final URI  movies;
    private final URI  billing;
    private final URI  idmAuthenticate;
    private final Long maxLogs;

    public GatewayServiceConfig(String idm,
                                String movies,
                                String billing,
                                String authenticatePath,
                                Long maxLogs)
        throws URISyntaxException
    {
        this.idm = new URI(idm);
        this.movies = new URI(movies);
        this.billing = new URI(billing);
        this.idmAuthenticate = new URI(idm + authenticatePath);
        this.maxLogs = maxLogs;
    }

    public URI getIdm()
    {
        return idm;
    }

    public URI getMovies()
    {
        return movies;
    }

    public URI getBilling()
    {
        return billing;
    }

    public URI getIdmAuthenticate()
    {
        return idmAuthenticate;
    }

    public Long getMaxLogs()
    {
        return maxLogs;
    }

    public String fromHeader(String accessToken)
    {
        JSONObject request = new JSONObject();

        request.put(
            "accessToken",
            accessToken.startsWith(JWTAuthenticationFilter.BEARER_PREFIX) ?
                accessToken.substring(JWTAuthenticationFilter.BEARER_PREFIX.length()) :
                accessToken
        );

        return request.toJSONString();
    }
}
