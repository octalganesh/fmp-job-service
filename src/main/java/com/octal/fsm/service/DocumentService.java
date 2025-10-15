package com.octal.fsm.service;

import com.octal.fsm.dto.DocumentDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;

public interface DocumentService {

    void uploadDocument(DocumentDTO.Add addJobDTO) throws CodeException;

    PageItem<DocumentDTO.ListResponse> getListOfDocument(String type, String typeId, String uploadedByType, String uploadByTypeId, String fileType, int page, int size, String sortBy, Boolean order,String loggedInUserEmail) throws CodeException;
}
