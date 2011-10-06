package org.geoserver.web.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

public abstract class AbstractConfirmRemovalPanel<T> extends Panel {

    private static final long serialVersionUID = 1L;
    
    List<T> roots;
    List<StringResourceModel> problems;

    public AbstractConfirmRemovalPanel(String id, T... roots) {
        this(id, null,Arrays.asList(roots));
    }
    
    public AbstractConfirmRemovalPanel(String id, Model<?> model,T... roots) {
        this(id, model, Arrays.asList(roots));
    }
    
    public AbstractConfirmRemovalPanel(String id, List<T> roots) {
        this(id,null,roots);
    }

    public AbstractConfirmRemovalPanel(String id,  Model<?> model,List<T> rootObjects) {
        super(id,model);
        setRootObjectsAndProblems(rootObjects);
        

        // add roots
        WebMarkupContainer root = new WebMarkupContainer("rootObjects");
        //root.add(new Label("rootObjectNames", names(roots)));
        //root.setVisible(!roots.isEmpty());
        add(root);

        // removed objects root (we show it if any removed object is on the list)
        WebMarkupContainer removed = new WebMarkupContainer("removedObjects");
        add(removed);

        // removed 
        WebMarkupContainer rulesRemoved = new WebMarkupContainer("rulesRemoved");
        removed.add(rulesRemoved);        
        if (roots.size() == 0)
            removed.setVisible(false);
        else
            rulesRemoved.add(new MultiLineLabel("rules", names(roots)));

        
        WebMarkupContainer problematic = new WebMarkupContainer("problematicObjects");
        add(problematic);

        WebMarkupContainer rulesNotRemoved = new WebMarkupContainer("rulesNotRemoved");
        problematic.add(rulesNotRemoved);
        if (problems.size()==0)
            problematic.setVisible(false);
        else
            rulesNotRemoved.add(new MultiLineLabel("problems", problemsString(problems)));
    }

    void setRootObjectsAndProblems(List<T> rootObjects) {
        roots = new ArrayList<T>();
        problems= new ArrayList<StringResourceModel>();
        for (T obj : rootObjects) {
            StringResourceModel model = canRemove(obj);
            if (model==null)
                roots.add(obj);
            else    
                problems.add(model);
        }
                
    }

    String problemsString(List<StringResourceModel> objects) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < objects.size(); i++) {            
            sb.append(objects.get(i).getObject());
            if (i < (objects.size() - 1))
                sb.append("\n");
        }
        return sb.toString();
    }

    
    String names(List<T> objects) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < objects.size(); i++) {
            sb.append(name(objects.get(i)));
            if (i < (objects.size() - 1))
                //sb.append(", ");
                sb.append("\n");
        }
        return sb.toString();
    }

    String name(T object) {
        try {
            return getConfirmationMessage(object);
        } catch (IOException ioEx) {
            throw new RuntimeException(ioEx);  
        } catch (Exception e) {
            throw new RuntimeException("A data object that does not have "
                    + "a 'name' property has been used, this is unexpected", e);
        }
    }

    protected StringResourceModel canRemove(T data) {
        return null;
    }
    
    abstract protected String getConfirmationMessage(T object) throws Exception;

    public List<T> getRoots() {
        return roots;
    }        
}
