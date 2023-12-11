package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MagistratesStandardList {
    private DefendantInfo defendantInfo;
    private List<CaseSitting> caseSittings;
}
