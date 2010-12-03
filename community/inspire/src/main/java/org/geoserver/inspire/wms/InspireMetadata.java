package org.geoserver.inspire.wms;

public enum InspireMetadata {
    LANGUAGE("inspire.language"),
    METADATA_URL("inpsire.metadataURL");

    public String key;
    private InspireMetadata(String key) {
        this.key = key;
    }
}
