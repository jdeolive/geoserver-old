package org.geoserver.security.web.role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.geoserver.web.GeoServerApplication;

public class RoleServiceChoice extends DropDownChoice<String> {

    public RoleServiceChoice(String id) {
        super(id,new RoleServiceNamesModel(), new RoleServiceChoiceRenderer());
    }

    public RoleServiceChoice(String id, IModel<String> model) {
        super(id, model, new RoleServiceNamesModel(), new RoleServiceChoiceRenderer()); 
    }

    static class RoleServiceNamesModel implements IModel<List<String>> {

        List<String> serviceNames;

        RoleServiceNamesModel() {
            try {
                this.serviceNames = new ArrayList(
                    GeoServerApplication.get().getSecurityManager().listRoleServices());
            } catch (IOException e) {
                throw new WicketRuntimeException(e);
            }
        }

        RoleServiceNamesModel(List<String> serviceNames) {
            this.serviceNames = serviceNames;
        }

        @Override
        public List<String> getObject() {
            return serviceNames;
        }

        @Override
        public void detach() {
            //do nothing
        }

        @Override
        public void setObject(List<String> object) {
            throw new UnsupportedOperationException();
        }
    }

    static class RoleServiceChoiceRenderer extends ChoiceRenderer<String> {
        @Override
        public Object getDisplayValue(String object) {
            //do a resource lookup
            return new ResourceModel(object, object).getObject();
        }
    }
}
