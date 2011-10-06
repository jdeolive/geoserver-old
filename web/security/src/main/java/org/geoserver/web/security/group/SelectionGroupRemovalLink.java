package org.geoserver.web.security.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverStoreFactory;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.ParamResourceModel;

public class SelectionGroupRemovalLink extends AjaxLink<Object> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    GeoServerTablePanel<GeoserverUserGroup> groups;

    GeoServerDialog dialog;
    boolean disassociateRoles =false;
    ConfirmRemovalGroupPanel removePanel;
    GeoServerDialog.DialogDelegate delegate;

    public SelectionGroupRemovalLink(String id, GeoServerTablePanel<GeoserverUserGroup> groups,
            GeoServerDialog dialog,boolean disassociateRoles) {
        super(id);
        this.groups = groups;
        this.dialog = dialog;
        this.disassociateRoles=disassociateRoles;
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        final List<GeoserverUserGroup> selection = groups.getSelection();
        if (selection.size() == 0)
            return;

        dialog.setTitle(new ParamResourceModel("confirmRemoval", this));

        // if there is something to cancel, let's warn the user about what
        // could go wrong, and if the user accepts, let's delete what's needed
        dialog.showOkCancel(target, delegate =new GeoServerDialog.DialogDelegate() {
            protected Component getContents(String id) {
                // show a confirmation panel for all the objects we have to remove
                Model<Boolean> model = new Model<Boolean>(SelectionGroupRemovalLink.this.disassociateRoles); 
                return removePanel=new ConfirmRemovalGroupPanel(id,model,  selection) {
                    @Override
                    protected StringResourceModel canRemove(GeoserverUserGroup group) {
                        return SelectionGroupRemovalLink.this.canRemove(group);
                    }
                };
            }

            protected boolean onSubmit(AjaxRequestTarget target, Component contents) {

                GeoserverUserGroupService ugService = 
                        GeoserverUserDetailsServiceImpl.get().getUserGroupService();
                try {
                    GeoserverUserGroupStore ugStore = GeoserverStoreFactory.Singleton.getStoreFor(ugService);
                    for (GeoserverUserGroup group : removePanel.getRoots()) {                     
                         ugStore.removeGroup(group);
                        // TODO, keep admins ?
                         if (disassociateRoles) {
                             GeoserverGrantedAuthorityStore gaStore = 
                                     GeoserverStoreFactory.Singleton.getStoreFor(
                                             GeoserverUserDetailsServiceImpl.get().getGrantedAuthorityService());
                             List<GeoserverGrantedAuthority> list= new ArrayList<GeoserverGrantedAuthority>();
                             list.addAll(gaStore.getRolesForGroup(group.getGroupname()));
                             for (GeoserverGrantedAuthority role: list)
                                 gaStore.disAssociateRoleFromGroup(role, group.getGroupname());
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
                groups.clearSelection();
                return true;
            }

            @Override
            public void onClose(AjaxRequestTarget target) {
                // if the selection has been cleared out it's sign a deletion
                // occurred, so refresh the table
                if (groups.getSelection().size() == 0) {
                    setEnabled(false);
                    target.addComponent(SelectionGroupRemovalLink.this);
                    target.addComponent(groups);
                }
            }

        });

    }

    protected StringResourceModel canRemove(GeoserverUserGroup group) {
        return null;
    }

}
