/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "CallChainCompletor.java". Description: 
"A CommandCompletor that suggests completions based on Python variable names and 
  methods/fields of Python objects.
     
  @author Bryan Tripp"

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
 * Created on 9-Nov-07
 */
package ca.nengo.ui.script;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.python.core.PyClass;
//import org.python.core.PyJavaClass;
//import org.python.core.PyJavaInstance;
import org.python.core.PyJavaType;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyObjectDerived;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;

import ca.nengo.config.JavaSourceParser;

/**
 * A CommandCompletor that suggests completions based on Python variable names and 
 * methods/fields of Python objects.
 *    
 * @author Bryan Tripp
 */
public class CallChainCompletor extends CommandCompletor {
	
	private PythonInterpreter myInterpreter;
	private List<String> myDocumentation;
	
	/**
	 * @param interpreter The Python interpreter from which variables are gleaned  
	 */
	public CallChainCompletor(PythonInterpreter interpreter) {
		myInterpreter = interpreter;
	}
	
	/**
	 * Rebuilds the completion options list from a "base" call chain.
	 *  
	 * @param callChain A partial call chain, eg "x.getY().get", from which we would extract
	 * 		the base call chain "x.getY()". (In this case the new options list might include 
	 * 		"x.getY().toString()", "x.getY().wait()", etc.)
	 */
	public void setBase(String callChain) {
		List<String> options = null;
		
		boolean endsWithBracket = callChain.endsWith("(");
		if (endsWithBracket) callChain = callChain.substring(0, callChain.length()-1);
		
		PyObject pc = getKnownClass(callChain);
		
		if (callChain.lastIndexOf('.') > 0) { //the root variable is specified
			String base = callChain.substring(0, callChain.lastIndexOf('.'));
			options = getMembers(base);
		} else if (endsWithBracket && pc != null) {
			options = getConstructors(pc);
		} else if (endsWithBracket) { 
			//looks like an unrecognized full class name
			options = new ArrayList<String>(0);
		} else {
			options = getVariables();
		}
		getOptions().clear();
		getOptions().addAll(options);
		
		resetIndex();
	}
	
	/**
	 * @return Documentation for the current completion item if available, otherwise null
	 */
	public String getDocumentation() {
		String result = null;
		int index = getIndex();
		if (myDocumentation != null && index >= 0 && index < myDocumentation.size()) {
			result = myDocumentation.get(index);
			if (result != null && result.length() == 0) result = null;
			if (result != null && !result.contains("<html>")) result = "<html>" + result + "</html>";
			if (result != null) result = result.replaceAll("\n", "<br>").replaceAll("<\\\\p><br>", "<\\p>"); //TODO: allow for whitespace before BR
		}
		return result;
	}
	
