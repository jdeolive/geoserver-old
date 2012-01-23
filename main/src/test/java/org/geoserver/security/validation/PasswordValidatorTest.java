package org.geoserver.security.validation;

import java.io.IOException;

import org.geoserver.security.config.PasswordPolicyConfig;
import org.geoserver.security.impl.AbstractSecurityServiceTest;
import static org.geoserver.security.validation.PasswordPolicyException.*;

public class PasswordValidatorTest extends AbstractSecurityServiceTest {

    PasswordPolicyConfig config;
    PasswordValidatorImpl validator;
       
    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
        config = new PasswordPolicyConfig();
        validator = new PasswordValidatorImpl(getSecurityManager());
        validator.setConfig(config);
        
    }
    
    public void testPasswords() throws IOException{
        checkForException(null, PW_IS_NULL);
        
        validator.validatePassword("");
        validator.validatePassword("a");
        
        
        checkForException("plain:a", PW_RESERVED_PREFIX,"plain:");
        checkForException("crypt1:a", PW_RESERVED_PREFIX,"crypt1:");
        checkForException("digest1:a", PW_RESERVED_PREFIX,"digest1:");
        
        validator.validatePassword("plain");
        validator.validatePassword("plaina");
        
        config.setMinLength(2);
        checkForException("a", PW_MIN_LENGTH,2);
        validator.validatePassword("aa");
        
        config.setMaxLength(10);
        checkForException("01234567890", PW_MAX_LENGTH,10);
        validator.validatePassword("0123456789");
        
        config.setDigitRequired(true);
        checkForException("abcdef", PW_NO_DIGIT);

        validator.validatePassword("abcde4");
        
        config.setUppercaseRequired(true);
        checkForException("abcdef4", PW_NO_UPPERCASE);
        validator.validatePassword("abcde4F");
        
        config.setLowercaseRequired(true);
        checkForException("ABCDE4F", PW_NO_LOWERCASE);
        validator.validatePassword("abcde4F");        
    }
    
    
    protected void assertSecurityException (IOException ex, String id, Object... params) {
        assertTrue (ex.getCause() instanceof AbstractSecurityException);
        AbstractSecurityException secEx = (AbstractSecurityException) ex.getCause(); 
        assertEquals(id,secEx.getErrorId());
        for (int i = 0; i <  params.length ;i++) {
            assertEquals(params[i], secEx.getArgs()[i]);
        }
    }
        
    protected void checkForException(String password, String id,Object... params) {
        try {
            validator.validatePassword(password);
        } catch (IOException ex) {
            assertSecurityException(ex, id, params);
        }        
    }
}
