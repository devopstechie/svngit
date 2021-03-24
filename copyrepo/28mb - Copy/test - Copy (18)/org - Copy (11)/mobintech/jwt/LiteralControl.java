package org.mobintech.jwt;

import java.io.*;

/**
 *
 * @author  holoduke
 */
class LiteralControl extends Control {

    private String text = null;

    /** Creates a new instance of LiteralControl */
    public LiteralControl() {
    }

    public LiteralControl(String text) {
        this.text = text;
    }

    protected void render(PrintWriter out) {
        out.print(text);
    }
}