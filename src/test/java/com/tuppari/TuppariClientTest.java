package com.tuppari;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class TuppariClientTest {

    @Test
    public void testConstructor() {
        TuppariClient client = new TuppariClient("appId", "accessKey", "secretKey");

        assertThat(client.getApplicationId(), is("appId"));
        assertThat(client.getAccessKeyId(), is("accessKey"));
        assertThat(client.getAccessSecretKey(), is("secretKey"));
        assertThat(client.getConnectTimeout(), is(0));
        assertThat(client.getReadTimeout(), is(0));
        assertThat(client.getTargetUri(), is(URI.create("https://api.tuppari.com")));
        assertThat(client.getMessagesApiEndpoint(), is(URI.create("https://api.tuppari.com/messages")));
    }

    @Test
    public void testConstructor_withTargetUrl() {
        TuppariClient client = new TuppariClient("appId", "accessKey", "secretKey", URI.create("http://127.0.0.1"));

        assertThat(client.getApplicationId(), is("appId"));
        assertThat(client.getAccessKeyId(), is("accessKey"));
        assertThat(client.getAccessSecretKey(), is("secretKey"));
        assertThat(client.getConnectTimeout(), is(0));
        assertThat(client.getReadTimeout(), is(0));
        assertThat(client.getTargetUri(), is(URI.create("http://127.0.0.1")));
        assertThat(client.getMessagesApiEndpoint(), is(URI.create("http://127.0.0.1/messages")));
    }

    @Test
    public void testJoin() {
        TuppariClient client = new TuppariClient("appId", "accessKey", "secretKey");
        TuppariChannel channel = client.join("test");
        assertNotNull(channel);
    }

}
