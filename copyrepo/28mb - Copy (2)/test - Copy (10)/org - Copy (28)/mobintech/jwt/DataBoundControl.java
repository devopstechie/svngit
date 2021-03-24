package org.mobintech.jwt;

import java.io.*;

/**
 *
 * @author  holoduke
 */
class DataBoundControl extends Control {

    public String handle = null;

    /** Creates a new instance of LiteralControl */
    public DataBoundControl() {
    }

    public DataBoundControl(String handle) {
        this.handle = handle;
    }

    protected void render(PrintWriter out) {
        out.print(BlockParent.getString(handle));
    }
}