/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.wicket.property;

import java.util.List;
import java.util.Properties;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;

/**
 * A form component that can be used to edit {@link Properties}
 */

public abstract class AbstractListEditorFormComponent<T> extends FormComponentPanel<List<T>> {
    private static final long serialVersionUID = 1L;

    public class ColumnLabelFragment extends Fragment {
        private static final long serialVersionUID = 1L;

        public ColumnLabelFragment(String id, String markupId, String resourceKey) {
            super(id, markupId,null,null);
            add(new Label("columnlabel", new ResourceModel(resourceKey)));
        }
    }    

    public abstract class EditorButton extends AjaxButton {
        
        public EditorButton(String id, IModel<String> model) {
            super(id, model);
            
        }


        private static final long serialVersionUID = 1L;    
        private transient ListItem< ? > parent;

        public EditorButton(String id)
        {
            super(id);
        }

        protected final ListItem< ? > getItem()
        {
            if (parent == null)
            {
                parent = findParent(ListItem.class);
            }
            return parent;
        }

        protected final List< ? > getList()
        {
            return getEditor().items;
        }

        protected final ListEditor< ? > getEditor()
        {
            return (ListEditor< ? >)getItem().getParent();
        }


        @Override
        protected void onDetach()
        {
            parent = null;
            super.onDetach();
        }

    }
    
    public class RemoveButton extends EditorButton {
        
        public RemoveButton(String id, IModel<String> model) {
            super(id, model);
            
        }
    
        private static final long serialVersionUID = 1L;
        public RemoveButton(String id)
        {
            super(id);
            setDefaultFormProcessing(false);
        }
    
        
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?>  f)
        {
            ListItem<?> removedItem =getItem();  
            int idx = removedItem.getIndex();
            for (int i = idx + 1; i < getItem().getParent().size(); i++)
            {
               ListItem< ? > item = (ListItem< ? >)getItem().getParent().get(i);
               item.setIndex(item.getIndex() - 1);
            }
            getList().remove(idx);
            target.addComponent(getEditor().getParent());
            getEditor().remove(getItem());
            
            target.prependJavascript(
                    String.format(
                    "Wicket.$('%s').removeChild(Wicket.$('%s'));",
                    "editortable",removedItem.getMarkupId()));
           
           }
    
        @Override
        public boolean isEnabled()
        {
            return getEditor().checkRemove(getItem());
        }
    
    
    }

    
    protected ListEditor<T> editor;
    protected IModel<List<T>> editorModel;
    
    public AbstractListEditorFormComponent(String id, List<T> entryList) {
        
        super(id);
                        
        editorModel =  new ListModel<T>();        
        editorModel.setObject(entryList);

        WebMarkupContainer table =new WebMarkupContainer("editortable");
        table.setOutputMarkupId(true);
        add(table);
        
        editor = new ListEditor<T>("editor",editorModel) {
            private static final long serialVersionUID = 1L;
            
            @Override
            protected void onPopulateItem(ListItem<T> item) {
                item.setModel(new CompoundPropertyModel<T>(item.getModel()));
                for (Component c : getColumnComponents())
                    item.add(c);
                item.add(new RemoveButton("remove"));
            }            
        };
        table.add(editor);
        editor.setOutputMarkupId(true);
        
        Loop loop = new Loop("tableheader", 2) {
            private static final long serialVersionUID = 1L;

            protected void populateItem(LoopItem item) {
                String[] resourceKeys=getHeaderColumnResourceKeys();
                int index = item.getIteration();
                item.add(new ColumnLabelFragment("header", "columnlabelfragment",resourceKeys[index]));
            }
        };
        table.add(loop);

                       
        add(new AjaxButton("add")
            {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?>  f)
            {                  
                ListItem<T> item = editor.addItem(createNew());
                item.setOutputMarkupId(true);
                target.prependJavascript(
                  String.format(
                  "var item=document.createElement('%s');item.id='%s';"+
                  "Wicket.$('%s').appendChild(item);",
                  "tr", item.getMarkupId(), "editortable"));
                                
                target.addComponent(item.getParent().getParent());                
             }
          });        
    }
    
    protected abstract String[] getHeaderColumnResourceKeys();
    protected abstract List<Component> getColumnComponents();
    protected abstract T createNew();
    
    @Override
    public void updateModel() {
        editor.updateModel();
    }        
}
