package resender.mt;

import java.util.List;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import std_msgs.Int32;
import std_msgs.MultiArrayDimension;
import std_msgs.MultiArrayLayout;

public class IntArrReceiver extends AbstractNodeMain {


	private final java.lang.String ann2ros = "ann2ros";
	protected final java.lang.String ros2ann = "ros2ann";
	
	@Override
	public GraphName getDefaultNodeName() { return GraphName.of("IntArrReceiver"); }

	
	  @Override
	  public void onStart(ConnectedNode connectedNode) {
	    final Log log = connectedNode.getLog();
	    
	    Subscriber<std_msgs.Int32MultiArray> subscriber = connectedNode.newSubscriber(ros2ann, std_msgs.Int32MultiArray._TYPE);
	    
	    MessageListener<std_msgs.Int32MultiArray> ml = new MessageListener<std_msgs.Int32MultiArray>() {
		      @Override
		      public void onNewMessage(std_msgs.Int32MultiArray message) {
		    	  
		    	  MultiArrayLayout l = message.getLayout();
		    	  List <MultiArrayDimension> d =  l.getDim();
		    	  
		    	  
		    	  
		    	  for(int i=0; i<d.size(); i++){
		    		  System.out.println("dim no: "+i+
		    				  " label: "+d.get(i).getLabel()+
		    				  " size: "+d.get(i).getSize()+
		    				  " stride: "+d.get(i).getStride());
		    		  
		    	  }
		    	  
		    	  
		    	  int[] x = message.getData();
		    	  
		    	  for(int i=0;i<x.length; i++)
		    		  System.out.print(" "+x[i]);
		    	  System.out.println("\n---");
		    	  
		        log.info("YYY I heard: \"" + message.getData() + "\"");
		      }
		};
	    
	    subscriber.addMessageListener(ml);
		
	    
	   log.info("HEY! RECEIVER ready now!");
	}
	
}
