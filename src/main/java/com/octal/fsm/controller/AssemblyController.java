package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.dto.AssemblyCreateRequest;
import com.octal.fsm.exceptions.CodeException;
import com.octal.fsm.service.CreateAssemblyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("assembly")
@RestController
public class AssemblyController {

    @Autowired
    private CreateAssemblyService createAssemblyService;

    @PostMapping("/create")
    public ResponseEntity<?> createAssembly(@RequestBody AssemblyCreateRequest.AssemblyComponentAdd request) throws CodeException {
        if (request.getComponents() == null || request.getComponents().isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(Boolean.FALSE, "Assembly must have components.", null, "101", HttpStatus.OK), HttpStatus.OK);
        }
        return createAssemblyService.saveAssemblyQueue(request);
    }

}
