/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.geoserver.security.impl.GeoserverUserGroup;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.wicket.GeoServerDataProvider;

/**
 * Page listing for {@link GeoserverUserGroup} objects
 * 
 * @author christian
 *
 */
@SuppressWarnings("serial")
public class GroupListProvider extends GeoServerDataProvider<GeoserverUserGroup> {
    
    public static final Property<GeoserverUserGroup> GROUPNAME = new BeanProperty<GeoserverUserGroup>("groupname", "groupname");
    public static final Property<GeoserverUserGroup> ENABLED = new BeanProperty<GeoserverUserGroup>("enabled", "enabled");

    @Override
    protected List<GeoserverUserGroup> getItems() {
        SortedSet<GeoserverUserGroup> groups=null;
        try {
            groups = GeoServerApplication.get().getUserDetails().getUserGroupService().getUserGroups();
        } catch (IOException e) {
            // TODO, is this correct ?
            throw new RuntimeException(e); 
        }
        List<GeoserverUserGroup> groupList = new ArrayList<GeoserverUserGroup>();
        groupList.addAll(groups);
        return groupList;
    }

    @Override
    protected List<Property<GeoserverUserGroup>> getProperties() {
        List<Property<GeoserverUserGroup>> result = new ArrayList<GeoServerDataProvider.Property<GeoserverUserGroup>>();
        result.add(GROUPNAME);
        result.add(ENABLED);
        return result;
    }

}
