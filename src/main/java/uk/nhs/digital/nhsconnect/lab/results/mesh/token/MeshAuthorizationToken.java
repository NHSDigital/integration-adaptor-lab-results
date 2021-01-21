package uk.nhs.digital.nhsconnect.lab.results.mesh.token;

import uk.nhs.digital.nhsconnect.lab.results.mesh.http.MeshConfig;

import java.time.Instant;

/**
 * MESH authorization token
 * One time use only - each MESH API call should use new token
 */
public class MeshAuthorizationToken {

    private static final String MESSAGE_TYPE = "NHSMESH ";

    private final String data;
    private final String hash;

    public MeshAuthorizationToken(MeshConfig meshConfig, Instant timestamp, Nonce nonce,
        AuthorizationHashGenerator authorizationHashGenerator) {
        final String prefix = MESSAGE_TYPE + meshConfig.getMailboxId();
        final String currentTimeFormatted = new TokenTimestamp(timestamp).getValue();
        this.data = String.join(":", prefix, nonce.getValue(), Nonce.getCOUNT(), currentTimeFormatted);
        this.hash = authorizationHashGenerator.computeHash(meshConfig, nonce, currentTimeFormatted);
    }

    public MeshAuthorizationToken(MeshConfig meshConfig) {
        this(meshConfig, Instant.now(), new Nonce(), new AuthorizationHashGenerator());
    }

    public String getValue() {
        return String.join(":", data, hash);
    }

}
