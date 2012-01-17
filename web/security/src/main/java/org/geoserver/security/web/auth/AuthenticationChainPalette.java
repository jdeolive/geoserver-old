package org.geoserver.security.web.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.geoserver.security.web.SecurityManagerConfigModel;
import org.geoserver.web.GeoServerApplication;

public class AuthenticationChainPalette extends Palette<String> {

    public AuthenticationChainPalette(String id) {
        super(id, new AvailableAuthProviderNamesModel(), 
            new ChoiceRenderer() {
            @Override
            public String getIdValue(Object object, int index) {
                return (String) getDisplayValue(object);
            }
            @Override
                public Object getDisplayValue(Object object) {
                     return object.toString();
                }
        }, 10, true);
    }

    static class AvailableAuthProviderNamesModel implements IModel<List<String>> {

        @Override
        public List<String> getObject() {
            try {
                return new ArrayList<String>(
                    GeoServerApplication.get().getSecurityManager().listAuthenticationProviders());
            } catch (IOException e) {
                throw new WicketRuntimeException(e);
            }
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
}
