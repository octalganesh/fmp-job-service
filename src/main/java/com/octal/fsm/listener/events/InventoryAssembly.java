package com.octal.fsm.listener.events;

import com.octal.fsm.dto.AssemblyCreateRequest;
import com.octal.fsm.entities.Appointment;
import org.springframework.context.ApplicationEvent;


public class InventoryAssembly extends ApplicationEvent {

    private final AssemblyCreateRequest.AssemblyComponentAdd assemblyComponentAdd;

    public InventoryAssembly(Object source, AssemblyCreateRequest.AssemblyComponentAdd assemblyComponentAdd) {
        super(source);
        this.assemblyComponentAdd = assemblyComponentAdd;
    }
    public AssemblyCreateRequest.AssemblyComponentAdd getAssemblyComponentAdd() {
        return assemblyComponentAdd;
    }
}
