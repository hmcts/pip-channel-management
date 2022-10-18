package uk.gov.hmcts.reform.pip.channel.management.controllers;


import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.pip.channel.management.authentication.roles.IsAdmin;
import uk.gov.hmcts.reform.pip.channel.management.services.FileHandlerService;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@Api(tags = "Controller to handle CRUD operations of Excel/PDF publications")
@RequestMapping("/file")
@IsAdmin
public class FileHandlerController {

    @Autowired
    FileHandlerService fileHandlerService;

    @PostMapping("/{artefactId}")
    public ResponseEntity<Void> generateFiles(@PathVariable UUID artefactId) throws IOException {
        fileHandlerService.generateFiles(artefactId);
        return ResponseEntity.accepted().build();
    }

    // Artefact ID -> generate/store excel and pdf (response code for done)
    // Artefact ID -> Get summary string
    // Artefact ID -> Get PDF/Excel
}
