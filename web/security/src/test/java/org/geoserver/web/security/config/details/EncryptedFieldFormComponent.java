/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;

/**
 * A form component for testing encrypted fields
 */
public class EncryptedFieldFormComponent extends FormComponentPanel<Serializable> {
    private static final long serialVersionUID = 1L;

    TextField<String> field;

    public EncryptedFieldFormComponent() {        
        super("encryptedFieldFormComponent");
        
        Label label = new Label("toBeEncryptedLabel", "Data for Encryption");
        addOrReplace(label);
        
        field = new TextField<String>("config.toBeEncrypted");
        addOrReplace(field);
    }
    
    @Override
    public void updateModel() {
        field.updateModel();
    }
}
