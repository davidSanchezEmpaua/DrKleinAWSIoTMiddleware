/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.drklein.awsiot.middleware;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This is a helper class to facilitate reading of the configurations and
 * certificate from the resource files.
 */
public class AWSutilities {
    private static String PropertyFile;

    public static void setPropertyFileName( String propertyFileName ) {
        PropertyFile = propertyFileName;
    }

    public static class KeyStorePasswordPair {
        public KeyStore keyStore;
        public String keyPassword;

        public KeyStorePasswordPair(KeyStore keyStore, String keyPassword) {
            this.keyStore = keyStore;
            this.keyPassword = keyPassword;
        }
    }

    public static String getConfig(String name) {
        Properties prop = new Properties();
        
        try (FileInputStream stream = new FileInputStream(new File(PropertyFile))) {
            prop.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String value = prop.getProperty(name);
        if (value == null || value.trim().length() == 0) {
            return null;
        } else {
            return value;
        }
    }

    public static KeyStorePasswordPair getKeyStorePasswordPair(final List<Certificate> certificates, final PrivateKey privateKey) {
        KeyStore keyStore;
        String keyPassword;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            // randomly generated key password for the key in the KeyStore
            keyPassword = new BigInteger(128, new SecureRandom()).toString(32);

            Certificate[] certChain = new Certificate[certificates.size()];
            certChain = certificates.toArray(certChain);
            keyStore.setKeyEntry("alias", privateKey, keyPassword.toCharArray(), certChain);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            System.err.println(LocalDateTime.now() + " ---ERROR: Failed to create key store");
            return null;
        }

        return new KeyStorePasswordPair(keyStore, keyPassword);
    }

    @SuppressWarnings("unchecked")
    public static List<Certificate> createCertificates(String certificate) {
        try {
            InputStream initialStream = new ByteArrayInputStream(certificate.getBytes());
            BufferedInputStream stream = new BufferedInputStream(initialStream);
            Certificate t =  CertificateFactory.getInstance("X.509").generateCertificate(new BufferedInputStream(new ByteArrayInputStream(certificate.getBytes())));
            List<Certificate> certificates = new ArrayList<>();
            certificates.add(t);
            return certificates;

        } catch (CertificateException e) {
            System.err.println(LocalDateTime.now() + " ---ERROR: Failed to create certificate with " + certificate);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<Certificate> loadCertificatesFromFile(final String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.err.println(LocalDateTime.now() + " ---ERROR: Certificate file: " + filename + " is not found.");
            return null;
        }

        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            final CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            
            return (List<Certificate>) certFactory.generateCertificates(stream);
        } catch (IOException | CertificateException e) {
            System.err.println(LocalDateTime.now() + " ---ERROR: Failed to load certificate file " + filename);
        }
        return null;
    }

    static PrivateKey loadPrivateKeyFromFile(final String filename, final String algorithm) {
        PrivateKey privateKey = null;

        File file = new File(filename);
        if (!file.exists()) {
            System.err.println(LocalDateTime.now() + " ---ERROR: Private key file not found: " + filename);
            return null;
        }
        try (DataInputStream stream = new DataInputStream(new FileInputStream(file))) {
            privateKey = PrivateKeyReader.getPrivateKey(stream, algorithm);
        } catch (IOException | GeneralSecurityException e) {
            System.err.println(LocalDateTime.now() + " ---ERROR: Failed to load private key from file " + filename);
        }

        return privateKey;
    }

    static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
