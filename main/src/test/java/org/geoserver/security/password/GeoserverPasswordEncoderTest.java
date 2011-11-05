package org.geoserver.security.password;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoserverUserGroupService;
import org.geoserver.security.xml.XMLUserGroupService;
import org.geoserver.test.GeoServerTestSupport;
import org.geotools.util.logging.Logging;

public class GeoserverPasswordEncoderTest extends GeoServerTestSupport {

    
    protected String testPassword="geoserver";
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");

    @Override
    protected String[] getSpringContextLocations() {
        String[] locations = super.getSpringContextLocations();
        String[] newLocations=null;
        String classPath = "classpath*:/passwordSecurityContext.xml";
        if (locations==null) {
            newLocations = new String[] {classPath};
        } else {        
            newLocations =Arrays.copyOf(locations, locations.length+1);
            newLocations[newLocations.length-1]=classPath;
        }
        return newLocations;
    }

        
    public void testPlainTextEncoder() {
        GeoserverPasswordEncoder encoder = 
                GeoserverPlainTextPasswordEncoder.get();

        assertEquals(PasswordEncodingType.PLAIN,encoder.getEncodingType());
        assertEquals("plain:"+testPassword,encoder.encodePassword(testPassword, null));
        assertTrue(encoder.isResponsibleForEncoding("plain:123"));
        assertFalse(encoder.isResponsibleForEncoding("digest1:123"));
        
        String enc = encoder.encodePassword(testPassword, null);
        assertTrue(encoder.isPasswordValid(enc, testPassword, null));
        assertFalse(encoder.isPasswordValid(enc, "plain:blabla", null));
        
        assertEquals(testPassword, encoder.decode(enc));
        
        enc = encoder.encodePassword("", null);
        assertTrue(encoder.isPasswordValid(enc, "", null));
        
    }
    
    public void testDigestEncoder() {
        
        
        GeoserverPasswordEncoder encoder = 
                GeoserverDigestPasswordEncoder.get();

        assertEquals(PasswordEncodingType.DIGEST,encoder.getEncodingType());
        assertTrue(encoder.encodePassword(testPassword, null).startsWith("digest1:"));
        
        String enc = encoder.encodePassword(testPassword, null);
        assertTrue(encoder.isPasswordValid(enc, testPassword, null));
        assertFalse(encoder.isPasswordValid(enc, "digest1:blabla", null));
        

        boolean fail = true;
        try {
            encoder.decode(enc);
        } catch (UnsupportedOperationException ex) {            
            fail = false;
        }
        assertFalse("Must fail, digested passwords cannot be decoded", fail);

        enc = encoder.encodePassword("", null);
        assertTrue(encoder.isPasswordValid(enc, "", null));
        

        
        // Test if encoding does not change between versions 
        assertTrue(encoder.isPasswordValid(
                "digest1:CTBPxdfHvqy0K0M6uoYlb3+fPFrfMhpTm7+ey5rL/1xGI4s6g8n/OrkXdcyqzJ3D",
                testPassword,null));
                
    }

    protected List<String> getConfigPBEEncoderNames() {
        List<String> result = new ArrayList<String>();
        result.add(GeoserverConfigPBEPasswordEncoder.BeanName);
        if (AbstractGeoserverPasswordEncoder.isStrongCryptographyAvailable()) {
            result.add(GeoserverConfigPBEPasswordEncoder.StrongBeanName);
        } else {
            LOGGER.warning("Skipping strong encryption tests for configuration passwords");
        }
        return result;
    }
    
    public void testConfigPBEEncoder() {
        
        // TODO runs from eclpise, but not from mnv clean install 
        //assertTrue("masterpw".equals(MasterPasswordProviderImpl.get().getMasterPassword()));
        
        System.out.println("Strong cryptography enabled: " +
                AbstractGeoserverPasswordEncoder.isStrongCryptographyAvailable());

        

        List<String> encoderNames = getConfigPBEEncoderNames();
        for (String encoderName: encoderNames) {
            GeoserverPasswordEncoder encoder = (GeoserverPBEPasswordEncoder) 
                    GeoServerExtensions.bean(encoderName);

            assertEquals(PasswordEncodingType.ENCRYPT,encoder.getEncodingType());
            
            assertTrue(encoder.encodePassword(testPassword, null).
                    startsWith(encoder.getPrefix()+AbstractGeoserverPasswordEncoder.PREFIX_DELIMTER));
            
            String enc = encoder.encodePassword(testPassword, null);
            assertTrue(encoder.isPasswordValid(enc, testPassword, null));
            assertFalse(encoder.isPasswordValid(enc, "crypt1:blabla", null));
            
            assertEquals(testPassword, encoder.decode(enc));

            // empty Passwords
            enc = encoder.encodePassword("", null);
            assertTrue(encoder.isPasswordValid(enc, "", null));            
        }
        
    }
    
    protected List<String> getUserPBEEncoderNames() {
        List<String> result = new ArrayList<String>();
        result.add(GeoserverUserPBEPasswordEncoder.PrototypeName);
        if (AbstractGeoserverPasswordEncoder.isStrongCryptographyAvailable()) {
            result.add(GeoserverUserPBEPasswordEncoder.StrongPrototypeName);
        } else {
            LOGGER.warning("Skipping strong encryption tests for user passwords");
        }
        return result;
    }

    
    public void testUserGroupServiceEncoder() throws Exception {
        
        GeoserverUserGroupService service = getSecurityManager().
                loadUserGroupService(XMLUserGroupService.DEFAULT_NAME);
        
        GeoserverUserPasswordEncoder encoder = (GeoserverUserPasswordEncoder) 
                GeoServerExtensions.bean(GeoserverUserPBEPasswordEncoder.PrototypeName);
        
        
//        boolean fail = true;
//        try {
//            encoder.initializeFor(service);
//        } catch (IOException ex){
//            fail = false;
//        }
//        assertFalse(fail);
        
        String password = "testpassword";
        KeyStoreProvider.get().setUserGroupKey(service.getName(), password);
        
        for (String encoderName: getUserPBEEncoderNames()) {
            encoder = (GeoserverUserPasswordEncoder) 
                    GeoServerExtensions.bean(encoderName);
            encoder.initializeFor(service);
                         
            assertEquals(PasswordEncodingType.ENCRYPT,encoder.getEncodingType());
            assertEquals(((GeoserverUserPBEPasswordEncoder)encoder).getKeyAliasInKeyStore(),
                KeyStoreProvider.get().aliasForGroupService(service.getName()));
            
        
            GeoserverUserPasswordEncoder encoder2 = (GeoserverUserPasswordEncoder) 
                GeoServerExtensions.bean(encoderName);
            encoder2.initializeFor(service);
        
            assertFalse(encoder==encoder2);        
            String enc = encoder.encodePassword(password , null);
            assertTrue(enc.
                    startsWith(encoder.getPrefix()+AbstractGeoserverPasswordEncoder.PREFIX_DELIMTER));
            assertFalse(enc.equals(password ));
            assertTrue(encoder2.isPasswordValid(enc, password , null));
            assertEquals(password ,encoder2.decode(enc));
            assertEquals(password ,encoder.decode(enc));
        }
    }
    
    public void testCustomPasswordProvider() {
        List<GeoserverPasswordEncoder> encoders = GeoServerExtensions.extensions(GeoserverPasswordEncoder.class);
        boolean found = false;
        for (GeoserverPasswordEncoder enc : encoders) {
            if (enc.getPrefix()!= null && enc.getPrefix().equals("plain2")) {
                found=true;
                break;
            }            		
        }
        assertTrue(found);
    }
        
}
