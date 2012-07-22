package com.tuppari;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import net.arnx.jsonic.JSON;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SignUtilTest {

    @Before
    public void before() {
        DateUtil.setDummyTime("1970-01-01 00:00:00 GMT");
    }

    @After
    public void after() {
        DateUtil.clearDummyTime();
    }

    @Test
    public void testHmac() {
        String result = SignUtil.hmac("123", "abc");
        String result2 = SignUtil.hmac("123", "abc");
        String result3 = SignUtil.hmac("1234", "abc");
        String result4 = SignUtil.hmac("123", "abcd");

        assertThat("Return same value when key and data is same", result, equalTo(result2));
        assertThat("Return not same value when key is not same", result, not(equalTo(result3)));
        assertThat("Return not same value when data is not same", result, not(equalTo(result4)));

        assertThat(result, is("8f16771f9f8851b26f4d460fa17de93e2711c7e51337cb8a608a0f81e1c1b6ae"));
    }

    @Test
    public void testCreateCannicalUri() {
        assertThat(SignUtil.createCanonicalUri(null), is("/"));
        assertThat(SignUtil.createCanonicalUri(""), is("/"));
        assertThat(SignUtil.createCanonicalUri("/"), is("/"));
        assertThat(SignUtil.createCanonicalUri("path"), is("/path"));
        assertThat(SignUtil.createCanonicalUri("/path"), is("/path"));
    }

    @Test
    public void testCreateCanonicalQueryString() {
        assertThat(SignUtil.createCanonicalQueryString(null), is(""));
        assertThat(SignUtil.createCanonicalQueryString(""), is(""));
        assertThat(SignUtil.createCanonicalQueryString("a=b"), is("a=b"));
        assertThat(SignUtil.createCanonicalQueryString("a=b&c=d"), is("a=b&c=d"));
        assertThat(SignUtil.createCanonicalQueryString("c=d&a=b"), is("a=b&c=d"));
        assertThat(SignUtil.createCanonicalQueryString("c=d&a=b&c=e"), is("a=b&c=d&c=e"));
    }

    @Test
    public void testCreateCanonicalHeaders_withEmptyMap() {
        assertThat(SignUtil.createCanonicalHeaders(null), is(""));
        assertThat(SignUtil.createCanonicalHeaders(new HashMap<String, String>()), is(""));
    }

    @Test
    public void testCreateCanonicalHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", " application/json ");
        headers.put("Host", "http://localhost");

        String result = SignUtil.createCanonicalHeaders(headers);
        assertThat(result, is("content-type:application/json\nhost:http://localhost"));
    }

    @Test
    public void testCreateSignedHeaders_withEmptyMap() {
        assertThat(SignUtil.createSignedHeaders(null), is(""));
        assertThat(SignUtil.createSignedHeaders(new HashMap<String, String>()), is(""));
    }

    @Test
    public void testCreateSignedHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", " application/json ");
        headers.put("Host", "http://localhost");

        String result = SignUtil.createSignedHeaders(headers);
        assertThat(result, is("content-type;host"));
    }

    @Test
    public void testCreateBodyHash_withNull() {
        assertThat("if body is empty, use the empty string", SignUtil.createBodyHash(null), is("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
    }

    @Test
    public void testCreateBodyHash() {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("a", "b");
        body.put("c", "d");
        assertThat(SignUtil.createBodyHash(body), is("b85c7da93e8790518898c280e15e3f1af5d46bf4aaa4407690f0f0a3b0316478"));
    }

    @Test
    public void testCreateCanonicalRequest() {
        String method = "POST";
        String uri = "/test";
        String queryString = "b=v2&a=v1";

        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("Host", "api.tuppari.com");
        headers.put("Content-type", "application/json");
        headers.put("X-Tuppari-Operation", "CreateApplication");

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("applicationName", "example1");

        String result = SignUtil.createCanonicalRequest(method, uri, queryString, headers, body);

        assertThat(result, is("" +
                "POST\n" +
                "/test\n" +
                "a=v1&b=v2\n" +
                "content-type:application/json\n" +
                "host:api.tuppari.com\n" +
                "x-tuppari-operation:CreateApplication\n" +
                "content-type;host;x-tuppari-operation\n" +
                "8f2d5fe4a93000d3546e578d265fc936806f6ef6dc6f7ee87715e1a5c514c168"));
    }

    @Test
    public void testCreateStringToSign() throws ParseException {
        String canonicalRequest =
                "POST\n" +
                "/test\n" +
                "a=v1&b=v2\n" +
                "content-type:application/json\n" +
                "host:api.tuppari.com\n" +
                "x-tuppari-operation:CreateApplication\n" +
                "content-type;host;x-tuppari-operation\n" +
                "8f2d5fe4a93000d3546e578d265fc936806f6ef6dc6f7ee87715e1a5c514c168";

        Date now = DateUtil.now();
        String result = SignUtil.createStringToSign(canonicalRequest, now);
        assertThat(result, is("" +
                "SHA256\n" +
                "19700101T000000Z\n" +
                "152176000cc08c7d9d0558bc3a50368aa38619a695ad20f50bec1344429cb315"));
    }

    @Test
    public void testCreateSignature_1() throws ParseException {
        String secretKey = "secretKey1";
        String stringToSign =
                "SHA256\n" +
                "19700101T000000Z\n" +
                "152176000cc08c7d9d0558bc3a50368aa38619a695ad20f50bec1344429cb315";

        Date now = DateUtil.now();
        String host = "api.tuppari.com";
        String expectedSignature = "4815ff1681a278e7c852902ea3604f17831a80a78dc0ff82f5142598a034509b";

        // Check with the same secret key, siganature returns same signature
        for (int i = 0; i < 1000; ++i) {
            String result = SignUtil.createSignature(secretKey, stringToSign, now, host);
            assertThat(result, is(expectedSignature));
        }
    }

    @Test
    public void testCreateSignature_2() throws ParseException {
        String secretKey1 = "secretKey1";
        String secretKey2 = "secretKey2";

        String stringToSign =
                "SHA256\n" +
                "19700101T000000Z\n" +
                "152176000cc08c7d9d0558bc3a50368aa38619a695ad20f50bec1344429cb315";

        Date now = DateUtil.now();
        String host = "api.tuppari.com";
        String expectedSignature = "4815ff1681a278e7c852902ea3604f17831a80a78dc0ff82f5142598a034509b";

        String result1 = SignUtil.createSignature(secretKey1, stringToSign, now, host);
        String result2 = SignUtil.createSignature(secretKey2, stringToSign, now, host);

        assertThat(result1, not(equalTo(result2)));
    }

    @Test
    public void testCreateSignedRequestConfig() throws ParseException {
        Date now = DateUtil.now();

        String method = "POST";
        URI uri = URI.create("http://api.tuppari.com/test?a=v1&b=v2");
        String operation = "CreateApplication";
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("applicationName", "example1");
        String accessKeyId = "accessKeyId";
        String accessSecretKey = "accessSecretKey";

        Map<String, Object> config = SignUtil.createSignedRequestConfig(method, uri, operation, body, accessKeyId, accessSecretKey);

        assertThat((URI) config.get("uri"), is(uri));
        assertThat((String) config.get("body"), is(JSON.encode(body)));

        Map<String, String> headers = (Map<String, String>) config.get("headers");
        assertThat(headers.get("Host"), is("api.tuppari.com"));
        assertThat(headers.get("Content-Type"), is("application/json"));
        assertThat(headers.get("X-Tuppari-Date"), is("Thu, 01 Jan 1970 00:00:00 GMT"));
        assertThat(headers.get("X-Tuppari-Operation"), is("CreateApplication"));
        assertThat(headers.get("Authorization"), is("HMAC-SHA256 Credential=accessKeyId,SignedHeaders=content-type;host;x-tuppari-date;x-tuppari-operation,Signature=f35767c9fdba4ba5d5bbbf1c622fceed0dbaeb210303bb56b419c6a51bcf1e5d"));
    }

    @Test
    public void testCreateAuthorizationHeader() {
        Date now = DateUtil.now();

        String method = "POST";
        String hostname = "api.tuppari.com";
        String path = "/test";
        String query = "a=v1&b=v2";

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Host", "api.tuppari.com");
        headers.put("Content-type", "application/json");
        headers.put("X-Tuppari-Operation", "CreateApplication");

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("applicationName", "example1");
        String accessKeyId = "accessKeyId";
        String accessSecretKey = "accessSecretKey";

        String authorization = SignUtil.createAuthorizationHeader(method, hostname, path, query, headers, body, now, accessKeyId, accessSecretKey);
        assertThat(authorization, is("HMAC-SHA256 Credential=accessKeyId,SignedHeaders=content-type;host;x-tuppari-operation,Signature=050f8711271747d4f63a3caa3ffb420e4cd5a0e9d9dda8ba7e4faad6794c40d0"));
    }

}
