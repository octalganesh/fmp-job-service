package com.octal.fsm.listener.events;

import com.octal.fsm.entities.InventoryRequest;
import org.springframework.context.ApplicationEvent;

public class InventoryRequestNotificationEvent extends ApplicationEvent {

    private final InventoryRequest inventoryRequest;
    private final Long tenantId;
    private final String loggedInser;

    public InventoryRequestNotificationEvent(InventoryRequest inventoryRequest, Long tenantId, String loggedInuser) {
        super(inventoryRequest);
        this.inventoryRequest = inventoryRequest;
        this.tenantId = tenantId;
        this.loggedInser = loggedInuser;
    }

    public InventoryRequest getInventoryRequest() {
        return inventoryRequest;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getLoggedInuser() {
        return loggedInser;
    }
}
