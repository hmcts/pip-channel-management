package uk.gov.hmcts.reform.pip.channel.management.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.channel.management.models.PublicationFileSizes;
import uk.gov.hmcts.reform.pip.channel.management.services.PublicationManagementService;
import uk.gov.hmcts.reform.pip.model.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.model.publication.FileType;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Controller to handle the generation and retrieval of Excel/PDF publications and artefact summaries")
@RequestMapping("/publication")
@ApiResponse(responseCode = "401", description = "Invalid access credential")
@ApiResponse(responseCode = "403", description = "User has not been authorized")
@IsAdmin
@SecurityRequirement(name = "bearerAuth")
public class PublicationManagementController {

    private static final String NOT_FOUND_DESCRIPTION = "No artefact found";
    private final PublicationManagementService publicationManagementService;

    private static final String OK_CODE = "200";
    private static final String ACCEPTED_CODE = "202";
    private static final String NO_CONTENT_CODE = "204";
    private static final String NOT_FOUND_CODE = "404";
    private static final String PAYLOAD_TOO_LARGE_CODE = "413";
    private static final String INTERNAL_ERROR_CODE = "500";

    @Autowired
    public PublicationManagementController(PublicationManagementService publicationManagementService) {
        this.publicationManagementService = publicationManagementService;
    }

    @ApiResponse(responseCode = OK_CODE, description = "Artefact summary string returned")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @ApiResponse(responseCode = INTERNAL_ERROR_CODE, description = "Cannot process the artefact")
    @Operation(summary = "Takes in an artefact ID and returns an artefact summary")
    @GetMapping("/summary/{artefactId}")
    public ResponseEntity<String> generateArtefactSummary(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.generateArtefactSummary(artefactId));
    }

    @ApiResponse(responseCode = ACCEPTED_CODE, description = "Request to generate files accepted")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @ApiResponse(responseCode = INTERNAL_ERROR_CODE, description = "Cannot process the artefact")
    @Operation(summary = "Takes in an artefact ID and generates/stores a publication file")
    @PostMapping("/{artefactId}")
    @Deprecated
    public ResponseEntity<Void> generateFiles(@PathVariable UUID artefactId) {
        publicationManagementService.generateFiles(artefactId, null);
        return ResponseEntity.accepted().build();
    }

    @ApiResponse(responseCode = ACCEPTED_CODE, description = "Request to generate files accepted")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @ApiResponse(responseCode = INTERNAL_ERROR_CODE, description = "Cannot process the artefact")
    @Operation(summary = "Generates/stores a publication file for a given artefact ID and payload")
    @PostMapping("/v2/{artefactId}")
    public ResponseEntity<Void> generateFiles(@PathVariable UUID artefactId, @RequestBody String payload) {
        publicationManagementService.generateFiles(artefactId, payload);
        return ResponseEntity.accepted().build();
    }

    @ApiResponse(responseCode = OK_CODE, description = "PDF or Excel file for an artefact returned successfully")
    @ApiResponse(responseCode = NOT_FOUND_CODE, description = NOT_FOUND_DESCRIPTION)
    @ApiResponse(responseCode = PAYLOAD_TOO_LARGE_CODE, description = "File size too large")
    @Operation(summary = "Takes in an artefact ID and returns the stored PDF or Excel file ")
    @GetMapping("/v2/{artefactId}")
    public ResponseEntity<String> getFile(
        @PathVariable UUID artefactId,
        @RequestHeader(value = "x-user-id", required = false) String userId,
        @RequestHeader(value = "x-system", required = false) boolean system,
        @RequestHeader(name = "x-file-type") FileType fileType,
        @RequestHeader(name = "x-additional-pdf", defaultValue = "false") boolean additionalPdf,
        @RequestParam(name = "maxFileSize", required = false) Integer maxFileSize) {
        return ResponseEntity.ok(
            publicationManagementService.getStoredPublication(
                artefactId, fileType, maxFileSize, userId, system, additionalPdf
            )
        );
    }

    @ApiResponse(responseCode = NO_CONTENT_CODE, description = "The files have been deleted")
    @Operation(summary = "Takes in an artefact ID and delete all publication files associated with the artefact")
    @DeleteMapping("/v2/{artefactId}")
    public ResponseEntity<Void> deleteFiles(
        @PathVariable UUID artefactId,
        @RequestHeader(name = "x-list-type") ListType listType,
        @RequestHeader(name = "x-language") Language language
    ) {
        publicationManagementService.deleteFiles(artefactId, listType, language);
        return ResponseEntity.noContent().build();
    }

    @ApiResponse(responseCode = OK_CODE, description = "PDF or Excel file for an artefact exists")
    @Operation(summary = "Checks if any publication file exists for the artefact")
    @GetMapping("/{artefactId}/exists")
    public ResponseEntity<Boolean> fileExists(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.fileExists(artefactId));
    }

    @ApiResponse(responseCode = OK_CODE, description = "PDF or Excel file for an artefact exists")
    @Operation(summary = "Returns the publication file sizes from Azure blob storage")
    @GetMapping("/{artefactId}/sizes")
    public ResponseEntity<PublicationFileSizes> getFileSizes(@PathVariable UUID artefactId) {
        return ResponseEntity.ok(publicationManagementService.getFileSizes(artefactId));
    }
}
