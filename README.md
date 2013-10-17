Nengo+ROS = NengoRos
========================

This is modification of original Nengo simulator for integration with ROS infrastructure.
For more information about Nengo-ros integration see: http://nengoros.wordpress.com/ .

Author: Jaroslav Vitku [vitkujar@fel.cvut.cz]
Address of this repository: https://github.com/jvitku/nengo_1.4/

This modification is used in the "nengo-ros" package, which integrates (all credits to authors) :
* Nengo simulator 	(large scale neural simulations, see: http://nengo.ca/ , original readme renamed to README_origNengo.rst )
* rosjava_core  		(java-based implementation of ROS core, see: http://wiki.ros.org/rosjava )
	-potentially with some neccessary ROS-components

Together, these two packages provide platform-independent tool for simulating hybrid neural systems usable e.g. for robotic applications.


Prerequisites:
-------------

If you want to install the entire Nengo-ros multiproject (recommended), see http://nengoros.wordpress.com/ or clone the repository on https://github.com/jvitku/nengoros

If you want to install just the modified Nengoros simulator (with precompiled rosjava libraries), see the next chapter.

Installation:
-------------

Original build system is moved from maven to Gradle. 

Alternative to the complete one, standalone installation can be made and is handled by the script: ./installStandalone .

* This generates eclipse projects into the folders simulator and simulator-ui.

* In eclipse, import (simulator and simulator-ui) projects from nengo folder, clean/build. 

* Launch Nengo by calling main method in class: NengoLauncher or by runnign scripts either simulator-ui/nengo or simulator-ui/nengo-cl.  


Notes:
-------------

In order to distinguish between standalone and rosjava-based project settings, build.gradle and settings.gradle files under nengo and nengo/simulator-ui are appended with either *.rosjava or *.standalone, please, edit these files.