/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.web.security;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.geoserver.web.FormTestPage;

public abstract class AbstractConfirmRemovalPanelTest<T> extends AbstractSecurityWicketTestSupport implements Serializable{
    private static final long serialVersionUID = 1L;

    protected abstract void setupPanel(List<T> roots);
    
    protected abstract T getRemoveableObject() throws Exception;
    protected abstract T getProblematicObject() throws Exception;
    
    protected abstract String getProblematicObjectRegExp() throws Exception;
    protected abstract String getRemoveableObjectRegExp() throws Exception;
    
    protected void removeObject() throws Exception {
        
        insertValues();
                
        T removeableObject = getRemoveableObject();
        assertNotNull(removeableObject);
        
        setupPanel(Collections.singletonList(removeableObject));
        
        
        tester.assertRenderedPage(FormTestPage.class);
        tester.assertNoErrorMessage();
        
        assertTrue(  labelTextForRemovedObjects().matches(getRemoveableObjectRegExp()));
        
        tester.assertVisible("form:panel:removedObjects");
        tester.assertInvisible("form:panel:problematicObjects");
        
        T problematicObject = getProblematicObject();
        
        if (problematicObject!=null) { 
        
            setupPanel(Collections.singletonList(problematicObject));
            tester.assertRenderedPage(FormTestPage.class);
            tester.assertNoErrorMessage();
        
            assertTrue(labelTextForProblematicObjects().matches(getProblematicObjectRegExp()));

            tester.assertInvisible("form:panel:removedObjects");
            tester.assertVisible("form:panel:problematicObjects");
        }

        if (removeableObject !=null && problematicObject !=null) {
            List<T> objects =new ArrayList<T>();
            objects.add(removeableObject);
            objects.add(problematicObject);
        
            setupPanel(objects);
            tester.assertRenderedPage(FormTestPage.class);
            tester.assertNoErrorMessage();

            assertTrue( labelTextForRemovedObjects().matches(getRemoveableObjectRegExp()));
            assertTrue(labelTextForProblematicObjects().matches(getProblematicObjectRegExp()));

            tester.assertVisible("form:panel:removedObjects");
            tester.assertVisible("form:panel:problematicObjects");
        }                
    }
    
    protected String labelTextForRemovedObjects() {
        MultiLineLabel label = (MultiLineLabel) tester.getComponentFromLastRenderedPage(
            "form:panel:removedObjects:rulesRemoved:rules");
        return label.getDefaultModelObjectAsString();
    }
    
    protected String labelTextForProblematicObjects() {
        MultiLineLabel label = (MultiLineLabel) tester.getComponentFromLastRenderedPage(
            "form:panel:problematicObjects:rulesNotRemoved:problems");
        return label.getDefaultModelObjectAsString();
    }    


}
