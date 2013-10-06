/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NodeViewer.java". Description:
"Viewer for looking at NEO Node models

  @author Shu"

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

package ca.nengo.ui.models.viewers;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import ca.nengo.model.Node;
import ca.nengo.ui.lib.actions.LayoutAction;
import ca.nengo.ui.lib.objects.activities.TrackedStatusMsg;
import ca.nengo.ui.lib.objects.models.ModelObject;
import ca.nengo.ui.lib.util.UIEnvironment;
import ca.nengo.ui.lib.util.Util;
import ca.nengo.ui.lib.util.menus.PopupMenuBuilder;
import ca.nengo.ui.lib.world.Interactable;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.lib.world.elastic.ElasticWorld;
import ca.nengo.ui.lib.world.handlers.AbstractStatusHandler;
import ca.nengo.ui.models.ModelsContextMenu;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.nodes.UINodeViewable;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * Viewer for looking at NEO Node models
 * 
 * @author Shu
 */
public abstract class NodeViewer extends ElasticWorld implements Interactable {

    private MyNodeListener myNodeListener;
    private Boolean justOpened;

    /**
     * Viewer Parent
     */
    private final UINodeViewable parentOfViewer;

    /**
     * Children of NEO nodes
     */
    protected final Hashtable<Node, UINeoNode> neoNodesChildren = new Hashtable<Node, UINeoNode>();

    /**
     * @param nodeContainer
     *            UI Object containing the Node model
     */
    public NodeViewer(UINodeViewable nodeContainer) {
        super(nodeContainer.getName() + " (" + nodeContainer.getTypeName() + " Viewer)");
        this.parentOfViewer = nodeContainer;
        this.justOpened = false;

        initialize();
    }

    private void initChildModelListener() {
        myNodeListener = new MyNodeListener();

        getGround().addChildrenListener(new WorldObject.ChildListener() {
            public void childAdded(WorldObject wo) {

                if (wo instanceof UINeoNode) {
                    ((UINeoNode) wo).addModelListener(myNodeListener);
                }
            }

            public void childRemoved(WorldObject wo) {
                if (wo instanceof UINeoNode) {
                    ((UINeoNode) wo).removeModelListener(myNodeListener);
                }
            }
        });
    }

    public Boolean getJustOpened() {
        return justOpened;
    }

    public void setJustOpened(Boolean justOpened) {
        this.justOpened = justOpened;
    }

    /**
     * @param node
     *            node to be added
     * @param updateModel
     *            if true, the network model is updated. this may be false, if
     *            it is known that the network model already contains this node
     * @param dropInCenterOfCamera
     *            whether to drop the node in the center of the camera
     * @param moveCameraToNode
     *            whether to move the camera to where the node is
     */
    protected void addUINode(UINeoNode node, boolean dropInCenterOfCamera, boolean moveCameraToNode) {

        /**
         * Moves the camera to where the node is positioned, if it's not dropped
         * in the center of the camera
         */
        if (moveCameraToNode) {
            getWorld().animateToSkyPosition(node.getOffset().getX(), node.getOffset().getY());
        }

        neoNodesChildren.put(node.getModel(), node);

        if (dropInCenterOfCamera) {
            getGround().addChildFancy(node, dropInCenterOfCamera);
        } else {
            getGround().addChild(node);
        }

    }

    public Point2D localToView(Point2D localPoint) {
        localPoint = getSky().parentToLocal(localPoint);
        localPoint = getSky().localToView(localPoint);
        return localPoint;
    }

    public void doSortByNameLayout(){
        applySortLayout(SortMode.BY_NAME);
    }

    protected abstract boolean canRemoveChildModel(Node node);

    /*   @Override
        protected void constructLayoutMenu(MenuBuilder menu) {
        super.constructLayoutMenu(menu);
        MenuBuilder sortMenu = menu.addSubMenu("Sort");

        sortMenu.addAction(new SortNodesAction(SortMode.BY_NAME));
        sortMenu.addAction(new SortNodesAction(SortMode.BY_TYPE));
    }
     */
    protected void initialize() {
        initChildModelListener();

        setStatusBarHandler(new NodeViewerStatus(this));

        TrackedStatusMsg msg = new TrackedStatusMsg("Building nodes in Viewer");

        updateViewFromModel(true);

        msg.finished();

    }

    protected abstract void removeChildModel(Node node);

    /**
     * Called when the model changes. Updates the viewer based on the NEO model.
     */
    protected abstract void updateViewFromModel(boolean isFirstUpdate);

    /**
     * Applies the default layout
     */
    public abstract void applyDefaultLayout();

