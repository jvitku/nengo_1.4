Tracks changes in the original Nengo simulator
======================================

# nengoros-master-v0.0.1

First official verison of Nengoros simulator. 

# nengoros-master-v0.0.2

Adds support of time synchronization with the ROS network:

1. TimeMaster
2. TimeIgnore
3. TimeSlave (experimental)
	
# nengoros-master-v0.0.3

Adds support of passing private parameters to nodes (includes jroscore project) from the commandline or ParameterServer. 
Each node can read own private parameters by means of PrivateRosparam class.

# nengoros-master-v0.0.4

TODO: Add support for multiple terminations of one encoder. This simulates multiple projections to NeuralEnsemble and enables user to connect multiple modules to one input of one NeuralModule. This also enables weighted connections between modules.

structure:

* each encoder has list of its terminations	
* nef_core has method connect(pre,post)

	if(post is MultipleInputEncoder)

	connect(pre,new_WeightedTermination('name'))
