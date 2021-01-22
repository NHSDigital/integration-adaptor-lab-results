package uk.nhs.digital.nhsconnect.lab.results.mesh.http;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.nhs.digital.nhsconnect.lab.results.utils.PemFormatter;

@Component
@Getter
public class MeshConfig {

    private final String mailboxId;
    private final String mailboxPassword;
    private final String sharedKey;
    private final String host;
    private final String certValidation;
    private final String endpointCert;
    private final String endpointPrivateKey;
    private final String subCAcert;

    @SuppressWarnings("checkstyle:parameternumber")
    @Autowired
    public MeshConfig(
            @Value("${labresults.mesh.mailboxId}") String mailboxId,
            @Value("${labresults.mesh.mailboxPassword}") String mailboxPassword,
            @Value("${labresults.mesh.sharedKey}") String sharedKey,
            @Value("${labresults.mesh.host}") String host,
            @Value("${labresults.mesh.certValidation}") String certValidation,
            @Value("${labresults.mesh.endpointCert}") String endpointCert,
            @Value("${labresults.mesh.endpointPrivateKey}") String endpointPrivateKey,
            @Value("${labresults.mesh.subCAcert}") String subCAcert) {
        this.mailboxId = mailboxId;
        this.mailboxPassword = mailboxPassword;
        this.sharedKey = sharedKey;
        this.host = host;
        this.certValidation = certValidation;
        this.endpointCert = endpointCert;
        this.endpointPrivateKey = endpointPrivateKey;
        this.subCAcert = subCAcert;
    }

    public String getEndpointCert() {
        return PemFormatter.format(endpointCert);
    }

    public String getEndpointPrivateKey() {
        return PemFormatter.format(endpointPrivateKey);
    }

    public String getSubCAcert() {
        return PemFormatter.format(subCAcert);
    }

}
