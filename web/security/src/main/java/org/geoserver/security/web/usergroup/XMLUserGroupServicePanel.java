package org.geoserver.security.web.usergroup;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.geoserver.security.xml.XMLUserGroupServiceConfig;

public class XMLUserGroupServicePanel extends UserGroupServicePanel<XMLUserGroupServiceConfig> {

    public XMLUserGroupServicePanel(String id, IModel<XMLUserGroupServiceConfig> model) {
        super(id, model);

        add(new TextField("fileName").setEnabled(isNew()));
        add(new CheckBox("validating"));
        add(new TextField("checkInterval"));
    }
}
