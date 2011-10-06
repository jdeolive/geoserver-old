package org.geoserver.web.security.data;

import java.lang.reflect.Method;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.data.test.MockData;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;


public class DataAccessRulePageTest extends AbstractListPageTest<DataAccessRule> {

    

    protected Class<? extends Page> listPageClass() {
        return DataAccessRulePage.class;
    }

    
    protected Class<? extends Page> newPageClass() {
        return NewDataAccessRulePage.class;
    }
    
    
    protected Class<? extends Page> editPageClass() {
        return EditDataAccessRulePage.class;
    }

    @Override
    protected Property<DataAccessRule> getEditProperty() {
        return DataAccessRuleProvider.RULEKEY;
    }
    
    @Override
    protected boolean checkEditForm(String objectString) {
        String[] array=objectString.split("\\.");
        return  array[0].equals(
                    tester.getComponentFromLastRenderedPage("ruleForm:workspace").getDefaultModelObject()) &&
                array[1].equals( 
                        tester.getComponentFromLastRenderedPage("ruleForm:layer").getDefaultModelObject());
    }
    
    @Override
    protected String getSearchString() throws Exception{
        for (DataAccessRule rule : DataAccessRuleDAO.get().getRules()) {
            if (MockData.CITE_PREFIX.equals(rule.getWorkspace()) && 
                    MockData.BRIDGES.getLocalPart().equals(rule.getLayer()))
                return rule.getKey();
        }
        return null;
    }

    
    
    @Override
    protected void simulateDeleteSubmit() throws Exception {
        
        assertTrue (DataAccessRuleDAO.get().getRules().size()>0);
        
        SelectionDataRuleRemovalLink link = (SelectionDataRuleRemovalLink) getRemoveLink();
        Method m = link.delegate.getClass().getDeclaredMethod("onSubmit", AjaxRequestTarget.class,Component.class);
        m.invoke(link.delegate, null,null);
        
        assertEquals(0,DataAccessRuleDAO.get().getRules().size());        
    }

}
