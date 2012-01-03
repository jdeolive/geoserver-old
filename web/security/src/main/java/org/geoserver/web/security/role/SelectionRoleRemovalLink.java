package org.geoserver.web.security.role;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.validation.AbstractSecurityException;
import org.geoserver.security.validation.RoleServiceValidationWrapper;
import org.geoserver.security.validation.RoleStoreValidationWrapper;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.ParamResourceModel;

public class SelectionRoleRemovalLink extends AjaxLink<Object> {

    
    private static final long serialVersionUID = 1L;

    GeoServerTablePanel<GeoserverRole> roles;
    GeoServerDialog dialog;
    GeoServerDialog.DialogDelegate delegate;
    ConfirmRemovalRolePanel removePanel;
    String roleServiceName;
    

    public SelectionRoleRemovalLink(String roleServiceName,String id, GeoServerTablePanel<GeoserverRole> roles,
            GeoServerDialog dialog) {
        super(id);
        this.roles = roles;
        this.dialog = dialog;
        this.roleServiceName=roleServiceName;
        
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        final List<GeoserverRole> selection = roles.getSelection();
        if (selection.size() == 0)
            return;

        dialog.setTitle(new ParamResourceModel("confirmRemoval", this));

        // if there is something to cancel, let's warn the user about what
        // could go wrong, and if the user accepts, let's delete what's needed
        dialog.showOkCancel(target,delegate=new GeoServerDialog.DialogDelegate() {
            private static final long serialVersionUID = 1L;
            protected Component getContents(String id) {
                // show a confirmation panel for all the objects we have to remove
                return removePanel= new ConfirmRemovalRolePanel(id, selection) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected StringResourceModel canRemove(GeoserverRole role) {
                        return SelectionRoleRemovalLink.this.canRemove(role);
                    }
                };
            }

            protected boolean onSubmit(AjaxRequestTarget target, Component contents) {
                // cascade delete the whole selection

                
                GeoserverRoleStore gaStore = null;
                try {
                    GeoserverRoleService gaService =
                            GeoServerApplication.get().getSecurityManager().loadRoleService(roleServiceName);
                    gaStore = new RoleStoreValidationWrapper(gaService.createStore());
                    for (GeoserverRole role : removePanel.getRoots()) {                     
                         gaStore.removeRole(role);
                    }
                    gaStore.store();
                } catch (IOException ex) {
                    try {gaStore.load(); } catch (IOException ex2) {};
                    throw new RuntimeException(ex);
                }
                // the deletion will have changed what we see in the page
                // so better clear out the selection
                roles.clearSelection();
                return true;
            }

            @Override
            public void onClose(AjaxRequestTarget target) {
                // if the selection has been cleared out it's sign a deletion
                // occurred, so refresh the table
                if (roles.getSelection().size() == 0) {
                    setEnabled(false);
                    target.addComponent(SelectionRoleRemovalLink.this);
                    target.addComponent(roles);
                }
            }
        });
        
    }

    protected StringResourceModel canRemove(GeoserverRole role) {
        
        GeoserverRoleService gaService=null;
        try {
            gaService = GeoServerApplication.get().getSecurityManager().loadRoleService(roleServiceName);
            boolean isActive = GeoServerApplication.get().getSecurityManager().
                    getActiveRoleService().getName().equals(roleServiceName);                    
            RoleServiceValidationWrapper valService = new RoleServiceValidationWrapper(gaService,isActive); 
            valService.checkRemovalOfAdminRole(role);
            valService.checkRoleIsUsed(role);
        } catch (IOException e) {
            if (e.getCause() instanceof AbstractSecurityException) {
                AbstractSecurityException secEx = 
                        (AbstractSecurityException)e.getCause();
                return new StringResourceModel("security."+secEx.getErrorId(),null,secEx.getArgs());
            } else {
                throw new RuntimeException(e);
            }            
        }
        
        return null;
    }

}
