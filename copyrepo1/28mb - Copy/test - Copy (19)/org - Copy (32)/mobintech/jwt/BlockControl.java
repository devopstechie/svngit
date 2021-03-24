package org.mobintech.jwt;

import java.io.*;
import java.util.*;

/**
 *
 * @author  holoduke
 */
class BlockControl extends DataBoundControl {

    public int SelectedItemIndex = 0;

    protected transient Vector blockitems;

    public BlockControl() {
    }

    public BlockControl(String handle) {
        this.handle = handle;
    }

    public void setBlockItems(Vector blockitems) {
        this.blockitems = blockitems;
    }

    protected Object getValue(String handle) {
        Hashtable values;

        try {
            values = (Hashtable) blockitems.elementAt(SelectedItemIndex);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        return values.get(handle);
    }

    public String getString(String handle) {
        Object value = getValue(handle);
        return value == null ? "" : value.toString();
    }

    public Vector getVector(String handle) {
        Object value = getValue(handle);
        return value == null ? new Vector() : (Vector) value;
    }

    protected void render(PrintWriter out) {

        for (int i = 0; i < blockitems.size(); i++) {
            SelectedItemIndex = i;
            renderChildren(out);
        }

        SelectedItemIndex = 0;
    }

    protected void renderChildren(PrintWriter out) {
        if (hasControls()) {
            for (int i = 0; i < Controls.size(); i++) {
                Control ctrl = Controls.getControl(i);

                if (ctrl instanceof BlockControl) {
                    BlockControl blockctrl = (BlockControl) ctrl;
                    blockctrl.setBlockItems(getVector(blockctrl.handle));
                }

                ctrl.renderControl(out);
            }
        }
    }
}