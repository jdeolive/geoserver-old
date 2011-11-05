package org.geoserver.security.password;

import org.geoserver.security.config.impl.PasswordPolicyConfigImpl;
import org.geoserver.security.impl.AbstractSecurityServiceTest;

public class PasswordValidatorTest extends AbstractSecurityServiceTest {

    PasswordPolicyConfigImpl config;
    PasswordValidatorImpl validator;
       
    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();
        config = new PasswordPolicyConfigImpl();
        validator = new PasswordValidatorImpl();
        validator.setConfig(config);
        
    }
    
    public void testPasswords() throws PasswordValidationException{
        assertTrue(checkForException(null, PasswordInvalidReason.PW_IS_NULL));
        
        validator.validatePassword("");
        validator.validatePassword("a");
        
        assertTrue(checkForException("plain:a", PasswordInvalidReason.PW_RESERVED_PREFIX));
        assertTrue(checkForException("crypt1:a", PasswordInvalidReason.PW_RESERVED_PREFIX));
        assertTrue(checkForException("digest1:a", PasswordInvalidReason.PW_RESERVED_PREFIX));
        
        validator.validatePassword("plain");
        validator.validatePassword("plaina");
        
        config.setMinLength(2);
        assertTrue(checkForException("a", PasswordInvalidReason.PW_MIN_LENGTH));
        validator.validatePassword("aa");
        
        config.setMaxLength(10);
        assertTrue(checkForException("01234567890", PasswordInvalidReason.PW_MAX_LENGTH));
        validator.validatePassword("0123456789");
        
        config.setDigitRequired(true);
        assertTrue(checkForException("abcdef", PasswordInvalidReason.PW_NO_DIGIT));
        validator.validatePassword("abcde4");
        
        config.setUppercaseRequired(true);
        assertTrue(checkForException("abcdef4", PasswordInvalidReason.PW_NO_UPPERCASE));
        validator.validatePassword("abcde4F");
        
        config.setLowercaseRequired(true);
        assertTrue(checkForException("ABCDE4F", PasswordInvalidReason.PW_NO_LOWERCASE));
        validator.validatePassword("abcde4F");        
    }
    
    protected boolean checkForException(String password, PasswordInvalidReason reason) {
        try {
            validator.validatePassword(password);
        } catch (PasswordValidationException ex) {
            if (ex.getReason()==reason)
                return true;
        }
        return false;
    }
}
