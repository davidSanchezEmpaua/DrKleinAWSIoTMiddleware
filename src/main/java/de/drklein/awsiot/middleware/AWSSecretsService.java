package de.drklein.awsiot.middleware;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.drklein.awsiot.middleware.PrivateKeyReader.getRSAKeySpec;

public class AWSSecretsService {
  public AwsConfig getAwsConfig() {
    String secretName = "drklein/test/kex-push-service/credentials";
    String region = "us-east-2";
    String secret;

    AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
        .withRegion(region)
        .build();

    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
        .withSecretId(secretName);

    try {
      GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);

      if (getSecretValueResult == null) {
        return null;
      }

      // Decrypts secret using the associated KMS CMK.
      // Depending on whether the secret is a string or binary, one of these fields will be populated.
      if (getSecretValueResult.getSecretString() != null) {
        secret = getSecretValueResult.getSecretString();

        HashMap<String, String> secretMap;
        ObjectMapper objectMapper = new ObjectMapper();
        secretMap = objectMapper.readValue(secret, new TypeReference<HashMap<String, String>>() {
        });
        System.out.println("Secrets are: " + secretMap);

        return getProductionConfig(secretMap);
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
    catch (JsonMappingException e) {
      System.out.println("The requested secret " + secretName + " could not converted to map");
      e.printStackTrace();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  // Local Configuration - loading AWS Config via .properties
  private static AwsConfig getProductionConfig(Map<String, String> secretMap) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, CertificateException {
    AwsConfig awsConfig = new AwsConfig();
    awsConfig.setClientEndpoint(secretMap.get("clientEndpoint"));
    awsConfig.setClientId(secretMap.get("clientId"));
    awsConfig.setSubscriptionTopic(secretMap.get("subscriptionTopic"));
    awsConfig.setPrivateKey(getPrivateKey(secretMap));
    awsConfig.setCertificates(getCertificate(secretMap));

    awsConfig.setLOG_VERBOSE(secretMap.get("LOG_VERBOSE"));
    awsConfig.setBUFFERING_SIZE(secretMap.get("BUFFERING_SIZE"));
    awsConfig.setBUFFERING_TIMELIMIT(secretMap.get("BUFFERING_TIMELIMIT"));

    awsConfig.setSF_USERNAME(secretMap.get("SF_USERNAME"));
    awsConfig.setSF_PASSWORD(secretMap.get("SF_PASSWORD"));
    awsConfig.setSF_SECTOKEN(secretMap.get("SF_SECTOKEN"));
    awsConfig.setSF_LOGINURL(secretMap.get("SF_LOGINURL"));
    awsConfig.setSF_GRANTSERVICE(secretMap.get("SF_GRANTSERVICE"));
    awsConfig.setSF_CLIENTID(secretMap.get("SF_CLIENTID"));
    awsConfig.setSF_CLIENTSECRET(secretMap.get("SF_CLIENTSECRET"));
    return awsConfig;
  }

  private static PrivateKey getPrivateKey(Map<String, String> secretMap) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    String privateKey = secretMap.get("privateKey");
    if (privateKey == null) {
      System.err.println(LocalDateTime.now() + " ---ERROR: private key file missing");
    }

    byte[] encoded = Base64.decodeBase64(privateKey);
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(getRSAKeySpec(encoded));
  }

  private static List<Certificate> getCertificate(Map<String, String> secretMap) {
    String certificate = secretMap.get("certificate");
    if (certificate == null) {
      System.err.println(LocalDateTime.now() + " ---ERROR: certificate missing");
    }

    assert certificate != null;
    return AWSutilities.createCertificates(certificate);
  }
}
