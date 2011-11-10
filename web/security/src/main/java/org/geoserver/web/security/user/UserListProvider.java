/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.geoserver.security.impl.GeoserverUser;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * Page listing the users contained in the users.properties file 
 */
@SuppressWarnings("serial")
public class UserListProvider extends GeoServerDataProvider<GeoserverUser> {
    
    public static final Property<GeoserverUser> USERNAME = new BeanProperty<GeoserverUser>("username", "username");
    public static final Property<GeoserverUser> ENABLED = new BeanProperty<GeoserverUser>("enabled", "enabled");
    protected String userGroupServiceName;
    
    public UserListProvider(String userGroupServiceName) {
        this.userGroupServiceName=userGroupServiceName;
    }
/*     
    public static final Property<GeoserverUser> ROLES = new Property<GeoserverUser>() {

        public Comparator<GeoserverUser> getComparator() {
            return new PropertyComparator<GeoserverUser>(this);  
        }

        public IModel getModel(IModel itemModel) {
            return new Model((String) getPropertyValue((GeoserverUser) itemModel.getObject()));
        }

        public String getName() {
            return "roles";
        }

        public Object getPropertyValue(GeoserverUser item) {
            if(item.getAuthorities().size() == 0)
                return "";
            
            StringBuffer sb = new StringBuffer();
            for (GrantedAuthority ga : item.getAuthorities()) {
                sb.append(ga.getAuthority());
                sb.append(",");
            }
            sb.setLength(sb.length() - 1);
            return sb.toString();
        }

        public boolean isVisible() {
            return true;
        }
        
        public boolean isSearchable() {
            return true;
        };
        
    };
    
*/    
    public static final Property<GeoserverUser> HASATTRIBUTES = new Property<GeoserverUser>() {

        @Override
        public String getName() {
            return "hasattributes";
        }

        @Override
        public Object getPropertyValue(GeoserverUser item) {
            if (item.getProperties().size()==0)
                return Boolean.FALSE;
            else
                return Boolean.TRUE;                    
        }

        @Override
        public IModel getModel(IModel itemModel) {
            return new Model((Boolean) getPropertyValue((GeoserverUser) itemModel.getObject()));
        }

        @Override
        public Comparator<GeoserverUser> getComparator() {
            return new PropertyComparator<GeoserverUser>(this);
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public boolean isSearchable() {
            return true;
        }        
    };

    
    
/*    
    public static final Property<GeoserverUser> ADMIN = new Property<GeoserverUser>() {

        public Comparator<GeoserverUser> getComparator() {
            return new PropertyComparator<GeoserverUser>(this);  
        }

        public IModel getModel(IModel itemModel) {
            return new Model((Boolean) getPropertyValue((GeoserverUser) itemModel.getObject()));
        }

        public String getName() {
            return "admin";
        }

        public Object getPropertyValue(GeoserverUser item) {
            for (GrantedAuthority ga : item.getAuthorities()) {
                if(ga.getAuthority().equals("ROLE_ADMINISTRATOR"))
                    return true;
            }
            return false;
        }

        public boolean isVisible() {
            return true;
        }
        
        public boolean isSearchable() {
            return true;
        }
        
    };
*/    
    
//    public static final Property<User> REMOVE = new PropertyPlaceholder<User>("remove");

    @Override
    protected List<GeoserverUser> getItems() {
        SortedSet<GeoserverUser> users=null;
        try {
            users = getApplication().getSecurityManager().loadUserGroupService(userGroupServiceName).getUsers();
        } catch (IOException e) {
            // TODO, is this correct ?
            throw new RuntimeException(e); 
        }
        List<GeoserverUser> userList = new ArrayList<GeoserverUser>();
        userList.addAll(users);
        return userList;
    }

    @Override
    protected List<Property<GeoserverUser>> getProperties() {
        List<Property<GeoserverUser>> result = new ArrayList<GeoServerDataProvider.Property<GeoserverUser>>();
        result.add(USERNAME);
        result.add(ENABLED);
        result.add(HASATTRIBUTES);
//        result.add(ROLES);
//        result.add(ADMIN);        
        return result;
                
    }

}
