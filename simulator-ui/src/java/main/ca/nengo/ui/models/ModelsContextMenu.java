/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ModelsContextMenu.java". Description: 
"Creates a Popup menu which applies to a collection of models
  
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

package ca.nengo.ui.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JPopupMenu;

import ca.nengo.ui.lib.objects.models.ModelObject;
import ca.nengo.ui.lib.util.menus.AbstractMenuBuilder;
import ca.nengo.ui.lib.util.menus.PopupMenuBuilder;
import ca.nengo.ui.lib.world.WorldObject;

/**
 * Creates a Popup menu which applies to a collection of models
 * 
 * @author Shu Wu
 */
public class ModelsContextMenu {

	/**
	 * @param selectedObjects
	 *            Selected objects which a popup menu is created for
	 * @return Context menu for selected objects
	 */
	public static void constructMenu(PopupMenuBuilder menuBuilder,
			Collection<ModelObject> selectedObjects) {

		new ModelsContextMenu(menuBuilder, selectedObjects);
	}

	private Collection<ModelObject> selectedObjects;

	private HashMap<Class<? extends ModelObject>, LinkedList<ModelObject>> selectionMap = new HashMap<Class<? extends ModelObject>, LinkedList<ModelObject>>();
	private PopupMenuBuilder menuBuilder;

	protected ModelsContextMenu(PopupMenuBuilder menuBuilder, Collection<ModelObject> models) {
		super();
		this.menuBuilder = menuBuilder;
		this.selectedObjects = models;
		init();
	}

	private void init() {
		initSelectionMap();
		constructMenu();
	}

	private synchronized JPopupMenu initSelectionMap() {

		selectionMap.clear();

		/*
		 * sort the selection by class type, so that for each class type a
		 * collection of models are of the same type (homogeneous)
		 */
		for (WorldObject object : selectedObjects) {
			if (object instanceof ModelObject) {
				ModelObject modelUI = (ModelObject) object;

				LinkedList<ModelObject> objects = selectionMap.get(modelUI.getClass());

				if (objects == null) {
					objects = new LinkedList<ModelObject>();
					selectionMap.put(modelUI.getClass(), objects);
				}

				objects.add(modelUI);

			}
		}

		return null;
	}

	protected void constructMenu() {
		for (Class<? extends ModelObject> type : selectionMap.keySet().toArray(new Class[0])) {

			LinkedList<ModelObject> homogeneousModels = selectionMap.get(type);
			String typeName = homogeneousModels.getFirst().getTypeName();

			String typeMenuName = homogeneousModels.size() + " " + typeName + "s";

			AbstractMenuBuilder typeMenu;
			if (menuBuilder == null) {
				typeMenu = new PopupMenuBuilder(typeMenuName);
				menuBuilder = (PopupMenuBuilder) typeMenu;
			} else {
				typeMenu = menuBuilder.addSubMenu(typeMenuName);

			}

			UIModels.constructMenuForModels(typeMenu, type, typeName, homogeneousModels);

		}
	}
}
