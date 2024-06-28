package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface ArtefactSummaryData {

    /**
     * Interface method that retrieve the data required to generate summary from the payload.
     */
    Map<String, List<Map<String, String>>> get(JsonNode payload) throws JsonProcessingException;
}
