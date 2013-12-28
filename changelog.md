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

-----------------TODO
==========================
# Multiple Inputs

* Test combination of multiple Termination values into one

* Implement API for simple connecting multiple Terminations

* Implement support for nef.connect

* Make more tests


# Simulator Core and General

* Better implementation of waiting for `SynchedUnit`s?

* Better handling of Exceptions

* Deprecated ParameterHandler, is launched twice? Delete the deprecated one, use the one in RosUtils?


# ROS integration into Nengo

* Enable sending/receiving entire `RealOutput` value (multiple values) over the ROS network 

	* Sending is done in the `ctu.nengoros.modules.impl.DefaultNeuraoModule.runAllEncoders()`, and therefore in the `Encoder.run()`
	* Receiving values from the ROS network is done asynchronously (events) in the Decoder class.
	
* Enable encoding, sending and decoding `SpikeOutput` to/from the ROS messages. 

	* This requires ROS nodes with support of `SpikeOutput`


# ROS support 

* Encoding/Decoding multidimensional messages. Test it.