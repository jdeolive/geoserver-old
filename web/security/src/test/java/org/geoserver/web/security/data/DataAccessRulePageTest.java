package org.geoserver.web.security.data;

import java.lang.reflect.Method;
import java.util.Collections;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.geoserver.data.test.MockData;
import org.geoserver.security.AccessMode;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geoserver.web.security.AbstractListPageTest;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;


public class DataAccessRulePageTest extends AbstractListPageTest<DataAccessRule> {

    
    
    protected Page listPage(PageParameters params) {
        return new  DataAccessRulePage();
    }
    protected Page newPage(Object...params) {
        return new  NewDataAccessRulePage();
    }
    protected Page editPage(Object...params) {
        if (params.length==0)
            return new  EditDataAccessRulePage( new DataAccessRule("it.geosolutions", "layer.dots", 
                    AccessMode.READ, Collections.singleton("ROLE_ABC")));
        else
            return new  EditDataAccessRulePage( (DataAccessRule) params[0]);
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
