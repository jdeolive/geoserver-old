/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;


import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.Icon;
import org.geoserver.web.wicket.SimpleAjaxLink;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;

/**
 * A page listing users, allowing for removal, addition and linking to an edit page
 */
@SuppressWarnings("serial")
public class GroupPage extends AbstractSecurityPage {

    protected GeoServerTablePanel<GeoserverUserGroup> groups;
    protected GeoServerDialog dialog;
    protected SelectionGroupRemovalLink removal, removalWithRoles;
    protected BookmarkablePageLink<NewGroupPage> add;

    public GroupPage() {
        super(null);
        GroupListProvider provider = new GroupListProvider();
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
        setHeaderPanel(headerPanel());

    }
    
    protected Component headerPanel() {
        Fragment header = new Fragment(HEADER_PANEL, "header", this);

        // the add button
        header.add(add=new BookmarkablePageLink<NewGroupPage>("addNew", NewGroupPage.class));
        add.setVisible(hasUserGroupStore());

        // the removal button
        header.add(removal = new SelectionGroupRemovalLink("removeSelected", groups, dialog,false));
        removal.setOutputMarkupId(true);
        removal.setEnabled(false);
        removal.setVisibilityAllowed(hasUserGroupStore());

        // the removal button
        header.add(removalWithRoles  = new SelectionGroupRemovalLink("removeSelectedWithRoles", groups, dialog,true));
        removalWithRoles.setOutputMarkupId(true);
        removalWithRoles.setEnabled(false);
        removalWithRoles.setVisibilityAllowed(hasUserGroupStore()&& hasRoleStore());
        
        return header;
    }

//    AjaxLink<Object> addGroupLink() {
//        return new AjaxLink<Object>("addGroup", new Model()) {
//
//            @Override
//            public void onClick(AjaxRequestTarget target) {
//                   setResponsePage(new NewGroupPage());
//            }
//        };
//    }

    Component editGroupLink(String id, IModel itemModel, Property<GeoserverUserGroup> property) {
        return new SimpleAjaxLink(id, itemModel, property.getModel(itemModel)) {
            @Override
            protected void onClick(AjaxRequestTarget target) {
                setResponsePage(new EditGroupPage((GeoserverUserGroup) getDefaultModelObject()));
            }
        };
    }

}
