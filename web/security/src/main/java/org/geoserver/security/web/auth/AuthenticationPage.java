package org.geoserver.security.web.auth;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.security.config.SecurityManagerConfig;
import org.geoserver.security.web.SecurityManagerConfigModel;
import org.geoserver.web.GeoServerHomePage;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.wicket.HelpLink;

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

        form.add(new AjaxSubmitLink("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    getSecurityManager()
                        .saveSecurityConfig((SecurityManagerConfig) form.getModelObject());
                    doReturn();
                } catch (Exception e) {
                    error(e);
                }
            }
        });
        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
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
