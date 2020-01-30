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

    public AWSIotConnect(AwsConfig awsConfig) {
        try {
            System.err.println(LocalDateTime.now() + " ---clientEndpoint: " + awsConfig.getClientEndpoint());
            System.err.println(LocalDateTime.now() + " ---clientId: " + awsConfig.getClientId());

            if (awsIotClient == null && awsConfig.getCertificates() != null && awsConfig.getPrivateKey() != null) {
                KeyStorePasswordPair pair = AWSutilities.getKeyStorePasswordPair(awsConfig.getCertificates(), awsConfig.getPrivateKey());
                awsIotClient = new AWSIotMqttClient(awsConfig.getClientEndpoint(), awsConfig.getClientId(), pair.keyStore, pair.keyPassword);

                try {
                    awsIotClient.connect();
                } catch (AWSIotException e) {
                    throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
                }
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
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
