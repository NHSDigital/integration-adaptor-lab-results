package uk.nhs.digital.nhsconnect.lab.results.mesh.token;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Used in MESH authorization token - can by used only once per API request.
 */
@Getter
@RequiredArgsConstructor
class Nonce {
    @NonNull
    final String value;
    final String count = "1"; //token should use Nonce only once

    public Nonce() {
        this.value = UUID.randomUUID().toString();
    }
}
