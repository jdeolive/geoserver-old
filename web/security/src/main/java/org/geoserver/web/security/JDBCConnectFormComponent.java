/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */


package org.geoserver.web.security;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.geotools.util.logging.Logging;


/**
 * Reusable form component for jdbc connect
 * configurations
 * 
 * @author mcr
 * @param <JDBCConnectConfig>
 *
 */
public class JDBCConnectFormComponent extends 
    FormComponentPanel<JDBCConnectFormComponent.JDBCConnectConfig>{
    
    /**
     * Mode of the panel
     * {@link #JNDI} for JNDI connections
     * {@link #JDBC} for JDBC connections
     * {@link #DYNAMIC} offers both possibilities
     * @author mcr
     *
     */
    public enum Mode {
      JNDI,DRIVER,DYNAMIC  
    };

    
    /**
     * The model object class
     * @author mcr
     *
     */
    public static class JDBCConnectConfig implements Serializable{
        
        private static final long serialVersionUID = 1L;
        public static final String TYPEDRIVER="driver";
        public static final String TYPEJNDI ="jndi";
        
        private String jndiName,username,password,driverName,connectURL,type;

        public String getJndiName() {
            return jndiName;
        }

        public void setJndiName(String jndiName) {
            this.jndiName = jndiName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDriverName() {
            return driverName;
        }

        public void setDriverName(String driverName) {
            this.driverName = driverName;
        }

        public String getConnectURL() {
            return connectURL;
        }

        public void setConnectURL(String connectURL) {
            this.connectURL = connectURL;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
        
    };

    
    private static final long serialVersionUID = 1L;
    static Logger LOGGER = Logging.getLogger("org.geoserver.security");
    
    Mode mode;    
    RadioGroup<String> typeComponent;
    TextField<String> jndiNameComponent;
    Label jndiNameLabel;
    TextField<String> usernameComponent;
    TextField<String> passwordComponent;
    TextField<String> driverNameComponent;
    TextField<String> connectURLComponent;
        
    AjaxSubmitLink testComponent;

        
    public JDBCConnectFormComponent(String id, Mode mode) {
        super(id);
        this.mode=mode;
        JDBCConnectConfig config = new JDBCConnectConfig();
        if (mode==Mode.JNDI)
            config.setType(JDBCConnectConfig.TYPEJNDI);
        else
            config.setType(JDBCConnectConfig.TYPEDRIVER);
        setModel(new CompoundPropertyModel<JDBCConnectConfig>(config));
        initializeComponents();
    }
    
    
    public JDBCConnectFormComponent(String id, Mode mode, String jndiName) {
        super(id);
        this.mode=mode;
        if (mode==Mode.DRIVER)
            throw new RuntimeException("Invalid combinaton, mode: "+mode.toString()+
                    " jndiName: "+jndiName);
        
        JDBCConnectConfig config = new JDBCConnectConfig();
        config.setType(JDBCConnectConfig.TYPEJNDI);
        config.setJndiName(jndiName);        
        setModel(new CompoundPropertyModel<JDBCConnectConfig>(config));
        initializeComponents();
    }

    public JDBCConnectFormComponent(String id, Mode mode, String driverName,String url
            ,String username,String password) {
        super(id);
        this.mode=mode;
        if (mode==Mode.JNDI)
            throw new RuntimeException("Invalid combinaton, mode: "+mode.toString()+
                    " driverName: "+driverName);
        
        JDBCConnectConfig config = new JDBCConnectConfig();
        config.setType(JDBCConnectConfig.TYPEDRIVER);
        config.setDriverName(driverName);
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectURL(url);
        setModel(new CompoundPropertyModel<JDBCConnectConfig>(config));
        initializeComponents();
    }
    
    protected void initializeComponents() {
        
        
        
        typeComponent = new RadioGroup<String>("type");
        typeComponent.add(new Radio<String>(JDBCConnectConfig.TYPEDRIVER,
                new PropertyModel<String>(getModelObject(), "type")));
        typeComponent.add(new Radio<String>(JDBCConnectConfig.TYPEJNDI,
                new PropertyModel<String>(getModelObject(), "type")));
        
//        List<String> types= new ArrayList<String>();
//        types.add(JDBCConnectConfig.TYPEDRIVER);
//        types.add(JDBCConnectConfig.TYPEJNDI);
//        typeComponent = new RadioChoice<String>("type",types,new IChoiceRenderer<String>() {
//            private static final long serialVersionUID = 1L;
//
//            @Override
//            public Object getDisplayValue(String object) {
//                return new ResourceModel(
//                        this.getClass().getSimpleName()+"."+object.toString()).getObject();
//            }
//            @Override
//            public String getIdValue(String object, int index) {
//                return object;
//            }
//        });
    
        AjaxFormComponentUpdatingBehavior behavior = new AjaxFormComponentUpdatingBehavior("onchange") { 
                      
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String type = JDBCConnectFormComponent.this.getModelObject().getType();
                boolean isJndi = JDBCConnectConfig.TYPEJNDI.equals(type);
                jndiNameComponent.setVisible(isJndi);
                usernameComponent.setVisible(!isJndi);
                passwordComponent.setVisible(!isJndi);
                driverNameComponent.setVisible(!isJndi);
                connectURLComponent.setVisible(!isJndi);
                target.addComponent(jndiNameComponent);
                target.addComponent(usernameComponent);
                target.addComponent(passwordComponent);
                target.addComponent(driverNameComponent);
                target.addComponent(connectURLComponent);
            }
            };
                        
        typeComponent.setVisible(mode==Mode.DYNAMIC);
        typeComponent.add(behavior);
        add(typeComponent);
        
        boolean isJndi = JDBCConnectConfig.TYPEJNDI.equals(getModelObject().getType());
        
        add(jndiNameLabel=new Label("jndiNameLabel"));
        jndiNameLabel.setVisible(isJndi);
        jndiNameLabel.setOutputMarkupPlaceholderTag(true);
        
        jndiNameComponent = new TextField<String>("jndiName");
        jndiNameComponent.setVisible(isJndi);
        jndiNameComponent.setOutputMarkupPlaceholderTag(true);
        add(jndiNameComponent);

        usernameComponent = new TextField<String>("username");
        usernameComponent.setVisible(!isJndi);
        usernameComponent.setOutputMarkupPlaceholderTag(true);
        add(usernameComponent);
        
        passwordComponent = new TextField<String>("password");
        passwordComponent.setVisible(!isJndi);
        passwordComponent.setOutputMarkupPlaceholderTag(true);
        add(passwordComponent);


        driverNameComponent = new TextField<String>("driverName");
        driverNameComponent.setVisible(!isJndi);
        driverNameComponent.setOutputMarkupPlaceholderTag(true);
        add(driverNameComponent);


        connectURLComponent = new TextField<String>("connectURL");
        connectURLComponent.setVisible(!isJndi);
        connectURLComponent.setOutputMarkupPlaceholderTag(true);
        add(connectURLComponent);
        
        
        testComponent = new AjaxSubmitLink("testConnection") {
            
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                JDBCConnectConfig config = JDBCConnectFormComponent.this.getModelObject();
                
                String msg = null;                
                if (JDBCConnectConfig.TYPEJNDI.equals(config.getType()))
                    msg=testJNDI(config.getJndiName());
                else
                    msg=testDriver(config.getDriverName(), 
                            config.getConnectURL(),config.getUsername(),
                            config.getPassword());
                
                if(msg==null) {
                    this.info(
                            new ResourceModel(
                                JDBCConnectFormComponent.class.getSimpleName()
                                +".connectSuccess").getObject());                    
                } else {
                    this.warn(
                            new ResourceModel(
                                JDBCConnectFormComponent.class.getSimpleName()
                                +".connectFailure").getObject());
                    this.warn(msg);
                }
            }
        };
        add(testComponent);
    }

    public String testDriver(String driverName,String connectUrl,String username,String password) {
        try {
            Class.forName(driverName);
            Connection con = DriverManager.getConnection(connectUrl,username,password);
            con.close();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return ex.getLocalizedMessage();
        }
        return null;
    }
    
    public String testJNDI(String jndiName) {
        try {
        Context initialContext = new InitialContext();        
        DataSource datasource = (DataSource)initialContext.lookup(jndiName);
        Connection con = datasource.getConnection();
        con.close();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return ex.getLocalizedMessage();
        }
        return null;        
    }
    
    @Override
    public void updateModel() {
        updateComponent(typeComponent);
        updateComponent(typeComponent);
        updateComponent(jndiNameComponent);
        updateComponent(usernameComponent);
        updateComponent(passwordComponent);
        updateComponent(driverNameComponent);
        updateComponent(connectURLComponent);
    }
    protected void updateComponent(FormComponent<?> c) {
        if (c.isVisible() && c.isEnabled())
            c.updateModel();
    }
}
