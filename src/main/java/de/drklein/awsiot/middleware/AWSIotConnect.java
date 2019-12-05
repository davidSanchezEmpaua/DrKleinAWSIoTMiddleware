/*
 *  @Name  : AWSIotConnect
 * 
 *  @What:  Connect to AWS IoT
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

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import de.drklein.awsiot.middleware.AWSutilities.KeyStorePasswordPair;

public class AWSIotConnect {
    
    public static final AWSIotQos TestTopicQos = AWSIotQos.QOS0;

    public static AWSIotMqttClient awsIotClient;

    public static void setClient(AWSIotMqttClient client) {
        awsIotClient = client;
    }

    public AWSIotConnect() {
        String clientEndpoint = AWSutilities.getConfig("clientEndpoint");
        String clientId = AWSutilities.getConfig("clientId");
        String certificateFile = AWSutilities.getConfig("certificateFile");
        String privateKeyFile = AWSutilities.getConfig("privateKeyFile");

        System.err.println(LocalDateTime.now() + " ---clientEndpoint: " + clientEndpoint);
        System.err.println(LocalDateTime.now() + " ---clientId: " + clientId);
        System.err.println(LocalDateTime.now() + " ---certificateFile: " + certificateFile);
        System.err.println(LocalDateTime.now() + " ---privateKeyFile: " + privateKeyFile);

        if (awsIotClient == null && certificateFile != null && privateKeyFile != null) {
            String algorithm = null;
            System.err.println("---algorithm: " + algorithm);

            KeyStorePasswordPair pair = AWSutilities.getKeyStorePasswordPair(certificateFile, privateKeyFile, algorithm);
            //System.err.println("---pair: " + pair);

            awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
            //System.err.println("-1--awsIotClient: " + awsIotClient);
        }      

        if (awsIotClient == null) {
            throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
        }
        else {
            try {
                awsIotClient.connect();
            } catch (AWSIotException e) {
                // -TO-DO- Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void subscribe(String subscriptionTopic, BlockingQueue<String> queue) {
        System.out.println(LocalDateTime.now() + " ---TopicListener----- Before TestTopicListener--- TestTopic: " + subscriptionTopic);
        
        AWSIotTopic topic = new AWSIotTopicListener(subscriptionTopic, TestTopicQos, queue);

        // System.out.println("---TopicListener----- Subscribe to: " + topic);
        try {
            awsIotClient.subscribe(topic, true);
        } catch (AWSIotException e) {
            // -TO-DO- Auto-generated catch block
            e.printStackTrace();
        }
    }
}