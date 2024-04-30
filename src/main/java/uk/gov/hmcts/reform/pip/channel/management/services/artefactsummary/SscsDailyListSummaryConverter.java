package uk.gov.hmcts.reform.pip.channel.management.services.artefactsummary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtHouse;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.CourtRoom;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.Hearing;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.HearingCase;
import uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.sscsdailylist.Sitting;
import uk.gov.hmcts.reform.pip.channel.management.services.helpers.listmanipulation.SscsListHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class SscsDailyListSummaryConverter implements ArtefactSummaryConverter {
    /**
     * parent method - first iterates through json file to build courthouse object list (with nested courtroom,
     * hearing and sitting objects within. Then iterate through those to produce final string output. Utilises a lot
     * of the same methods as the PDF.
     *
     * @param payload - json body.
     * @return String with final summary data.
     * @throws JsonProcessingException - jackson req.
     */
    @Override
    public String convert(JsonNode payload) throws JsonProcessingException {
        StringBuilder output = new StringBuilder(67);
        List<CourtHouse> courtHouseList = jsonParsePayload(payload);
        for (CourtHouse courtHouse : courtHouseList) {
            output.append("\n•")
                .append(courtHouse.getName())
                .append(courtRoomIterator(courtHouse.getListOfCourtRooms()));
        }
        return output.toString();
    }

    private String courtRoomIterator(List<CourtRoom> courtRoomList) {
        StringBuilder output = new StringBuilder();
        for (CourtRoom courtRoom : courtRoomList) {
            for (Sitting sitting : courtRoom.getListOfSittings()) {
                for (Hearing hearing : sitting.getListOfHearings()) {
                    Iterator<HearingCase> caseIterator = hearing.getListOfCases().iterator();
                    while (caseIterator.hasNext()) {
                        HearingCase hearingCase = caseIterator.next();
                        output.append('\n')
                            .append(courtRoom.getName()).append(", Time: ").append(hearingCase.getHearingTime())
                            .append(caseBuilder(hearingCase, sitting));
                    }
                }
            }
        }
        return output.toString();
    }

    private List<CourtHouse> jsonParsePayload(JsonNode payload) throws JsonProcessingException {
        List<CourtHouse> courtHouseList = new ArrayList<>();
        for (JsonNode courtHouse : payload.get("courtLists")) {
            courtHouseList.add(SscsListHelper.courtHouseBuilder(courtHouse));
        }
        return courtHouseList;
    }

    private String caseBuilder(HearingCase hearingCase, Sitting sitting) {
        StringBuilder appellant = new StringBuilder(30);
        appellant.append("\n Appellant: ")
            .append(hearingCase.getAppellant());
        if (StringUtils.isNotEmpty(hearingCase.getAppellantRepresentative())) {
            if (StringUtils.isNotEmpty(hearingCase.getAppellant())) {
                appellant.append(", ");
            }
            appellant.append("Legal Advisor: ")
                .append(hearingCase.getAppellantRepresentative());
        }

        return appellant
            + "\nFTA/Respondent: " + hearingCase.getRespondent()
            + "\nPanel: " + hearingCase.getJudiciary()
            + "\nTribunal type: " + sitting.getChannel();
    }
}
