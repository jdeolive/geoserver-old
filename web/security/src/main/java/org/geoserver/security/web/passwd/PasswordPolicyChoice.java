package org.geoserver.security.web.passwd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.geoserver.web.GeoServerApplication;

public class PasswordPolicyChoice extends DropDownChoice<String> {

    public PasswordPolicyChoice(String id) {
        super(id, new PasswordPolicyNamesModel());
    }

    static class PasswordPolicyNamesModel implements IModel<List<String>> {

        List<String> policyNames;

        PasswordPolicyNamesModel() {
            try {
                policyNames = new ArrayList<String>(
                    GeoServerApplication.get().getSecurityManager().listPasswordValidators());
            } catch (IOException e) {
                throw new WicketRuntimeException(e);
            }
        }

        @Override
        public List<String> getObject() {
            return policyNames;
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

    static class PasswordPolicyChoiceRenderer extends ChoiceRenderer<String> {
        @Override
        public Object getDisplayValue(String object) {
            return object;
        }
        @Override
        public String getIdValue(String object, int index) {
            return object;
        }
    }
}
