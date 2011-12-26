/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.web.security;




import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.web.ComponentBuilder;
import org.geoserver.web.FormTestPage;
import org.geoserver.web.security.JDBCConnectFormComponent.JDBCConnectConfig;
import org.geoserver.web.security.JDBCConnectFormComponent.Mode;

public class JDBCConnectFormComponentTest extends AbstractSecurityWicketTestSupport {

    JDBCConnectFormComponent current;
    
    protected void setupPanel(final JDBCConnectFormComponent.Mode mode) {
        tester.startPage(new FormTestPage(new ComponentBuilder() {
            private static final long serialVersionUID = 1L;

            public Component buildComponent(String id) {
                return current=(new JDBCConnectFormComponent(id, mode));
                };
        }));
    }
    
    public void testJNDI() throws Exception {
        setupPanel(Mode.JNDI);
        tester.assertRenderedPage(FormTestPage.class);
        assertEquals(JDBCConnectConfig.TYPEJNDI,
                current.getModelObject().getType());

        assertModeVisibility(current.mode);
        assertVisibility(true);        
        FormTester ftester = tester.newFormTester("form");
        ftester.setValue("panel:jndiName", "jndiurl" );
        ftester.submit("panel:testConnection");
        assertEquals (2,tester.getMessages(FeedbackMessage.WARNING).size());
        tester.assertRenderedPage(FormTestPage.class);
        //ftester.submit();
        assertEquals("jndiurl",
                current.getModelObject().getJndiName());        
    }
    
    public void testDriver() throws Exception {        

        setupPanel(Mode.DRIVER);
        assertEquals(JDBCConnectConfig.TYPEDRIVER,
                current.getModelObject().getType());

        assertModeVisibility(current.mode);

        assertVisibility(false);
        FormTester ftester = tester.newFormTester("form");
        ftester.setValue("panel:username", "user1" );
        ftester.setValue("panel:password", "pw" );
        ftester.setValue("panel:driverName", "a.b.c" );
        ftester.setValue("panel:connectURL", "jdbc:db2" );

        ftester.submit("panel:testConnection");
        assertEquals (2,tester.getMessages(FeedbackMessage.WARNING).size());
        tester.assertRenderedPage(FormTestPage.class);

//        ftester.submit();

        assertEquals("user1",
                current.getModelObject().getUsername());
        assertEquals("pw",
                current.getModelObject().getPassword());
        assertEquals("a.b.c",
                current.getModelObject().getDriverName());
        assertEquals("jdbc:db2",
                current.getModelObject().getConnectURL());
        
    }

    public void testDynamic() throws Exception {
        setupPanel(Mode.DYNAMIC);
        assertEquals(JDBCConnectConfig.TYPEDRIVER,
                current.getModelObject().getType());

        assertModeVisibility(current.mode);
        assertVisibility(false);
        
        
        tester.executeAjaxEvent("form:panel:type:jndi", "onclick");        
        assertVisibility(true);
        
        FormTester ftester = tester.newFormTester("form");
        ftester.select("panel:type", 1);
        ftester.setValue("panel:jndiName", "jndiurl" );
        ftester.submit("panel:testConnection");
        tester.assertRenderedPage(FormTestPage.class);
        assertEquals (2,tester.getMessages(FeedbackMessage.WARNING).size());
        
        assertEquals(JDBCConnectConfig.TYPEJNDI,
                current.getModelObject().getType());
        assertEquals("jndiurl",
                current.getModelObject().getJndiName());        

        tester.executeAjaxEvent("form:panel:type:driver", "onclick");        
        assertVisibility(false);

        ftester = tester.newFormTester("form");
        ftester.select("panel:type", 0);
        ftester.setValue("panel:username", "user1" );
        ftester.setValue("panel:password", "pw" );
        ftester.setValue("panel:driverName", "a.b.c" );
        ftester.setValue("panel:connectURL", "jdbc:db2" );
        ftester.submit();

        assertEquals(JDBCConnectConfig.TYPEDRIVER,
                current.getModelObject().getType());

        assertEquals("user1",
                current.getModelObject().getUsername());
        assertEquals("pw",
                current.getModelObject().getPassword());
        assertEquals("a.b.c",
                current.getModelObject().getDriverName());
        assertEquals("jdbc:db2",
                current.getModelObject().getConnectURL());
                                
    }


    protected void assertVisibility(boolean isJndi) {
        assertTrue(current.testComponent.isVisible());
        
        assertTrue(current.jndiNameComponent.isVisible()==isJndi);
        assertTrue(current.jndiNameLabel.isVisible()==isJndi);
        
        assertTrue(current.usernameComponent.isVisible()==!isJndi);
        assertTrue(current.usernameLabel.isVisible()==!isJndi);
        assertTrue(current.passwordComponent.isVisible()==!isJndi);
        assertTrue(current.passwordLabel.isVisible()==!isJndi);
        assertTrue(current.driverNameComponent.isVisible()==!isJndi);
        assertTrue(current.driverNameLabel.isVisible()==!isJndi);
        assertTrue(current.connectURLComponent.isVisible()==!isJndi);
        assertTrue(current.connectURLLabel.isVisible()==!isJndi);
                    

    }
    
    protected void assertModeVisibility(Mode mode) {        
        if (mode==Mode.DYNAMIC) {
            assertTrue(current.typeComponent.isVisible());
//            tester.assertVisible("form:panel:type");
//            tester.assertVisible("form:panel:type:driver");
//            tester.assertVisible("form:panel:type:jndi");
        } else
            assertFalse(current.typeComponent.isVisible());
//            tester.assertInvisible("form:panel:type");
//            tester.assertInvisible("form:panel:type:driver");
//            tester.assertInvisible("form:panel:type:jndi");
    }
}
