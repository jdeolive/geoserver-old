/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;

/**
 * A form component that can be used for xml configurations
 */
public class XMLFileFormComponent extends FormComponent<Serializable>{
    private static final long serialVersionUID = 1L;
    protected CheckBox validating;
    protected RequiredTextField<String> fileName;
    protected RequiredTextField<Integer> checkInterval;
    
    public XMLFileFormComponent() {
        super("xmlFileFormComponent");

        fileName = new RequiredTextField<String>("config.fileName");
        add(fileName);

        checkInterval = new RequiredTextField<Integer>("config.checkInterval", Integer.class);
        add(checkInterval);
        
        validating  = new CheckBox("config.validating");
        add(validating);
        
    };
        

    @Override
    public void updateModel() {
        validating.updateModel();
        fileName.updateModel();
        checkInterval.updateModel();
    }
    
    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
    }
    @Override
    protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
        super.onComponentTagBody(markupStream, openTag);
    }
 
}
