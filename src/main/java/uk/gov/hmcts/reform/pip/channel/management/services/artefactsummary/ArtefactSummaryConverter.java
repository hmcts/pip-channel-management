package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface ArtefactSummaryConverter {

    /**
     * Interface method that captures the conversion of an artefact to an artefact summary string.
     */
    String convert(JsonNode payload) throws JsonProcessingException;
}
