package org.geoserver.security.web.role;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.geoserver.security.xml.XMLRoleServiceConfig;

public class XMLRoleServicePanel extends RoleServicePanel<XMLRoleServiceConfig> {

    public XMLRoleServicePanel(String id, IModel<XMLRoleServiceConfig> model) {
        super(id, model);

        add(new TextField("fileName").setEnabled(isNew()));
        add(new CheckBox("validating"));
        add(new TextField("checkInterval"));
    }
}