	public List<String> getConstructors(PyObject pc) {
		List<String> result = new ArrayList<String>(10);
		myDocumentation = new ArrayList<String>(10);
		
		if (pc instanceof PyJavaType) {
			String className = ((PyJavaType) pc).toString().split("'")[1];
			try {
				Class<?> c = Class.forName(className);
				Constructor<?>[] constructors = c.getConstructors();
				for (int i = 0; i < constructors.length; i++) {
					int mods = constructors[i].getModifiers();
					if (Modifier.isPublic(mods)) {
						StringBuffer buf = new StringBuffer(c.getSimpleName());
						buf.append("(");
						String[] names = JavaSourceParser.getArgNames(constructors[i]);
						Class<?>[] types = constructors[i].getParameterTypes();
						for (int j = 0; j < types.length; j++) {
							buf.append(types[j].getSimpleName());
							buf.append(" ");
							buf.append(names[j]);
							if (j < types.length - 1) buf.append(", ");
						}
						buf.append(")");
						result.add(buf.toString());	
						
						myDocumentation.add(JavaSourceParser.getDocs(constructors[i]));						
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			//do nothing for Python classes
		}
		
		return result;
	}
	
	private PyObject getKnownClass(String callChain) {
		PyObject result = null;
		
		PyStringMap map = (PyStringMap) myInterpreter.getLocals();
		PyList keys = (PyList) map.keys();
		PyObject iter = keys.__iter__();

		for (PyObject item; (item = iter.__iternext__()) != null && result == null; ) {
			if (item.toString().equals(callChain) && (map.get(item) instanceof PyJavaType || map.get(item) instanceof PyClass)) {
				result = (PyObject) map.get(item);
			}
		}
		
		return result;
	}
	
	/**
	 * @return List of variable names known to the interpreter. 
	 */
	public List<String> getVariables() {
		PyStringMap map = (PyStringMap) myInterpreter.getLocals();
		PyList keys = (PyList) map.keys();
		PyObject iter = keys.__iter__();

		List<String> result = new ArrayList<String>(50);
		myDocumentation = new ArrayList<String>(50);
		for (PyObject item; (item = iter.__iternext__()) != null; ) {
			result.add(item.toString());
			
//			PyObject po = map.get(item);
//			if (po instanceof PyClass) {
//				myDocumentation.add(getClassDocs(((PyClass) po).__name__));
//			} else if (po instanceof PyInstance) {
//				PyClass pc = ((PyInstance) po).instclass;
//				if (pc instanceof PyClass) {
//					myDocumentation.add(getClassDocs(((PyClass) pc).__name__));					
//				} else {
//					myDocumentation.add("");					
//				}
//			} else {
//				myDocumentation.add("");
//			}
		}
		
		return result;
	}
	
//	private static String getClassDocs(String className) {
//		String result = null;
//		try {
//			Class<?> c = Class.forName(className);
//			result = JavaSourceParser.getDocs(c);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		return (result == null) ? "" : result;
//	}
	
	/**
	 * @param base A variable name in the interpreter. For variables that wrap 
	 * 		Java objects, this arg can consist of a call chain, eg x.getY().getZ()
	 * 		For native python variables, the return type of a call isn't known, 
	 * 		so this method can't return anything given a call chain.   
	 * @return A list of completion options including methods (with args) 
	 * 		and fields on the named variable.  
	 */
	public List<String> getMembers(String base) {
		PyStringMap map = (PyStringMap) myInterpreter.getLocals();

		StringTokenizer varTok = new StringTokenizer(base, ".", false);
		String rootVariable = varTok.nextToken();
		
		PyObject po = getObject(map, rootVariable);
		
		List<String> result = new ArrayList<String>(20);
		myDocumentation = new ArrayList<String>(20);
		if (po instanceof PyJavaType) {
			String className = ((PyJavaType)po).toString().split("'")[1];
			
			try {
				Class<?> c = Class.forName(className);
				
				Field[] fields = c.getFields();
				for (int i = 0; i < fields.length; i++) {
					int mods = fields[i].getModifiers();
					if (Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
						result.add(base + "." + fields[i].getName());
						myDocumentation.add("");						
					}					
				}
				
				Method[] methods = c.getMethods();
				for (int i = 0; i < methods.length; i++) {
					int mods = methods[i].getModifiers();
					if (Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
						result.add(getMethodSignature(base, methods[i]));
//						myDocumentation.add(JavaSourceParser.getDocs(methods[i]));								
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else if (po instanceof PyObjectDerived) {
			
			String rootClassName = po.toString().split("@")[0];
			
			try {
				Class<?> c = getReturnClass(rootClassName, base);

				Field[] fields = c.getFields();
				for (int i = 0; i < fields.length; i++) {
					int mods = fields[i].getModifiers();
					if (Modifier.isPublic(mods)) {
						result.add(base + "." + fields[i].getName());
						myDocumentation.add("");						
					}
				}
				
				Method[] methods = c.getMethods();
				
				for (int i = 0; i < methods.length; i++) {
					int mods = methods[i].getModifiers();
					if (Modifier.isPublic(mods)) {
						result.add(getMethodSignature(base, methods[i]));
//						myDocumentation.add(JavaSourceParser.getDocs(methods[i]));						
					}
				}
			} catch (ClassNotFoundException e) {
				System.out.println("Class not found: " + rootClassName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} 
		else if (po != null) { //then it's a python object (?)
			if ( !varTok.hasMoreTokens() ) {
				PyObject iter = po.__dir__().__iter__();
				
				for (PyString item; (item = (PyString) iter.__iternext__()) != null; ) {
					PyObject attr = po.__findattr__(item);
					StringBuffer buf = new StringBuffer(base + ".");					
					buf.append(item.toString());
					
					if (attr.isCallable()) {						
						buf.append("(");
//						try {
//							String[] varnames = ((PyTableCode) ((PyFunction) ((PyMethod) attr).im_func).func_code).co_varnames;
//							for (int i = 1; i < varnames.length; i++) { //skip 'self' arg
//								buf.append(varnames[i]);
//								if (i < varnames.length - 1) buf.append(", ");
//							}
//						} catch (ClassCastException e) {
//							e.printStackTrace();
//						}
						buf.append(")");
					}
					result.add(buf.toString());
				}
			}
		}
		
		return result;		
	}
	
	private static String getMethodSignature(String base, Method m) {
		StringBuffer buf = new StringBuffer(base + ".");
		buf.append(m.getName());
		buf.append('(');
		Class<?>[] paramTypes = m.getParameterTypes();
		String[] paramNames = JavaSourceParser.getArgNames(m);
		for (int j = 0; j < paramTypes.length; j++) {
			buf.append(paramTypes[j].getSimpleName());
			buf.append(" ");
			buf.append(paramNames[j]);
			if (j < paramTypes.length - 1) buf.append(", ");
		}
		buf.append(')');
		return buf.toString();
	}
	
	private PyObject getObject(PyStringMap map, String key) {
		PyObject result = null;
		
		PyList keys = (PyList) map.keys();
		PyObject iter = keys.__iter__();
		for (PyObject item; (item = iter.__iternext__()) != null && result == null; ) {
			if (item.toString().equals(key)) {
				result = map.get(item);
			}
		}
		
		return result;
	}
	
	/**
	 * We only match methods by name and number of parameters, not by parameter type. 
	 *   
	 * @param rootClassName Name of class of root variable
	 * @param callChain Full call chain, eg x.getY().getZ(foo)
	 * @return The class of the final return value
	 */
	private Class<?> getReturnClass(String rootClassName, String callChain) throws Exception {
		Class<?> result = Class.forName(rootClassName);				

		StringTokenizer varTok = new StringTokenizer(callChain, ".", false);
		varTok.nextToken(); //skip root	
		
		while (varTok.hasMoreTokens()) {
			String member = varTok.nextToken();
			if (member.contains("(")) { //a method
				StringTokenizer memberTok = new StringTokenizer(member, "(, )", false);						
				int numParams = memberTok.countTokens() - 1;
				member = memberTok.nextToken();
				
				Method[] methods = result.getMethods();
				Method firstMatchingMethod = null;
				for (int i = 0; i < methods.length && firstMatchingMethod == null; i++) {
					if (methods[i].getName().equals(member) && methods[i].getGenericParameterTypes().length == numParams) {
						firstMatchingMethod = methods[i];
					}
				}
				
				if (firstMatchingMethod == null) {
					throw new NoSuchMethodException("No method named " + member + " with " + numParams + " parameters");
				}
				
				result = firstMatchingMethod.getReturnType();
			} else { //a field
				result = result.getField(member).getClass();
			}
		}

		return result;
	}
	
}
