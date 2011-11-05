/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * This class is common helper for
 * {@link AbstractRoleService} and {@link AbstractRoleStore} 
 * to avoid code duplication
 * 
 * @author christian
 *
 */
public class RoleStoreHelper{
    public TreeMap<String,GeoserverRole> roleMap =
            new TreeMap<String,GeoserverRole>();    
    public TreeMap<String, SortedSet<GeoserverRole>>group_roleMap =
            new TreeMap<String, SortedSet<GeoserverRole>>();
    public TreeMap<String, SortedSet<GeoserverRole>> user_roleMap =
            new TreeMap<String, SortedSet<GeoserverRole>>();
    public HashMap<GeoserverRole, GeoserverRole> role_parentMap =
            new HashMap<GeoserverRole, GeoserverRole>();
   
   
   public void clearMaps() {
       roleMap.clear();
       role_parentMap.clear();
       group_roleMap.clear();
       user_roleMap.clear();
   }
   
   public  Map<String,String> getParentMappings() throws IOException {
       Map<String,String> parentMap = new HashMap<String,String>();
       for (GeoserverRole role: roleMap.values()) {
           GeoserverRole parentRole = role_parentMap.get(role); 
           parentMap.put(role.getAuthority(), 
                   parentRole == null ? null : parentRole.getAuthority());
       }
       return Collections.unmodifiableMap(parentMap);
   }
      
   
   public SortedSet<GeoserverRole> getRoles()   throws IOException{              
       SortedSet<GeoserverRole> result = new TreeSet<GeoserverRole>();
       result.addAll(roleMap.values());
       return Collections.unmodifiableSortedSet(result);
   }

   public  SortedSet<GeoserverRole> getRolesForUser(String username)  throws IOException{
       SortedSet<GeoserverRole> roles = user_roleMap.get(username);
       if (roles==null)
           roles=new TreeSet<GeoserverRole>();
       return Collections.unmodifiableSortedSet(roles);
   }

   public  SortedSet<GeoserverRole> getRolesForGroup(String groupname)  throws IOException{
       SortedSet<GeoserverRole> roles = group_roleMap.get(groupname);
       if (roles==null)
           roles=new TreeSet<GeoserverRole>();
       return Collections.unmodifiableSortedSet(roles);
   }

   public GeoserverRole getParentRole(GeoserverRole role)   throws IOException{
       return role_parentMap.get(role);        
   }
   public GeoserverRole getRoleByName(String role) throws  IOException {
       return roleMap.get(role);       
   }
   public SortedSet<String> getGroupNamesForRole(GeoserverRole role) throws IOException {
       SortedSet<String> result = new TreeSet<String>();
       for (Entry<String,SortedSet<GeoserverRole>> entry : group_roleMap.entrySet()) {
           if (entry.getValue().contains(role))
               result.add(entry.getKey());
       }
       return Collections.unmodifiableSortedSet(result);
   }

   public SortedSet<String> getUserNamesForRole(GeoserverRole role) throws IOException{
       SortedSet<String> result = new TreeSet<String>();
       for (Entry<String,SortedSet<GeoserverRole>> entry : user_roleMap.entrySet()) {
           if (entry.getValue().contains(role))
               result.add(entry.getKey());
       }
       return Collections.unmodifiableSortedSet(result);        
   }

}
