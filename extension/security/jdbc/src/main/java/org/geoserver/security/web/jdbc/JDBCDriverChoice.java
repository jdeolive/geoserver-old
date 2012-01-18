package org.geoserver.security.web.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

/**
 * Drop down choice widget for available JDBC drivers.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class JDBCDriverChoice extends DropDownChoice<String> {

    public JDBCDriverChoice(String id) {
        super(id, new JDBCDriverClassNamesModel());
    }

    static class JDBCDriverClassNamesModel implements IModel<List<String>> {
    
        @Override
        public List<String> getObject() {
            List<String> driverClassNames = new ArrayList<String>();
            Enumeration<Driver> e = DriverManager.getDrivers(); 
            while(e.hasMoreElements()) {
                driverClassNames.add(e.nextElement().getClass().getCanonicalName());
            }
            return driverClassNames;
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