package uk.nhs.digital.nhsconnect.lab.results.model.edifact;

public enum NhsAckStatus {
    ACCEPTED {
        @Override
        public String toString() {
            return "IAF";
        }
    },
    PARTIALLY_ACCEPTED {
        @Override
        public String toString() {
            return "IAP";
        }
    },
    INTERCHANGE_REJECTED {
        @Override
        public String toString() {
            return "IRI";
        }
    },
    MESSAGE_REJECTED {
        @Override
        public String toString() {
            return "IRM";
        }
    },
    ALL_MESSAGES_REJECTED {
        @Override
        public String toString() {
            return "IRA";
        }
    }
}
