package com.octal.fsm.service;

import com.octal.fsm.dto.DocumentDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;

import java.util.List;

public interface DocumentService {

    void uploadDocument(DocumentDTO.Add addJobDTO) throws CodeException;

    PageItem<DocumentDTO.ListResponse> getListOfDocument(String type, String typeId, String uploadedByType, String uploadByTypeId, String fileType, int page, int size, String sortBy, Boolean order, String documentTypeId, String loggedInUserEmail) throws CodeException;

    void uploadMultipleDocument(List<DocumentDTO.Add> list) throws CodeException;

    void uploadMultipleDocumentForCSR(List<DocumentDTO.Add> list) throws CodeException;
}
