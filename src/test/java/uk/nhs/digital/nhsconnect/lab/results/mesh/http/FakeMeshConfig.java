package uk.nhs.digital.nhsconnect.lab.results.mesh.http;

public class FakeMeshConfig extends MeshConfig {
    public FakeMeshConfig() {
        super("mailboxId",
            "password",
            "SharedKey",
            System.getProperty("LAB_RESULTS_MESH_HOST"),
            "false",
            System.getProperty("LAB_RESULTS_MESH_ENDPOINT_CERT"),
            System.getProperty("LAB_RESULTS_MESH_ENDPOINT_KEY"),
            System.getProperty("LAB_RESULTS_MESH_SUB_CA"));
    }
}
