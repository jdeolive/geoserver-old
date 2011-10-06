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
import org.geoserver.security.GeoserverGrantedAuthorityService;
import org.geoserver.security.GeoserverGrantedAuthorityStore;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.GeoserverUserGroupStore;
import org.geoserver.security.impl.AbstractGrantedAuthorityServiceTest;
import org.geoserver.security.impl.AbstractUserGroupServiceTest;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geoserver.security.impl.GeoserverGrantedAuthority;
import org.geoserver.security.impl.GeoserverUserDetailsServiceImpl;
import org.geoserver.security.impl.MemoryGrantedAuthorityService;
import org.geoserver.security.impl.MemoryGrantedAuthorityStore;
import org.geoserver.security.impl.MemoryUserGroupService;
import org.geoserver.security.impl.MemoryUserGroupStore;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.impl.ServiceAccessRuleDAO;
import org.geoserver.security.jdbc.H2GrantedAuthorityServiceTest;
import org.geoserver.security.jdbc.H2UserGroupServiceTest;
import org.geoserver.security.xml.XMLGrantedAuthorityServiceTest;
import org.geoserver.security.xml.XMLUserGroupServiceTest;
import org.geoserver.web.GeoServerWicketTestSupport;

public class AbstractSecurityWicketTestSupport extends GeoServerWicketTestSupport {
    
    public static class ReadOnlyGAService extends MemoryGrantedAuthorityService {

        public ReadOnlyGAService(String name) {
            super(name);            
        }        
    };
    
    public static class ReadOnlyUGService extends MemoryUserGroupService {

        public ReadOnlyUGService(String name) {
            super(name);            
        }        
    };
    
    protected AbstractGrantedAuthorityServiceTest gaTest;
    protected AbstractUserGroupServiceTest ugTest;
    protected GeoserverUserGroupService ugService;
    protected GeoserverGrantedAuthorityService gaService;
    protected GeoserverGrantedAuthorityStore gaStore;
    protected GeoserverUserGroupStore ugStore;
    
    
    @Override
    protected void setUpInternal() throws Exception {
        login();        
        Locale.setDefault(Locale.ENGLISH);
    }

    
    protected void initializeForXML() throws IOException {
        gaTest = new XMLGrantedAuthorityServiceTest();        
        ugTest = new XMLUserGroupServiceTest();
        gaService=gaTest.createGrantedAuthorityService("test");
        GeoserverUserDetailsServiceImpl.get().setGrantedAuthorityService(gaService);
        ugService=ugTest.createUserGroupService("test");
        GeoserverUserDetailsServiceImpl.get().setUserGroupService(ugService);
        
        gaStore =  gaTest.createStore(gaService);                
        ugStore =  ugTest.createStore(ugService);
        initializeServiceRules();
        initializeDataAccessRules();

    }

    protected void initializeForJDBC() throws Exception {
        gaTest = new H2GrantedAuthorityServiceTest();        
        ugTest = new H2UserGroupServiceTest();
        gaService=gaTest.createGrantedAuthorityService("");
        GeoserverUserDetailsServiceImpl.get().setGrantedAuthorityService(gaService);
        ugService=ugTest.createUserGroupService("");
        GeoserverUserDetailsServiceImpl.get().setUserGroupService(ugService);
        

        // create tables
        gaStore =  gaTest.createStore(gaService);                
        ugStore =  ugTest.createStore(ugService);
        initializeServiceRules();
        initializeDataAccessRules();
    }
    
    protected void addAdditonalData() throws Exception {
        gaStore.associateRoleToGroup(
                gaStore.getGrantedAuthorityByName("ROLE_WMS"), "group1");
        gaStore.associateRoleToGroup(
                gaStore.getGrantedAuthorityByName("ROLE_WFS"), "group1");
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
        gaService = new ReadOnlyGAService("ReadOnlyGAService");
        gaStore = new MemoryGrantedAuthorityStore("tmpstore");
        gaStore.initializeFromService(gaService);
        gaTest.insertValues(gaStore);
        gaStore.store();
        GeoserverUserDetailsServiceImpl.get().setGrantedAuthorityService(gaService);
        gaStore=null;
    }
    
    protected void activateROUGService() throws Exception{
        ugService = new ReadOnlyUGService("ReadOnlyUGService");
        ugStore = new MemoryUserGroupStore("tmpstore"); 
        ugStore.initializeFromService(ugService);
        ugTest.insertValues(ugStore);
        ugStore.store();
        GeoserverUserDetailsServiceImpl.get().setUserGroupService(ugService);        
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
          dao.addRule(new ServiceAccessRule("*", "*", GeoserverGrantedAuthority.ADMIN_ROLE.getAuthority()));
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
                  GeoserverGrantedAuthority.ADMIN_ROLE.getAuthority()));
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
