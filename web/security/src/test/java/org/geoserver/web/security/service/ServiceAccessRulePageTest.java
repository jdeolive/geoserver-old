package org.geoserver.web.security.service;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.security.impl.ServiceAccessRule;
import org.geoserver.security.impl.ServiceAccessRuleDAO;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;


    
    
    

public class ServiceAccessRulePageTest extends AbstractListPageTest<ServiceAccessRule> {

    
    
    protected Class<? extends Page> listPageClass() {
        return ServiceAccessRulePage.class;
    }

    
    protected Class<? extends Page> newPageClass() {
        return NewServiceAccessRulePage.class;
    }
    
    
    protected Class<? extends Page> editPageClass() {
        return EditServiceAccessRulePage.class;
    }

    @Override
    protected Property<ServiceAccessRule> getEditProperty() {
        return ServiceAccessRuleProvider.RULEKEY;
    }

    @Override
    protected boolean checkEditForm(String objectString) {
        String[] array=objectString.split("\\.");
        return  array[0].equals(
                    tester.getComponentFromLastRenderedPage("ruleForm:service").getDefaultModelObject()) &&
                array[1].equals( 
                        tester.getComponentFromLastRenderedPage("ruleForm:method").getDefaultModelObject());
    }
    
    @Override
    protected String getSearchString() throws Exception{
        for (ServiceAccessRule rule : ServiceAccessRuleDAO.get().getRules()) {
            if ("wms".equals(rule.getService()) && "GetMap".equals(rule.getMethod()))
                return rule.getKey();
        }
        return null;
    }

    @Override
    protected void simulateDeleteSubmit() throws Exception {
        
        assertTrue (ServiceAccessRuleDAO.get().getRules().size()>0);
        
        SelectionServiceRemovalLink link = (SelectionServiceRemovalLink) getRemoveLink();
        Method m = link.delegate.getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.delegate, null,null);
        
        assertEquals(0,ServiceAccessRuleDAO.get().getRules().size());
        
    }

        

}
