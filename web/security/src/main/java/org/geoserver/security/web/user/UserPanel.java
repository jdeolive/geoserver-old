/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.user;

import java.io.IOException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.geoserver.security.GeoServerUserGroupService;
import org.geoserver.security.impl.GeoServerUser;
import org.geoserver.security.web.AbstractSecurityPage;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.Icon;
import org.geoserver.web.wicket.SimpleAjaxLink;

/**
 * A page listing users, allowing for removal, addition and linking to an edit page
 */
@SuppressWarnings("serial")
public class UserPanel extends Panel {

    protected GeoServerTablePanel<GeoServerUser> users;
    protected GeoServerDialog dialog;
    protected SelectionUserRemovalLink removal,removalWithRoles;
    protected Link<NewUserPage> add;
    protected String serviceName;

    protected GeoServerUserGroupService getService() {
        try {
            return GeoServerApplication.get().getSecurityManager().
                    loadUserGroupService(serviceName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public UserPanel(String id, String serviceName) throws IOException{
        super(id);
        
        this.serviceName=serviceName;
        UserListProvider provider = new UserListProvider(this.serviceName);
        add(users = new GeoServerTablePanel<GeoServerUser>("table", provider, true) {

            @SuppressWarnings("rawtypes")
            @Override
            protected Component getComponentForProperty(String id, IModel itemModel,
                    Property<GeoServerUser> property) {
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
                }                
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
        headerComponents();
        
    }
    
    protected void headerComponents() {

        
        boolean canCreateStore=getService().canCreateStore();

        if (!canCreateStore) {
            add(new Label("message", new StringResourceModel("noCreateStore", this, null))
                .add(new AttributeAppender("class", new Model("info-link"), " ")));
        }
        else {
            add(new Label("message", new Model())
                .add(new AttributeAppender("class", new Model("displayNone"), " ")));
        }

        // the add button
        add(add=new Link("addNew") {
            @Override
            public void onClick() {
                setResponsePage(new NewUserPage(serviceName, 
                        (AbstractSecurityPage) this.getPage()));
            }
        });
        
        //<NewUserPage><NewUserPage>("addNew", NewUserPage.class));
        //add.setParameter(AbstractSecurityPage.ServiceNameKey, serviceName);
        add.setVisible(canCreateStore);

        // the removal button
        add(removal = new SelectionUserRemovalLink(serviceName,"removeSelected", users, dialog,false));
        removal.setOutputMarkupId(true);
        removal.setEnabled(false);
        removal.setVisible(canCreateStore);
        

        add(removalWithRoles = new SelectionUserRemovalLink(serviceName,"removeSelectedWithRoles", users, dialog,true));
        removalWithRoles.setOutputMarkupId(true);
        removalWithRoles.setEnabled(false);
        removalWithRoles.setVisible(canCreateStore && 
                GeoServerApplication.get().getSecurityManager().
                    getActiveRoleService().canCreateStore());
        
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

    Component editUserLink(String id, IModel itemModel, Property<GeoServerUser> property) {
        return new SimpleAjaxLink(id, itemModel, property.getModel(itemModel)) {

            @Override
            protected void onClick(AjaxRequestTarget target) {
                setResponsePage(new EditUserPage(serviceName,(GeoServerUser) getDefaultModelObject(),
                        (AbstractSecurityPage) getPage()));
            }

        };
    }

}
