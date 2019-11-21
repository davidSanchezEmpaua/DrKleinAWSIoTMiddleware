/*
 *  @Name  : SalesforceConnect
 * 
 *  @What:  Connect to Salesforce Webhook
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

import java.io.IOException;
import java.time.LocalDateTime;

// import java.io.IOException;
// import java.util.HashMap;
// import java.util.Map;

//import com.amazonaws.services.iot.client.AWSIotTopic;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SalesforceConnect {
    
    private String accessToken = null;
    private String instanceUrl = null;

    public SalesforceConnect() {

        final String USERNAME = AWSutilities.getConfig("SF_USERNAME");
        final String PASSWORD = AWSutilities.getConfig("SF_PASSWORD");
        final String SECTOKEN = AWSutilities.getConfig("SF_SECTOKEN");
        final String LOGINURL = AWSutilities.getConfig("SF_LOGINURL");
        final String GRANTSERVICE = AWSutilities.getConfig("SF_GRANTSERVICE");
        final String CLIENTID = AWSutilities.getConfig("SF_CLIENTID");
        final String CLIENTSECRET = AWSutilities.getConfig("SF_CLIENTSECRET");

        if ( AWSutilities.isNullOrEmpty(USERNAME)) {
            System.err.println("Salesforce Username is required.");
            throw new SecurityException("Salesforce Username is required.");
        }
        if ( AWSutilities.isNullOrEmpty(PASSWORD)) {
            System.err.println("Salesforce Password is required.");
            throw new SecurityException("Salesforce Password is required.");
        }
        if ( AWSutilities.isNullOrEmpty(SECTOKEN)) {
            System.err.println("Salesforce Security Token is required.");
            throw new SecurityException("Salesforce Security Token is required.");
        }
        if ( AWSutilities.isNullOrEmpty(LOGINURL)) {
            System.err.println("Salesforce Login URL is required.");
            throw new SecurityException("Salesforce Login URL is required.");
        }
        if ( AWSutilities.isNullOrEmpty(GRANTSERVICE)) {
            System.err.println("Salesforce Grant Service is required.");
            throw new SecurityException("Salesforce Grant Service is required.");
        }
        if ( AWSutilities.isNullOrEmpty(CLIENTID)) {
            System.err.println("Salesforce Client Id is required.");
            throw new SecurityException("Salesforce Client Id is required.");
        }
        if ( AWSutilities.isNullOrEmpty(CLIENTSECRET)) {
            System.err.println("Salesforce Client Secret is required.");
            throw new SecurityException("Salesforce Client Secret is required.");
        }

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        
        // Assemble the login request URL
        String loginURL = LOGINURL +
                            GRANTSERVICE +
                            "&client_id=" + CLIENTID +
                            "&client_secret=" + CLIENTSECRET +
                            "&username=" + USERNAME +
                            "&password=" + PASSWORD + SECTOKEN;

        //System.err.println("---AWSIotMqttClientToSalesforce--- loginURL: "+loginURL);

        // Login requests must be POSTs
        HttpPost httpPost = new HttpPost(loginURL);
        HttpResponse response = null;

        try {
            // Execute the login POST request
            response = httpclient.execute(httpPost);
        } catch (ClientProtocolException cpException) {
            // Handle protocol exception
        } catch (IOException ioException) {
            // Handle system IO exception
        }
        
        // verify response is HTTP OK
        final int statusCode =
        response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            System.out.println("Error authenticating to Force.com: "+statusCode);
            // Error is in EntityUtils.toString(response.getEntity())
            //return null;
        }

        String getResult = null;

        try {
            getResult = EntityUtils.toString(response.getEntity());
        } catch (IOException ioException) {
            // Handle system IO exception
        }

        JSONObject jsonObject = null;
        

        try {
            jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
            accessToken = jsonObject.getString("access_token");
            instanceUrl = jsonObject.getString("instance_url");
        } catch (JSONException jsonException) {
            // Handle JSON exception
        }

        //System.out.println(response.getStatusLine());
        System.out.println(LocalDateTime.now() + " --- Successful Salesforce login");
        
        System.out.println(" instance URL: "+instanceUrl);
        System.out.println(" access token/session ID: "+accessToken);
    }

    public void sendData(String payload) {
        String finalURI = instanceUrl + "/services/apexrest/kexPush/";
        Header oAuthHeader = new BasicHeader("Authorization", "OAuth " + accessToken) ;
        Header printHeader = new BasicHeader("X-PrettyPrint", "1");
        
        try {

        System.out.println("---SalesforceConnect.sendData--- payload: " + payload);

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(finalURI);
        httpPost.addHeader(oAuthHeader);
        httpPost.addHeader(printHeader);
        StringEntity entityBody = new StringEntity(payload);
        entityBody.setContentType("application/json");
        httpPost.setEntity(entityBody);

        HttpResponse httpResponse = httpClient.execute(httpPost);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String responseString = EntityUtils.toString(httpResponse.getEntity());
            System.err.println("Response: " + responseString);
        } else {
            System.err.println(LocalDateTime.now() + " ---ERROR Salesforce Response -- Status code: " + statusCode + " Message: " + EntityUtils.toString(httpResponse.getEntity()));
        }
        } catch (JSONException jsonException) {
            System.err.println(LocalDateTime.now() + " ---ERRORIssue creating JSON or processing results");
            jsonException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}