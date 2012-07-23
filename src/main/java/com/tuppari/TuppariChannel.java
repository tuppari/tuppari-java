package com.tuppari;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import net.arnx.jsonic.JSON;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Channel for Tuppari system.
 */
public class TuppariChannel {

    private TuppariClient client;

    private String channelName;

    private Client webClient;

    /**
     * @param client      The client
     * @param channelName The name of channel name
     */
    TuppariChannel(TuppariClient client, String channelName) {
        this.client = client;
        this.channelName = channelName;
        this.webClient = Client.create();
        this.webClient.setConnectTimeout(client.getConnectTimeout());
        this.webClient.setReadTimeout(client.getReadTimeout());
    }

    /**
     * Publish message to specified event name.
     *
     * @param eventName Event name
     * @param message Message string that send to server
     * @return Response data
     * @see https://github.com/hakobera/tuppari-servers/wiki/API-Reference#wiki-messages_publish
     */
    public Map<String, String> send(String eventName, String message) {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("applicationId", client.getApplicationId());
        body.put("channel", channelName);
        body.put("event", eventName);
        body.put("message", message);

        Map<String, Object> config = SignUtil.createSignedRequestConfig("POST", client.getMessagesApiEndpoint(), TuppariClient.TUPPARI_MESSAGE_PUBLISH_API_METHOD, body, client.getAccessKeyId(), client.getAccessSecretKey());
        Map<String, String> headers = (Map<String, String>) config.get("headers");

        try {
            WebResource webResource = webClient.resource((URI) config.get("uri"));
            WebResource.Builder builder = webResource.getRequestBuilder();
            builder.accept(MediaType.APPLICATION_JSON_TYPE);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
            String result = builder.post(String.class, JSON.encode(body));
            return JSON.decode(result);
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();
            String responseBody = response.getEntity(String.class);
            throw new TuppariException(responseBody, e);
        } catch (RuntimeException e) {
            throw new TuppariException(e);
        } catch (Exception e) {
            throw new TuppariException(e);
        }
    }

}