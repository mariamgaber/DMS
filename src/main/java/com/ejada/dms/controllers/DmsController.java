package com.ejada.dms.controllers;

import com.ejada.commons.errors.exceptions.ErrorCodeException;
import com.ejada.commons.logs.annotations.FailureQueueDescriptor;
import com.ejada.dms.services.CmisDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.chemistry.opencmis.client.api.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.ejada.dms.constants.FailureQueues.DOWNLOAD_DOCUMENT_FAILURE_QUEUE;

@RestController
@RequestMapping("/dms")
@RequiredArgsConstructor
@Validated
@Slf4j
public class DmsController {
     private final CmisDocumentService cmisDocumentService;
    @PostMapping("/create")
    @FailureQueueDescriptor(failureQueue = DOWNLOAD_DOCUMENT_FAILURE_QUEUE)
    public ResponseEntity<String> createDocument() {
        try {
            String path = "Keycloak Material.pdf";
            Map<String, String> properties = new HashMap<>();
            String documentId = cmisDocumentService.createDocumentFromPath(path, properties);
            return ResponseEntity.ok(documentId);
        } catch (ErrorCodeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/content")
    @FailureQueueDescriptor(failureQueue = DOWNLOAD_DOCUMENT_FAILURE_QUEUE)
    public ResponseEntity<String> create() {
        try {
            String c = "SGVsbG8sIFdvcmxkIQ==";
            Map<String, String> properties = new HashMap<>();
            String documentId = cmisDocumentService.createDocumentFromContent(properties, c.getBytes());
            return ResponseEntity.ok(documentId);
        } catch (ErrorCodeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/retrieve/{docId}")
    @FailureQueueDescriptor(failureQueue = DOWNLOAD_DOCUMENT_FAILURE_QUEUE)
    public ResponseEntity<Document> retrieveDocument(@PathVariable String docId, @RequestParam(required = false) String downloadPath) {
        try {
            Document document = cmisDocumentService.retrieveDocumentById(docId, downloadPath);
            return ResponseEntity.ok(document);
        } catch (ErrorCodeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @PutMapping("/update/{docId}")
    @FailureQueueDescriptor(failureQueue = DOWNLOAD_DOCUMENT_FAILURE_QUEUE)
    public ResponseEntity<String> updateDocument(@PathVariable String docId, @RequestParam String path, @RequestBody Map<String, String> properties) {
        try {
            String updatedDocId = cmisDocumentService.updateDocumentById(path, docId, properties);
            return ResponseEntity.ok(updatedDocId);
        } catch (ErrorCodeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
