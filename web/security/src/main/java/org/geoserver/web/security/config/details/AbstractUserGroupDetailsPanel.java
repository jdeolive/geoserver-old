/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.security.config.details;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.security.concurrent.LockingUserGroupService;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.config.impl.SecurityNamedServiceConfigImpl;
import org.geoserver.web.security.config.SecurityNamedConfigModelHelper;

/**
 * A form component without any details
 * 
 * @author christian
 *
 */
public abstract class AbstractUserGroupDetailsPanel extends AbstractNamedConfigDetailsPanel {
    private static final long serialVersionUID = 1L;
    protected CheckBox isLockingNeeded;
    protected DropDownChoice<String> passwordEncoderName,passwordPolicyName;

    
    public AbstractUserGroupDetailsPanel(String id, IModel<SecurityNamedConfigModelHelper> model) {
        super(id,model);
    }

    @Override
    protected void initializeComponents() {
        
        add(isLockingNeeded=new CheckBox("config.isLockingNeeded"));
        
//        passwordEncoderName = new DropDownChoice<String>("config.passwordEncoderName",                  
//                //new PropertyModel<String>(model, "config.className"),
//                classNames,
//                new IChoiceRenderer<String>() {
//                    private static final long serialVersionUID = 1L;
//
//                    @Override
//                    public Object getDisplayValue(String className) {
//                        return new ResourceModel("security."+className,
//                                className).getObject();
//                    }
//
//                    @Override
//                    public String getIdValue(String className, int index) {
//                        return className;
//                    }
//                }
//                );                
//        return;
//    }

//    public String getPasswordEncoderName();
//    public void   setPasswordEncoderName(String name);
//    public String getPasswordPolicyName();
//    public void   setPasswordPolicyName(String name);
//    /**
//     * Indicates if a {@link LockingUserGroupService} wrapper
//     * is created automatically to protect concurrent access
//     * to user/group objects.
//     * 
//     * @return
//     */
//    public boolean isLockingNeeded();
//    public void setLockingNeeded(boolean needed);

                            
}
