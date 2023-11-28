package uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapresslist.Offence;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapresslist.OpaCaseInfo;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapresslist.OpaDefendantInfo;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapresslist.OpaPressList;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.GeneralHelper;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class OpaPressListHelper {
    private static final String COURT_LISTS = "courtLists";
    private static final String COURT_HOUSE = "courtHouse";
    private static final String COURT_ROOM = "courtRoom";
    private static final String SESSION = "session";
    private static final String SITTINGS = "sittings";
    private static final String HEARING = "hearing";
    private static final String CASE = "case";
    private static final String CASE_URN = "caseUrn";
    private static final String SCHEDULED_HEARING_DATE = "scheduledHearingDate";
    private static final String INFORMANT = "informant";
    private static final String PROSECUTING_AUTHORITY_REF = "prosecutionAuthorityRef";

    private static final String PARTY = "party";
    private static final String PARTY_ROLE = "partyRole";
    private static final String INDIVIDUAL_DETAILS = "individualDetails";
    private static final String INDIVIDUAL_FORENAMES = "individualForenames";
    private static final String INDIVIDUAL_MIDDLE_NAME = "individualMiddleName";
    private static final String INDIVIDUAL_SURNAME = "individualSurname";
    private static final String DOB = "dateOfBirth";
    private static final String AGE = "age";
    private static final String ADDRESS = "address";
    private static final String POSTCODE = "postCode";
    private static final String ORGANISATION_DETAILS = "organisationDetails";
    private static final String ORGANISATION_NAME = "organisationName";
    private static final String ORGANISATION_ADDRESS = "organisationAddress";

    private static final String OFFENCE = "offence";
    private static final String OFFENCE_TITLE = "offenceTitle";
    private static final String OFFENCE_SECTION = "offenceSection";
    private static final String OFFENCE_WORDING = "offenceWording";
    private static final String PLEA = "plea";
    private static final String PLEA_DATE = "pleaDate";
    private static final String REPORTING_RESTRICTION_DETAIL = "reportingRestrictionDetail";

    private static final String DEFENDANT = "DEFENDANT";
    private static final String PROSECUTING_AUTHORITY = "PROSECUTING_AUTHORITY";
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String DELIMITER = ", ";

    private OpaPressListHelper() {
    }

    /**
     * Process raw JSON for OPA press list to generate cases sorted by the plea date.
     * @param jsonData JSON data for the list
     * @return a sorted map of plea date to OPA press list cases
     */
    public static Map<String, List<OpaPressList>> processRawListData(JsonNode jsonData) {
        Map<String, List<OpaPressList>> result = new ConcurrentHashMap<>();

        jsonData.get(COURT_LISTS).forEach(
            courtList -> courtList.get(COURT_HOUSE).get(COURT_ROOM).forEach(
                courtRoom -> courtRoom.get(SESSION).forEach(
                    session -> session.get(SITTINGS).forEach(
                        sitting -> sitting.get(HEARING).forEach(hearing -> {
                            if (hearing.has(PARTY)) {
                                // Each case can have multiple defendants. They will be shown as separate entry on the
                                // list as each can have its own plea date
                                processPartyRoles(hearing).forEach(defendant -> {
                                    List<OpaPressList> rows = new ArrayList<>();

                                    hearing.get(CASE).forEach(hearingCase -> {
                                        OpaCaseInfo caseInfo = buildHearingCase(hearingCase);
                                        rows.add(new OpaPressList(defendant, caseInfo));
                                    });

                                    // All the offences under the same defendant have the same plea date
                                    String pleaDate = defendant.getOffences().get(0).getPleaDate();
                                    result.computeIfAbsent(pleaDate, x -> new ArrayList<>())
                                        .addAll(rows);
                                });
                            }
                        })
                    )
                )
            )
        );
        return OpaPressListSorter.sort(result);
    }

    private static OpaCaseInfo buildHearingCase(JsonNode hearingCase) {
        String scheduledHearingDate = DateHelper.formatTimeStampToBst(
            GeneralHelper.findAndReturnNodeText(hearingCase, SCHEDULED_HEARING_DATE),
            Language.ENGLISH, false, false, DATE_FORMAT
        );
        return new OpaCaseInfo(
            GeneralHelper.findAndReturnNodeText(hearingCase, CASE_URN),
            scheduledHearingDate,
            formatReportingRestriction(hearingCase)
        );
    }

    private static String formatReportingRestriction(JsonNode node) {
        if (node.has(REPORTING_RESTRICTION_DETAIL)) {
            List<String> restrictions = new ArrayList<>();
            node.get(REPORTING_RESTRICTION_DETAIL)
                .forEach(restriction -> restrictions.add(restriction.asText()));

            return restrictions.stream()
                .filter(r -> !StringUtils.isBlank(r))
                .collect(Collectors.joining(DELIMITER));
        }
        return "";
    }

    private static List<OpaDefendantInfo> processPartyRoles(JsonNode hearing) {
        List<OpaDefendantInfo> defendantInfo = new ArrayList<>();
        hearing.get(PARTY).forEach(party -> {
            if (party.has(PARTY_ROLE) && DEFENDANT.equals(party.get(PARTY_ROLE).asText())) {
                OpaDefendantInfo defendant = new OpaDefendantInfo();
                processDefendant(party, defendant);

                // The offence's plea date is used to group and sort the cases for the defendant. If plea date is
                // missing the entry will be dropped
                if (!defendant.getName().isEmpty()
                    && !defendant.getOffences().isEmpty()
                    && !defendant.getOffences().get(0).getPleaDate().isEmpty()) {
                    defendantInfo.add(defendant);
                }
            }
        });
        defendantInfo.forEach(d -> d.setProsecutor(processProsecutor(hearing)));
        return defendantInfo;
    }

    private static void processDefendant(JsonNode party, OpaDefendantInfo defendantInfo) {
        if (party.has(INDIVIDUAL_DETAILS)) {
            JsonNode individualDetails = party.get(INDIVIDUAL_DETAILS);
            String address = individualDetails.has(ADDRESS)
                ? CrimeListHelper.formatDefendantAddress(individualDetails.get(ADDRESS)) : "";

            String postcode = (individualDetails.has(ADDRESS) && individualDetails.get(ADDRESS).has(POSTCODE))
                ? individualDetails.get(ADDRESS).get(POSTCODE).asText() : "";

            defendantInfo.setName(formatDefendantName(individualDetails));
            defendantInfo.setDob(GeneralHelper.findAndReturnNodeText(individualDetails, DOB));
            defendantInfo.setAge(GeneralHelper.findAndReturnNodeText(individualDetails, AGE));
            defendantInfo.setAddress(address);
            defendantInfo.setPostcode(postcode);
            defendantInfo.setOffences(processOffences(individualDetails));
        } else if (party.has(ORGANISATION_DETAILS)) {
            JsonNode organisationDetails = party.get(ORGANISATION_DETAILS);
            String address = organisationDetails.has(ORGANISATION_ADDRESS)
                ? CrimeListHelper.formatDefendantAddress(organisationDetails.get(ORGANISATION_ADDRESS)) : "";

            defendantInfo.setName(
                GeneralHelper.findAndReturnNodeText(organisationDetails, ORGANISATION_NAME)
            );
            defendantInfo.setAddress(address);
            defendantInfo.setOffences(processOffences(organisationDetails));
        }
    }

    private static String formatDefendantName(JsonNode individualDetails) {
        String forename = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_FORENAMES);
        String middleName = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_MIDDLE_NAME);
        String surname = GeneralHelper.findAndReturnNodeText(individualDetails, INDIVIDUAL_SURNAME);

        String forenames = List.of(forename, middleName).stream()
            .filter(n -> !StringUtils.isBlank(n))
            .collect(Collectors.joining(" "));

        return List.of(surname, forenames).stream()
            .filter(n -> !StringUtils.isBlank(n))
            .collect(Collectors.joining(DELIMITER));
    }

    private static String processProsecutor(JsonNode hearing) {
        String prosecutor = getPartyInformant(hearing);
        if (prosecutor.isEmpty()) {
            prosecutor = getPartyProsecutor(hearing);
        }
        return prosecutor;

    }

    private static String getPartyInformant(JsonNode hearing) {
        List<String> informants = new ArrayList<>();
        hearing.get("case").forEach(hearingCase -> {
            if (hearingCase.has(INFORMANT)) {
                JsonNode informantNode = hearingCase.get(INFORMANT);
                informants.add(GeneralHelper.findAndReturnNodeText(informantNode, PROSECUTING_AUTHORITY_REF));
            }
        });
        return informants.stream()
            .distinct()
            .filter(n -> !StringUtils.isBlank(n))
            .collect(Collectors.joining(DELIMITER));
    }

    private static String getPartyProsecutor(JsonNode hearing) {
        List<String> prosecutors = new ArrayList<>();
        if (hearing.has(PARTY)) {
            hearing.get(PARTY).forEach(party -> {
                if (party.has(PARTY_ROLE)
                    && PROSECUTING_AUTHORITY.equals(party.get(PARTY_ROLE).asText())
                    && party.has(ORGANISATION_DETAILS)) {
                    prosecutors.add(GeneralHelper.findAndReturnNodeText(party.get(ORGANISATION_DETAILS),
                                                                        ORGANISATION_NAME));
                }
            });
        }
        return prosecutors.stream()
            .collect(Collectors.joining(DELIMITER));
    }

    private static List<Offence> processOffences(JsonNode detailsNode) {
        List<Offence> offences = new ArrayList<>();

        if (detailsNode.has(OFFENCE)) {
            detailsNode.get(OFFENCE).forEach(o -> {
                String pleaDate = DateHelper.formatTimeStampToBst(
                    GeneralHelper.findAndReturnNodeText(o, PLEA_DATE),
                    Language.ENGLISH, false, false, DATE_FORMAT
                );

                Offence offence = new Offence();
                offence.setOffenceTitle(GeneralHelper.findAndReturnNodeText(o, OFFENCE_TITLE));
                offence.setOffenceSection(GeneralHelper.findAndReturnNodeText(o, OFFENCE_SECTION));
                offence.setOffenceWording(GeneralHelper.findAndReturnNodeText(o, OFFENCE_WORDING));
                offence.setPlea(GeneralHelper.findAndReturnNodeText(o, PLEA));
                offence.setPleaDate(pleaDate);
                offence.setOffenceReportingRestriction(formatReportingRestriction(o));
                offences.add(offence);
            });
        }
        return offences;
    }
}
