package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ArtefactSummaryConverter {

    /**
     * Interface method that captures the conversion of an artefact to an artefact summary string.
     */
    String convert(String payload) throws JsonProcessingException;
}
