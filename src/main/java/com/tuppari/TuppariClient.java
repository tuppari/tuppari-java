package com.tuppari;

import java.net.URI;

/**
 * Client for Tuppari system.
 */
public class TuppariClient {

    private static final String DEFAULT_TARGET_URL = "https://api.tuppari.com";

    static final String TUPPARI_MESSAGE_PUBLISH_API_PATH = "/messages";

    static final String TUPPARI_MESSAGE_PUBLISH_API_METHOD = "PublishMessage";

    private String applicationId;

    private String accessKeyId;

    private String accessSecretKey;

    private URI targetUri;

    private int connectTimeout = 0;

    private int readTimeout = 0;

    /**
     * Construct from parmeters.
     *
     * @param applicationId   Application ID to connect
     * @param accessKeyId     Access key id of the application
     * @param accessSecretKey Access secret key of the application
     * @param targetUri       The target URI to connect
     */
    public TuppariClient(String applicationId, String accessKeyId, String accessSecretKey, URI targetUri) {
        this.applicationId = applicationId;
        this.accessKeyId = accessKeyId;
        this.accessSecretKey = accessSecretKey;
        this.targetUri = targetUri;
    }

    /**
     * Construct from parmeters.
     * Target URI is set to "https://api.tuppari.com"
     *
     * @param applicationId   Application ID to connect
     * @param accessKeyId     Access key id of the application
     * @param accessSecretKey Access secret key of the application
     */
    public TuppariClient(String applicationId, String accessKeyId, String accessSecretKey) {
        this(applicationId, accessKeyId, accessSecretKey, URI.create(DEFAULT_TARGET_URL));
    }

    /**
     * Create channel.
     *
     * @param channelName The name of channel
     * @return {TuppariChannel} instance
     */
    public TuppariChannel join(String channelName) {
        TuppariChannel channel = new TuppariChannel(this, channelName);
        return channel;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getAccessSecretKey() {
        return accessSecretKey;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    URI getMessagesApiEndpoint() {
        return targetUri.resolve(TUPPARI_MESSAGE_PUBLISH_API_PATH);
    }

}
