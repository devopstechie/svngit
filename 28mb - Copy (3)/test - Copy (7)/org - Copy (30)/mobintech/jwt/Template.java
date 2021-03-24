package org.mobintech.jwt;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * <p>Description: ¬ыполн€ет основные операции с шаблоном</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Afina</p>
 * @author not attributable
 * @version 1.0
 */
public class Template {

    private String root;
    private String cachedir;

    private Vector blockitems = new Vector();
    private Hashtable files = new Hashtable();
    private Hashtable filenames = new Hashtable();
    private Hashtable uncompiled = new Hashtable();
    private Hashtable compiled = new Hashtable();

    /** Creates a new instance of JTemplate */
    public Template() {
        getSubItems(blockitems);
    }

    /** Creates a new instance of Template
     * @param root Set path to folder which contain templates
     */
    public Template(String root) {
        this();
        setRootDir(root);
    }

    /** Clear inner data structure assigned by assignVar, assignBlockVars, assignVars methods */
    public void destroy() {
        blockitems = new Vector();
    }

    /** Set path to folder which contain templates */
    public boolean setRootDir(String dir) {

        File folder = new File(dir);
        if (!folder.isDirectory()) {
            return false;
        }

        root = dir;
        cachedir = root + "/cache";

        return true;
    }

    public void setFileName(String handle, String filename) throws FileNotFoundException {
        filenames.put(handle, filename);
        files.put(handle, makeFileName(filename));
    }

