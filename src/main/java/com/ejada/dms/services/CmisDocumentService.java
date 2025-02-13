package com.ejada.dms.services;


import com.ejada.commons.errors.exceptions.ErrorCodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CmisDocumentService {
    private final CMISUtils cmisUtils;
    private final String DOCUMENT_CLASS_SYMBOLIC_NAME = "Document";
    //root folder path of dms
    private final  String folderPath = "/SmartContract";

    // path: The file path of the document content to be uploaded.
    // properties: A map containing metadata properties for the document.
    public  String createDocumentFromPath(String path, Map<String, String> properties) {
        System.out.println("**************Enter create document **************");
        try{
            //To ensure only reading Document
            System.out.println("**************Reading File**************"+ cmisUtils.getFileContentStream(path, "txt"));
        properties.put(PropertyIds.OBJECT_TYPE_ID, DOCUMENT_CLASS_SYMBOLIC_NAME);
        //Retrieves the parent folder object using the folder path
            System.out.println("**************Generate parent folder **************");

            Folder root = (Folder) cmisUtils.getSession().getRootFolder();
            System.out.println("**************Root************** " + root);
            ItemIterable<CmisObject> children =  cmisUtils.getSession().getRootFolder().getChildren();
            // Get an iterator
            Iterator<CmisObject> iterator = children.iterator();
            // Iterate using the iterator
            while (iterator.hasNext()) {
                CmisObject child = iterator.next();
                System.out.println("**************Child************** " + child);

            }
        Folder parent = (Folder) cmisUtils.getSession().getObjectByPath(folderPath);
            System.out.println("**************Done generating parent folder **************");
        // Creates the document in the folder by uploading the file content as a ContentStream.
        // VersioningState can be MAJOR or MINOR
        //TODo: we need to convert base64 format to content stream..
            System.out.println("**************Create Document **************");
        Document newDoc = parent.createDocument(properties, cmisUtils.getFileContentStream(path, "txt"), VersioningState.MAJOR);
            System.out.println("**************Done Creating Document**************");
        //Returns the ID of the newly created document
        return newDoc.getId();
        } catch (CmisBaseException e) {
            System.out.println("**************Error1 Creating Document **************");
            log.error("Error creating document from path '{}': {}", path, e.getMessage());
            throw new ErrorCodeException("CMIS_FAILED_CREATING_DOCUMENT");
        } catch (Exception e) {
            System.out.println("**************Error2 Creating  Document**************");
            log.error("Unexpected error while creating document: {}", e.getMessage());
            throw new ErrorCodeException("UNEXPECTED_ERROR_CREATING_DOCUMENT");
        }
    }


    // Purpose: Fetches a document by its ID, writes its content to a specified by its path, and prints its properties in XML format.
    //Inputs:
    // docId: The ID of the document to retrieve.
    //downloadPath: The path where the document's content should be saved. If null, no content is downloaded.
    public  Document retrieveDocumentById(String docId, String downloadPath) {
        //Retrieves the document using its ID.
        try {
            Document doc = (Document) cmisUtils.getSession().getObject(cmisUtils.getSession().createObjectId(docId));
            //TODO : we need to convert to base64, no need to save
            //If downloadPath is specified, writes the document's content to a file using cmisUtils.writeDocContentToFile.
            if (downloadPath != null) cmisUtils.writeDocContentToFile(doc, downloadPath);
            //Converts the document's properties to an XML string using cmisUtils.getXMLProperties and prints it.
            List<Property<?>> props = doc.getProperties();
            List<String> docProps = new ArrayList<String>();
            docProps.add(PropertyIds.NAME);
            System.out.println(cmisUtils.getXMLProperties(props, docProps));
            //Returns the document object.
            return doc;
        } catch (CmisBaseException e) {
            log.error("Error retrieving document with ID '{}': {}", docId, e.getMessage());
            throw new ErrorCodeException("FAILED_RETRIEVING_DOCUMENT");
        } catch (Exception e) {
            log.error("Unexpected error while retrieving document: {}", e.getMessage());
            throw new ErrorCodeException("UNEXPECTED_ERROR_RETRIEVING_DOCUMENT");
        }
    }
    // Purpose: Updates the content and/or properties of a document by its ID.
    // Inputs:
    // path: The file path of the new content to update.
    //docId: The ID of the document to update.
    //properties: A map containing updated metadata properties.
    public  String updateDocumentById(String path, String docId, Map<String, String> properties) {
        // Retrieves the document by its ID.
        Document doc = (Document) cmisUtils.getSession().getObject(cmisUtils.getSession().createObjectId(docId));
        //Checks out the document to create a private working copy (PWC).
        Document pwc = (Document) cmisUtils.getSession().getObject(doc.checkOut());
        try {
            //Updates the PWC with the new content and properties and checks it in as a new version.
            // true: Indicates the document is being checked in as the next version.
            // properties: A Map<String, String> containing the updated metadata or properties for the document.
            // cmisUtils.getFileContentStream(path, "txt"): A helper method from cmisUtils that creates a ContentStream object from the file located at path. This represents the new content to be stored in the document.
            // "major version": A comment or label indicating this update creates a major version.
            pwc.checkIn(true, properties, cmisUtils.getFileContentStream(path, "txt"), "major version");
        } catch (CmisBaseException e) {
            e.printStackTrace();
            System.out.println("checkIn failed, trying to cancel the checkout");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //If check-in fails, cancels the checkout and updates the properties only
        pwc.cancelCheckOut();
        pwc.updateProperties(properties);
        //Returns the ID of the updated document.
        return pwc.getId();
    }

    //TODO: Read the following to Understand the idea of CheckOut and CheckIn:
    // Checkout: First, the document is checked out to create a PWC, allowing updates to be made safely without affecting the main document in the repository.
    // Update and Check-In:
    // The PWC is updated with new content (from the provided file path) and metadata (from the properties map).'
    // The changes are saved to the repository as a new version of the document.


    // retrieve documents ids by query
    public  List<String> retrieveDocumentsByQuery(String documentClassSymbolicName, String where) {
        List<String> docsIds = new ArrayList<String>();
        ObjectType type = cmisUtils.getSession().getTypeDefinition(documentClassSymbolicName);
        PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
        String objectIdQueryName = objectIdPropDef.getQueryName();
        String queryString = "SELECT" + objectIdQueryName + " FROM " + type.getQueryName() + " " + where;
        //execute query
        ItemIterable<QueryResult> results = cmisUtils.getSession().query(queryString, false);
        for (QueryResult qResult : results) {
            String objectid = qResult.getPropertyValueByQueryName(objectIdQueryName);
            docsIds.add(cmisUtils.getSession().createObjectId(objectid).getId());
        }
        return docsIds;

    }
}
