package ctu.nengoros.comm.nodeFactory;

import java.util.ArrayList;

import ctu.nengoros.comm.rosutils.Mess;
import ctu.nengoros.comm.rosutils.utilNode.time.impl.DefaultTimeMaster;
import ctu.nengoros.comm.rosutils.utilNode.time.impl.DefaultTimeSlave;

/**
 * Stores names (each name includes namespace too) of
 * ROS nodes that should be currently running in the ROS
 * network and provides unique names (or namespaces) for 
 * groups of new nodes.
 * 
 * @author Jaroslav Vitku
 *
 */
public class NameProvider{

	private final String[] ignoredNames = new String[]{DefaultTimeMaster.name, DefaultTimeSlave.name};
	
	private ArrayList<String> nameList;
	// note: this separator "_" does not work at all, look at screens/nameconflict.png
	public final String separator = "__";
	

	public NameProvider(){
		nameList = new ArrayList<String>(10);
	}
	/**
	 * If you do not allow pushing yourself (this group) into own namespace.
	 * @param namespace if you have own namespace, pass it here, if not, pass null or empty str.  
	 * @param prefferedNames list of node names that I would like to use (e.g. Modem,RL)
	 * @param group name of group (e.g. null if none or name of SmartNeuron)
	 * @return list of modified names that are unique in a current ROS net 
	 */
	public String[] modifyNames(String namespace, String[] prefferedNames, String group){
		String groupMod=group+separator;	// modified name of group
		
		if(repeat(prefferedNames)){
			return null;
		}
		// do not have preferred namespace?
		if(namespace == null || namespace.length() ==0){
			if(allUnique(group, prefferedNames, this.separator)){
				return saveNameModification(group, prefferedNames, true);
			}
			int poc = 1;
			while(!allUnique(groupMod+poc, prefferedNames, this.separator)){
				poc++;
			}
			return saveNameModification(groupMod+poc, prefferedNames, true);
		// preferred namespace provided, so work with modified names
		}else{
			if(allUnique(namespace, group, prefferedNames)){
				return saveNameModification(namespace, group, prefferedNames, true);
			}
			int poc = 1;
			while(!allUnique(namespace, groupMod+poc, prefferedNames)){
				poc++;
			}
			return saveNameModification(namespace, groupMod+poc, prefferedNames, true);
		}
	}

	/**
	 * If nodes allow pushing them into own namespace, use this.
	 * @param names list of names, for each node 
	 * @param group name of node logic-group (e.g. name of SmartNeuron)
	 * @return namespace that corresponds to group name, while all resulting nodes are unique
	 */
	public String findNamespace(String[] names, String group){
		if(repeat(names)){
			return null;
		}
		if(allUnique(group, names, "/")){
			saveNamespaceModification(group, names, false);
			return group;
		}
		int poc = 1;
		String groupMod=group+separator;	// modified name of the group
		while(!allUnique(groupMod+poc, names, "/")){
			poc++;
		}
		saveNamespaceModification(groupMod+poc, names, false);
		return groupMod+poc;
	}

	/**
	 * Shutdown nodes, that is: remove them from the map. 
	 * @param names full names (we assume that these nodes do not allow namespacing)
	 */
	public void shutDown(String[] names){
		for(String n:names)
			removeName(n);
	}

	public void shutDown(String name){ removeName(name); }

	public void shutDown(String name, String namespace){
		removeName(namespace+separator+name);
	}

	public void shutDown(String[] names, String namesapce){
		for(int i=0; i<names.length; i++)
			removeName(namesapce+separator+names[i]);
	}

	public int numOfRunningNodes(){ return nameList.size(); }

	public String[] namesOfRunningNodes(){ 
		String[] out = new String[nameList.size()];
		for(int i=0; i<nameList.size(); i++)
			out[i] = nameList.get(i);
		return out;
	}

	private void removeName(String n){
		
		if(this.isIgnored(n))
			return;
		
		if(!nameList.contains(n.toLowerCase())){
			System.err.println("NameProvider: you want to remove " +
					"this node: "+n+ " but it is not in the list! " +
							"list size: "+this.numOfRunningNodes()+"  "
					+" names of nodes: "+Mess.toAr(this.namesOfRunningNodes()));
		}else{
			nameList.remove(n.toLowerCase());
		}
	}

	private boolean isIgnored(String n){
		for(int i=0; i<ignoredNames.length; i++){
			if(ignoredNames[i].equalsIgnoreCase(n))
				return true;
		}
		return false;
	}
	
	private String[] saveNamespaceModification(String group, String[] names, 
			boolean modifyNames){
		String tmp;
		String[] modified = new String[names.length];
		for(int i=0; i<names.length; i++){
			tmp = group+"/"+names[i];
			nameList.add(tmp.toLowerCase());
			modified[i]=tmp;
		}
		if(modifyNames)
			return modified;
		return names;
	}
	
	private String[] saveNameModification(String group, String[] names, 
			boolean modifyNames){
		String tmp;
		String[] modified = new String[names.length];
		for(int i=0; i<names.length; i++){
			tmp = group+separator+names[i];
			nameList.add(tmp.toLowerCase());
			modified[i]=tmp;
		}
		if(modifyNames)
			return modified;
		return names;
	}

	/**
	 * here it is not nice, we need to save modification of names, but we do not
	 * want to save the namespace used by the name provider 
	 * @param namespace namespace to run the nodes (part of unique name)
	 * @param group group that these nodes belong to
	 * @param names list of node names
	 * @param modifyNames whether to modify the names (yes, but do not save the namespace)
	 * @return
	 */
	private String[] saveNameModification(String namespace, String group, 
			String[] names, boolean modifyNames){
		String tmp;
		String[] modified = new String[names.length];
		String[] modifiedII = new String[names.length];
		for(int i=0; i<names.length; i++){
			tmp = namespace+"/"+group+separator+names[i];
			modifiedII[i] = group+separator+names[i];
			nameList.add(tmp.toLowerCase());
			modified[i]=tmp;
		}
		if(modifyNames)
			return modifiedII;
		return names;
	}
	
	private boolean allUnique(final String ns, final String group, final String[] names){
		for(String s:names){
			if(nameList.contains((ns+"/"+group+separator+s).toLowerCase()))
				return false;
		}
		return true;
	}
	
	private boolean allUnique(final String group, final String[] names, String sep){
		for(String s:names){
			if(nameList.contains((group+sep+s).toLowerCase()))
				return false;
		}
		return true;
	}

	/**
	 * You should not provide two identical names in one group..
	 * @param names
	 * @return
	 */
	private boolean repeat(String[] names){
		for(int i=0; i<names.length; i++){
			for(int j=0; j<names.length; j++){
				if( j!= i && names[i].equalsIgnoreCase(names[j]) ){
					System.err.println("NameProvider: please specify group " +
							"with non-duplicate names, will not run a " +
							"group with these names: "+Mess.toAr(names));
					return true;
				}
			}
		}
		return false;
	}
}
