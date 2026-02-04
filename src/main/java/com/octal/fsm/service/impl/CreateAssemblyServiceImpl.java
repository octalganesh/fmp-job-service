package com.octal.fsm.service.impl;

import com.octal.fsm.clients.QuickBookClientService;
import com.octal.fsm.dto.ApiResponse;
import com.octal.fsm.dto.AssemblyCreateRequest;
import com.octal.fsm.entities.CreateAssemblyComponent;
import com.octal.fsm.entities.CreateAssemblyQueue;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.exceptions.ErrorCode;
import com.octal.fsm.listener.events.InventoryAssembly;
import com.octal.fsm.repositories.CreateAssemblyQueueRepository;
import com.octal.fsm.service.CreateAssemblyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CreateAssemblyServiceImpl implements CreateAssemblyService {

    @Autowired
    private CreateAssemblyQueueRepository repository;

    @Autowired
    private QuickBookClientService quickBookClientService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ResponseEntity<?> saveAssemblyQueue(AssemblyCreateRequest.AssemblyComponentAdd request) throws CodeException {
        try{
            if (repository.existsByAssemblyName(request.getName())) {
                throw new CodeException("Assembly with name "+request.getName()+" already exist.", ErrorCode.COMMON);
            }
            CreateAssemblyQueue queue = new CreateAssemblyQueue();
            queue.setAssemblyName(request.getName());
            queue.setIncomeAccount(request.getIncomeAccount());
            queue.setCogsAccount(request.getCogsAccount());
            queue.setAssetAccount(request.getAssetAccount());
            queue.setSalesPrice(request.getSalesPrice());
            queue.setSyncStatus("QUEUED");
            queue.setActiveToken(null);
            for (AssemblyCreateRequest.AssemblyComponentDto dto : request.getComponents()) {
                CreateAssemblyComponent component = new CreateAssemblyComponent();
                component.setItemName(dto.getItemName());
                component.setQuantity(dto.getQuantity());
                component.setAssemblyQueue(queue);
                queue.getComponents().add(component);
            }
            CreateAssemblyQueue save = repository.save(queue);
            request.setRequestId(save.getUuid());
            applicationEventPublisher.publishEvent(new InventoryAssembly(this, request));
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.TRUE, "Assembly data created.", save.getUuid(), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new com.octal.fsm.common.ApiResponse(Boolean.FALSE, e.getMessage(), null, "101", HttpStatus.OK), HttpStatus.OK);
        }
    }
}
