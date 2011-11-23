/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.IModel;

/**
 * A form component that can be used for xml configurations
 */
public class NamedConfigDetailsEmptyPanel extends FormComponentPanel<SecurityConfigModelHelper>{
    private static final long serialVersionUID = 1L;
    
    public NamedConfigDetailsEmptyPanel(String id, IModel<SecurityConfigModelHelper> model) {
        super(id,model);
        setOutputMarkupId(true);
    }
                            
}
