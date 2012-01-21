/* Copyright (c) 2001 - 2012 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.auth;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.web.wicket.HelpLink;

/**
 * Main menu page for authentication.
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class AuthenticationPage extends AbstractSecurityPage {

    public AuthenticationPage() {
        initComponents();
    }

    void initComponents() {
        Form<SecurityManagerConfig> form = new Form("form", 
            new CompoundPropertyModel<SecurityManagerConfig>(getSecurityManager().getSecurityConfig()));
        add(form);

        form.add(new CheckBox("anonymousAuth"));
        form.add(new AuthenticationProvidersPanel("authProviders"));
        form.add(new HelpLink("authProvidersHelp").setDialog(dialog));
        form.add(new AuthenticationChainPanel("authChain", form));
        form.add(new HelpLink("authChainHelp").setDialog(dialog));

        form.add(new SubmitLink("save", form) {
            @Override
            public void onSubmit() {
                try {
                    getSecurityManager()
                        .saveSecurityConfig((SecurityManagerConfig) getForm().getModelObject());
                    doReturn();
                } catch (Exception e) {
                    error(e);
                }
            }
        });
        form.add(new Link("cancel") {
            @Override
            public void onClick() {
                doReturn();
            }
        });

    }

    class AuthenticationChainPanel extends FormComponentPanel {

        public AuthenticationChainPanel(String id, Form form) {
            super(id, new Model());

            add(new AuthenticationChainPalette("authProviderNames"));
        }
    }
}
