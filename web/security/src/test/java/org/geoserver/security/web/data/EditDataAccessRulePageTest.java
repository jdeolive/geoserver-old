package org.geoserver.security.web.data;

import java.util.List;

import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.util.tester.FormTester;
import org.geoserver.data.test.MockData;
import org.geoserver.security.AccessMode;
import org.geoserver.security.impl.GeoServerRole;
import org.geoserver.security.impl.DataAccessRule;
import org.geoserver.security.impl.DataAccessRuleDAO;
import org.geoserver.security.web.AbstractSecurityWicketTestSupport;
import org.geoserver.security.web.role.NewRolePage;

public class EditDataAccessRulePageTest extends AbstractSecurityWicketTestSupport {

    EditDataAccessRulePage page;
    String ruleName = MockData.CITE_PREFIX+
            "."+MockData.LAKES.getLocalPart()+"."+AccessMode.WRITE.getAlias();
    
    public void testFill() throws Exception {
        
        initializeForXML();
        //insertValues();        
        
        
        tester.startPage(page=new EditDataAccessRulePage(getRule(ruleName)));        
        tester.assertRenderedPage(EditDataAccessRulePage.class);

        tester.assertModelValue("ruleForm:workspace", MockData.CITE_PREFIX);
        tester.assertModelValue("ruleForm:layer", MockData.LAKES.getLocalPart());
        tester.assertModelValue("ruleForm:accessMode", AccessMode.WRITE);
        
        // Does not work with Palette
        //tester.assertModelValue("ruleForm:roles:roles:recorder", { ROLE_WMS,ROLE_WFS });
        
        tester.assertModelValue("ruleForm:roles:hasany",Boolean.FALSE);
        tester.assertComponent("ruleForm:roles:roles:recorder", Recorder.class);
        
        FormTester form = tester.newFormTester("ruleForm");
        form.setValue("roles:hasany", true);
                
        // open new role dialog again to ensure that the current state is not lost
        form.submit("roles:addRole");
        tester.assertRenderedPage(NewRolePage.class);
        tester.clickLink("roleForm:cancel");
        tester.assertRenderedPage(EditDataAccessRulePage.class);

        form=tester.newFormTester("ruleForm");        
        form.submit("save");
        
        tester.assertErrorMessages(new String[0]);
        tester.assertRenderedPage(DataSecurityPage.class);

        DataAccessRule rule = getRule(ruleName);
        assertNotNull(rule);
        assertEquals(1,rule.getRoles().size());
        assertEquals(GeoServerRole.HASANY_ROLE,rule.getRoles().iterator().next());        
    }
    
    
    public void testEmptyRoles() throws Exception {
        initializeForXML();
        initializeServiceRules();
        tester.startPage(page=new EditDataAccessRulePage(getRule(ruleName)));
                
        FormTester form = tester.newFormTester("ruleForm");
        form.setValue("roles:roles:recorder", "");                
                        
        form.submit("save");   
        //print(tester.getLastRenderedPage(),true,true);
        assertTrue(testErrorMessagesWithRegExp(".*"+MockData.CITE_PREFIX+"\\."+
                MockData.LAKES.getLocalPart()+".*"));
        tester.assertRenderedPage(EditDataAccessRulePage.class);
    }


    
    public void testReadOnlyRoleService() throws Exception{
        initializeForXML();
        activateRORoleService();
        tester.startPage(page=new EditDataAccessRulePage(getRule(ruleName)));
        tester.assertInvisible("ruleForm:roles:addRole");
    }

    protected int indexOf(List<? extends String> strings, String searchValue) {
        int index =0;
        for (String s : strings) {
            if (s.equals(searchValue))
                return index;
            index++;
        }
        assertTrue(index!=-1);
        return -1;
    }

    DataAccessRule getRule(String key) {
        for (DataAccessRule rule : DataAccessRuleDAO.get().getRules()) {
            if (key.equals(rule.getKey())) {
                return rule;
            }
        }
        return null;
    }
}
