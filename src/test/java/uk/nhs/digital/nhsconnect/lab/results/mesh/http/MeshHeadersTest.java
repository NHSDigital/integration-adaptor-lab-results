package uk.nhs.digital.nhsconnect.lab.results.mesh.http;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.nhsconnect.lab.results.mesh.message.WorkflowId;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class MeshHeadersTest {

    private static final String HEADER_MEX_CLIENT_VERSION = "Mex-ClientVersion";
    private static final String HEADER_MEX_OS_VERSION = "Mex-OSVersion";
    private static final String HEADER_MEX_OS_NAME = "Mex-OSName";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_MEX_FROM = "Mex-From";
    private static final String HEADER_MEX_TO = "Mex-To";
    private static final String HEADER_MEX_WORKFLOW_ID = "Mex-WorkflowID";
    private static final String HEADER_MEX_FILE_NAME = "Mex-FileName";
    private static final String HEADER_MEX_MESSAGE_TYPE = "Mex-MessageType";
    private static final String HEADER_MEX_CONTENT_COMPRESSED = "Mex-Content-Compressed";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_MEX_JAVA_VERSION = "Mex-JavaVersion";
    private static final String HEADER_MEX_OS_ARCHITECTURE = "Mex-OSArchitecture";

    private final MeshConfig meshConfig = new FakeMeshConfig();
    private final MeshHeaders meshHeaders = new MeshHeaders(meshConfig);

    @Test
    void createSendHeaders() {
        final String meshRecipient = "some_recipient";

        final Header[] headers = meshHeaders.createSendHeaders(meshRecipient, WorkflowId.REGISTRATION);

        final List<String> headerNames = Arrays.stream(headers)
            .map(BasicHeader.class::cast)
            .map(BasicHeader::getName)
            .collect(Collectors.toList());
        assertThat(headerNames).containsExactlyInAnyOrder(
                HEADER_MEX_CLIENT_VERSION,
                HEADER_MEX_OS_VERSION,
                HEADER_MEX_OS_NAME,
                HEADER_AUTHORIZATION,
                HEADER_MEX_FROM,
                HEADER_MEX_TO,
                HEADER_MEX_WORKFLOW_ID,
                HEADER_MEX_FILE_NAME,
                HEADER_MEX_MESSAGE_TYPE,
                HEADER_MEX_CONTENT_COMPRESSED,
                HEADER_CONTENT_TYPE);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_CLIENT_VERSION)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_OS_VERSION)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_OS_NAME)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_AUTHORIZATION)).startsWith("NHSMESH mailboxId:");
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_FROM)).isEqualTo("mailboxId");
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_TO)).isEqualTo(meshRecipient);
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_WORKFLOW_ID)).isEqualTo("LAB_RESULTS_REG");
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_FILE_NAME)).isEqualTo("edifact.dat");
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_MESSAGE_TYPE)).isEqualTo("DATA");
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_CONTENT_COMPRESSED)).isEqualTo("N");
            softly.assertThat(getHeaderValue(headers, HEADER_CONTENT_TYPE)).isEqualTo("application/octet-stream");
        });
    }

    @Test
    void createMinimalHeaders() {
        final Header[] headers = meshHeaders.createMinimalHeaders();

        final List<String> headerNames = Arrays.stream(headers)
            .map(BasicHeader.class::cast)
            .map(BasicHeader::getName)
            .collect(Collectors.toList());
        assertThat(headerNames).containsExactlyInAnyOrder(
                HEADER_MEX_CLIENT_VERSION,
                HEADER_MEX_OS_VERSION,
                HEADER_MEX_OS_NAME,
                HEADER_AUTHORIZATION);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_CLIENT_VERSION)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_OS_VERSION)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_OS_NAME)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_AUTHORIZATION)).startsWith("NHSMESH mailboxId:");
        });
    }

    @Test
    void createAuthenticateHeaders() {
        final Header[] headers = meshHeaders.createAuthenticateHeaders();

        final List<String> headerNames = Arrays.stream(headers)
            .map(BasicHeader.class::cast)
            .map(BasicHeader::getName)
            .collect(Collectors.toList());
        assertThat(headerNames).containsExactlyInAnyOrder(
                HEADER_MEX_JAVA_VERSION,
                HEADER_MEX_OS_ARCHITECTURE,
                HEADER_MEX_CLIENT_VERSION,
                HEADER_MEX_OS_VERSION,
                HEADER_MEX_OS_NAME,
                HEADER_AUTHORIZATION);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_JAVA_VERSION)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_OS_ARCHITECTURE)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_CLIENT_VERSION)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_OS_VERSION)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_MEX_OS_NAME)).isNotBlank();
            softly.assertThat(getHeaderValue(headers, HEADER_AUTHORIZATION)).startsWith("NHSMESH mailboxId:");
        });
    }

    private String getHeaderValue(Header[] headers, String header) {
        return Arrays.stream(headers)
            .map(BasicHeader.class::cast)
            .filter(h -> header.equals(h.getName()))
            .findFirst()
            .map(BasicHeader::getValue)
            .orElseThrow();
    }
}
