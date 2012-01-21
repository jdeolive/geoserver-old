/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.wicket.property;

import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;

public class ListItem<T> extends Item<T> {
    private static final long serialVersionUID = 1L;

    public ListItem(String id, int index, T value)
    {
        super(id, index);
        setModel(new ListItemModel(value));
    }

    //private class ListItemModel extends AbstractReadOnlyModel<T>
    private class ListItemModel extends CompoundPropertyModel<T>
    {
        private static final long serialVersionUID = 1L;        
        
                
        public ListItemModel(T value) {
            super(value);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public T getObject()
        {
            return ((ListEditor<T>)ListItem.this.getParent()).items.get(getIndex());
        }
    }
}
