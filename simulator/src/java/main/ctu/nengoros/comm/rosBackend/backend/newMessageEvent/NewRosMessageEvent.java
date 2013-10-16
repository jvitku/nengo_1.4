package ctu.nengoros.comm.rosBackend.backend.newMessageEvent;

/**
 * This event is fired when a Backend receives a ROS message. 
 * 
 * @author Jaroslav Vitku
 *
 */

public class NewRosMessageEvent extends java.util.EventObject {
    private static final long serialVersionUID = -1227410909088514065L;

    public NewRosMessageEvent(Object source) {
        super(source);
    }
}
