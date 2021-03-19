package uk.nhs.digital.nhsconnect.lab.results.configuration;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Component
@Slf4j
@NoArgsConstructor
public class CustomTrustStore {

    @Autowired(required = false)
    private AmazonS3 s3Client;

    @SneakyThrows
    public void addToDefault(String trustStorePath, String trustStorePassword) {
        final X509TrustManager defaultTrustManager = getDefaultTrustManager();
        final X509TrustManager customTrustManager =
            getCustomDbTrustManager(new AmazonS3URI(trustStorePath), trustStorePassword);
        X509TrustManager combinedTrustManager = new CombinedTrustManager(customTrustManager, defaultTrustManager);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] {combinedTrustManager}, null);
        LOGGER.info("Overriding default TrustStore with combined one");
        SSLContext.setDefault(sslContext);
    }

    @SneakyThrows
    private X509TrustManager getDefaultTrustManager() {
        var trustManagerFactory = getDefaultTrustManagerFactory();
        return getX509TrustManager(trustManagerFactory);
    }

    @SneakyThrows
    private X509TrustManager getCustomDbTrustManager(AmazonS3URI s3URI, String trustStorePassword) {
        var trustManagerFactory = getDefaultTrustManagerFactory();

        LOGGER.info("Loading custom KeyStore from '{}'", s3URI.toString());
        try (var s3Object = s3Client.getObject(new GetObjectRequest(s3URI.getBucket(), s3URI.getKey()));
             var content = s3Object.getObjectContent()) {
            KeyStore customKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            customKeyStore.load(content, trustStorePassword.toCharArray());
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(customKeyStore);
        }

        return getX509TrustManager(trustManagerFactory);
    }

    @SneakyThrows
    private TrustManagerFactory getDefaultTrustManagerFactory() {
        TrustManagerFactory trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null); // Using null here initialises the TMF with the default trust store.
        return trustManagerFactory;
    }

    private X509TrustManager getX509TrustManager(TrustManagerFactory trustManagerFactory) {
        return Arrays.stream(trustManagerFactory.getTrustManagers())
            .filter(X509TrustManager.class::isInstance)
            .map(X509TrustManager.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Cannot find trust manager"));
    }

    @RequiredArgsConstructor
    private static class CombinedTrustManager implements X509TrustManager {
        private final X509TrustManager primaryTrustManager;
        private final X509TrustManager secondaryTrustManager;

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return secondaryTrustManager.getAcceptedIssuers();
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                primaryTrustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException e) {
                // This will throw another CertificateException if this fails too.
                secondaryTrustManager.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            secondaryTrustManager.checkClientTrusted(chain, authType);
        }
    }
}
