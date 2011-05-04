/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wfs.v2_0;

import net.opengis.wfs20.NativeType;

import org.geoserver.wfs.NativeElementHandler;

public class NativeHandler extends NativeElementHandler {

    public NativeHandler() {
        super(NativeType.class);
    }
}
