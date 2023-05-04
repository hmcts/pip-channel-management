package uk.gov.hmcts.reform.pip.channel.management.services.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class LocationHelper {
    private static final String VENUE = "venue";
    private static final String VENUE_ADDRESS = "venueAddress";
    private static final String LINE = "line";
    private static final String TOWN = "town";
    private static final String COUNTY = "county";
    private static final String POSTCODE = "postCode";
    private static final String COURT_HOUSE = "courtHouse";

    private LocationHelper() {
        throw new UnsupportedOperationException();
    }

    public static List<String> formatVenueAddress(JsonNode artefact) {
        List<String> address = addAddressLines(artefact);

        if (!GeneralHelper.findAndReturnNodeText(artefact.get(VENUE).get(VENUE_ADDRESS), POSTCODE).isEmpty()) {
            address.add(artefact.get(VENUE).get(VENUE_ADDRESS).get(POSTCODE).asText());
        }
        return address;
    }

    public static List<String> formatFullVenueAddress(JsonNode artefact) {
        List<String> address = addAddressLines(artefact);

        if (!GeneralHelper.findAndReturnNodeText(artefact.get(VENUE).get(VENUE_ADDRESS), TOWN).isEmpty()) {
            address.add(artefact.get(VENUE).get(VENUE_ADDRESS).get(TOWN).asText());
        }

        if (!GeneralHelper.findAndReturnNodeText(artefact.get(VENUE).get(VENUE_ADDRESS), COUNTY).isEmpty()) {
            address.add(artefact.get(VENUE).get(VENUE_ADDRESS).get(COUNTY).asText());
        }

        if (!GeneralHelper.findAndReturnNodeText(artefact.get(VENUE).get(VENUE_ADDRESS), POSTCODE).isEmpty()) {
            address.add(artefact.get(VENUE).get(VENUE_ADDRESS).get(POSTCODE).asText());
        }
        return address;
    }

    public static void formatCourtAddress(JsonNode artefact, String delimiter, boolean addCourtHouseName) {
        artefact.get("courtLists").forEach(courtList -> {
            StringBuilder formattedCourtAddress = new StringBuilder();
            JsonNode courtHouse = courtList.get(COURT_HOUSE);

            if (addCourtHouseName && courtHouse.has("courtHouseName")) {
                formattedCourtAddress
                    .append(courtHouse.get("courtHouseName").asText())
                    .append(delimiter);
            }

            if (courtHouse.has("courtHouseAddress")) {
                JsonNode courtHouseAddress = courtHouse.get("courtHouseAddress");
                GeneralHelper.loopAndFormatString(courtHouseAddress, LINE, formattedCourtAddress, delimiter);
                checkAndFormatAddress(courtHouseAddress, TOWN, formattedCourtAddress, delimiter);
                checkAndFormatAddress(courtHouseAddress, COUNTY, formattedCourtAddress, delimiter);
                checkAndFormatAddress(courtHouseAddress, POSTCODE, formattedCourtAddress, delimiter);
            }

            ((ObjectNode)courtHouse).put("formattedCourtHouseAddress",
                                         StringUtils.stripEnd(formattedCourtAddress.toString(), delimiter));
        });
    }

    private static void checkAndFormatAddress(JsonNode node, String nodeName,
                                              StringBuilder builder, String delimiter) {
        if (!GeneralHelper.findAndReturnNodeText(node, nodeName).isEmpty()) {
            builder
                .append(node.get(nodeName).asText())
                .append(delimiter);
        }
    }

    private static List<String> addAddressLines(JsonNode artefact) {
        List<String> addressLines = new ArrayList<>();
        if (artefact.get(VENUE).has(VENUE_ADDRESS)) {
            JsonNode arrayNode = artefact.get(VENUE).get(VENUE_ADDRESS).get(LINE);
            for (JsonNode jsonNode : arrayNode) {
                if (!jsonNode.asText().isEmpty()) {
                    addressLines.add(jsonNode.asText());
                }
            }
        }
        return addressLines;
    }

    public static void formatRegionName(JsonNode artefact) {
        try {
            ((ObjectNode) artefact).put("regionName",
                                        artefact.get("locationDetails").get("region").get("name").asText());
        } catch (Exception e) {
            ((ObjectNode) artefact).put("regionName", "");
        }
    }

    public static void formatRegionalJoh(JsonNode artefact) {
        StringBuilder formattedJoh = new StringBuilder();
        try {
            artefact.get("locationDetails").get("region").get("regionalJOH").forEach(joh -> {
                if (formattedJoh.length() != 0) {
                    formattedJoh.append(", ");
                }

                formattedJoh.append(GeneralHelper.findAndReturnNodeText(joh, "johKnownAs"));
                formattedJoh.append(' ');
                formattedJoh.append(GeneralHelper.findAndReturnNodeText(joh, "johNameSurname"));
            });

            ((ObjectNode) artefact).put("regionalJoh", formattedJoh.toString());
        } catch (Exception e) {
            ((ObjectNode) artefact).put("regionalJoh", "");
        }
    }

    public static void formattedCourtRoomName(JsonNode courtRoom, JsonNode session, StringBuilder formattedJudiciary) {
        if (StringUtils.isBlank(formattedJudiciary.toString())) {
            formattedJudiciary.append(courtRoom.get("courtRoomName").asText());
        } else {
            formattedJudiciary.insert(0, courtRoom.get("courtRoomName").asText() + ": ");
        }

        ((ObjectNode)session).put("formattedSessionCourtRoom",
                                  GeneralHelper.trimAnyCharacterFromStringEnd(formattedJudiciary.toString()));
    }
}
