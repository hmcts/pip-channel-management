package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Locale;

@Service
public class SjpPressListSummaryConverter implements ArtefactSummaryConverter {
    private static final String INDIVIDUAL_DETAILS = "individualDetails";

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
        return node.get("reportingRestriction").asBoolean() ? "(Reporting restriction)" : "";
    }

    /**
     * role iteration method for sjp press.
     *
     * @param hearing - iterator of hearing.
     * @return list of roles.
     */
    private String processRolesSjpPress(JsonNode hearing) {
        Iterator<JsonNode> partyNode = hearing.get("party").elements();
        String accused = "";
        String postCode = "";
        String prosecutor = "";
        while (partyNode.hasNext()) {
            JsonNode currentParty = partyNode.next();
            if ("accused".equals(currentParty.get("partyRole").asText().toLowerCase(Locale.ROOT))) {
                String forename = currentParty.get(INDIVIDUAL_DETAILS).get("individualForenames").asText();
                String surname = currentParty.get(INDIVIDUAL_DETAILS).get("individualSurname").asText();
                postCode = currentParty.get(INDIVIDUAL_DETAILS).get("address").get("postCode").asText();
                accused = forename + " " + surname;
            } else {
                prosecutor = currentParty.get("organisationDetails").get("organisationName").asText();
            }
        }
        return "Accused: " + accused + "\nPostcode: " + postCode + "\nProsecutor: " + prosecutor;
    }
}
