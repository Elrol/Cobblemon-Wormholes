package dev.elrol.wormholes.events;

import dev.elrol.wormholes.data.PlacedCellData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface CellPlacedCallback {

    Event<CellPlacedCallback> EVENT = EventFactory.createArrayBacked(CellPlacedCallback.class, (listeners) -> (cellData) -> {
        for(CellPlacedCallback listener : listeners) {
            listener.placed(cellData);
        }
    });

    void placed(PlacedCellData placedData);

}
