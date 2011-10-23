/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.web.security;


import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackMessage;
import org.geoserver.data.test.MockData;
import org.geoserver.security.AccessMode;
import org.geoserver.security.GeoserverRoleService;
import org.geoserver.security.GeoserverRoleStore;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.security.impl.AbstractRoleServiceTest;
import org.geoserver.security.impl.AbstractUserGroupServiceTest;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.MemoryRoleService;
import org.geoserver.security.impl.MemoryRoleStore;
import org.geoserver.security.impl.MemoryUserGroupService;
import org.geoserver.security.impl.MemoryUserGroupStore;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.impl.ServiceAccessRuleDAO;
import org.geoserver.security.jdbc.H2RoleServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;
import org.geoserver.security.xml.XMLRoleServiceTest;
import org.geoserver.security.xml.XMLUserGroupServiceTest;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.GeoServerWicketTestSupport;

public class AbstractSecurityWicketTestSupport extends GeoServerWicketTestSupport {
    
    public static class ReadOnlyGAService extends MemoryRoleService {

        public ReadOnlyGAService() {
            super();            
        }

        @Override
        public boolean canCreateStore() {
            return false;
        }
    };
    
    public static class ReadOnlyUGService extends MemoryUserGroupService {

        public ReadOnlyUGService() {
            super();            
        }
        
        @Override
        public boolean canCreateStore() {
            return false;
        }
    };
    
    protected AbstractRoleServiceTest gaTest;
    protected AbstractUserGroupServiceTest ugTest;
    protected GeoserverUserGroupService ugService;
    protected GeoserverRoleService gaService;
    protected GeoserverRoleStore gaStore;
    protected GeoserverUserGroupStore ugStore;
    
    
    @Override
    protected void setUpInternal() throws Exception {
        login();        
        Locale.setDefault(Locale.ENGLISH);
    }

    
    protected void initializeForXML() throws IOException {
        gaTest = new XMLRoleServiceTest();        
        ugTest = new XMLUserGroupServiceTest();
        gaService=gaTest.createRoleService("test");
        getSecurityManager().setActiveRoleService(gaService);
        ugService=ugTest.createUserGroupService("test");
        getSecurityManager().setActiveUserGroupService(ugService);
        
        gaStore =  gaTest.createStore(gaService);                
        ugStore =  ugTest.createStore(ugService);
        initializeServiceRules();
        initializeDataAccessRules();

    }

    protected void initializeForJDBC() throws Exception {
        gaTest = new H2RoleServiceTest();        
        ugTest = new H2UserGroupServiceTest();
        gaService=gaTest.createRoleService("");
        getSecurityManager().setActiveRoleService(gaService);
        ugService=ugTest.createUserGroupService("");
        getSecurityManager().setActiveUserGroupService(ugService);

                
        // create tables
        gaStore =  gaTest.createStore(gaService);                
        ugStore =  ugTest.createStore(ugService);
        initializeServiceRules();
        initializeDataAccessRules();
    }
    
    protected void addAdditonalData() throws Exception {
        gaStore.associateRoleToGroup(
                gaStore.getRoleByName("ROLE_WMS"), "group1");
        gaStore.associateRoleToGroup(
                gaStore.getRoleByName("ROLE_WFS"), "group1");
        gaStore.store();                    
    }
    protected void insertValues() throws IOException{
        gaTest.insertValues(gaStore);
        gaStore.store();
        ugTest.insertValues(ugStore);
        ugStore.store();
    }
    
    protected void modifyValues() throws IOException{
        gaTest.modifyValues(gaStore);
        gaStore.store();
        ugTest.modifyValues(ugStore);
        ugStore.store();
    }

    protected void removeValues() throws IOException{
        gaTest.removeValues(gaStore);
        gaStore.store();
        ugTest.removeValues(ugStore);
        ugStore.store();
    }
    
                
    protected void activateROGAService() throws Exception{
        SecurityNamedServiceConfig config = new SecurityNamedServiceConfigImpl();
        config.setName("ReadOnlyGAService");        
        gaService = new ReadOnlyGAService();
        gaService.initializeFromConfig(config);
        gaService.setSecurityManager(GeoServerApplication.get().getSecurityManager());
        gaStore = new MemoryRoleStore();
        gaStore.initializeFromService(gaService);
        gaTest.insertValues(gaStore);
        gaStore.store();
        getSecurityManager().setActiveRoleService(gaService);
        gaStore=null;
    }
    
