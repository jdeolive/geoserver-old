/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.wicket.property;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;

/**
 * A form component that can be used to edit {@link Properties}
 */

public  class PropertyEditorFormComponent extends AbstractListEditorFormComponent<PropertyEditorFormComponent.InternalEntry> {
    private static final long serialVersionUID = 1L;

    static protected class InternalEntry implements Serializable{
        private static final long serialVersionUID = 1L;
        private String key;
        private String value;
        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
    
    static List<InternalEntry> convertFromProperties(Properties props) {
        SortedSet<String> sorted = new TreeSet<String>();
        for (Object obj : props.keySet())
            sorted.add(obj.toString());

        List<InternalEntry> entryList = new ArrayList<InternalEntry>();                
        for (String key: sorted) {
            InternalEntry internalEntry = new InternalEntry();
            internalEntry.setKey(key);
            internalEntry.setValue(props.getProperty(key)==null ? "" : props.getProperty(key).toString());
            entryList.add(internalEntry);
        }
        return entryList;
    }

    public PropertyEditorFormComponent(String id, List<InternalEntry> list) {
        super(id,list);
        setType(Properties.class);
    }
    
    public PropertyEditorFormComponent(String id, Properties props) {
        this(id,convertFromProperties(props));
    }    
                        
                   
    protected String[] getHeaderColumnResourceKeys() {
        String prefix = this.getClass().getSimpleName();
        return new String[] {
                prefix+".key",
                prefix+".value"
        };
    }
    
    protected  List<Component> getColumnComponents() {
        List<Component> list = new ArrayList<Component>();
        list.add(new TextField<String>("key"));
        list.add(new TextField<String>("value"));
        return list;
    }

    
    protected InternalEntry createNew() {
        return new InternalEntry();
    }
    
    
    
  public Properties getProperties() {
      Properties props = new Properties();        
      for (InternalEntry e : editorModel.getObject()) {
          if (e.getKey()==null || e.getKey().isEmpty()) 
              continue;            
          props.put(e.getKey(), e.getValue()==null ? "" : e.getValue());
      }
      return props;        
  }


}
