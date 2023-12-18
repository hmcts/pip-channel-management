package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SjpPressListSummaryConverter implements ArtefactSummaryConverter {
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";
    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";

    /**
     * sjp press parent method - iterates over session data. Routes to specific methods which handle offences and
     * judiciary roles.
     *
     * @param payload - json body.
     * @return String with final summary data.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder();

        payload.get("courtLists").forEach(
            courtList -> courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(
                    session -> session.get("sittings").forEach(
                        sitting -> sitting.get("hearing").forEach(
                            hearing -> output
                                .append('•')
                                .append(processRolesSjpPress(hearing))
                                .append(processCaseUrns(hearing))
                                .append(processOffencesSjpPress(hearing.get("offence")))
                                .append('\n')
                        )
                    )
                )
            )
        );

        return output.toString();
    }

    /**
     * offences iterator method - handles logic of accused of single or multiple offences and returns output string.
     *
     * @param offencesNode - iterator on offences.
     * @return string with offence data.
     */
    private String processOffencesSjpPress(JsonNode offencesNode) {
        StringBuilder outputString = new StringBuilder();
        boolean offencesNodeSizeBool = offencesNode.size() > 1;
        if (offencesNodeSizeBool) {
            Iterator<JsonNode> offences = offencesNode.elements();
            int counter = 1;
            while (offences.hasNext()) {
                JsonNode thisOffence = offences.next();
                outputString
                    .append("\nOffence ")
                    .append(counter)
                    .append(": ")
                    .append(thisOffence.get("offenceTitle").asText())
                    .append(processReportingRestrictionSjpPress(thisOffence));
                counter += 1;
            }
        } else {
            outputString
                .append("\nOffence: ")
                .append(offencesNode.get(0).get("offenceTitle").asText())
                .append(processReportingRestrictionSjpPress(offencesNode.get(0)));
        }
        return outputString.toString();
    }

    /**
     * handles reporting restrictions for sjp press.
     *
     * @param node - node which is checked for reporting restriction.
     * @return - text based on whether restriction exists.
     */
    private String processReportingRestrictionSjpPress(JsonNode node) {
        return node.get("reportingRestriction").asBoolean() ? " (Reporting restriction)" : "";
    }

    /**
     * Concatenate case URNs for a hearing.
     *
     * @param hearing - hearing node.
     * @return Case URNs.
     */
    private String processCaseUrns(JsonNode hearing) {
        List<String> caseUrns = new ArrayList<>();
        hearing.get("case").forEach(hearingCase -> {
            if (hearingCase.has("caseUrn")) {
                caseUrns.add(hearingCase.get("caseUrn").asText());
            }
        });
        return "\nCase URN: " + caseUrns.stream()
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.joining(", "));
    }

    /**
     * role iteration method for sjp press.
     *
     * @param hearing - iterator of hearing.
     * @return list of roles.
     */
    private String processRolesSjpPress(JsonNode hearing) {
        Iterator<JsonNode> partyNode = hearing.get(PARTY).elements();
        String accused = "";
        String postcode = "";
        String prosecutor = "";

        while (partyNode.hasNext()) {
            JsonNode party = partyNode.next();
            if (!GeneralHelper.findAndReturnNodeText(party, PARTY_ROLE).isEmpty()) {
                if ("ACCUSED".equals(party.get(PARTY_ROLE).asText())) {
                    accused = getAccusedName(party);
                    postcode = getAccusedPostcode(party);
                } else if ("PROSECUTOR".equals(party.get(PARTY_ROLE).asText())) {
                    prosecutor = PartyRoleHelper.createOrganisationDetails(party);
                }
            }
        }
        return "Accused: " + accused + "\nPostcode: " + postcode + "\nProsecutor: " + prosecutor;
    }

    private String getAccusedName(JsonNode party) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            return PartyRoleHelper.createIndividualDetails(party, false);
        }
        return PartyRoleHelper.createOrganisationDetails(party);
    }

    private String getAccusedPostcode(JsonNode party) {
        if (party.has(INDIVIDUAL_DETAILS) && party.get(INDIVIDUAL_DETAILS).has("address")) {
            return GeneralHelper.findAndReturnNodeText(
                party.get(INDIVIDUAL_DETAILS).get("address"),
                "postCode"
            );
        } else if (party.has(ORGANISATION_DETAILS)
            && party.get(ORGANISATION_DETAILS).has(ORGANISATION_ADDRESS)) {
            return GeneralHelper.findAndReturnNodeText(
                party.get(ORGANISATION_DETAILS).get(ORGANISATION_ADDRESS),
                "postCode"
            );
        }
        return "";
    }
}
