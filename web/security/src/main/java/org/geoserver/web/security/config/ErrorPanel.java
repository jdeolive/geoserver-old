/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Panel showing errors for services
 * 
 * @author christian
 *
 */
public class ErrorPanel extends Panel {

    private static final long serialVersionUID = 1L;
    protected TextArea<String> stacktrace;

    public ErrorPanel(String id, IOException ex) {
        super(id);
     
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(out);
        ex.printStackTrace(w);
        w.close();

        IModel<String> model = Model.of(new String(out.toByteArray()));
        stacktrace = new TextArea<String>("stacktrace",model);
        add(stacktrace);
                
    }
}
