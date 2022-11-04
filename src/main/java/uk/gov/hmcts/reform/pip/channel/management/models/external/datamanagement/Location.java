package uk.gov.hmcts.reform.pip.channel.management.models.external.datamanagement;

import lombok.Data;

import java.util.List;

@Data
public class Location {

    private Integer locationId;

    private String name;

    private String welshName;

    private List<String> jurisdiction;

    private List<String> region;

}
