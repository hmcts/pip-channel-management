package uk.gov.hmcts.reform.pip.channel.management.models.templatemodels.opapresslist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpaDefendantInfo {
    private String name = "";
    private String dob = "";
    private String age = "";
    private String address = "";
    private String postcode = "";
    private String prosecutor = "";
    private List<Offence> offences;
}
