package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.CaseHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DataManipulation;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers.PartyRoleHelper;

import java.io.IOException;
import java.util.Map;

/**
 * FileConverter class for the IAC daily list to generate the PDF.
 */
public class IacDailyListFileConverter implements FileConverter {

    @Override
    public String convert(JsonNode artefact, Map<String, String> metadata,
                          Map<String, Object> languageResources) throws IOException {
        Context context = new Context();

        Language language = Language.valueOf(metadata.get("language"));
        calculateListData(artefact, language);

        context.setVariable("i18n", languageResources);
        context.setVariable("provenance", metadata.get("provenance"));
        context.setVariable("artefact", artefact);
        context.setVariable("contentDate", metadata.get("contentDate"));

        context.setVariable("locationName", metadata.get("locationName"));
        String publicationDate = artefact.get("document").get("publicationDate").asText();
        context.setVariable("publicationDate", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               false, false));
        context.setVariable("publicationTime", DateHelper.formatTimeStampToBst(publicationDate, language,
                                                                               true, false));

        context.setVariable("telephone", artefact.get("venue").get("venueContact").get("venueTelephone").asText());
        context.setVariable("email", artefact.get("venue").get("venueContact").get("venueEmail").asText());

        SpringTemplateEngine templateEngine = new ThymeleafConfiguration().templateEngine();
        return templateEngine.process("iacDailyList.html", context);
    }

    /**
     * This method calculates the list data for the artefact.
     * @param artefact List data to calculate.
     * @param language The language for the list type.
     */
    private void calculateListData(JsonNode artefact, Language language) {
        artefact.get("courtLists").forEach(courtList -> {

            ((ObjectNode) courtList).put(
                "isBailList",
                "bail list".equalsIgnoreCase(courtList.get("courtListName")
                                                 .asText())
            );

            courtList.get("courtHouse").get("courtRoom").forEach(
                courtRoom -> courtRoom.get("session").forEach(session -> {
                    String formattedJoh = DataManipulation.findAndManipulateJudiciaryForCop(session);
                    ((ObjectNode) session).put("formattedJudiciary", formattedJoh);

                    session.get("sittings").forEach(sitting -> {
                        String sittingStart = DateHelper.timeStampToBstTime(
                            sitting.get("sittingStart").asText(), "h:mma");

                        ((ObjectNode) sitting).put("formattedStart", sittingStart);

                        DataManipulation.findAndConcatenateHearingPlatform(sitting, session);

                        sitting.get("hearing").forEach(hearing -> {
                            PartyRoleHelper.findAndManipulatePartyInformation(hearing, language, false);
                            hearing.get("case").forEach(CaseHelper::formatLinkedCases);
                        });
                    });
                })
            );
        });
    }
}
