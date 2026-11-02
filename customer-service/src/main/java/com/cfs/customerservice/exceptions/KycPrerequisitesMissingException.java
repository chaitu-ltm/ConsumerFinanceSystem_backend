package com.cfs.customerservice.exceptions;

import java.util.List;

public class KycPrerequisitesMissingException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final List<String> missingSections;

    private final List<String> missingDocumentTypes;

    public KycPrerequisitesMissingException(
            List<String> missingSections,
            List<String> missingDocumentTypes) {

        super(buildMessage(missingSections, missingDocumentTypes));
        this.missingSections = List.copyOf(missingSections);
        this.missingDocumentTypes = List.copyOf(missingDocumentTypes);
    }

    public List<String> getMissingSections() {
        return missingSections;
    }

    public List<String> getMissingDocumentTypes() {
        return missingDocumentTypes;
    }

    private static String buildMessage(
            List<String> missingSections,
            List<String> missingDocumentTypes) {

        StringBuilder message = new StringBuilder(
                "KYC prerequisites are incomplete.");

        if (!missingSections.isEmpty()) {
            message.append(" Missing sections: ")
                    .append(String.join(", ", missingSections))
                    .append('.');
        }

        if (!missingDocumentTypes.isEmpty()) {
            message.append(" Missing documents: ")
                    .append(String.join(", ", missingDocumentTypes))
                    .append('.');
        }

        return message.toString();
    }
}
