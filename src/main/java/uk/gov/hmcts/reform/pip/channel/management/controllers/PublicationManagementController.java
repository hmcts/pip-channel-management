package uk.gov.hmcts.reform.pip.channel.management.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.channel.management.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.channel.management.models.FileType;
import uk.gov.hmcts.reform.pip.channel.management.services.PublicationManagementService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@Api(tags = "Controller to handle the generation and retrieval of Excel/PDF publications and artefact summaries")
@RequestMapping("/publication")
@IsAdmin
public class PublicationManagementController {

    private static final String UNAUTHORIZED_DESCRIPTION = "User has not been authorized";
    private static final String NOT_FOUND_DESCRIPTION = "No artefact found";
    private final PublicationManagementService publicationManagementService;

    @Autowired
    public PublicationManagementController(PublicationManagementService publicationManagementService) {
        this.publicationManagementService = publicationManagementService;
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Artefact summary string returned"),
        @ApiResponse(code = 404, message = NOT_FOUND_DESCRIPTION),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(code = 500, message = "Cannot process the artefact")
    })
    @ApiOperation("Takes in an artefact ID and returns an artefact summary")
    @GetMapping("/summary/{artefactId}")
    public ResponseEntity<String> generateArtefactSummary(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.generateArtefactSummary(artefactId));
    }

    @ApiResponses({
        @ApiResponse(code = 202, message = "Request to generate files accepted"),
        @ApiResponse(code = 404, message = NOT_FOUND_DESCRIPTION),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(code = 500, message = "Cannot process the artefact")
    })
    @ApiOperation("Takes in an artefact ID and generates/stores a publication file")
    @PostMapping("/{artefactId}")
    public ResponseEntity<Void> generateFiles(@PathVariable UUID artefactId) {
        publicationManagementService.generateFiles(artefactId);
        return ResponseEntity.accepted().build();
    }

    @ApiResponses({
        @ApiResponse(code = 200, message = "Map<FileType, byte[]> returned for each file for an artefact"),
        @ApiResponse(code = 404, message = NOT_FOUND_DESCRIPTION),
        @ApiResponse(code = 403, message = UNAUTHORIZED_DESCRIPTION)
    })
    @ApiOperation("Takes in an artefact ID and returns a map of stored files")
    @GetMapping("/{artefactId}")
    public ResponseEntity<Map<FileType, byte[]>> getFiles(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.getStoredPublications(artefactId));
    }
}