    protected void activateROUGService() throws Exception{
        SecurityNamedServiceConfig config = new SecurityNamedServiceConfigImpl();
        config.setName("ReadOnlyUGService");        
        ugService = new ReadOnlyUGService();
        ugService.initializeFromConfig(config);
        ugService.setSecurityManager(GeoServerApplication.get().getSecurityManager());
        ugStore = new MemoryUserGroupStore(); 
        ugStore.initializeFromService(ugService);
        ugTest.insertValues(ugStore);
        ugStore.store();
        getSecurityManager().setActiveUserGroupService(ugService);
        
        ugStore=null;
    }
    
    public void executeModalWindowClosedCallback(ModalWindow modalWindow) {
        for (IBehavior behavior : modalWindow.getBehaviors()) {
          if (behavior instanceof AbstractDefaultAjaxBehavior) {
            String name = behavior.getClass().getSimpleName();
            if (name.startsWith("WindowClosedBehavior")) {
              tester.executeBehavior((AbstractAjaxBehavior) behavior);
            }
          }
        }
      }

      public void executeModalWindowCloseButtonCallback(ModalWindow modalWindow) {
        for (IBehavior behavior : modalWindow.getBehaviors()) {
          if (behavior instanceof AbstractDefaultAjaxBehavior) {
            String name = behavior.getClass().getSimpleName();
            
            if (name.startsWith("CloseButtonBehavior")) {
              tester.executeBehavior((AbstractAjaxBehavior) behavior);
            }
          }
        }
      } 

      protected void initializeServiceRules() throws IOException {
          ServiceAccessRuleDAO dao = ServiceAccessRuleDAO.get();
          dao.getRules();
          dao.addRule(new ServiceAccessRule("wms", "GetMap", "ROLE_AUTHENTICATED"));                    
          dao.addRule(new ServiceAccessRule("wms", "*", "ROLE_WMS"));
          dao.addRule(new ServiceAccessRule("wfs", "GetFeature", "ROLE_AUTHENTICATED"));                    
          dao.addRule(new ServiceAccessRule("wfs", "*", "ROLE_WFS"));
          dao.addRule(new ServiceAccessRule("*", "*", GeoserverRole.ADMIN_ROLE.getAuthority()));
          dao.storeRules();
      }
      
      protected void initializeDataAccessRules() throws IOException{
/*
 *      List of namespace:layer in test data          
          cdf:Other
          cite:NamedPlaces
          sf:PrimitiveGeoFeature
          cgf:MPoints
          cgf:Points
          cgf:Lines
          cgf:Polygons
          cite:DividedRoutes
          cgf:MLines
          sf:AggregateGeoFeature
          cdf:Locks
          cite:Geometryless
          cite:MapNeatline
          cite:RoadSegments
          cdf:Nulls
          cdf:Deletes
          cite:Forests
          cite:Ponds
          cdf:Updates
          cdf:Fifteen
          cdf:Seven
          cite:Bridges
          sf:GenericEntity
          cite:Lakes
          cite:Buildings
          cite:Streams
          cite:BasicPolygons
          cdf:Inserts
          cgf:MPolygons
          */
                    
//          Catalog catalog = (Catalog) GeoServerExtensions.bean("catalog");                    
//          for (LayerInfo info : catalog.getLayers()) {                            
//              System.out.println(
//                      info.getResource().getNamespace().getName() 
//                      + ":" +info.getName());
//          }
          
          DataAccessRuleDAO dao = DataAccessRuleDAO.get();
          dao.getRules();
          dao.addRule(new DataAccessRule("*", "*", AccessMode.WRITE, 
                  GeoserverRole.ADMIN_ROLE.getAuthority()));
          dao.addRule(new DataAccessRule(MockData.CITE_PREFIX, "*", AccessMode.READ,                   
                  "ROLE_AUTENTICATED"));
          dao.addRule(new DataAccessRule(MockData.CITE_PREFIX, MockData.LAKES.getLocalPart(), AccessMode.WRITE,                   
                  "ROLE_WMS,ROLE_WFS"));
          dao.addRule(new DataAccessRule(MockData.CITE_PREFIX, MockData.BRIDGES.getLocalPart(), AccessMode.WRITE,                   
                  "ROLE_WMS,ROLE_WFS"));          
          dao.storeRules();

      }
      
      protected boolean testErrorMessagesWithRegExp(String regExp) {
          List<Serializable> msgs = tester.getMessages(FeedbackMessage.ERROR);
          for (Serializable msg : msgs) {
              if (msg.toString().matches(regExp))
                  return true;
          }
          return false;
      }
}
