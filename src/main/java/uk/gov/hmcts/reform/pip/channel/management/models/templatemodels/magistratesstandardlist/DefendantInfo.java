package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.magistratesstandardlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefendantInfo {
    private String dob;
    private String age;
    private String address;
    private String plea;
    private String pleaDate;
}
