/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security.config;

import org.apache.wicket.util.tester.FormTester;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.web.security.AbstractSecurityPage;
import org.geoserver.web.security.AbstractSecurityWicketTestSupport;

public  class RememberMeConfigPanelTest extends AbstractSecurityWicketTestSupport {

    protected GlobalTabbedPage page;
    protected FormTester form; 
    protected String formComponentId = AbstractSecurityPage.TabbedPanelId+":panel:RememberMeConfigPanel";
    GeoServerSecurityManager manager; 
    
    protected void newFormTester() {
        form = tester.newFormTester(formComponentId);
    }


    public void testMangerConfigPanel() throws Exception {
        initializeForXML();
        manager= getSecurityManager();
        
        tester.startPage(page=new GlobalTabbedPage());
        tester.assertRenderedPage(GlobalTabbedPage.class);
        tester.clickLink(AbstractSecurityPage.TabbedPanelId+":tabs-container:tabs:1:link",true);
        tester.assertComponent(AbstractSecurityPage.TabbedPanelId+":panel", RememberMeConfigPanel.class);

        newFormTester();
        form.submit("save");
        
        print(page,true,true);
                                                
    }
    
}
