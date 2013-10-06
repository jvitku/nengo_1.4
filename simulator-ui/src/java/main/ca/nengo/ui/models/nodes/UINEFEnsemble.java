/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "UINEFEnsemble.java". Description: 
"A UI object for NEFEnsemble
  
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

package ca.nengo.ui.models.nodes;

//import java.util.List;

import ca.nengo.model.Origin;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Termination;
import ca.nengo.model.nef.NEFEnsemble;
import ca.nengo.model.nef.impl.DecodedOrigin;
import ca.nengo.plot.Plotter;
import ca.nengo.ui.configurable.ConfigException;
import ca.nengo.ui.lib.actions.ActionException;
import ca.nengo.ui.lib.actions.ReversableAction;
import ca.nengo.ui.lib.actions.StandardAction;
import ca.nengo.ui.lib.actions.UserCancelledException;
import ca.nengo.ui.lib.util.menus.MenuBuilder;
import ca.nengo.ui.lib.util.menus.PopupMenuBuilder;
import ca.nengo.ui.models.constructors.CDecodedOrigin;
import ca.nengo.ui.models.constructors.CDecodedTermination;
import ca.nengo.ui.models.constructors.ModelFactory;
import ca.nengo.ui.models.nodes.widgets.UIOrigin;
import ca.nengo.ui.models.nodes.widgets.UITermination;
import ca.nengo.ui.models.tooltips.TooltipBuilder;
//import ca.shu.ui.lib.util.UserMessages;

/**
 * A UI object for NEFEnsemble
 * 
 * @author Shu Wu
 */
public class UINEFEnsemble extends UIEnsemble {

	public static final String typeName = "NEFEnsemble";

	public UINEFEnsemble(NEFEnsemble model) {
		super(model);
		init();
	}

	private void init() {
		try {
			if (getModel().getOrigin(NEFEnsemble.X) != null) {
				showOrigin(NEFEnsemble.X);
			}
		} catch (StructuralException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void constructMenu(PopupMenuBuilder menu) {
		super.constructMenu(menu);

		menu.addSection("NEFEnsemble");
		MenuBuilder plotMenu = menu.addSubMenu("Plot");

		plotMenu.addAction(new StandardAction("Constant Rate Responses") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void action() {
				Plotter.plot(getModel());
			}

		});
		Origin[] origins = getModel().getOrigins();

		for (Origin element : origins) {
			if (element instanceof DecodedOrigin) {
				if(getModel().getDimension() > 1)
					plotMenu.addAction(new PlotDecodedOriginMSE(element.getName()));
				else
					plotMenu.addAction(new PlotDecodedOriginDistortion(element.getName()));
			}
		}

		// Decoded termination and origins
//		menu.addAction(new AddDecodedTerminationAction());
//		menu.addAction(new AddDecodedOriginAction());
	}

	@Override
	protected void constructTooltips(TooltipBuilder tooltips) {
		super.constructTooltips(tooltips);
		tooltips.addProperty("# Dimension", "" + getModel().getDimension());

	}

	/**
	 * Adds a decoded termination to the UI and Ensemble Model The UI is used to
	 * configure it
	 * 
	 * @return PTermination created, null if not
	 */
	public UITermination addDecodedTermination() {

		try {
			Termination term = (Termination) ModelFactory.constructModel(this,
					new CDecodedTermination(getModel()));

			UITermination termUI = UITermination.createTerminationUI(this, term);
			showPopupMessage("New decoded TERMINATION added");
			addWidget(termUI);
			return termUI;

		} catch (ConfigException e) {
			e.defaultHandleBehavior();
		}

		return null;
	}

	public UIOrigin addDecodedOrigin() {

		try {

			Origin origin = (Origin) ModelFactory.constructModel(this, new CDecodedOrigin(
					getModel()));
			UIOrigin originUI = UIOrigin.createOriginUI(this, origin);

			addWidget(originUI);
			showPopupMessage("New decoded ORIGIN added");
			setModelBusy(false);

			return originUI;
		} catch (ConfigException e) {
			e.defaultHandleBehavior();
		}

		return null;
	}

	@Override
	public NEFEnsemble getModel() {
		return (NEFEnsemble) super.getModel();
	}

	@Override
	public int getDimensionality() {
		if (getModel() != null) {
			return getModel().getDimension();
		} else {
			return -1;
		}
	}
	
	@Override
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Action for adding a decoded termination
	 * 
	 * @author Shu Wu
	 */
	class AddDecodedTerminationAction extends ReversableAction {

		private static final long serialVersionUID = 1L;

		private UITermination addedTermination;

		public AddDecodedTerminationAction() {
			super("Add decoded termination");
		}

		@Override
		protected void action() throws ActionException {
			UITermination term = addDecodedTermination();
			if (term == null)
				throw new UserCancelledException();
			else
				addedTermination = term;
		}

		@Override
		protected void undo() throws ActionException {
			addedTermination.destroy();

		}

	}

	/**
	 * Action for adding a decoded termination
	 * 
	 * @author Shu Wu
	 */
	class AddDecodedOriginAction extends ReversableAction {

		private static final long serialVersionUID = 1L;

		private UIOrigin addedOrigin;

		public AddDecodedOriginAction() {
			super("Add decoded origin", null, false);
		}

		@Override
		protected void action() throws ActionException {
			UIOrigin origin = addDecodedOrigin();

			if (origin != null) {
				addedOrigin = origin;
			} else {
				throw new UserCancelledException();
			}

		}

		@Override
		protected void undo() throws ActionException {
			addedOrigin.destroy();
		}
	}

	/**
	 * Action for plotting a decoded origin
	 * 
	 * @author Shu Wu
	 */
	class PlotDecodedOriginDistortion extends StandardAction {
		private static final long serialVersionUID = 1L;
		String decodedOriginName;

		public PlotDecodedOriginDistortion(String decodedOriginName) {
			super("Plot distortion: " + decodedOriginName,false);
			this.decodedOriginName = decodedOriginName;
		}

		@Override
		protected void action() throws ActionException {
				Plotter.plot(getModel(), decodedOriginName);
		}
	}

	/**
	 * Action for plotting the mean squared error for a decoded origin of an ensemble with multiple dimensions.
	 * 
	 * @author Steven Leigh
	 */
	class PlotDecodedOriginMSE extends StandardAction {
		private static final long serialVersionUID = 1L;
		String decodedOriginName;

		public PlotDecodedOriginMSE(String decodedOriginName) {
			super("Plot MSE: " + decodedOriginName,false);
			this.decodedOriginName = decodedOriginName;
		}

		@Override
		protected void action() throws ActionException {
			Plotter.plot(getModel(), decodedOriginName);

		}
	}
	
}
