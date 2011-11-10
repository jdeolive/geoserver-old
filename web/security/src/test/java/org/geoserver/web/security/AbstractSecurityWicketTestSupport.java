/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.web.security;


import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.PageParameters;
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
import org.geoserver.security.config.impl.MemoryRoleServiceConfigImpl;
import org.geoserver.security.config.impl.MemoryUserGroupServiceConfigImpl;
import org.geoserver.security.impl.AbstractRoleServiceTest;
import org.geoserver.security.impl.AbstractUserGroupServiceTest;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geoserver.security.impl.GeoserverRole;
import org.geoserver.security.impl.MemoryRoleStore;
import org.geoserver.security.impl.MemoryUserGroupStore;
import org.geoserver.security.impl.ReadOnlyRoleService;
import org.geoserver.security.impl.ReadOnlyUGService;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.impl.ServiceAccessRuleDAO;
import org.geoserver.security.password.GeoserverDigestPasswordEncoder;
import org.geoserver.security.password.PasswordValidator;
import org.geoserver.security.xml.XMLRoleServiceTest;
import org.geoserver.security.xml.XMLUserGroupServiceTest;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.GeoServerWicketTestSupport;

public class AbstractSecurityWicketTestSupport extends GeoServerWicketTestSupport {
    
    
    public String getRoleServiceName() {
        return "test";
    }
    public String getUserGroupServiceName() {
        return "test";
    }

    
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
        // run all tests with url param encryption
        getSecurityManager().setEncryptingUrlParams(true); 
    }

    protected void initialize(AbstractUserGroupServiceTest ugTest, AbstractRoleServiceTest gaTest) 
        throws IOException {

        this.ugTest = ugTest;
        this.gaTest = gaTest;

        gaService=gaTest.createRoleService(getRoleServiceName());
        getSecurityManager().setActiveRoleService(gaService);
        ugService=ugTest.createUserGroupService(getUserGroupServiceName());
        //getSecurityManager().setActiveUserGroupService(ugService);
        
        gaStore =  gaTest.createStore(gaService);
        ugStore =  ugTest.createStore(ugService);
        initializeServiceRules();
        initializeDataAccessRules();
    }

    protected void initializeForXML() throws IOException {
        initialize(new XMLUserGroupServiceTest() ,new XMLRoleServiceTest());
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
    
    public String getRORoleServiceName() {
        return "ReadOnlyRoleService";
    }
    public String getROUserGroupServiceName() {
        return "ReadOnlyUGService";
    }

    
                
    protected void activateRORoleService() throws Exception{
        MemoryRoleServiceConfigImpl config = new MemoryRoleServiceConfigImpl();
        config.setName(getRORoleServiceName());
        config.setAdminRoleName(GeoserverRole.ADMIN_ROLE.getAuthority());
        config.setLockingNeeded(false);
        config.setClassName(ReadOnlyRoleService.class.getName());
        getSecurityManager().saveRoleService(config);
        gaService = getSecurityManager().loadRoleService(getRORoleServiceName());
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
        MemoryUserGroupServiceConfigImpl config = new MemoryUserGroupServiceConfigImpl();         
        config.setName(getROUserGroupServiceName());        
        config.setClassName(ReadOnlyUGService.class.getName());
        config.setLockingNeeded(false);
        config.setPasswordEncoderName(GeoserverDigestPasswordEncoder.BeanName);
        config.setPasswordPolicyName(PasswordValidator.DEFAULT_NAME);
        getSecurityManager().saveUserGroupService(config);
        ugService = getSecurityManager().loadUserGroupService(getROUserGroupServiceName());
        ugService.initializeFromConfig(config);
        ugService.setSecurityManager(GeoServerApplication.get().getSecurityManager());
        ugStore = new MemoryUserGroupStore(); 
        ugStore.initializeFromService(ugService);
        ugTest.insertValues(ugStore);
        ugStore.store();
                
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
      
      protected PageParameters getParamsForService(String serviceName) {
          PageParameters result = new PageParameters();
          result.put(AbstractSecurityPage.ServiceNameKey, serviceName);
          return result;
      }
}
