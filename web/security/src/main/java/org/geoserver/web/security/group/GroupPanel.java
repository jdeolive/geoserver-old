/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;


import java.io.IOException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.Icon;
import org.geoserver.web.wicket.SimpleAjaxLink;

/**
 * A page listing users, allowing for removal, addition and linking to an edit page
 */
@SuppressWarnings("serial")
public class GroupPanel extends Panel {

    protected GeoServerTablePanel<GeoserverUserGroup> groups;
    protected GeoServerDialog dialog;
    protected SelectionGroupRemovalLink removal, removalWithRoles;
    protected Link<?> add;
    protected String serviceName;

    
    protected GeoserverUserGroupService getService() {
        try {
            return GeoServerApplication.get().getSecurityManager().
                    loadUserGroupService(serviceName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public GroupPanel(String id, String serviceName) throws IOException{
        super(id);
        
        this.serviceName=serviceName;
        GroupListProvider provider = new GroupListProvider(serviceName);
        add(groups = new GeoServerTablePanel<GeoserverUserGroup>("table", provider, true) {

            @Override
            protected Component getComponentForProperty(String id, IModel itemModel,
                    Property<GeoserverUserGroup> property) {
                if (property == GroupListProvider.GROUPNAME) {
                    return editGroupLink(id, itemModel, property);
                } else if (property == GroupListProvider.ENABLED) {
                    if((Boolean) property.getModel(itemModel).getObject())
                        return new Icon(id, CatalogIconFactory.ENABLED_ICON);
                    else
                        return new Label(id, "");
                } 
                throw new RuntimeException("Uknown property " + property);
            }
            
            @Override
            protected void onSelectionUpdate(AjaxRequestTarget target) {
                removal.setEnabled(groups.getSelection().size() > 0);               
                target.addComponent(removal);
                removalWithRoles.setEnabled(groups.getSelection().size() > 0);               
                target.addComponent(removalWithRoles);

            }

        });
        groups.setOutputMarkupId(true);
        add(dialog = new GeoServerDialog("dialog"));
        headerComponents();
    }
    
    protected void headerComponents() {

        boolean canCreateStore=getService().canCreateStore();
        // the add button
        
        add(add = new Link("addNew") {
            @Override
            public void onClick() {
                setResponsePage(new NewGroupPage(serviceName,
                        (AbstractSecurityPage)getPage()));
            }            
        });
        add.setVisible(canCreateStore);

        // the removal button
        add(removal = new SelectionGroupRemovalLink(serviceName,"removeSelected", groups, dialog,false));
        removal.setOutputMarkupId(true);
        removal.setEnabled(false);
        removal.setVisibilityAllowed(canCreateStore);

        // the removal button
        add(removalWithRoles  = new SelectionGroupRemovalLink(serviceName,"removeSelectedWithRoles", groups, dialog,true));
        removalWithRoles.setOutputMarkupId(true);
        removalWithRoles.setEnabled(false);
        removalWithRoles.setVisibilityAllowed(canCreateStore&& 
                GeoServerApplication.get().getSecurityManager().getActiveRoleService().canCreateStore());
        
    }


    Component editGroupLink(String id, IModel itemModel, Property<GeoserverUserGroup> property) {
        return new SimpleAjaxLink(id, itemModel, property.getModel(itemModel)) {
            @Override
            protected void onClick(AjaxRequestTarget target) {
                setResponsePage(new EditGroupPage(serviceName,(GeoserverUserGroup) getDefaultModelObject(),
                        (AbstractSecurityPage)getPage()));
            }
        };
    }

}
