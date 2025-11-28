package com.octal.fsm.service;

import com.octal.fsm.dto.HTMLFormDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;
import org.springframework.stereotype.Component;

@Component
public interface HTMLFormPageService {

    PageItem<HTMLFormDTO.Details> getAllFormsPage(PageRequest.List listRequest, Long tenantId);

    HTMLFormDTO.Details getById(String id) throws CodeException;

    Boolean changeStatus(String id, Long tenantId) throws CodeException;

}
