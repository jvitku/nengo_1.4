package ca.nengo.ui.lib.actions;

import ca.nengo.ui.lib.AuxillarySplitPane;

public class SetSplitPaneVisibleAction extends StandardAction {

	private static final long serialVersionUID = 1L;
	private boolean visible;
	private AuxillarySplitPane splitPane;

	public SetSplitPaneVisibleAction(String actionName, AuxillarySplitPane splitPane, boolean visible) {
		super(actionName);
		this.visible = visible;
		this.splitPane = splitPane;
	}

	@Override
	protected void action() throws ActionException {
		splitPane.setAuxVisible(visible);
	}

}