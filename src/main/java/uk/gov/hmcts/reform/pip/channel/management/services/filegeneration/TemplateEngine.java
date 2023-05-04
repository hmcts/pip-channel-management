package uk.gov.hmcts.reform.pip.channel.management.services.filegeneration;

import org.thymeleaf.context.IContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.channel.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

public final class TemplateEngine {
    private static final SpringTemplateEngine TEMPLATE_ENGINE = new ThymeleafConfiguration().templateEngine();

    private TemplateEngine() {
    }

    static String processTemplate(String listTypeName, IContext context) {
        ListType listType = ListType.valueOf(listTypeName);
        String templateFileName = listType.getParentListType() == null
            ? listTypeName
            : listType.getParentListType().name();
        String template = UPPER_UNDERSCORE.to(LOWER_CAMEL, templateFileName) + ".html";
        return TEMPLATE_ENGINE.process(template, context);
    }
}
