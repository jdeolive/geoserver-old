/* Copyright (c) 2001 - 2011 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */

package org.geoserver.security.password;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerUserGroupService;
import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.BeanNameAware;


/**
 * Class for Geoserver specific key management
 * 
 * <strong>requires a master password</strong> form
 * {@link MasterPasswordProviderImpl}
 * 
 * The type of the keystore is JCEKS and can be used/modified
 * with java tools like "keytool" from the command line.
 *  *  
 * 
 * @author christian
 *
 */
public class KeyStoreProvider implements BeanNameAware{
    
    public final static String DEFAULT_BEAN_NAME="DefaultKeyStoreProvider";
    public final static String DEFAULT_FILE_NAME="geoserver.jceks";
    public final static String PREPARED_FILE_NAME="geoserver.jceks.new";
    
    public final static String CONFIGPASSWORDKEY = "config:password:key";
    public final static String URLPARAMKEY = "url:param:key";
    public final static String USERGROUP_PREFIX = "ug:";
    public final static String USERGROUP_POSTFIX = ":key";
    
    static protected Logger LOGGER = Logging.getLogger("org.geoserver.security");
    protected String beanName;    
    protected File keyStoreFile;
    protected KeyStore ks;
    
    /**
     * get the singleton
     * 
     * @return
     */
    public static KeyStoreProvider get() {
          return GeoServerExtensions.bean(KeyStoreProvider.class);
    }
    @Override
    public void setBeanName(String name) {
        beanName=name;
    }
    public String getBeanName() {
        return beanName;
    }

    public KeyStoreProvider()  {
        keyStoreFile=getKeyStoreProvderFile();
    }
        
