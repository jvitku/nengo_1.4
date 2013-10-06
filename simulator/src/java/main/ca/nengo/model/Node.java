/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "Node.java". Description:
"A part of a Network that can be run independently (eg a Neuron)"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on 7-Jun-2006
 */
package ca.nengo.model;

import java.io.Serializable;
import java.util.HashMap;

import ca.nengo.util.ScriptGenException;
import ca.nengo.util.VisiblyMutable;

/**
 * A part of a Network that can be run independently (eg a Neuron). Normally
 * a source of Origins and/or Terminations.
 *
 * @author Bryan Tripp
 */
public interface Node extends Serializable, Resettable, SimulationMode.ModeConfigurable, VisiblyMutable, Cloneable {

	/**
	 * @return Name of Node (must be unique in a Network)
	 */
	public String getName();

	/**
	 * @param name The new name
	 * @throws StructuralException if name already exists?
	 */
	public void setName(String name) throws StructuralException;

	/**
	 * Runs the Node (including all its components), updating internal state and outputs as needed.
	 * Runs should be short (eg 1ms), because inputs can not be changed during a run, and outputs
	 * will only be communicated to other Nodes after a run.
	 *
	 * @param startTime simulation time at which running starts (s)
	 * @param endTime simulation time at which running ends (s)
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime) throws SimulationException;

	/**
	 * @return Sets of ouput channels (eg spiking outputs, gap junctional outputs, etc.)
	 */
	public Origin[] getOrigins();

	/**
	 * @param name Name of an Origin on this Node
	 * @return The named Origin if it exists
	 * @throws StructuralException if the named Origin does not exist
	 */
	public Origin getOrigin(String name) throws StructuralException;

	/**
	 * @return Sets of input channels (these have the same dimension as corresponding Origins
	 * 		to which they are connected).
	 */
	public Termination[] getTerminations();

	/**
	 * @param name Name of a Termination onto this Node
	 * @return The named Termination if it exists
	 * @throws StructuralException if the named Termination does not exist
	 */
	public Termination getTermination(String name) throws StructuralException;
	
	
	public Node[] getChildren();
	
    /**
     * @param scriptData Map of class parent and prefix data for generating python script
     * @return Python script for generating the node
     * @throws ScriptGenException if the node cannot be generated in script
     */
	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException;

	/**
	 * @return User-specified documentation for the Node, if any
	 */
	public String getDocumentation();

	/**
	 * @param text New user-specified documentation for the Node
	 */
	public void setDocumentation(String text);
	
	/**
	 * @return An independent copy of the Node
	 * @throws CloneNotSupportedException if clone can't be made
	 */
	public Node clone() throws CloneNotSupportedException;
	

	/**
	 * This method tells the Node (network) that is being deleted from Nengo, 
	 * Node can inform its children (child Nodes) that are being deleted, 
	 * these nodes can e.g. stop their ROS backends, their ROS nodes etc..	 
	 * 
	 * Note that it is expected that node returns from this method when 
	 * everything that should be done before his deletion is done. 
	 * 
	 * Also, Node does not have to react to this (as in pure Nengo version).
	 * 
	 * ///my @author Jaroslav Vitku
	 */
	public void notifyAboutDeletion();	
}
