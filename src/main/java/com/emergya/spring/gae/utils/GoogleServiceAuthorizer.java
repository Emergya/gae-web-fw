package com.emergya.spring.gae.utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 * Common OAuth code used to authenticate service calls to Google Services.
 */
@Component
public final class GoogleServiceAuthorizer {

    private GoogleServiceAuthorizer() {

    }

    /**
     * Authorize using service account credentials.
     *
     * @param httpTransport The HTTP transport to use for network requests.
     * @param jsonFactory The JSON factory to use for serialization /
     * de-serialization.
     * @param serviceKeyResourcePath The path to the resource holding the
     * service key.
     * @param scopes The scopes for which this app should authorize.
     * @return the credentials for the service.
     * @throws java.io.IOException if there is an error acessing the credentials
     * file.
     */
    public static Credential authorizeGoogleService(
            HttpTransport httpTransport,
            JsonFactory jsonFactory,
            String serviceKeyResourcePath,
            Collection<String> scopes)
            throws IOException {

        InputStream keyStream = GoogleServiceAuthorizer.class.getClassLoader().getResourceAsStream(serviceKeyResourcePath);

        try {
            GoogleCredential credential = GoogleCredential.fromStream(keyStream).createScoped(scopes);

            // Force a first-time update, so we have a fresh key
            credential.refreshToken();
            return credential;
        } catch (FileNotFoundException e) {
            throw new AssertionError("File not found should already be handled.", e);
        }
    }

}
