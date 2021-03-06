Nengo+ROS = NengoRos
========================

This is modification of original Nengo simulator for integration with ROS infrastructure.
For more information about Nengo-ros integration see: http://nengoros.wordpress.com/ .

Author: Jaroslav Vitku [vitkujar@fel.cvut.cz]
Address of this repository: https://github.com/jvitku/nengo_1.4/

This modification is used in the "nengo-ros" package, which integrates (all credits to authors) :

* [Nengo simulator](http://nengo.ca/) - large scale neural simulations (original readme renamed to `README_origNengo.rst`)
* [Rosjava_core](http://wiki.ros.org/rosjava) - java-based implementation of ROS core

	-and potentially with some necessary ROS-components (e.g. [messages](http://wiki.ros.org/std_msgs)) included

Together, these two packages provide platform-independent tool for simulating hybrid neural systems usable e.g. for robotic applications.


Prerequisites:
-------------

If you want to install the entire Nengo-ros multiproject (**recommended**), see http://nengoros.wordpress.com/ or clone the repository on https://github.com/jvitku/nengoros

If you want to install just the modified Nengoros simulator (with precompiled rosjava libraries), see the next chapter.

Installation:
-------------

**To obtain the recommended complete installation, see [installation instructions](http://nengoros.wordpress.com/installation/).**

### Not recommended installation:

In order to install just standalone version of Nengoros, you can use the script `./installStandalone`.

* This generates eclipse projects into the folders simulator and simulator-ui.

* In Eclipse, import (simulator and simulator-ui) projects from nengo folder, run: `clean & build` in Eclipse. 

* Launch Nengo by calling main method in class: `NengoLauncher` or by running scripts either `simulator-ui/nengo` or `simulator-ui/nengo-cl`. 


Changelog
--------------

###nengoros-master-v0.0.4
* Added support for the [my modification](https://github.com/jvitku/vivae) of the [Vivae](http://cig.felk.cvut.cz/projects/robo/) simulator. More precisely: the old version of Vivae support was removed from the Nengoros and placed into the `vivae/vivaeplugin` project.

###nengoros-master-v0.0.2

* Added three possibilities how to sync time between Nengo and ROS nodes: TimeMaster, TimeIgnore and TimeSlave. These are used in the `ca.nengo.util.impl.NodeThreadPool.step()`. 

* Added demos representing time synchronization in the project demonodes/basic, the corresponding python scripts are located under `nr-demo/basic/time*`

###nengoros-master-v0.0.1

* The first stable version. Version is mainly taken from my older repositories on bitbucket.org. 

* Includes demos on rosjava, ROS nodes and native process.

* Communication with ROS nodes is synchronous or asynchronous



Technical Notes:
-------------

###Gradle tests

The project `nengo/simulator` has the src folder set to: `src/java/test/ctu` to ensure that all Nengo tests are omitted, in order to run also Nengo tests, set this folder to `src/java/test`.


###Other

1. Note that the standalone version contains only pre-compiled rosjava_core jar files and no additional projects, so the limitations are:

	* Does not include any demos (or those included may not work correctly).
	* User cannot define [custom messages](http://wiki.ros.org/ROS/Tutorials/DefiningCustomMessages) (interfaces for messages are generated during rosjava_core compilation)


2. Compared to Nengo, the build system is changed from Maven to Gradle (which is used by rosjava_core). 

3. In order to distinguish between standalone and rosjava-based project settings, build.gradle and settings.gradle files under nengo and nengo/simulator-ui are appended with either *.rosjava or *.standalone, please, edit these files.
