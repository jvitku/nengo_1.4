package ctu.nengoros.comm.rosBackend.decoders;

import ca.nengo.model.Origin;
import ctu.nengoros.comm.rosBackend.backend.newMessageEvent.MyEventListenerInterface;
import ctu.nengoros.network.node.synchedStart.SyncedUnitInterface;

public interface CommonDecoder  extends SyncedUnitInterface, Origin, MyEventListenerInterface{

}
