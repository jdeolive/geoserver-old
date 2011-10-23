package org.geoserver.web.security.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.ParamResourceModel;

public class SelectionUserRemovalLink extends AjaxLink<Object> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    GeoServerTablePanel<GeoserverUser> users;

    GeoServerDialog dialog;
    boolean disassociateRoles;
    ConfirmRemovalUserPanel removePanel;
    GeoServerDialog.DialogDelegate delegate;

    public SelectionUserRemovalLink(String id, GeoServerTablePanel<GeoserverUser> users,
            GeoServerDialog dialog,boolean disassociateRoles) {
        super(id);
        this.users = users;
        this.dialog = dialog;
        this.disassociateRoles=disassociateRoles;
    }
    //return new ConfirmRemovalPanel<GeoserverUserGroup>(id,"username", selection) {                //return new ConfirmRemovalPanel<GeoserverUserGroup>(id,"username", selection) {


    @Override
    public void onClick(AjaxRequestTarget target) {
        final List<GeoserverUser> selection = users.getSelection();
        if (selection.size() == 0)
            return;

        dialog.setTitle(new ParamResourceModel("confirmRemoval", this));

        // if there is something to cancel, let's warn the user about what
        // could go wrong, and if the user accepts, let's delete what's needed
        dialog.showOkCancel(target, delegate=new GeoServerDialog.DialogDelegate() {
            protected Component getContents(String id) {
                // show a confirmation panel for all the objects we have to remove
                Model<Boolean> model = new Model<Boolean>(SelectionUserRemovalLink.this.disassociateRoles);
                return removePanel= new ConfirmRemovalUserPanel(id,model ,selection) {
                    @Override
                    protected StringResourceModel canRemove(GeoserverUser user) {
                        return SelectionUserRemovalLink.this.canRemove(user);
                    }
                };
            }

            protected boolean onSubmit(AjaxRequestTarget target, Component contents) {
                // cascade delete the whole selection


                GeoserverUserGroupService ugService = GeoServerApplication.get().getSecurityManager().getActiveUserGroupService();
                try {
                    GeoserverUserGroupStore ugStore = ugService.createStore();
                    for (GeoserverUser user : removePanel.getRoots()) { // keep admins                    
                        ugStore.removeUser(user);
                        if (disassociateRoles) {
                            GeoserverGrantedAuthorityStore gaStore = 
                                    GeoServerApplication.get().getSecurityManager()
                                        .getActiveRoleService().createStore();
                            List<GeoserverGrantedAuthority> list= new ArrayList<GeoserverGrantedAuthority>();
                            list.addAll(gaStore.getRolesForUser(user.getUsername()));
                            for (GeoserverGrantedAuthority role: list)
                                gaStore.disAssociateRoleFromUser(role, user.getUsername());
                            gaStore.store();        
                        }
                    }
                    ugStore.store();
                } catch (IOException e) {
                    // TODO, is this correct ?
                    throw new RuntimeException(e);
                }
                // the deletion will have changed what we see in the page
                // so better clear out the selection
                users.clearSelection();
                return true;
            }

            @Override
            public void onClose(AjaxRequestTarget target) {
                // if the selection has been cleared out it's sign a deletion
                // occurred, so refresh the table
                if (users.getSelection().size() == 0) {
                    setEnabled(false);
                    target.addComponent(SelectionUserRemovalLink.this);
                    target.addComponent(users);
                }
            }

        });

    }

    protected StringResourceModel canRemove(GeoserverUser user) {
        return null;
    }

}
