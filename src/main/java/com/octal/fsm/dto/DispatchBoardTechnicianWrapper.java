package com.octal.fsm.dto;

import lombok.Data;

import java.util.List;

@Data
public class DispatchBoardTechnicianWrapper {

    private TechnicianDTO.GetDetails technician;
    private List<DispatchBoardDataResponseDTO> tasks;
}
