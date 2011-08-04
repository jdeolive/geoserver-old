package org.geoserver.geogit;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.geogit.api.GeoGIT;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

public class GEOGITTest extends TestCase {

    private GEOGIT geogitFacade;

    private Catalog mockCatalog;

    private GeoGIT mockGeoGIT;

    @Override
    protected void setUp() throws Exception {
        GrantedAuthority[] credentials = { new GrantedAuthorityImpl("ROLE_ADMINISTRATOR") };
        User principal = new User("admin", "geoserver", true, true, true, true, credentials);
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(principal, credentials);
        SecurityContextHolder.getContext().setAuthentication(mockAuth);

        mockCatalog = mock(Catalog.class);
        mockGeoGIT = mock(GeoGIT.class);
        File baseDirectory = new File(new File("target"), getClass().getName());
        FileUtils.deleteDirectory(baseDirectory);
        baseDirectory.mkdirs();
        GeoServerDataDirectory mockDataDir = new GeoServerDataDirectory(baseDirectory);
        geogitFacade = new GEOGIT(mockCatalog, mockDataDir);
    }

    public void testInitialize() throws Exception {
        fail("Not yet implemented");
        Name typeName = new NameImpl("http://geoserver/geogit/test", "states");
        try {
            geogitFacade.initialize(typeName);
            fail("Expected IAE on non existent type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("No FeatureType"));
        }

        FeatureTypeInfo mockFeatureTypeInfo = mock(FeatureTypeInfo.class);
        when(mockCatalog.getFeatureTypeByName(eq(typeName))).thenReturn(mockFeatureTypeInfo);
        geogitFacade.initialize(typeName);
    }

    public void testIsReplicated() {
        fail("Not yet implemented");
    }

    public void testInitChangeSet() {
        fail("Not yet implemented");
    }

    public void testStageInsert() {
        fail("Not yet implemented");
    }

    public void testStageUpdate() {
        fail("Not yet implemented");
    }

    public void testStageDelete() {
        fail("Not yet implemented");
    }

    public void testCommitChangeSet() {
        fail("Not yet implemented");
    }

    public void testRollBackChangeSet() {
        fail("Not yet implemented");
    }

}
