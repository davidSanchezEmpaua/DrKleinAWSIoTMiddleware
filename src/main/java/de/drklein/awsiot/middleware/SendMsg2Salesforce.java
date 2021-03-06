/*
 *  @Name  : SendMsg2Salesforce
 * 
 *  @What:  Creates a thread to monitor a BlockingQueue queue for messages/events receive from the subscription to AWS IoT.
 *          The messages are deduplicated using the "vorgangsnummer" and cached to reduce calls to Salesforce.
 *          Messages are send to Salesforce are sent after either 100 unique messages have been buffered or after
 *          an interval of 60 seconds, which ever occurs first. These values can be changed in the property file.
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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

// import org.json.JSONArray;

public class SendMsg2Salesforce implements Runnable {
    private SalesforceConnect sforceCnt;
    private BlockingQueue<String> queue;
    private Map<String,String> messageBuffer = new HashMap<String,String>();

    private int  bufferingSize = 100;
    private long bufferingTimeLimit = 60L;

    /*
        @what: Class constructor
        @parameter: sforceCnt -- connection to Salesforce to where messages should be send
                    queue     -- Queue from where the messages to be sent are retrived.
     */
    public SendMsg2Salesforce(AwsConfig config, SalesforceConnect sforceCnt, BlockingQueue<String> queue) {
        this.queue = queue;
        this.sforceCnt = sforceCnt;

        String buffSizeProperty = config.getBUFFERING_SIZE();
        String buffTimeLmtProperty = config.getBUFFERING_TIMELIMIT();

        if (buffSizeProperty != null)
            bufferingSize = Integer.parseInt(buffSizeProperty);

        if (buffTimeLmtProperty != null)
            bufferingTimeLimit = Long.parseLong(buffTimeLmtProperty);
    }

    public void run() {
        try {
            while (true) {
                String msg = this.queue.poll(bufferingTimeLimit,TimeUnit.SECONDS);
                
                //System.out.println("----------------------------------------------------- ");
                if (msg != null) {
                    //System.out.println("---SendMsg2Salesforce--- " + Thread.currentThread().getName() + " result: " + msg);

                    try {
                        JSONObject jsonObject = (JSONObject) new JSONTokener(msg).nextValue();

                        //System.out.println("----jsonObject: "+jsonObject);

                        String vorgangsnummer = jsonObject.getString("vorgangsnummer");

                        //System.out.println("----vorgangsnummer: "+vorgangsnummer);

                        messageBuffer.put(vorgangsnummer,msg);

                        //System.out.println("---------- size: "+messageBuffer.size());

                        if (messageBuffer.size() >= bufferingSize) {
                        	if (AWSIoT2Salesforce.VERBOSE)
                        		System.out.println("----Reached buffer size------ send message to Salesforce");
                        	
                            sendMessages(this.sforceCnt, messageBuffer);
                        }
                    } catch (JSONException jsonException) {
                        // Handle JSON exception
                    }
                }
                else {
                    // System.out.println("----Timeout------ size: "+messageBuffer.size()+ " isEmpty: " + messageBuffer.isEmpty());
                    if (!messageBuffer.isEmpty()) {
                    	if (AWSIoT2Salesforce.VERBOSE)
                    		System.out.println("----Reached buffering time------ send message to Salesforce");
                    	
                        sendMessages(this.sforceCnt,messageBuffer);
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("----Interrupt------");
            Thread.currentThread().interrupt();
        }
    }

    private void sendMessages(SalesforceConnect sforceCnt, Map<String,String> messages ) {
        JSONObject jsonPayload = new JSONObject();
        
        jsonPayload.put("keyPushMsg",messages.values());
        messages.clear();

        if (AWSIoT2Salesforce.VERBOSE)
        	System.err.println(LocalDateTime.now() + " ----SendMsg2Salesforce.sendMessages: "+jsonPayload.toString());

        if (sforceCnt.sendData(jsonPayload.toString()).equals("INVALID_SESSION_ID")) { // Usually due to session having expired.
        	sforceCnt.login();													       // Try re-login and resend.
        	sforceCnt.sendData(jsonPayload.toString());
        }
    }
}
