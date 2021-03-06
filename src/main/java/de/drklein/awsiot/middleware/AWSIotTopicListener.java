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

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

import java.util.concurrent.BlockingQueue;

/**
 * This class extends {@link AWSIotTopic} to receive messages from a subscribed
 * topic.
 */
public class AWSIotTopicListener extends AWSIotTopic {

    private BlockingQueue<String> msgQueue;

    public AWSIotTopicListener(String topic, AWSIotQos qos, BlockingQueue<String> queue) {
        super(topic, qos);
        this.msgQueue = queue;
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        //System.out.println(System.currentTimeMillis() + ": <<< " + message.getStringPayload());
        try {
            msgQueue.put(message.getStringPayload());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
