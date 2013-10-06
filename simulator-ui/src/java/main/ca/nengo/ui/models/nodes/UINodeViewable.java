/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "UINodeViewable.java". Description:
"UI Wrapper for Node Containers such as Ensembles and Networks.

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

package ca.nengo.ui.models.nodes;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.swing.SwingUtilities;

import ca.nengo.model.Node;
import ca.nengo.ui.brainView.BrainViewer;
import ca.nengo.ui.lib.world.piccolo.objects.Window;
import ca.nengo.ui.lib.world.piccolo.objects.Window.WindowState;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.tooltips.TooltipBuilder;
import ca.nengo.ui.models.viewers.NodeViewer;

/**
 * UI Wrapper for Node Containers such as Ensembles and Networks.
 * 
 * @author Shu
 */
public abstract class UINodeViewable extends UINeoNode {

    /**
     * Weak reference to the viewer window
     */
    private WeakReference<Window> viewerWindowRef;

    public UINodeViewable(Node model) {
        super(model);
    }

    @Override
    protected void constructTooltips(TooltipBuilder tooltips) {
        super.constructTooltips(tooltips);
        tooltips.addProperty("# Nodes", "" + getNodesCount());
    }

    /*@Override
	protected void constructViewMenu(AbstractMenuBuilder menu) {
		super.constructViewMenu(menu);

		if (viewerWindowRef.get() == null || viewerWindowRef.get().isDestroyed()
				|| (viewerWindowRef.get().getWindowState() == Window.WindowState.MINIMIZED)) {

			menu.addAction(new StandardAction("Open viewer") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void action() throws ActionException {
					openViewer();
				}
			});

		} else {
			menu.addAction(new StandardAction("Close viewer") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void action() throws ActionException {
					closeViewer();
				}
			});

		}

//		menu.addAction(new StandardAction("Brain View (under construction)") {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			protected void action() throws ActionException {
//				createBrainViewer();
//			}
//		});
	}*/

    /**
     * Creates a new Viewer
     * 
     * @return Viewer created
     */
    protected abstract NodeViewer createViewerInstance();

    /**
     * @return Viewer Window
     */
    protected Window getViewerWindow() {
        if (!isViewerWindowVisible()) {

            NodeViewer nodeViewer = createViewerInstance();
            Window viewerWindow = new Window(this, nodeViewer);
            nodeViewer.applyDefaultLayout();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (getWorld() != null) {
                        if (viewerWindowRef.get() != null && !viewerWindowRef.get().isDestroyed()) {
                            getWorld().zoomToObject(viewerWindowRef.get());
                        }
                    }
                }
            });

            viewerWindowRef = new WeakReference<Window>(viewerWindow);
        }

        return viewerWindowRef.get();

    }

    public boolean isViewerWindowVisible() {
        return (viewerWindowRef.get() != null && !viewerWindowRef.get().isDestroyed());
    }

    public void moveViewerWindowToFront() {
        getViewerWindow().moveToFront();
    }

    @Override
    protected void initialize() {
        viewerWindowRef = new WeakReference<Window>(null);

        super.initialize();
    }

    @Override
    protected void prepareForDestroy() {
        closeViewer();
        super.prepareForDestroy();
    }

    /**
     * Closes the viewer Window
     */
    public void closeViewer() {
        if (viewerWindowRef.get() != null) {
            viewerWindowRef.get().destroy();
        }

    }

    /**
     * Opens a new instance of Brain View
     */
    public void createBrainViewer() {
        BrainViewer brainViewer = new BrainViewer();

        new Window(this, brainViewer);
        // window.setOffset(0, -brainViewer.getHeight());
        // addChild(brainViewer);
    }

    @Override
    public void detachViewFromModel() {
        closeViewer();
        super.detachViewFromModel();
    }

    @Override
    public void doubleClicked() {
        openViewer();
    }

    /**
     * @return Number of nodes contained by the Model
     */
    public abstract int getNodesCount();

    /**
     * @return Number of dimensions in this population contained by the Model
     */
    public abstract int getDimensionality();

    /**
     * @return Container Viewer
     */
    public NodeViewer getViewer() {
        if (viewerWindowRef.get() != null) {
            return (NodeViewer) viewerWindowRef.get().getContents();
        }
        return null;
    }

    /**
     * Opens the Container Viewer
     * 
     * @return the Container viewer
     */
    public NodeViewer openViewer() {
        Window viewerWindow = getViewerWindow();
        if (viewerWindow.getWindowState() == WindowState.MINIMIZED) {
            viewerWindow.restoreSavedWindow();
        }
        ((NodeViewer) viewerWindow.getContents()).setJustOpened(true);
        return (NodeViewer) viewerWindow.getContents();
    }

    /**
     * Saves the configuration of this node container
     */
    public abstract void saveContainerConfig();

    @Override
    public void saveModel(File file) throws IOException {
        saveContainerConfig();
        super.saveModel(file);
    }

}
