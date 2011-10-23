/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.role;

import java.io.IOException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.Icon;
import org.geoserver.web.wicket.SimpleAjaxLink;

/**
 * A page listing roles, allowing for removal, addition and linking to an edit page
 */
@SuppressWarnings("serial")
public class RolePage extends AbstractSecurityPage {

    protected GeoServerTablePanel<GeoserverRole> roles;
    protected GeoServerDialog dialog;
    protected SelectionRoleRemovalLink removal;
    protected BookmarkablePageLink<NewRolePage> add;

    public RolePage() {
        super(null);
        RoleListProvider provider = new RoleListProvider();
        add(roles = new GeoServerTablePanel<GeoserverRole>("table", provider, true) {

            @Override
            protected Component getComponentForProperty(String id, IModel itemModel,
                    Property<GeoserverRole> property) {
                if (property == RoleListProvider.ROLENAME) {
                    return editRoleLink(id, itemModel, property);
                } else if (property == RoleListProvider.PARENTROLENAME) {
                        return editParentRoleLink(id, itemModel, property);                    
                } else if (property == RoleListProvider.HASROLEPARAMS) {
                    if((Boolean) property.getModel(itemModel).getObject())
                        return new Icon(id, CatalogIconFactory.ENABLED_ICON);
                    else
                        return new Label(id, "");
                } 
                throw new RuntimeException("Uknown property " + property);
            }
            
            @Override
            protected void onSelectionUpdate(AjaxRequestTarget target) {
                removal.setEnabled(roles.getSelection().size() > 0);               
                target.addComponent(removal);
            }

        });
        roles.setOutputMarkupId(true);
        add(dialog = new GeoServerDialog("dialog"));
        setHeaderPanel(headerPanel());

    }
    
    protected Component headerPanel() {
        Fragment header = new Fragment(HEADER_PANEL, "header", this);

        // the add button
        header.add(add=new BookmarkablePageLink<NewRolePage>("addNew", NewRolePage.class));
        add.setVisible(hasRoleStore());

        // the removal button
        header.add(removal = new SelectionRoleRemovalLink("removeSelected", roles, dialog));
        removal.setOutputMarkupId(true);
        removal.setEnabled(false);
        removal.setVisible(hasRoleStore());

        return header;
    }

//    AjaxLink addRoleLink() {
//        return new AjaxLink("addRole", new Model()) {
//
//            @Override
//            public void onClick(AjaxRequestTarget target) {
//                setResponsePage(new NewRolePage());
//            }
//
//        };
//    }

    @SuppressWarnings("unchecked")
    Component editRoleLink(String id, IModel itemModel, Property<GeoserverRole> property) {
        return new SimpleAjaxLink(id, itemModel, property.getModel(itemModel)) {

            @Override
            protected void onClick(AjaxRequestTarget target) {
                setResponsePage(new EditRolePage( (GeoserverRole) getDefaultModelObject()));
            }

        };
    }
    
    @SuppressWarnings("unchecked")
    Component editParentRoleLink(String id, IModel itemModel, Property<GeoserverRole> property) {
        return new SimpleAjaxLink(id, itemModel, property.getModel(itemModel)) {

            @Override
            protected void onClick(AjaxRequestTarget target) {
                GeoserverRole role = (GeoserverRole) getDefaultModelObject();
                GeoserverRole parentRole;
                try {
                    parentRole = getSecurityManager().getActiveRoleService().getParentRole(role);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                setResponsePage(new EditRolePage( parentRole));
            }

        };
    }


}
