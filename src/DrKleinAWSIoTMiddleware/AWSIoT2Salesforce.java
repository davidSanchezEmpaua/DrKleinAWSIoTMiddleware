/*
 *  @Name  : AWSIoT2Salesforce
 * 
 *  @What:  Provides a middleware between Kex Push AWS Iot events and Salesforce.
 *          It subscribes to the Kex Push AWS IoT topics and these are piped to Salesforce REST Webhook.
 * 
 *          The events received from Kex Push are buffered and deduplicated before being send to Salesforce.
 *          The Kex Push event payload is the (partner) ID of the record being update. An event is generated
 *          time a field is updated, so multiple updates could be sent when a record is being updated. The
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

package DrKleinAWSIoTMiddleware;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class AWSIoT2Salesforce {

    private static final String PropertyFile = "resources/AWSIoT2Salesforce.properties"; // "DrKleinAWSIoTMiddleware/resources/AWSIoT2Salesforce.properties";
    private static int BOUND = 1000;     // Buffering Queue Size

    private AWSIoT2Salesforce() {
    }

    /*----------------------------------------
     */
    public static void main(String[] args) {
        System.err.println("=====================================================================================================");
        System.err.println("---Working Directory: "+System.getProperty("user.dir"));
        System.err.println("=====================================================================================================");
        System.err.println(LocalDateTime.now() + " ---Initialization---");
        
        AWSutilities.setPropertyFileName(PropertyFile);

        String subscriptionTopic = AWSutilities.getConfig("subscriptionTopic");
        
        // Queue to store messages/events received before they are buffered for sending to Salesforce.
        BlockingQueue<String> queue = new LinkedBlockingQueue<>(BOUND);

        System.err.println(LocalDateTime.now() + " ---Connect to AWS IoT---");
        
        AWSIotConnect awsIotCnt = new AWSIotConnect();

        System.err.println(LocalDateTime.now() + " ---Subscribe to AWS IoT's Topic---");

        awsIotCnt.subscribe(subscriptionTopic, queue);

        System.err.println("---------------------------------Before AWSIotConnect---------------------------------------------");
        
        SalesforceConnect sforceCnt = new SalesforceConnect();

        System.err.println(LocalDateTime.now() + " ---Connect to Salesforce---");

        new Thread(new SendMsg2Salesforce(sforceCnt,queue)).start();
    }
}

