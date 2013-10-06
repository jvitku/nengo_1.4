/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigureAction.java". Description:
""

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

package ca.nengo.ui.actions;

import ca.nengo.ui.NengoGraphics;
import ca.nengo.ui.lib.actions.ActionException;
import ca.nengo.ui.lib.actions.StandardAction;

/**
 * TODO
 * 
 * @author TODO
 */
public class ConfigureAction extends StandardAction {

    private static final long serialVersionUID = 1L;

    private Object model;

    /**
     * TODO
     * 
     * @param model TODO
     */
    public ConfigureAction(Object model) {
        super("Inspector");
        init(model);
    }

    /**
     * TODO
     * 
     * @param actionName TODO
     * @param model TODO
     */
    public ConfigureAction(String actionName, Object model) {
        super("Inspector", actionName);
        init(model);
    }

    private void init(Object model) {
        this.model = model;
    }

    @Override
    protected void action() throws ActionException {
        NengoGraphics.getInstance().configureObject(model);
    }
}