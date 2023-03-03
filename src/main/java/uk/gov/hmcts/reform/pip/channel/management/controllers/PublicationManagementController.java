package uk.gov.hmcts.reform.pip.channel.management.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.channel.management.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.channel.management.models.FileType;
import uk.gov.hmcts.reform.pip.channel.management.services.PublicationManagementService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Controller to handle the generation and retrieval of Excel/PDF publications and artefact summaries")
@RequestMapping("/publication")
@IsAdmin
public class PublicationManagementController {

    private static final String UNAUTHORIZED_DESCRIPTION = "User has not been authorized";
    private static final String NOT_FOUND_DESCRIPTION = "No artefact found";
    private final PublicationManagementService publicationManagementService;

    private static final String OK_CODE = "200";
    private static final String NOT_FOUND_CODE = "404";
    private static final String NO_AUTH_CODE = "403";
    private static final String INTERNAL_ERROR_CODE = "500";

    @Autowired
    public PublicationManagementController(PublicationManagementService publicationManagementService) {
        this.publicationManagementService = publicationManagementService;
    }

    @ApiResponses({
        @ApiResponse(responseCode = OK_CODE, description = "Artefact summary string returned"),
        @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
        @ApiResponse(responseCode = NO_AUTH_CODE, description = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(responseCode = INTERNAL_ERROR_CODE, description = "Cannot process the artefact")
    })
    @Operation(summary = "Takes in an artefact ID and returns an artefact summary")
    @GetMapping("/summary/{artefactId}")
    public ResponseEntity<String> generateArtefactSummary(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.generateArtefactSummary(artefactId));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Request to generate files accepted"),
        @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
        @ApiResponse(responseCode = NO_AUTH_CODE, description = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(responseCode = INTERNAL_ERROR_CODE, description = "Cannot process the artefact")
    })
    @Operation(summary = "Takes in an artefact ID and generates/stores a publication file")
    @PostMapping("/{artefactId}")
    public ResponseEntity<Void> generateFiles(@PathVariable UUID artefactId) {
        publicationManagementService.generateFiles(artefactId);
        return ResponseEntity.accepted().build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = OK_CODE, description =
            "Map<FileType, byte[]> returned for each file for an artefact"),
        @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION),
        @ApiResponse(responseCode = NO_AUTH_CODE, description = UNAUTHORIZED_DESCRIPTION),
        @ApiResponse(responseCode = "401", description = "User not authorised to access requested data.")
    })
    @Operation(summary = "Takes in an artefact ID and returns a map of stored files")
    @GetMapping("/{artefactId}")
    public ResponseEntity<Map<FileType, byte[]>> getFiles(
        @PathVariable UUID artefactId,
        @RequestHeader(value = "x-user-id", required = false) String userId,
        @RequestHeader(value = "x-system", required = false) boolean system) {
        return ResponseEntity.ok(publicationManagementService.getStoredPublications(artefactId,
                                                                                    userId, system));
    }
}
