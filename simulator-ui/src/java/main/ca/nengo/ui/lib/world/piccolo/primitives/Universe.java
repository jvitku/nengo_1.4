package ca.nengo.ui.lib.world.piccolo.primitives;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import ca.nengo.ui.lib.Style.NengoStyle;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.world.Destroyable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.WorldObject.ChildListener;
import ca.nengo.ui.lib.world.WorldObject.Listener;
import ca.nengo.ui.lib.world.WorldObject.Property;
import ca.nengo.ui.lib.world.elastic.ElasticWorld;
import ca.nengo.ui.lib.world.piccolo.WorldImpl;
import ca.nengo.ui.lib.world.piccolo.WorldObjectImpl;
import ca.nengo.ui.lib.world.piccolo.objects.Window;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Holder of worlds
 * 
 * @author Shu Wu
 */
public class Universe extends PCanvas implements Destroyable {

	private static final long serialVersionUID = 1L;

	public static final String SELECTION_MODE_NOTIFICATION = "canvasSelectionMode";

	static final double CLICK_ZOOM_PADDING = 100;

	private Text interactionModeLabel;

	private boolean selectionModeEnabled;

	private Text statusMessageLabel;

	private String statusStr = "";

	private Text taskMessagesLabel;

	private Vector<String> taskStatusStrings = new Vector<String>();

	private ElasticWorld topWorld;

	private Collection<WorldImpl> worlds;

	public Universe() {

		super();
		setZoomEventHandler(null);
		setPanEventHandler(null);

		worlds = new Vector<WorldImpl>(5);

	}

	private Path statusTextBackground;

	/**
	 * Initializes the status bar
	 */
	private void initStatusPanel() {
		statusTextBackground = Path.createRectangle(0, 0, 1, 1);
		statusTextBackground.setPaint(NengoStyle.COLOR_BACKGROUND);
		statusTextBackground.setTransparency(0.7f);

		statusMessageLabel = new Text("Welcome to " + UIEnvironment.getInstance().getAppName());

		taskMessagesLabel = new Text();
		taskMessagesLabel.setTextPaint(NengoStyle.COLOR_LIGHT_BLUE);

		interactionModeLabel = new Text();
		interactionModeLabel.setPickable(true);
		interactionModeLabel.addInputEventListener(new PBasicInputEventHandler() {
			@Override
			public void mouseClicked(PInputEvent arg0) {
				setSelectionMode(!isSelectionMode());
			}
		});

		// statusPanel = new JPanel();
		// statusPanel.setLayout(new BorderLayout());
		//
		// statusPanel.setBackground(Style.COLOR_BACKGROUND);
		// statusPanel.setBorder(new EtchedBorder());
		textHolder = getWorld();
		textLabels = new WorldObjectImpl();
		textHolder.addChild(textLabels);
		textLayoutListener = new TextLayoutListener();
		textHolder.addPropertyChangeListener(Property.BOUNDS_CHANGED, textLayoutListener);
		textLabels.addChild(statusTextBackground);
		textLabels.addChild(statusMessageLabel);
		textLabels.addChild(interactionModeLabel);
		textLabels.addChild(taskMessagesLabel);

		taskMessagesLabel.setVisible(false);

		statusMessageLabel.setFont(NengoStyle.FONT_MENU);
		interactionModeLabel.setFont(NengoStyle.FONT_MENU);
		taskMessagesLabel.setFont(NengoStyle.FONT_MENU);

		layoutText();
	}

	private WorldObject textLabels;
	private TextLayoutListener textLayoutListener;

	class TextLayoutListener implements Listener {

		public void propertyChanged(Property event) {
			if (event == Property.BOUNDS_CHANGED) {
				layoutText();
			} else {
				throw new UnsupportedOperationException();
			}
		}

	}

	private WorldObject textHolder;

	static final double TEXT_PADDING = 2;

	protected void layoutText() {
		double posY = textHolder.getHeight() - TEXT_PADDING;
		double width = textHolder.getWidth() - TEXT_PADDING;

		interactionModeLabel.setOffset(width - interactionModeLabel.getWidth(), posY
				- interactionModeLabel.getHeight());

		posY -= statusMessageLabel.getHeight();
		statusMessageLabel.setOffset(TEXT_PADDING, posY);

		posY -= taskMessagesLabel.getHeight();
		taskMessagesLabel.setOffset(TEXT_PADDING, posY);

		statusTextBackground.setBounds(0, posY, textHolder.getWidth(), textHolder.getHeight()
				- posY);

	}

