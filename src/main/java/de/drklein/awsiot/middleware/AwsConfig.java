package de.drklein.awsiot.middleware;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

public class AwsConfig {
  private String clientEndpoint;
  private String clientId;
  private String thingName;
  private String subscriptionTopic;
  private String SF_USERNAME;
  private String SF_PASSWORD;
  private String SF_SECTOKEN;
  private String SF_LOGINURL;
  private String SF_GRANTSERVICE;
  private String SF_CLIENTID;
  private String SF_CLIENTSECRET;
  private String BUFFERING_SIZE;
  private String BUFFERING_TIMELIMIT;
  private String LOG_VERBOSE;
  private List<Certificate> certificates;
  private PrivateKey privateKey;

  public String getClientEndpoint() {
    return clientEndpoint;
  }

  public void setClientEndpoint(String clientEndpoint) {
    this.clientEndpoint = clientEndpoint;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getThingName() {
    return thingName;
  }

  public void setThingName(String thingName) {
    this.thingName = thingName;
  }

  public String getSubscriptionTopic() {
    return subscriptionTopic;
  }

  public void setSubscriptionTopic(String subscriptionTopic) {
    this.subscriptionTopic = subscriptionTopic;
  }

  public String getSF_USERNAME() {
    return SF_USERNAME;
  }

  public void setSF_USERNAME(String SF_USERNAME) {
    this.SF_USERNAME = SF_USERNAME;
  }

  public String getSF_PASSWORD() {
    return SF_PASSWORD;
  }

  public void setSF_PASSWORD(String SF_PASSWORD) {
    this.SF_PASSWORD = SF_PASSWORD;
  }

  public String getSF_SECTOKEN() {
    return SF_SECTOKEN;
  }

  public void setSF_SECTOKEN(String SF_SECTOKEN) {
    this.SF_SECTOKEN = SF_SECTOKEN;
  }

  public String getSF_LOGINURL() {
    return SF_LOGINURL;
  }

  public void setSF_LOGINURL(String SF_LOGINURL) {
    this.SF_LOGINURL = SF_LOGINURL;
  }

  public String getSF_GRANTSERVICE() {
    return SF_GRANTSERVICE;
  }

  public void setSF_GRANTSERVICE(String SF_GRANTSERVICE) {
    this.SF_GRANTSERVICE = SF_GRANTSERVICE;
  }

  public String getSF_CLIENTID() {
    return SF_CLIENTID;
  }

  public void setSF_CLIENTID(String SF_CLIENTID) {
    this.SF_CLIENTID = SF_CLIENTID;
  }

  public String getSF_CLIENTSECRET() {
    return SF_CLIENTSECRET;
  }

  public void setSF_CLIENTSECRET(String SF_CLIENTSECRET) {
    this.SF_CLIENTSECRET = SF_CLIENTSECRET;
  }

  public String getBUFFERING_SIZE() {
    return BUFFERING_SIZE;
  }

  public void setBUFFERING_SIZE(String BUFFERING_SIZE) {
    this.BUFFERING_SIZE = BUFFERING_SIZE;
  }

  public String getBUFFERING_TIMELIMIT() {
    return BUFFERING_TIMELIMIT;
  }

  public void setBUFFERING_TIMELIMIT(String BUFFERING_TIMELIMIT) {
    this.BUFFERING_TIMELIMIT = BUFFERING_TIMELIMIT;
  }

  public String getLOG_VERBOSE() {
    return LOG_VERBOSE;
  }

  public void setLOG_VERBOSE(String LOG_VERBOSE) {
    this.LOG_VERBOSE = LOG_VERBOSE;
  }

  public List<Certificate> getCertificates() {
    return certificates;
  }

  public void setCertificates(List<Certificate> certificates) {
    this.certificates = certificates;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(PrivateKey privateKey) {
    this.privateKey = privateKey;
  }
}