    /**
     * @return the default key store {@link File} object
     */
    public File getKeyStoreProvderFile() {
        GeoServerDataDirectory dd = (GeoServerDataDirectory) GeoServerExtensions.bean("dataDirectory");
        File secRoot=null;
        try {
            secRoot = dd.findOrCreateSecurityRoot();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new File(secRoot,DEFAULT_FILE_NAME);                           
    }
    
    /**
     * Forces a reload of the key store
     * 
     * @throws IOException
     */
    public void reloadKeyStore() throws IOException{
        ks=null;
        assertActivatedKeyStore();
    }

    /**
     * Gets the {@link Key} object for this alias
     * <code>null</code> if the alias does not
     * exist
     * 
     * @param alias
     * @return
     * @throws IOException
     */
    public Key getKey(String alias) throws IOException{
        assertActivatedKeyStore();
        try {
            return ks.getKey(alias, 
                    MasterPasswordProviderImpl.get().getMasterPassword().toCharArray());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Gets the key for encrypting passwords stored
     * in configuration files, may be <code>null</code>
     * 
     * @return
     * @throws IOException
     */
    public String getConfigPasswordKey() throws IOException{
        SecretKey key = getSecretKey(CONFIGPASSWORDKEY);
        if (key==null) return null;
        return new String(key.getEncoded());
    }
    
    /**
     * Checks if a such a key is available
     * without presenting the key itself
     * 
     * @return
     * @throws IOException
     */
    public boolean hasConfigPasswordKey() throws IOException {
        return containsAlias(CONFIGPASSWORDKEY);
    }
    
    /**
     * Gets the key for encrypting url parameters
     * may be <code>null</code>
     * 
     * @return
     * @throws IOException
     */
    public String getUrlParamKey() throws IOException{
        SecretKey key = getSecretKey(URLPARAMKEY);
        if (key==null) return null;
        return new String(key.getEncoded());

    }
    
    /**
     * Checks if a such a key is available
     * without presenting the key itself
     * 
     * @return
     * @throws IOException
     */
    public boolean hasUrlParamKey() throws IOException {
        return containsAlias(URLPARAMKEY);
    }
    
    /**
     * Test it the key store contains a alias
     * 
     * @param alias
     * @return
     * @throws IOException
     */
    public boolean containsAlias(String alias) throws IOException{
        try {
            return ks.containsAlias(alias);
        } catch (KeyStoreException e) {
            throw new IOException(e);
        }
    }
    /**
     * Returns the key for a {@link GeoServerUserGroupService} 
     * service Name. Needed if the service uses symmetric password
     * encryption 
     * 
     * may be <code>null</code>
     * @param serviceName
     * @return
     * @throws IOException
     */
    public String getUserGRoupKey(String serviceName) throws IOException{
        SecretKey key = getSecretKey(aliasForGroupService(serviceName));
        if (key==null) return null;
        return new String(key.getEncoded());

    }
    
    /**
     * Checks if a such a key is available
     * without presenting the key itself
     * 
     * @return
     * @throws IOException
     */
    public boolean hasUserGRoupKey(String serviceName) throws IOException {
        return containsAlias(aliasForGroupService(serviceName));
        
    }

    
    /**
     * Gets the {@link SecretKey} object for this alias
     * <code>null</code> if the alias does not
     * exist
     * @param name
     * @return
     * @throws IOException if the key exists but has the wrong type
     */
    public SecretKey getSecretKey(String name) throws IOException{
        Key key = getKey(name);
        if (key==null) return null;
        if ((key instanceof SecretKey) == false)
            throw new IOException("Invalid key type for: "+name);
        return (SecretKey) key;
    }
    
    /**
     * Gets the {@link SecretKey} object for this alias
     * <code>null</code> if the alias does not
     * exist
     * @param name
     * @return
     * @throws IOException if the key exists but has the wrong type
     */
    public PublicKey getPublicKey(String name) throws IOException{
        Key key = getKey(name);
        if (key==null) return null;
        if ((key instanceof PublicKey) == false)
            throw new IOException("Invalid key type for: "+name);
        return (PublicKey) key;
    }

    /**
     * Gets the {@link PrivateKey} object for this alias
     * <code>null</code> if the alias does not
     * exist
     * @param name
     * @return
     * @throws IOException if the key exists but has the wrong type
     */
    public PrivateKey getPrivateKey(String name) throws IOException{
        Key key = getKey(name);
        if (key==null) return null;
        if ((key instanceof PrivateKey) == false)
            throw new IOException("Invalid key type for: "+name);
        return (PrivateKey) key;
    }

    /**
     * 
     * @param serviceName for a {@link GeoServerUserGroupService}
     * @return the following String
     * {@link #USERGROUP_PREFIX}+serviceName+{@value #USERGROUP_POSTFIX}
     */
    public String aliasForGroupService(String serviceName) {
        StringBuffer buff = new StringBuffer(USERGROUP_PREFIX);
        buff.append(serviceName);
        buff.append(USERGROUP_POSTFIX);
        return buff.toString();            
    }
    
    /**
     * Opens or creates a {@link KeyStore} using the file
     * {@link #DEFAULT_FILE_NAME}
     * 
     * Throws an exception for an invalid master key
     * 
     * @throws IOException 
     */            
    protected void assertActivatedKeyStore() throws IOException {
        if (ks != null) 
            return;
        synchronized (keyStoreFile) {
            if (ks != null) 
                return;
            try {
                String masterPassword = MasterPasswordProviderImpl.get().getMasterPassword();
                checkForNewMasterPassword(); // is there a new prepared key store ??
                ks = KeyStore.getInstance("JCEKS");    
                if (keyStoreFile.exists()==false) { // create an empy one
                    ks.load(null, masterPassword.toCharArray());
                    addInitialKeys();
                    FileOutputStream fos = new FileOutputStream(keyStoreFile);                    
                    ks.store(fos, masterPassword.toCharArray());            
                    fos.close();
                } else {
                    FileInputStream fis =
                            new FileInputStream(keyStoreFile);
                    ks.load(fis, masterPassword.toCharArray());
                    fis.close();
                }
            } catch (Exception ex) {
                if (ex instanceof IOException) // avoid useless wrapping
                    throw (IOException) ex;
                throw new IOException (ex);
            }            
        }
        
    }
    
    /**
     * Adds/replaces a {@link SecretKey} with its alias
     * 
     * @param alias
     * @param key
     * @throws Exception
     */
    public void setSecretKey(String alias, String key  ) throws IOException {
        assertActivatedKeyStore();
        SecretKey mySecretKey=new SecretKeySpec(key.getBytes(),"PBE");
        KeyStore.SecretKeyEntry skEntry =
            new KeyStore.SecretKeyEntry(mySecretKey);
        try {
            ks.setEntry(alias, skEntry, 
                   new KeyStore.PasswordProtection(MasterPasswordProviderImpl.get().
                           getMasterPassword().toCharArray()));
        } catch (KeyStoreException e) {
            throw new IOException(e);
        }
    }
    
    /**
     * Sets  a secret for the name of a {@link GeoServerUserGroupService}
     * @param serviceName
     * @param password
     * @throws IOException
     */
    public void setUserGroupKey(String serviceName,String password) throws IOException{
        String alias = aliasForGroupService(serviceName);
        setSecretKey(alias, password);
    }
    
    /**
     * Remove a key belonging to the alias
     * 
     * @param alias
     * @throws IOException
     */
    public void removeKey(String alias ) throws IOException {
        assertActivatedKeyStore();
        try {
            ks.deleteEntry(alias);
        } catch (KeyStoreException e) {
            throw new IOException(e);
        }
    }

    
    /**
     * Stores the key store to {@link #ks}
     * 
     * @throws IOException
     */
    public void storeKeyStore() throws IOException{
        // store away the keystore
        assertActivatedKeyStore();
        FileOutputStream fos =
            new  FileOutputStream(KeyStoreProvider.get().keyStoreFile);
        try {
            ks.store(fos, MasterPasswordProviderImpl.get().getMasterPassword().toCharArray());
        } catch (Exception e) {
            throw new IOException(e);
        }
        fos.close();
    }
    
    /**
     * Creates initial key entries
     * auto generated keys
     * {@link #CONFIGPASSWORDKEY}
     * {@link #URLPARAMKEY}
     * 
     * @throws IOException
     */
    protected void addInitialKeys() throws IOException {
        String urlKey = RandomPasswordProvider.get().getRandomPassword(32);
        setSecretKey( URLPARAMKEY, urlKey);
        String configPasswordString = RandomPasswordProvider.get().getRandomPassword(32);
        setSecretKey( CONFIGPASSWORDKEY, configPasswordString);
    }
    
    /**
     * Prepares a master password change. The master password is used to encrypt
     * the {@link KeyStore} and each {@link Entry}; 
     * 
     * The password is checked against the {@link PasswordValidator} named
     * {@link PasswordValidator#MASTERPASSWORD_NAME}
     * 
     * A new keystore named {@link #PREPARED_FILE_NAME} is created. All keys
     * a reencrypted with the new password and stored in the new key store.
     *  
     *  
     * 
     * @param newPassword
     * @throws IOException
     */
    public void prepareForMasterPasswordChange(String newPassword) throws IOException{

        // check the if the master password is valid
        GeoServerSecurityManager secManager = 
                GeoServerExtensions.bean(GeoServerSecurityManager.class);
        PasswordValidator val = secManager.loadPasswordValidator(PasswordValidator.MASTERPASSWORD_NAME);
        val.validatePassword(newPassword);
        // OK, master password is valid
        assertActivatedKeyStore();
        File dir = keyStoreFile.getParentFile();
        File newKSFile = new File(dir,PREPARED_FILE_NAME);
        if (newKSFile.exists())
            newKSFile.delete();
        
        try {
            KeyStore oldKS=KeyStore.getInstance("JCEKS");
            FileInputStream fin = new FileInputStream(keyStoreFile);
            oldKS.load(fin, MasterPasswordProviderImpl.get().getMasterPassword().toCharArray());
            fin.close();
            
            KeyStore newKS = KeyStore.getInstance("JCEKS");
            newKS.load(null, newPassword.toCharArray());
            KeyStore.PasswordProtection protectionparam = 
                    new KeyStore.PasswordProtection(newPassword.toCharArray());

            Enumeration<String> enumeration = oldKS.aliases();
            while (enumeration.hasMoreElements()) {
                String alias =enumeration.nextElement();
                Key key = oldKS.getKey(alias, 
                        MasterPasswordProviderImpl.get().getMasterPassword().toCharArray());
                KeyStore.Entry entry =null;
                if (key instanceof SecretKey) 
                    entry = new KeyStore.SecretKeyEntry((SecretKey)key);
                if (key instanceof PrivateKey) 
                    entry = new KeyStore.PrivateKeyEntry((PrivateKey)key,
                            oldKS.getCertificateChain(alias));                         
                if (key instanceof PublicKey) 
                    entry = new KeyStore.TrustedCertificateEntry(oldKS.getCertificate(alias));                         
                if (entry == null)
                    LOGGER.warning("Unknown key in store, alias: "+alias+
                            " class: "+ key.getClass().getName());
                else
                    newKS.setEntry(alias, entry, protectionparam);
            }            
           FileOutputStream fos = new FileOutputStream(newKSFile);                    
           newKS.store(fos, newPassword.toCharArray());            
           fos.close();
            
        } catch (Exception ex) {
            throw new IOException(ex);
        } 
    }

    /**
     * if {@link #DEFAULT_FILE_NAME} and {@link #PREPARED_FILE_NAME} exist,
     * this method checks if {@link #PREPARED_FILE_NAME} can be used
     * with new {@link MasterPasswordProvider.#getMasterPassword() method.
     * 
     * YES: replace the old keystore with the new one
     * 
     * NO: Do nothing, log the problem and use the old configuration
     * A reason may be that the new master password is not properly injected
     * at startup 
     * 
     * @throws IOException
     */
    protected void checkForNewMasterPassword() throws IOException {
        File dir = keyStoreFile.getParentFile();
        File newKSFile = new File(dir,PREPARED_FILE_NAME);
        File oldKSFile = new File(dir,DEFAULT_FILE_NAME);
        
        if (newKSFile.exists()==false)
            return; //nothing to do

        if (oldKSFile.exists()==false)
            return; //not initialized
        
        // Try to open with new password
        FileInputStream fin = new FileInputStream(newKSFile);
        try {
            KeyStore newKS = KeyStore.getInstance("JCEKS");            
            newKS.load(fin, MasterPasswordProviderImpl.get().getMasterPassword().toCharArray());
            
            // to be sure, decrypt all keys
            Enumeration<String> enumeration = newKS.aliases();
            while (enumeration.hasMoreElements()) {
                newKS.getKey(enumeration.nextElement(), 
                        MasterPasswordProviderImpl.get().getMasterPassword().toCharArray());
            }            
            fin.close();
            fin=null;
            if (oldKSFile.delete()==false) { 
                LOGGER.severe("cannot delete " +keyStoreFile.getCanonicalPath());
                return;
            }
            
            if (newKSFile.renameTo(oldKSFile)==false) {
                String msg = "cannot rename "+ newKSFile.getCanonicalPath();
                msg += "to " + oldKSFile.getCanonicalPath();
                msg += "Try to rename manually and restart";
                LOGGER.severe(msg);
                return;
            }
            LOGGER.info("Successfully changed master password");
        } catch (Exception e) {
            String msg = "cannot open new keystore: "+ newKSFile.getCanonicalPath();
            msg+="\ncannot open new keystore: "+ newKSFile.getCanonicalPath();
            msg+="\nIs the new master password activated ? ";
            msg+="\nDetailed message: "+e.getMessage();
            LOGGER.warning(msg);
        } finally {
            if (fin != null)
               try{ 
                   fin.close();
                   } 
                catch (IOException ex) {
                    // give up
                }
        }

    }
}
