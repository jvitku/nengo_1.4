/**
 * This package contains neural modules, extensions of ca.nengo.model.Node class,
 * NeuralModule is similar to nef.SimpleNode, each neural module uses one modem:
 * A modem directly connects Nengo with ROS, does this by converting the Nengo data 
 * (arrays of floats (in continuous values mode)) to ROS messages (e.g. Float32MultiArray type).
 * A modem converts data between ROS messages and Nengo real-valued data: 
 * -received ROS messages converts into array of floats on origins of NeuralModule
 * -data received onto terminations of NeuralModule are converted into ROS messages and sent
 *  
 * Each modem belongs to its own NodeGroup (each group can contain 0 or 1 modem).
 * Each group usually contains some ROS nodes of other type than Modem. Modem communicates with 
 * these ROS nodes via the ROS network.  
 * 
 * So the standard process of defining neural module (e.g. in Jython script) looks as follows:
 * 	-define the node group
 * 	-add ROS nodes (native, java..) to the group
 * 	-add modem
 * 
 * 	-define the neural module with: modem=group.getModem
 *  -add coders/decoders to the modem (define how convert data between Nengo and ROS)
 *  
 *  -end the Jython configuration script
 *  -start the simulation
 * 	 
 */
/**
 * @author Jaroslav Vitku
 *
 */
package nengoros.modules;

