package com.octal.fsm.controller;

import com.octal.fsm.common.ApiResponse;
import com.octal.fsm.common.CommonConstants;
import com.octal.fsm.dto.DocumentDTO;
import com.octal.fsm.service.DocumentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController extends BaseController {

    private static final Logger logger = LogManager.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadDocument(@Valid @RequestBody DocumentDTO.Add addJobDTO, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            documentService.uploadDocument(addJobDTO);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Document Upload Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse> uploadMultipleDocument(@Valid @RequestBody List<DocumentDTO.Add> addJobDTO, HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            documentService.uploadMultipleDocument(addJobDTO);
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Document Upload Successfully.", null, "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse> documentList(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "createdAt") String sortBy,
                                                    @RequestParam(defaultValue = "true") Boolean order,
                                                    @RequestParam("type") String type,
                                                    @RequestParam("typeId") String typeId,
                                                    @RequestParam(defaultValue = "") String uploadedByType,
                                                    @RequestParam(defaultValue = "") String uploadByTypeId,
                                                    @RequestParam(defaultValue = "") String fileType,
                                                    @RequestParam(defaultValue = "") String documentTypeId,

                                                    HttpServletRequest request) {
        try {
            String userName = request.getHeader(CommonConstants.USER_NAME);
            //Todo List Method to get all Job List.
            return new ResponseEntity<>(new ApiResponse(Boolean.TRUE, "Job list successfully", documentService.getListOfDocument(type, typeId, uploadedByType, uploadByTypeId, fileType, page, size, sortBy, order, documentTypeId, userName), "200", HttpStatus.OK), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error creating job: {}", e.getMessage(), e);
            return handleException(e);
        }
    }

}
