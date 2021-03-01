package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

import java.util.Arrays;
import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.ProcedureRequest;

@Getter
@RequiredArgsConstructor
public enum ReportStatusCode {
    UNSPECIFIED("UN", "Unspecified");

    private final String code;
    private final String description;
    private static final Map<ReportStatusCode, ProcedureRequest.ProcedureRequestStatus> STATUS_CODE_MAPPING = Map.of(
        ReportStatusCode.UNSPECIFIED, ProcedureRequest.ProcedureRequestStatus.UNKNOWN
    );

    public static ReportStatusCode fromCode(@NonNull String code) {
        return Arrays.stream(ReportStatusCode.values())
            .filter(c -> code.equals(c.getCode()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No Report Status Code for '" + code + "'"));
    }

    public static ProcedureRequest.ProcedureRequestStatus mapToProcedureRequestStatus(ReportStatusCode code) {
        return STATUS_CODE_MAPPING.get(code);
    }
}
