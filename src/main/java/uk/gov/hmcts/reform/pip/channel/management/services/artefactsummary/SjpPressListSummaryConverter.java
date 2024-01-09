package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.PartyRoleHelper;

import java.util.Iterator;

@Service
public class SjpPressListSummaryConverter implements ArtefactSummaryConverter {
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";
    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String OFFENCE_TITLE = "offenceTitle";
    private static final String ACCUSED = "ACCUSED";

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
                            cases -> cases.get("case").forEach(
                                cases2 -> output
                                    .append('•')
                                    .append(processRolesSjpPress(hearing))
                                    .append(getOffenceTitle(cases2.get("parties")))
                                    .append('\n')
                            )
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
     * @param partiesNode - iterator on offences.
     * @return string with offence data.
     */
    private String getOffenceTitle(JsonNode partiesNode) {
        StringBuilder output = new StringBuilder();

        for (JsonNode party : partiesNode) {
            String role = party.get(PARTY_ROLE).asText();
            if (ACCUSED.equals(role)) {

                party.get("offence").elements().forEachRemaining(offence -> {

                    if (output.length() == 0) {

                        int counter = 1;
                        output.append("\nOffence ");
                        output.append(counter);
                        output.append(": ");
                        output.append(offence.get(OFFENCE_TITLE).asText());
                        output.append(this.processReportingRestrictionSjpPress(offence));

                        counter += 1;

                    } else {
                        output.append(", ").append(offence.get(OFFENCE_TITLE).asText());
                        output.append(this.processReportingRestrictionSjpPress((partiesNode.get(0))));
                    }
                });
            }
        }
        return output.toString();
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
