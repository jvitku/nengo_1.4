package ctu.nengoros.modules.impl;

import ctu.nengoros.network.node.synchedStart.impl.SyncedStart;

/**
 * Indicates whether the {@link ctu.nengoros.modules.NeuralModule} has successfully started.
 * If a module owns a {@link ctu.nengoros.comm.nodeFactory.modem.Modem} (or potentially ROS 
 * nodes) these should be added as childs of this Manager. 
 * 
 * @author Jaroslav Vitku
 */
public class SyncedStartManager extends SyncedStart{

	private String name;
	
	public SyncedStartManager(String fullName){
		this.name = fullName;
	}
	
	@Override
	public String getFullName() { return this.name; }

	@Override
	public void setFullName(String name) { this.name =name; }

}
