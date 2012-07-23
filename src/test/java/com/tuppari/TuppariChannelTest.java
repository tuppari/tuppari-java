package com.tuppari;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TuppariChannelTest {

    private static Properties props;

    private static String applicationId;
    private static String accessKeyId;
    private static String accessSecretKey;

    @BeforeClass
    public static void beforeClass() throws IOException {
        props = new Properties();
        ClassLoader cl  = Thread.currentThread().getContextClassLoader();
        props.load(cl.getResourceAsStream("keys.properties"));

        applicationId = props.getProperty("APPLICATION_ID");
        accessKeyId = props.getProperty("ACCESS_KEY_ID");
        accessSecretKey = props.getProperty("ACCESS_SECRET_KEY");
    }

    @Before
    public void before() {
        DateUtil.setDummyTime("1970-01-01 00:00:00 GMT");
    }

    @After
    public void after() {
        DateUtil.clearDummyTime();
    }

    @Test
    public void testSend() {
        String channelName = "channelName";
        String eventName = "eventName";
        String message = "message";

        TuppariClient client = new TuppariClient(applicationId, accessKeyId, accessSecretKey, URI.create("http://localhost:5100"));
        client.setReadTimeout(5000);
        TuppariChannel channel = client.join(channelName);

        Map<String, String> result = channel.send(eventName, message);

        assertNotNull(result);
        assertThat((String) result.get("applicationId"), is(applicationId));
        assertThat((String) result.get("channel"), is(channelName));
        assertThat((String) result.get("event"), is(eventName));
        assertThat((String) result.get("message"), is(message));
    }

    @Test(expected = TuppariException.class)
    public void testSend_toInValidHost() {
        String channelName = "channelName";
        String eventName = "eventName";
        String message = "message";

        TuppariClient client = new TuppariClient(applicationId, accessKeyId, accessSecretKey, URI.create("http://invalid"));
        client.setReadTimeout(5000);
        TuppariChannel channel = client.join(channelName);

        channel.send(eventName, message);
    }

    @Test(expected = TuppariException.class)
    public void testSend_withInvalidKeys() {
        String channelName = "channelName";
        String eventName = "eventName";
        String message = "message";

        TuppariClient client = new TuppariClient("invalid", accessKeyId, accessSecretKey, URI.create("http://127.0.0.1:5100"));
        client.setReadTimeout(5000);
        TuppariChannel channel = client.join(channelName);

        channel.send(eventName, message);
    }

}