    /**
     * Applies a square layout which is sorted
     * 
     * @param sortMode
     *            Type of sort layout to use
     */
    public void applySortLayout(SortMode sortMode) {
        getGround().setElasticEnabled(false);

        List<UINeoNode> nodes = getUINodes();

        switch (sortMode) {
        case BY_NAME:
            Collections.sort(nodes, new Comparator<UINeoNode>() {

                public int compare(UINeoNode o1, UINeoNode o2) {
                    return (o1.getName().compareToIgnoreCase(o2.getName()));

                }

            });

            break;
        case BY_TYPE:
            Collections.sort(nodes, new Comparator<UINeoNode>() {

                public int compare(UINeoNode o1, UINeoNode o2) {
                    if (o1.getClass() != o2.getClass()) {

                        return o1.getClass().getSimpleName().compareToIgnoreCase(
                                o2.getClass().getSimpleName());
                    } else {
                        return (o1.getName().compareToIgnoreCase(o2.getName()));
                    }

                }

            });

            break;
        }

        /*
         * basic rectangle layout variables
         */
        double x = 0;
        double y = 0;

        int numberOfColumns = (int) Math.sqrt(nodes.size());
        int columnCounter = 0;

        double startX = Double.MAX_VALUE;
        double startY = Double.MAX_VALUE;
        double maxRowHeight = 0;
        double endX = Double.MIN_VALUE;
        double endY = Double.MIN_VALUE;

        if (nodes.size() > 0) {
            for (UINeoNode node : nodes) {

                node.animateToPosition(x, y, 1000);

                if (x < startX) {
                    startX = x;
                } else if (x + node.getWidth() > endX) {
                    endX = x + node.getWidth();
                }

                if (y < startY) {
                    startY = y;
                } else if (y + node.getHeight() > endY) {
                    endY = y + node.getHeight();
                }

                if (node.getFullBounds().getHeight() > maxRowHeight) {
                    maxRowHeight = node.getFullBounds().getHeight();
                }

                x += node.getFullBounds().getWidth() + 50;

                if (++columnCounter > numberOfColumns) {
                    x = 0;
                    y += maxRowHeight + 50;
                    maxRowHeight = 0;
                    columnCounter = 0;
                }
            }

        }

        PBounds fullBounds = new PBounds(startX, startY, endX - startX, endY - startY);
        zoomToBounds(fullBounds);

    }

    //    @Override
    //    public void constructMenu(PopupMenuBuilder menu, Double posX, Double posY) {
    //        super.constructMenu(menu, posX, posY);
    //
    //        // File menu
    //        //	menu.addSection("File");
    //        //	menu.addAction(new SaveNodeAction(getViewerParent()));
    //
    //    }

    /**
     * @return NEO Model represented by the viewer
     */
    public Node getModel() {
        return parentOfViewer.getModel();
    }

    /**
     * @return A collection of NEO Nodes contained in this viewer
     */
    public List<UINeoNode> getUINodes() {
    	return new ArrayList<UINeoNode>(neoNodesChildren.values());
    }

    public UINeoNode getUINode(Node node) {
        return neoNodesChildren.get(node);
    }

    @Override
    protected void constructSelectionMenu(Collection<WorldObject> selection, PopupMenuBuilder menu) {
        super.constructSelectionMenu(selection, menu);
        ArrayList<ModelObject> models = new ArrayList<ModelObject>(selection.size());

        for (WorldObject object : selection) {
            if (object instanceof ModelObject) {
                models.add((ModelObject) object);
            }
        }

        ModelsContextMenu.constructMenu(menu, models);

    }

    /**
     * @return Parent of this viewer
     */
    public UINodeViewable getViewerParent() {
        return parentOfViewer;
    }

    public void setOriginsTerminationsVisible(boolean visible) {
        for (UINeoNode node : getUINodes()) {
            node.setWidgetsVisible(visible);
        }
    }

    public void updateViewFromModel() {
        updateViewFromModel(false);
    }

    private class MyNodeListener implements ModelObject.ModelListener {

        public void modelDestroyed(Object model) {
            removeChildModel((Node) model);
        }

        public void modelDestroyStarted(Object model) {
            if (!canRemoveChildModel((Node) model)) {
                throw new UnsupportedOperationException("Removing nodes not supported here");
            }

        }

    }

    /**
     * Supported types of sorting allowed in layout
     * 
     * @author Shu Wu
     */
    public static enum SortMode {
        BY_NAME("Name"), BY_TYPE("Type");

        private String name;

        SortMode(String name) {
            this.name = name;
        }

        protected String getName() {
            return name;
        }
    }

    /**
     * Action to apply a sorting layout
     * 
     * @author Shu Wu
     */
    class SortNodesAction extends LayoutAction {

        private static final long serialVersionUID = 1L;
        SortMode sortMode;

        public SortNodesAction(SortMode sortMode) {
            super(NodeViewer.this, "Sort nodes by " + sortMode.getName(), sortMode.getName());
            this.sortMode = sortMode;
        }

        @Override
        protected void applyLayout() {
            applySortLayout(sortMode);
        }

    }

    /**
     * Zooms the viewer to optimally fit all nodes
     * 
     * @author Shu Wu
     */
    class ZoomToFitActivity extends PActivity {

        public ZoomToFitActivity() {
            super(0);
            UIEnvironment.getInstance().addActivity(this);
        }

        @Override
        protected void activityStarted() {
            zoomToFit();
        }

    }

}

/**
 * Handler which updates the status bar of NeoGraphics to display information
 * about the node which the mouse is hovering over.
 * 
 * @author Shu Wu
 */
class NodeViewerStatus extends AbstractStatusHandler {

    public NodeViewerStatus(NodeViewer world) {
        super(world);
    }

    @Override
    protected String getStatusMessage(PInputEvent event) {
        ModelObject wo = (ModelObject) Util.getNodeFromPickPath(event, ModelObject.class);

        StringBuilder statusStr = new StringBuilder(200);
        if (getWorld().getGround().isElasticMode()) {
            statusStr.append("Elastic layout enabled | ");
        }
        statusStr.append(getWorld().getViewerParent().getFullName() + " -> ");

        if (getWorld().getSelection().size() > 1) {
            statusStr.append(getWorld().getSelection().size() + " Objects selected");

        } else {

            if (wo != null) {
                statusStr.append(wo.getFullName());
            } else {
                statusStr.append("No Model Selected");
            }
        }
        return statusStr.toString();
    }

    @Override
    protected NodeViewer getWorld() {
        return (NodeViewer) super.getWorld();
    }

}
