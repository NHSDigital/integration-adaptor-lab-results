package uk.nhs.digital.nhsconnect.lab.results.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PemFormatterTest {

    @Test
    void when_certHasExtraWhitespace_expect_itIsTrimmed() {
        final String withWhitespace = " -----BEGIN CERTIFICATE-----\n "
            +
            "    \t  MIIFXzCCA0egAwIBAgIJALRbCSor9bEbMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV \n"
            +
            "  \n\n    W/JNIRmhLoeFNGNh8HvhI2PwOCsFiqT1rrCaUtusTyH0Ggs=\n"
            +
            "   \r   -----END CERTIFICATE-----";
        final String trimmed = "-----BEGIN CERTIFICATE-----\n"
            +
            "MIIFXzCCA0egAwIBAgIJALRbCSor9bEbMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV\n"
            +
            "W/JNIRmhLoeFNGNh8HvhI2PwOCsFiqT1rrCaUtusTyH0Ggs=\n"
            +
            "-----END CERTIFICATE-----";
        final String formatted = PemFormatter.format(withWhitespace);
        assertThat(formatted).isEqualTo(trimmed);
    }

    @Test
    void when_certHasNoNewlines_expect_itIsReformatted() {
        final String withoutNewlines = "-----BEGIN RSA PRIVATE KEY-----"
            +
            " MIIJKQIBAAKCAgEA0x7V2cpEuXbLxb4TFigeN6e/TViXx4B9LMuHwwENX1P5V3O5"
            +
            " M0d/fLCFruu5dU3PWKoU2rTzUkflj5XOzu2xAftYi3KDMzRR2sByxjjxb/qMIybG"
            +
            " -----END RSA PRIVATE KEY-----";
        final String trimmed = "-----BEGIN RSA PRIVATE KEY-----\n"
            +
            "MIIJKQIBAAKCAgEA0x7V2cpEuXbLxb4TFigeN6e/TViXx4B9LMuHwwENX1P5V3O5\n"
            +
            "M0d/fLCFruu5dU3PWKoU2rTzUkflj5XOzu2xAftYi3KDMzRR2sByxjjxb/qMIybG\n"
            +
            "-----END RSA PRIVATE KEY-----";
        final String formatted = PemFormatter.format(withoutNewlines);
        assertThat(formatted).isEqualTo(trimmed);
    }

    @Test
    void when_certUsesDifferentHeaderAndFormattedCorrectly_expect_itIsNotModified() {
        final String pem = "-----BEGIN PRIVATE KEY-----\n"
            +
            "MIIJKQIBAAKCAgEA0x7V2cpEuXbLxb4TFigeN6e/TViXx4B9LMuHwwENX1P5V3O5\n"
            +
            "M0d/fLCFruu5dU3PWKoU2rTzUkflj5XOzu2xAftYi3KDMzRR2sByxjjxb/qMIybG\n"
            +
            "-----END PRIVATE KEY-----";
        final String formatted = PemFormatter.format(pem);
        assertThat(formatted).isEqualTo(pem);
    }

    @Test
    void when_certHasInvalidFormat_expect_throwException() {
        final String pem = "INVALID";
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> PemFormatter.format(pem));
        assertEquals("Invalid certificate or key format", exception.getMessage());
    }

}
