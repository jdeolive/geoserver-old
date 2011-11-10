/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.security.SelectionUserRemovalLink;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.web.CatalogIconFactory;
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
public class UserPage extends AbstractSecurityPage {

    protected GeoServerTablePanel<GeoserverUser> users;
    protected GeoServerDialog dialog;
    protected SelectionUserRemovalLink removal,removalWithRoles;
    protected BookmarkablePageLink<NewUserPage> add;
    protected String userGroupServiceName;

    
    public UserPage(PageParameters params) {
        this.userGroupServiceName=params.getString(ServiceNameKey);
        UserListProvider provider = new UserListProvider(userGroupServiceName);
        add(users = new GeoServerTablePanel<GeoserverUser>("table", provider, true) {

            @SuppressWarnings("rawtypes")
            @Override
            protected Component getComponentForProperty(String id, IModel itemModel,
                    Property<GeoserverUser> property) {
                if (property == UserListProvider.USERNAME) {
                    return editUserLink(id, itemModel, property);
                } else if (property == UserListProvider.ENABLED) {
                    if((Boolean) property.getModel(itemModel).getObject())
                        return new Icon(id, CatalogIconFactory.ENABLED_ICON);
                    else
                        return new Label(id, "");
                } else if (property == UserListProvider.HASATTRIBUTES) {
                    if((Boolean) property.getModel(itemModel).getObject())
                        return new Icon(id, CatalogIconFactory.ENABLED_ICON);
                    else
                        return new Label(id, "");                    
                } /*else if (property == UserListProvider.ROLES) {
                    return new Label(id, property.getModel(itemModel));
                } else if (property == UserListProvider.ADMIN) {
                //    return new Label(id, property.getModel(itemModel));
                    if((Boolean) property.getModel(itemModel).getObject())
                        return new Icon(id, CatalogIconFactory.ENABLED_ICON);
                    else
                        return new Label(id, "");
                }
                 */                
                throw new RuntimeException("Uknown property " + property);
            }
            
            @Override
            protected void onSelectionUpdate(AjaxRequestTarget target) {
                removal.setEnabled(users.getSelection().size() > 0);               
                target.addComponent(removal);
                removalWithRoles.setEnabled(users.getSelection().size() > 0);               
                target.addComponent(removalWithRoles);

                
            }

        });
        users.setOutputMarkupId(true);
        add(dialog = new GeoServerDialog("dialog"));
        setHeaderPanel(headerPanel());

    }
    
    protected Component headerPanel() {
        Fragment header = new Fragment(HEADER_PANEL, "header", this);

        // the add button
        header.add(add=new BookmarkablePageLink<NewUserPage>("addNew", NewUserPage.class));
        add.setParameter(ServiceNameKey, userGroupServiceName);
        add.setVisible(hasUserGroupStore(userGroupServiceName));

        // the removal button
        header.add(removal = new SelectionUserRemovalLink(userGroupServiceName,"removeSelected", users, dialog,false));
        removal.setOutputMarkupId(true);
        removal.setEnabled(false);
        removal.setVisible(hasUserGroupStore(userGroupServiceName));
        

        header.add(removalWithRoles = new SelectionUserRemovalLink(userGroupServiceName,"removeSelectedWithRoles", users, dialog,true));
        removalWithRoles.setOutputMarkupId(true);
        removalWithRoles.setEnabled(false);
        removalWithRoles.setVisible(hasUserGroupStore(userGroupServiceName) && 
                hasRoleStore(getSecurityManager().getActiveRoleService().getName()));
        
        
        return header;
    }

//    AjaxLink addUserLink() {
//        return new AjaxLink("addUser", new Model()) {
//
//            @Override
//            public void onClick(AjaxRequestTarget target) {
//                setResponsePage(new NewUserPage());
//            }
//
//        };
//    }

    Component editUserLink(String id, IModel itemModel, Property<GeoserverUser> property) {
        return new SimpleAjaxLink(id, itemModel, property.getModel(itemModel)) {

            @Override
            protected void onClick(AjaxRequestTarget target) {
                setResponsePage(new EditUserPage(userGroupServiceName,(GeoserverUser) getDefaultModelObject()));
            }

        };
    }

}
