package org.geoserver.web.security.config.details;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.GeoServerAuthenticationProvider;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerAuthenticationProcessingFilter;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.config.SecurityAuthProviderConfig;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.config.SecurityUserGroupServiceConfig;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidator;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.ParamResourceModel;

public class SelectionNamedServiceRemovalLink extends AjaxLink<Object> {

    
    private static final long serialVersionUID = 1L;

    GeoServerTablePanel<SecurityNamedServiceConfig> namedServices;
    GeoServerDialog dialog;
    GeoServerDialog.DialogDelegate delegate;

    ConfirmRemovalNamedServicePanel removePanel;
    Class<?> serviceClass;
    

    public SelectionNamedServiceRemovalLink(String id, GeoServerTablePanel<SecurityNamedServiceConfig> namedServices,
            GeoServerDialog dialog,Class<?> serviceClass) {
        super(id);
        this.namedServices = namedServices;
        this.dialog = dialog;
        this.serviceClass=serviceClass;        
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        final List<SecurityNamedServiceConfig> selection = namedServices.getSelection();
        if (selection.size() == 0)
            return;

        dialog.setTitle(new ParamResourceModel("confirmRemoval", this));

        // if there is something to cancel, let's warn the user about what
        // could go wrong, and if the user accepts, let's delete what's needed
        dialog.showOkCancel(target,delegate=new GeoServerDialog.DialogDelegate() {
            private static final long serialVersionUID = 1L;
            protected Component getContents(String id) {
                // show a confirmation panel for all the objects we have to remove
                return removePanel= new ConfirmRemovalNamedServicePanel(id, selection) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected StringResourceModel canRemove(SecurityNamedServiceConfig config) {
                        return SelectionNamedServiceRemovalLink.this.canRemove(config);
                    }
                };
            }

            protected boolean onSubmit(AjaxRequestTarget target, Component contents) {
                // cascade delete the whole selection
                
                GeoServerSecurityManager manager = GeoServerApplication.get().getSecurityManager();
                try {
                    for (SecurityNamedServiceConfig config : removePanel.getRoots()) {                     
                        if (serviceClass==GeoServerAuthenticationProvider.class)
                            manager.removeAuthenticationProvider((SecurityAuthProviderConfig)config);
                        if (serviceClass==GeoServerAuthenticationProcessingFilter.class)
                            manager.removeAuthenticationFilter(config);
                        if (serviceClass==PasswordValidator.class)
                            manager.removePasswordValidator((PasswordPolicyConfig)config);
                        if (serviceClass==GeoServerUserGroupService.class)
                            manager.removeUserGroupService((SecurityUserGroupServiceConfig)config);
                        if (serviceClass==GeoServerRoleService.class)
                            manager.removeRoleService((SecurityRoleServiceConfig)config);                         
                    }
                } catch ( SecurityConfigException ex ) {
                    throw new RuntimeException("Never should reach this point",ex);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // the deletion will have changed what we see in the page
                // so better clear out the selection
                namedServices.clearSelection();
                return true;
            }

            @Override
            public void onClose(AjaxRequestTarget target) {
                // if the selection has been cleared out it's sign a deletion
                // occurred, so refresh the table
                if (namedServices.getSelection().size() == 0) {
                    setEnabled(false);
                    target.addComponent(SelectionNamedServiceRemovalLink.this);
                    target.addComponent(namedServices);
                }
            }
        });
        
    }

    protected StringResourceModel canRemove(SecurityNamedServiceConfig config) {
        SecurityConfigValidator validator = 
                SecurityConfigValidator.getConfigurationValiator(serviceClass, config.getClassName());
        try {
            if (serviceClass==GeoServerAuthenticationProvider.class)
                validator.validateRemoveAuthProvider((SecurityAuthProviderConfig)config);
            if (serviceClass==GeoServerAuthenticationProcessingFilter.class)
                validator.validateRemoveFilter(config);
            if (serviceClass==PasswordValidator.class)
                validator.validateRemovePasswordPolicy((PasswordPolicyConfig)config);
            if (serviceClass==GeoServerUserGroupService.class)
                validator.validateRemoveUserGroupService((SecurityUserGroupServiceConfig)config);
            if (serviceClass==GeoServerRoleService.class)
                validator.validateRemoveRoleService((SecurityRoleServiceConfig)config);
        } catch (SecurityConfigException ex) {
            return new StringResourceModel("security."+ex.getErrorId(),null,ex.getArgs());
        }        
        return null;
    }
    
    public GeoServerDialog.DialogDelegate getDelegate() {
        return delegate;
    }
    
}
