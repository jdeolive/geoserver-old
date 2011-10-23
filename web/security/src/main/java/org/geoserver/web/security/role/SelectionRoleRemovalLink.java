package org.geoserver.web.security.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.impl.ServiceAccessRuleDAO;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.ParamResourceModel;
import org.springframework.util.StringUtils;

public class SelectionRoleRemovalLink extends AjaxLink<Object> {

    
    private static final long serialVersionUID = 1L;

    GeoServerTablePanel<GeoserverGrantedAuthority> roles;
    GeoServerDialog dialog;
    GeoServerDialog.DialogDelegate delegate;
    ConfirmRemovalRolePanel removePanel;
    

    public SelectionRoleRemovalLink(String id, GeoServerTablePanel<GeoserverGrantedAuthority> roles,
            GeoServerDialog dialog) {
        super(id);
        this.roles = roles;
        this.dialog = dialog;
        
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        final List<GeoserverGrantedAuthority> selection = roles.getSelection();
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
                    protected StringResourceModel canRemove(GeoserverGrantedAuthority role) {
                        return SelectionRoleRemovalLink.this.canRemove(role);
                    }
                };
            }

            protected boolean onSubmit(AjaxRequestTarget target, Component contents) {
                // cascade delete the whole selection

                
                GeoserverGrantedAuthorityService gaService =
                    GeoServerApplication.get().getSecurityManager().getActiveRoleService();

                try {
                    GeoserverGrantedAuthorityStore gaStore = gaService.createStore();
                    for (GeoserverGrantedAuthority role : removePanel.getRoots()) {                     
                         gaStore.removeGrantedAuthority(role);
                    }
                    gaStore.store();
                } catch (IOException e) {
                    throw new RuntimeException(e);
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

    protected StringResourceModel canRemove(GeoserverGrantedAuthority role) {        
        if (role.equals(GeoserverGrantedAuthority.ADMIN_ROLE))
            return new StringResourceModel(getClass().getSimpleName()+".noDelete",null,new Object[] {role.getAuthority()});
                        
        List<String> keys = new ArrayList<String>();
        for (ServiceAccessRule rule : ServiceAccessRuleDAO.get().getRulesAssociatedWithRole(role.getAuthority()))
            keys.add(rule.getKey());
        for (DataAccessRule rule : DataAccessRuleDAO.get().getRulesAssociatedWithRole(role.getAuthority()))
            keys.add(rule.getKey());
        
        if (keys.size()>0) {
            String ruleString = StringUtils.collectionToCommaDelimitedString(keys);
            return new StringResourceModel(getClass().getSimpleName()+".isUsed",null,new Object[] {role.getAuthority(), ruleString});
        }
        
        return null;
    }

}
