/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.web.security.config;
   
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.model.util.SetModel;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.password.AbstractGeoserverPasswordEncoder;
import org.geoserver.security.password.GeoServerConfigPBEPasswordEncoder;
import org.geoserver.web.GeoServerApplication;


public class RememberMeConfigPanel extends Panel {
    
    private static final long serialVersionUID = 1L;
    protected Form<SecurityConfigModelHelper> form;
    protected Palette<String> authProviders;

    
    public RememberMeConfigPanel(String id) {
        super(id);

        
        String formWicketId= this.getClass().getSimpleName();
        form = new Form<SecurityConfigModelHelper>(formWicketId);
        add(form);                        
        form.add(saveLink());
    }
    
    
    SubmitLink saveLink() {
        return new SubmitLink("save") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
//                SecurityConfigModelHelper helper =RememberMeConfigPanel.this.form.getModelObject();
//                if (helper.hasChanges()) {
//                    try {
//                        getSecurityManager().saveSecurityConfig(
//                                (SecurityManagerConfig) helper.getConfig());
//                    } catch (Exception ex) {
//                        throw new RuntimeException(ex);
//                    }
//                }

            }
        };
    }

    GeoServerSecurityManager getSecurityManager() {
        return GeoServerApplication.get().getSecurityManager();
    }
}
