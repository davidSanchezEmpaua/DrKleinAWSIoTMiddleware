package de.drklein.awsiot.middleware;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;

import java.security.InvalidParameterException;
import java.util.Base64;
import java.util.HashMap;

// https://dzone.com/articles/aws-secret-manager-protect-your-secrets-in-applica
public class AWSSecretsService {
  public AwsConfig getAwsConfig() {
    String secretName = "test/kex-push-service/credentials";
    String region = "us-east-2";
    String secret;
    String decodedBinarySecret;

    AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
        .withRegion(region)
        .build();

    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
        .withSecretId(secretName);

    try {
      GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);

      if(getSecretValueResult == null) {
        return null;
      }

      // Decrypts secret using the associated KMS CMK.
      // Depending on whether the secret is a string or binary, one of these fields will be populated.
      if (getSecretValueResult.getSecretString() != null) {
        secret = getSecretValueResult.getSecretString();

        AwsConfig awsConfig = new AwsConfig();


        System.out.println(secret);
      }
      else {
        decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        System.out.println(decodedBinarySecret);
      }
    }
    catch (DecryptionFailureException e) {
      System.out.println("Decryption Failure due to: " + e.getMessage());
      throw e;
    }
    catch (InternalServiceErrorException e) {
      System.out.println("Internal Server Error due to: " + e.getMessage());
      throw e;
    }
    catch (InvalidParameterException e) {
      System.out.println("The parameter was invalid due to: " + e.getMessage());
      throw e;
    }
    catch (InvalidRequestException e) {
      System.out.println("The request was invalid due to: " + e.getMessage());
      throw e;
    }
    catch (ResourceNotFoundException e) {
      System.out.println("The requested secret " + secretName + " was not found");
      throw e;
    }

    return null;
  }
}
