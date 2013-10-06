/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "CDecodedOrigin.java". Description:
"Swing component for selecting a origin

  @author Shu Wu"

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

package ca.nengo.ui.models.constructors;

//import java.util.List;

import javax.swing.JComboBox;

import ca.nengo.math.Function;
import ca.nengo.model.Origin;
import ca.nengo.model.StructuralException;
import ca.nengo.model.nef.NEFEnsemble;
import ca.nengo.ui.configurable.ConfigException;
import ca.nengo.ui.configurable.ConfigResult;
import ca.nengo.ui.configurable.ConfigSchema;
import ca.nengo.ui.configurable.ConfigSchemaImpl;
import ca.nengo.ui.configurable.Property;
import ca.nengo.ui.configurable.PropertyInputPanel;
import ca.nengo.ui.configurable.descriptors.PFunctionArray;
import ca.nengo.ui.configurable.descriptors.PString;
import ca.nengo.ui.models.nodes.widgets.UIDecodedOrigin;

public class CDecodedOrigin extends ProjectionConstructor {
	private static final Property pName = new PString("Name");

	private NEFEnsemble enfEnsembleParent;

	private Property pFunctions;

	//private Property pNodeOrigin;

	public CDecodedOrigin(NEFEnsemble enfEnsembleParent) {
		super();
		this.enfEnsembleParent = enfEnsembleParent;
		pName.setDescription("Name of the origin");

	}

	@Override
	public ConfigSchema getSchema() {
		pFunctions = new PFunctionArray("Functions", enfEnsembleParent.getDimension());
		pFunctions.setDescription("The function to compute");

		// Find common nodes
		//Node[] nodes = enfEnsembleParent.getNodes();
		//List<String> commonNodes = AbstractEnsemble.findCommon1DOrigins(nodes);

		//pNodeOrigin = new OriginSelector("Node Origin Name", commonNodes.toArray(new String[0]));

		Property[] zProperties = { pName, pFunctions};//, pNodeOrigin };
		return new ConfigSchemaImpl(zProperties);

	}

	public String getTypeName() {
		return UIDecodedOrigin.typeName;
	}

	@Override
	protected boolean IsNameAvailable(String name) {
		try {
			return enfEnsembleParent.getOrigin(name) == null;
		} catch (StructuralException e) {
			return false;
		}
	}

	@Override
	protected Object createModel(ConfigResult configuredProperties, String uniqueName) throws ConfigException {
		Origin origin = null;

		try {
			origin = enfEnsembleParent.addDecodedOrigin(uniqueName, (Function[]) configuredProperties
					.getValue(pFunctions), "AXON");//(String) configuredProperties.getValue(pNodeOrigin));

		} catch (StructuralException e) {
			throw new ConfigException(e.getMessage());
		}

		return origin;
	}


}

/**
 * Swing component for selecting a origin
 *
 * @author Shu Wu
 */
class OriginInputPanel extends PropertyInputPanel {

	/**
	 * Selector of the Node Origin
	 */
	private JComboBox comboBox;

	String[] origins;

	public OriginInputPanel(OriginSelector property, String[] originNames) {
		super(property);
		this.origins = originNames;

		comboBox = new JComboBox(origins);
		add(comboBox);
	}

	@Override
	public String getValue() {
		return (String) comboBox.getSelectedItem();
	}

	@Override
	public boolean isValueSet() {
		return true;
	}

	@Override
	public void setValue(Object value) {
		if (value != null && value instanceof String) {
			for (int i = 0; i < comboBox.getItemCount(); i++) {
				String item = (String) comboBox.getItemAt(i);

				if (item.compareTo((String) value) == 0) {
					comboBox.setSelectedIndex(i);
					return;
				}

			}
		}
	}

}

/**
 * Selects an available Node Origin
 *
 * @author Shu Wu
 */
class OriginSelector extends Property {

	private static final long serialVersionUID = 1L;
	String[] origins;

	public OriginSelector(String name, String description, String[] originNames) {
		super(name, description);
		this.origins = originNames;
	}

	@Override
	protected OriginInputPanel createInputPanel() {
		return new OriginInputPanel(this, origins);
	}

	@Override
	public Class<String> getTypeClass() {
		return String.class;
	}

	@Override
	public String getTypeName() {
		return "Node Origin Selector";
	}

}
