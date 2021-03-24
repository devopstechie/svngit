package org.mobintech.jwt;

import java.io.*;

/**
 *
 * @author  holoduke
 */
class Control implements Serializable {

    public Control Parent = null;
    public BlockControl BlockParent = null;

    public ControlCollection Controls = new ControlCollection(this);

    public Control() {
    }

    public void renderControl(PrintWriter out) {
        render(out);
    }

    protected void render(PrintWriter out) {
        renderChildren(out);
    }

    protected void renderChildren(PrintWriter out) {
        if (hasControls()) {
            for (int i = 0; i < Controls.size(); i++) {
                Controls.getControl(i).renderControl(out);
            }
        }
    }

    public boolean hasControls() {
        return Controls.size() > 0 ? true : false;
    }
}