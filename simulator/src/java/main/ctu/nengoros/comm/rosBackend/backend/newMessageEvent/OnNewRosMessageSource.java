package ctu.nengoros.comm.rosBackend.backend.newMessageEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ros.internal.message.Message;


public abstract class OnNewRosMessageSource {
	
	// I have a list of my listeners
	private List<MyEventListenerInterface> _listeners  = new ArrayList<MyEventListenerInterface>();

	public synchronized void addEventListener(MyEventListenerInterface listener)	{
		_listeners.add(listener);
	}

	public synchronized void removeEventListener(MyEventListenerInterface listener)	{
		_listeners.remove(listener);
	}

	
	public synchronized void fireOnNewMessage(Message mess){
		Iterator<MyEventListenerInterface> i = _listeners.iterator();

		while(i.hasNext())	
			((MyEventListenerInterface) i.next()).onNewRosMessage(mess);
		
		//System.out.println("firing now..");
	}
}
