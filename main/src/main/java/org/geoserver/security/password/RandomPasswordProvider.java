/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */


package org.geoserver.security.password;

import java.security.SecureRandom;
import java.util.logging.Logger;

import org.geoserver.platform.GeoServerExtensions;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Class for generating random passwords using {@link SecureRandom}
 * The password alphabet is {@link #PRINTABLE_ALPHABET}
 * 
 * Since the alphabet is not really big, the length 
 * of the password is important.
 * 
 * @author christian
 *
 */
public class RandomPasswordProvider implements BeanNameAware{
    
    public final static String DEFAULT_BEAN_NAME="DefaultRandomPasswordProvider";    
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");

    
    public static final char[] PRINTABLE_ALPHABET = {
        '!','\"','#','$','%','&','\'','(',
        ')','*','+',',','-','.','/','0',
        '1','2','3','4','5','6','7','8',
        '9',':',';','<','?','@','A','B',
        'C','D','E','F','G','H','I','J',
        'K','L','M','N','O','P','Q','R',
        'S','T','U','V','W','X','Y','Z',
        '[','\\',']','^','_','`','a','b',
        'c','d','e','f','g','h','i','j',
        'k','l','m','n','o','p','q','r',
        's','t','u','v','w','x','y','z',
        '{','|','}','~',
    };

    protected String beanName;    
    protected static RandomPasswordProvider Provider = null;
    
    /**
     * get the singleton
     * 
     * @return
     */
    public static RandomPasswordProvider get() {
        if (Provider!=null) return Provider;
        Provider = GeoServerExtensions.bean(RandomPasswordProvider.class);
        if (Provider==null) {
            Provider = new RandomPasswordProvider();
            Provider.setBeanName(DEFAULT_BEAN_NAME);
        }
        LOGGER.info("Using "+Provider.getBeanName()+ " for obtaining the RandomPasswordProvider implementation" );
        return Provider;
    }
    @Override
    public void setBeanName(String name) {
        beanName=name;
    }
    public String getBeanName() {
        return beanName;
    }

    /**
     * Protected singleton pattern
     * 
     */
    protected RandomPasswordProvider() {        
    }
    
    /**
     * Get the a random password
     * if length <=0, return <code>null</code>
     * 
     * @param length
     * @return
     */
    public String getRandomPassword (int length) {
        if (length <=0) 
            return null; 
        SecureRandom random = new SecureRandom();
        char[] buff = new char[length];
        for (int i = 0;i< length;i++) {
            int index = random.nextInt() % PRINTABLE_ALPHABET.length;
            if (index <0)
                index+=PRINTABLE_ALPHABET.length;
            buff[i]=PRINTABLE_ALPHABET[index];
        }
        return new String(buff);
    }

        
    }
