package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.Language;
import uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public final class LanguageResourceHelper {
    private static final String PATH_TO_LANGUAGES = "templates/languages/";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LanguageResourceHelper() {
    }

    /**
     * Get language resources for a particular list type.
     *
     * @param listType The list type.
     * @param language The language.
     * @return all language resources for the list.
     * @throws IOException thrown if error in getting language.
     */
    public static Map<String, Object> getLanguageResources(ListType listType, Language language) throws IOException {
        Map<String, Object> languageResources = readResourcesFromPath(listType, language);
        if (listType.getParentListType() != null) {
            Map<String, Object> parentLanguageResources = readResourcesFromPath(listType.getParentListType(), language);
            parentLanguageResources.putAll(languageResources);
            return parentLanguageResources;
        }
        return languageResources;
    }

    private static Map<String, Object> readResourcesFromPath(ListType listType, Language language) throws IOException {
        String path;
        String languageString = GeneralHelper.listTypeToCamelCase(listType);
        if (language.equals(Language.ENGLISH)) {
            path = PATH_TO_LANGUAGES + "en/" + languageString + ".json";
        } else {
            path = PATH_TO_LANGUAGES + "cy/" + languageString + ".json";
        }
        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream(path)) {
            return OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }
}
