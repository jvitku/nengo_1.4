package ca.nengo.ui.lib.actions;

import java.awt.geom.Point2D;

import ca.nengo.ui.lib.misc.WorldLayout;
import ca.nengo.ui.lib.world.World;
import ca.nengo.ui.lib.world.WorldObject;

public abstract class LayoutAction extends ReversableAction {
	private static final long serialVersionUID = 1L;

	private WorldLayout savedLayout;

	private World world;

	public LayoutAction(World world, String description, String actionName) {
		super(description, actionName);
		this.world = world;
	}

	@Override
	protected void action() throws ActionException {
		savedLayout = new WorldLayout("", world, false);
		applyLayout();
	}

	protected abstract void applyLayout();

	protected void restoreNodePositions() {

		for (WorldObject node : world.getGround().getChildren()) {
			Point2D savedPosition = savedLayout.getPosition(node);
			if (savedPosition != null) {
				node.setOffset(savedPosition);
			}
		}
	}

	@Override
	protected void undo() throws ActionException {
		restoreNodePositions();
	}

}