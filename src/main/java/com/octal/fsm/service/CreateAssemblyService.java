package com.octal.fsm.service;

import com.octal.fsm.dto.AssemblyCreateRequest;
import com.octal.fsm.exceptions.CodeException;
import org.springframework.http.ResponseEntity;

public interface CreateAssemblyService {

    ResponseEntity<?> saveAssemblyQueue(AssemblyCreateRequest.AssemblyComponentAdd req) throws CodeException;
}
