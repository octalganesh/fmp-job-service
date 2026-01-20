package com.octal.fsm.service;

import com.octal.fsm.dto.InventoryPartDTO;
import com.octal.fsm.dto.JobDTO;
import com.octal.fsm.dto.PageItem;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.models.request.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InventoryPartService {

     void saveInventoryData(List<InventoryPartDTO.Add> addList);

    PageItem<InventoryPartDTO.Response> getAllInventoryList(PageRequest.List pageRequest, Long tenantId, boolean isSuperAdmin) throws CodeException;
}
