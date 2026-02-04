package com.octal.fsm.listener;

import com.octal.fsm.clients.QuickBookClientService;
import com.octal.fsm.listener.events.InventoryAssembly;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableAsync
public class InventoryAssemblyListener {

    private final QuickBookClientService quickBookClientService;

    @Async("inventoryAssembly")
    @EventListener
    public void handleInventoryAssemblySync(InventoryAssembly event) {
        try {
            quickBookClientService.saveInventoryAssembly(event.getAssemblyComponentAdd());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

