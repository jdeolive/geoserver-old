/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config;

import org.apache.wicket.markup.html.form.CheckBox;

/**
 * A form component that can be used for xml configurations
 */
public class XMLNamedConfigPanel extends NamedConfigPanel{
    private static final long serialVersionUID = 1L;
    protected CheckBox validating;
    
    public XMLNamedConfigPanel(String id,SecurityConfigModelHelper helper,Class<?> extensionPoint) {        
        super(id,helper,extensionPoint);

        validating  = new CheckBox("config.validating");
        validating.setRequired(true);
        form.add(validating);
    }
                            
}
