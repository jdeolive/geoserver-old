package org.geoserver.security.password;

import java.util.List;

import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.test.GeoServerTestSupport;

public class GeoserverPasswordEncoderTest extends GeoServerTestSupport {

    protected String testPassword="geoserver";
     
    
    
    public void testPlainTextEncoder() {
        String beanName = "plainTextPasswordEncoder";
        GeoserverPasswordEncoder encoder= 
                (GeoserverPasswordEncoder)GeoServerExtensions.bean(beanName);
        assertEquals(PasswordEncoding.PLAIN,encoder.getEncodingType());
        assertEquals(beanName,encoder.getBeanName());
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
        String beanName = "digestPasswordEncoder";
        GeoserverPasswordEncoder encoder= 
                (GeoserverPasswordEncoder)GeoServerExtensions.bean(beanName);
        assertEquals(PasswordEncoding.DIGEST,encoder.getEncodingType());
        assertEquals(beanName,encoder.getBeanName());
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

    public void testPBEEncoder() {
        String beanName = "pbePasswordEncoder";
        GeoserverPasswordEncoder encoder= 
                (GeoserverPasswordEncoder)GeoServerExtensions.bean(beanName);
        assertEquals(PasswordEncoding.ENCRYPT,encoder.getEncodingType());
        assertEquals(beanName,encoder.getBeanName());
//       
//        boolean fail = true;
//        try {
//            encoder.encodePassword(testPassword, null);
//        } catch (Exception ex) {            
//            fail = false;
//        }
//        assertFalse("Must fail, no master password set", fail);
//        System.setProperty(MasterPasswordProvider.DEFAULT_PROPERTY_NAME, "masterpw");
        
        // test if the injection for MasterPasswordProvder worked.
        assertEquals("masterpw",MasterPasswordProvider.get().getMasterPassword());
        
        assertTrue(encoder.encodePassword(testPassword, null).startsWith("crypt1:"));
        
        String enc = encoder.encodePassword(testPassword, null);
        assertTrue(encoder.isPasswordValid(enc, testPassword, null));
        assertFalse(encoder.isPasswordValid(enc, "crypt1:blabla", null));
        
        assertEquals(testPassword, encoder.decode(enc));

        // empty Passwords
        enc = encoder.encodePassword("", null);
        assertTrue(encoder.isPasswordValid(enc, "", null));


        // Test if encoding does not change between versions 
        assertTrue(encoder.isPasswordValid(
                "crypt1:mA+NK9jLUX1o62/Fino6cSosTED7kJGA",
                testPassword,null));
        
    }
    
    public void testCustomPasswordProvider() {
        List<GeoserverPasswordEncoder> encoders = GeoServerExtensions.extensions(GeoserverPasswordEncoder.class);
        boolean found = false;
        for (GeoserverPasswordEncoder enc : encoders) {
            if (enc.getPrefix().equals("plain2")) {
                found=true;
                break;
            }            		
        }
        assertTrue(found);
    }
        
}