	/**
	 * Updates task-related messages in the status bar
	 */
	protected void updateTaskMessages() {
		StringBuilder strBuff = new StringBuilder("");
		if (taskStatusStrings.size() > 0) {
			// strBuff.append("- MESSAGES -<BR>");

			for (int i = taskStatusStrings.size() - 1; i >= 0; i--) {
				strBuff.append(taskStatusStrings.get(i) + "\n");
			}

			strBuff.append("---\n");

			taskMessagesLabel.setVisible(true);
		} else {
			taskMessagesLabel.setVisible(false);
		}

		taskMessagesLabel.setText(strBuff.toString());
		layoutText();
	}

	/**
	 * @param message
	 *            Task related status message to remove from the status bar
	 * @return status message
	 */
	public String addTaskStatusMsg(String message) {
		taskStatusStrings.add(message);
		updateTaskMessages();
		return message;
	}

	public void addWorld(WorldImpl world) {
		worlds.add(world);
	}

	public ElasticWorld getWorld() {
		return topWorld;
	}

	public Collection<WorldImpl> getWorlds() {
		return worlds;
	}

	public List<Window> getWorldWindows() {
		LinkedList<Window> windows = new LinkedList<Window>();
		for (WorldObject wo : getWorld().getSky().getChildren()) {
			if (wo instanceof Window) {
				windows.add((Window) wo);
			}
		}
		return new ArrayList<Window>(windows);
	}

	/**
	 * Checks whether the UI is in selection or navigation mode.
	 * 
	 * @return If true, selection mode is enabled. If false, navigation mode is
	 *         enabled.
	 */
	public boolean isSelectionMode() {
		return selectionModeEnabled;
	}

	/**
	 * @param message
	 *            Task-related status message to add to the status bar
	 */
	public void removeTaskStatusMsg(String message) {
		taskStatusStrings.remove(message);
		updateTaskMessages();
	}

	public void removeWorld(WorldImpl world) {
		worlds.remove(world);
	}

	/**
	 * @param world
	 *            World to be the background for it all
	 */
	public void initialize(ElasticWorld world) {
		Util.Assert(world != null);

		if (topWorld == null) {
			topWorld = world;
			getLayer().addChild(topWorld.getPiccolo());
			initStatusPanel();
		} else {
			throw new UnsupportedOperationException("Can only initialize once");
		}

		getWorld().getSky().addChildrenListener(new ChildListener() {

			public void childAdded(WorldObject wo) {
				if (wo instanceof Window) {
					UIEnvironment.getInstance().setTopWindow((Window) wo);
				}

			}

			public void childRemoved(WorldObject wo) {
				if (wo instanceof Window) {
					List<Window> windows = getWorld().getSky().getWindows();
					if (windows.size() > 0) {
						UIEnvironment.getInstance().setTopWindow(windows.get(windows.size() - 1));
					} else {
						UIEnvironment.getInstance().setTopWindow(null);
					}
				}
			}
		});
	}

	@Override
	public void setBounds(int x, int y, int w, int h) {

		PLayer layer = getLayer();
		layer.setBounds(layer.getX(), layer.getY(), w, h);
		topWorld.setBounds(topWorld.getX(), topWorld.getY(), w, h);

		super.setBounds(x, y, w, h);
	}

	/**
	 * @param enabled
	 *            True if selection mode is enabled, False if navigation
	 */
	public void setSelectionMode(boolean enabled) {
		if (selectionModeEnabled == enabled) {
			return;
		}
		
		selectionModeEnabled = enabled;
		if (selectionModeEnabled) {
			interactionModeLabel.setText("Selection Mode");
		} else {
			interactionModeLabel.setText("Navigation Mode");
		}

		for (WorldImpl world : getWorlds()) {
			world.setSelectionMode(selectionModeEnabled);
		}
		layoutText();
	}

	/**
	 * @param message
	 *            Sets the text of the status bar in the UI
	 */
	public void setStatusMessage(String message) {
		statusStr = message;
		statusMessageLabel.setText(statusStr);
		layoutText();
	}

	public void destroy() {
		if (textHolder != null) {
			textHolder.removePropertyChangeListener(Property.BOUNDS_CHANGED, textLayoutListener);
		}
	}

}
