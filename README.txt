This is modification of original Nengo simulator for integration with ROS infrastructure.
For more information about Nengo-ros integration see: http://nengoros.wordpress.com/ .

Author of this fork: Jaroslav Vitku [vitkujar@fel.cvut.cz]
Address of this repository: https://github.com/jvitku/nengo_1.4/

This modification is used in the "nengo-ros" package, which integrates:
-Nengo simulator 	(large scale neural simulations, see: http://nengo.ca/ , original readme renamed to README_origNengo.rst )
-rosjava_core  		(java-based implementation of ROS core, see: http://wiki.ros.org/rosjava )
	-potentially with some neccessary ROS-components

Together, these two packages provide platform-independent tool for simulating hybrid neural systems usable e.g. for robotic applications.

--------------------------- PREREQUISITES

If you want to install entire Nengo-ros multiproject, see http://nengoros.wordpress.com/ or https://github.com/jvitku/nengoros

If you want to install just the modified Nengo simulator, see the next chapter.

--------------------------- INSTALLATION

Original build system is moved from maven to Gragle. 

Standalone installation is handled by the script: ./installStandalone .

This generates eclipse projects into the folders simulator and simulator-ui.

In eclipse, import (simulator and simulator-ui) projects from nengo folder, clean/build. 

Launch Nengo by calling main method in class: NengoLauncher or by runnign scripts either simulator-ui/nengo or simulator-ui/nengo-cl.  
