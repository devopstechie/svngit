package org.mobintech.jwt;

import java.util.*;

/**
 *
 * @author  holoduke
 */
class ControlCollection extends Vector {

    private Control owner;

    /** Creates a new instance of ControlCollection */
    public ControlCollection(Control owner) {
        this.owner = owner;
    }

    public void addControl(Control ctrl) {
        super.add(ctrl);
        ctrl.Parent = owner;
    }

    public Control getControl(int index) {
        return (Control)super.elementAt(index);
    }
}