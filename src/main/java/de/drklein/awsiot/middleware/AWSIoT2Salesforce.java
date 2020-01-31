/*
 *  @Name  : AWSIoT2Salesforce
 * 
 *  @What:  Provides a middleware between Kex Push AWS Iot events and Salesforce.
 *          It subscribes to the Kex Push AWS IoT topics and these are piped to Salesforce REST Webhook.
 * 
 *          The events received from Kex Push are buffered and deduplicated before being send to Salesforce.
 *          The Kex Push event payload is the (partner) ID of the record being update. An event is generated
 *          each time a field is updated, so multiple updates could be sent when a record is being updated. The
 *          deduplication mergers these events into a single message before transmitting to Salesforce.
 * 
 *          Events are also buffered, so that a batch of events are transmitted to Salesforce instead of 
 *          every single one. Events are transmitted (by default) to Salesforce when either 100 unique events have been
 *          buffered or after the interval of 60 seconds has elapsed between events. These values can be changed in the
 *          property file.
 *
 *  @Who  :  David Sanchez <david.sanchez@empaua.com>
 *  @When :  2019-10-23
 *
 *   Modification Log
 *   ----------------------------------------------------------------------------------------------------------------------------
 *   #    Who               When            What
 *   ----------------------------------------------------------------------------------------------------------------------------    
 */

package de.drklein.awsiot.middleware;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class AWSIoT2Salesforce {

    private static final String PropertyFile = "resources/AWSIoT2Salesforce.properties";
    private static int BOUND = 1000;     // Buffering Queue Size
    
    public static Boolean VERBOSE = false;

    private AWSIoT2Salesforce() {
    }

    /*----------------------------------------
     */
    public static void main(String[] args) {
        System.err.println("=====================================================================================================");
        System.err.println("---Working Directory: "+System.getProperty("user.dir"));
        System.err.println("=====================================================================================================");
        System.err.println(LocalDateTime.now() + " ---Initialization---");

        AwsConfig awsConfig;

        if(System.getenv("stage").equals("production")) {
            AWSSecretsService secretsService = new AWSSecretsService();
            awsConfig = secretsService.getAwsConfig();
        } else {
            AWSutilities.setPropertyFileName(PropertyFile);
            awsConfig = getLocalConfig();
        }

        if (awsConfig.getLOG_VERBOSE().equals("1")) {
        	VERBOSE = true;
        }

        // Queue to store messages/events received before they are buffered for sending to Salesforce.
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(BOUND);

        System.err.println(LocalDateTime.now() + " ---Connect to AWS IoT---");
        
        AWSIotConnect awsIotCnt = new AWSIotConnect(awsConfig);

        System.err.println(LocalDateTime.now() + " ---Subscribe to AWS IoT's Topic---");

        awsIotCnt.subscribe(awsConfig.getSubscriptionTopic(), queue);

        System.err.println(LocalDateTime.now() + " ---Connect to Salesforce---");
        
        SalesforceConnect sforceCnt = new SalesforceConnect(awsConfig);

        new Thread(new SendMsg2Salesforce(awsConfig, sforceCnt,queue)).start();
    }

    // Local Configuration - loading AWS Config via .properties
    private static AwsConfig getLocalConfig() {
        AwsConfig awsConfig = new AwsConfig();
        awsConfig.setClientEndpoint(AWSutilities.getConfig("clientEndpoint"));
        awsConfig.setClientId(AWSutilities.getConfig("clientId"));
        awsConfig.setSubscriptionTopic(AWSutilities.getConfig("subscriptionTopic"));
        setCertificate(awsConfig);
        setPrivateKey(awsConfig);

        awsConfig.setLOG_VERBOSE(AWSutilities.getConfig("LOG_VERBOSE"));
        awsConfig.setBUFFERING_SIZE(AWSutilities.getConfig("BUFFERING_SIZE"));
        awsConfig.setBUFFERING_TIMELIMIT(AWSutilities.getConfig("BUFFERING_TIMELIMIT"));

        awsConfig.setSF_USERNAME(AWSutilities.getConfig("SF_USERNAME"));
        awsConfig.setSF_PASSWORD(AWSutilities.getConfig("SF_PASSWORD"));
        awsConfig.setSF_SECTOKEN(AWSutilities.getConfig("SF_SECTOKEN"));
        awsConfig.setSF_LOGINURL(AWSutilities.getConfig("SF_LOGINURL"));
        awsConfig.setSF_GRANTSERVICE(AWSutilities.getConfig("SF_GRANTSERVICE"));
        awsConfig.setSF_CLIENTID(AWSutilities.getConfig("SF_CLIENTID"));
        awsConfig.setSF_CLIENTSECRET(AWSutilities.getConfig("SF_CLIENTSECRET"));
        return awsConfig;
    }

    private static void setPrivateKey(AwsConfig awsConfig) {
        String privateKeyFile = AWSutilities.getConfig("privateKeyFile");
        if (privateKeyFile == null) {
            System.err.println(LocalDateTime.now() + " ---ERROR: private key file missing");
        }
        final PrivateKey privateKey = AWSutilities.loadPrivateKeyFromFile(privateKeyFile, "RSA");
        awsConfig.setPrivateKey(privateKey);
    }

    private static void setCertificate(AwsConfig awsConfig) {
        String certificateFile = AWSutilities.getConfig("certificateFile");
        if (certificateFile == null) {
            System.err.println(LocalDateTime.now() + " ---ERROR: certificate file missing");
        }
        final List<Certificate> certChain = AWSutilities.loadCertificatesFromFile(certificateFile);
        awsConfig.setCertificates(certChain);
    }
}