    public void setFileNames(Map filenames) throws FileNotFoundException {
        Iterator entries = filenames.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();

            setFileName(
                    entry.getKey().toString(),
                    entry.getValue().toString()
                    );
        }
    }

    private String makeFileName(String filename) throws FileNotFoundException {
        StringBuffer sb = new StringBuffer(filename);

        if (sb.charAt(0) != '/') {
            filename = root + '/' + filename;
        }

        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("Error - file " + filename + " does not exist");
        }

        return filename;
    }

    private Hashtable getSubItems(Vector blockitems) {
        Hashtable items;

        try {
            items = (Hashtable) blockitems.elementAt(0);
        } catch (IndexOutOfBoundsException e) {
            items = new Hashtable();
            blockitems.add(0, items);
        }

        return items;
    }

    public void assignVar(String varname, Object varvalue) {
        assignVar(varname, varvalue.toString());
    }

    public void assignVar(String varname, String varvalue) {
        Hashtable items = getSubItems(blockitems);

        if (varname.indexOf('.') != -1) {
            StringTokenizer st = new StringTokenizer(varname, ".");

            String token = null;
            Hashtable fork = items;

            while (st.hasMoreTokens()) {
                token = st.nextToken();

                if (token.equals("")) {
                    throw new IllegalArgumentException("Can not find handle \"" + varname + "\"");
                }

                Vector rootitem = (Vector)fork.get(token);

                if (rootitem == null) {
                    throw new IllegalArgumentException("Can not find handle \"" + varname + "\"");
                }

                try {
                    fork = (Hashtable)rootitem.elementAt(rootitem.size() - 1);
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Can not find handle \"" + varname + "\"");
                }
            }

            items = fork;
            varname = token;
        }

        items.put(varname, varvalue);
    }

    public void assignVarFromHandle(String varname, String handle) throws FileNotFoundException, IOException, ParseException {
        assignVar(varname, pparse(handle));
    }

    public void assignBlockVars(String blockname, Object[] vars) {
        Hashtable v = new Hashtable();

        for (int i = 0; i < vars.length; i++) {
            v.put(vars[i++], vars[i].toString());
        }

        assignBlockVars(blockname, v);
    }

    public void assignBlockVars(String blockname, Map vars) {
        StringBuffer sb = new StringBuffer(blockname);

        Hashtable items = getSubItems(blockitems);

        if (blockname.equals("")) {
            fillHashFromVars(items, vars);
            return;
        }

        if (sb.indexOf(".") != -1) {
            StringTokenizer st = new StringTokenizer(blockname, ".");

            Hashtable fork = items;

            while (st.hasMoreTokens()) {
                String token = st.nextToken();

                if (token.equals("")) {
                    throw new IllegalArgumentException("Can not find handle \"" + blockname + "\"");
                }

                Vector rootitem = (Vector) fork.get(token);

                if (rootitem == null) {
                    rootitem = new Vector();
                    fork.put(token, rootitem);
                    items = new Hashtable();
                    rootitem.add(items);
                } else if (st.hasMoreTokens()) {
                    items = (Hashtable) rootitem.elementAt(rootitem.size() - 1);
                } else {
                    items = new Hashtable();
                    rootitem.add(items);
                }

                fork = items;
            }

            fillHashFromVars(fork, vars);

        } else {

            Vector v = (Vector) items.get(blockname);
            if (v == null) {
                v = new Vector();
                items.put(blockname, v);
            }

            items = new Hashtable();

            fillHashFromVars(items, vars);

            v.add(items);
        }
    }

    public void assignVars(Map vars) {
        Iterator entries = vars.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();

            assignVar(
                    entry.getKey().toString(),
                    entry.getValue().toString()
                    );
        }
    }

    private boolean loadfile(String handle) throws FileNotFoundException, IOException {
        if (uncompiled.get(handle) != null) {
            return true;
        }

        String filename = (String) files.get(handle);
        if (filename == null) {
            throw new IllegalArgumentException("No file specified for handle " + handle);
        }

        BufferedReader in = new BufferedReader(new FileReader(filename));
        StringWriter out = new StringWriter();

        char[] buf = new char[1024];
        int c = 0;

        while ((c = in.read(buf)) > 0) {
            out.write(buf, 0, c);
        }

        in.close();

        if (out.getBuffer().length() == 0) {
            throw new IllegalArgumentException("File " + filename + " for handle " + handle + " is empty");
        }

        uncompiled.put(handle, out.getBuffer().toString());

        out.close();

        return true;
    }

    private void fillHashFromVars(Hashtable items, Map vars) {
        Iterator entries = vars.entrySet().iterator();

        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();

            items.put(
                    entry.getKey().toString(),
                    entry.getValue().toString()
                    );
        }
    }

    public String pparse(String handle) throws FileNotFoundException, IOException, ParseException {

        BlockControl ctrl = (BlockControl) compiled.get(handle);

        if (ctrl == null) {
            String cachefilepath = cachedir + "/" + (String) filenames.get(handle) + ".compiled";
            File cachefile = new File(cachefilepath);
            File file = new File((String) files.get(handle));

            if (cachefile.lastModified() == file.lastModified()) {

                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile)));
                try {
                    ctrl = (BlockControl) in.readObject();
                    compiled.put(handle, ctrl);
                } catch (ClassNotFoundException e) {
                    ctrl = null;
                } catch (InvalidClassException e) {
                    ctrl = null;
                }

                in.close();
            }

            if (ctrl == null) {
                if (!loadfile(handle)) {
                    throw new IllegalArgumentException("Couldn't load template file for handle " + handle);
                }

                ctrl = (BlockControl) compiled.get(handle);
                if (ctrl == null) {
                    ctrl = compile(uncompiled.get(handle).toString());

                    File dir = new File(cachedir);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(cachefile)));
                    out.writeObject(ctrl);
                    out.close();

                    cachefile.setLastModified(file.lastModified());

                    compiled.put(handle, ctrl);
                }
            }
        }

        ctrl.setBlockItems(blockitems);
        StringWriter content = new StringWriter();
        ctrl.renderControl(new PrintWriter(content));

        return content.getBuffer().toString();
    }

    private void compileInterBuffer(String buffer, BlockControl block, Hashtable handles) throws ParseException {

        int last_end = 0;

        Pattern p = Pattern.compile("\\{((([a-z0-9\\-_]+?)\\.)?([a-z0-9\\-_]+?))\\}", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = p.matcher(buffer);

        while (m.find()) {
            if (m.start() > last_end) {
                block.Controls.addControl(new LiteralControl(buffer.substring(last_end, m.start())));
            }

            DataBoundControl ctrl = new DataBoundControl();

            String handle = m.group(1);
            if (handle.indexOf('.') != -1) {
                String pair[] = handle.split("\\.");
                if (pair[0].equals(block.handle)) {
                    ctrl.BlockParent = block;
                } else {
                    ctrl.BlockParent = (BlockControl) handles.get(pair[0]);

                    if (ctrl.BlockParent == null) {
                        throw new ParseException("Error - there are no block for handle \"" + pair[0] + "\"");
                    }
                }

                ctrl.handle = pair[1];
            } else {
                ctrl.BlockParent = (BlockControl) handles.get(".");
                ctrl.handle = handle;
            }

            block.Controls.addControl(ctrl);

            last_end = m.end();
        }

        if (buffer.length() - 1 > last_end) {
            block.Controls.addControl(new LiteralControl(buffer.substring(last_end, buffer.length())));
        }
    }

    private BlockControl compile(String code) throws ParseException {
        return compile(".", code, new Hashtable());
    }

    private BlockControl compile(String handle, String buffer, Hashtable handles) throws ParseException {

        BlockControl block = new BlockControl(handle);
        handles.put(handle, block);

        Pattern p1 = Pattern.compile("<!-- BEGIN (.*?) -->", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        int last_end = 0;

        Matcher m1 = p1.matcher(buffer);

        while (m1.find(last_end)) {
            //
            // Parse interbuffer
            //
            String interbuffer = buffer.substring(last_end, m1.start());
            compileInterBuffer(interbuffer, block, handles);

            //
            // Parse block buffer
            //
            String blockbuffer = buffer.substring(m1.end());
            Pattern p2 = Pattern.compile("<!-- END " + m1.group(1) + " -->", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m2 = p2.matcher(blockbuffer);

            if (m2.find()) {
                blockbuffer = blockbuffer.substring(0, m2.start());
                block.Controls.addControl(compile(m1.group(1), blockbuffer, handles));

                last_end = m1.end() + m2.end();
            } else {
                last_end = buffer.length();
            }
        }

        //
        // Parse last buffer
        //
        if (buffer.length() - 1 > last_end) {
            String lastbuffer = buffer.substring(last_end, buffer.length());
            compileInterBuffer(lastbuffer, block, handles);
        }

        handles.remove(handle);
        return block;
    }
}