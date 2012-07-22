package com.tuppari;

import net.arnx.jsonic.JSON;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Create signature and make signed request utilities.
 *
 * @see https://github.com/hakobera/tuppari-servers/wiki/Making-Authorization-Header
 */
public abstract class SignUtil {

    /**
     * Generate HMAC-SHA256 hash from value using key.
     *
     * @param key sercet key
     * @param data source data
     * @return HMAC-SHA256 hash
     */
    static String hmac(String key, String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] result = mac.doFinal(data.getBytes());
            return new String(Hex.encodeHex(result));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the URI-encoded version of the absolute path component of the URI.
     * If the absolute path is empty, use a forward slash (/).
     *
     * @param uri Request path
     * @return Canonical URI
     */
    static String createCanonicalUri(String uri) {
        if (isEmpty(uri)) {
            return "/";
        }

        if (!uri.startsWith("/")) {
            return "/" + uri;
        }

        return uri;
    }

    /**
     * Create query string.
     *
     * @param queryString The query string of the HTTP request
     * @return Cnononical query string
     */
    static String createCanonicalQueryString(String queryString) {
        if (isEmpty(queryString)) {
            return "";
        }

        Map<String, List<String>> params = getUrlParameters(queryString);
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        List<String> result = new ArrayList<String>();
        for (String key : keys) {
            List<String> values = params.get(key);
            for (String value : values) {
                result.add(String.format("%s=%s", key, urlEncode(value)));
            }
        }

        return join(result, '&');
    }

    /**
     * Create canonical headers.
     *
     * 1. Convert all header names to lowercase and trim all header values
     * 2. Sort the headers by lower case character code
     * 3. For each header, append the lower case header name, followed by a colon, and append a newline.
     *
     * @param headers The map of HTTP request headers.
     * @return Canonical headers
     */
    static String createCanonicalHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        List<String> headerNames = new ArrayList<String>(headers.keySet());
        Collections.sort(headerNames);

        List<String> result = new ArrayList<String>();
        for (String headerName : headerNames) {
            String value = headers.get(headerName);
            result.add(String.format("%s:%s", headerName.toLowerCase(), value.trim()));
        }

        return join(result, '\n');
    }

    /**
     * Create signed headers.
     *
     * 1. Convert all header names to lowercase and trim all header values
     * 2. Sort the headers by lower case character code
     * 3. Join the sorted header name list with semicolon (';')
     *
     * @param headers The map of HTTP request headers.
     * @return Signed headers
     */
    static String createSignedHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        List<String> headerNames = new ArrayList<String>(headers.keySet());
        Collections.sort(headerNames);

        List<String> result = new ArrayList<String>();
        for (String headerName : headerNames) {
            result.add(headerName.toLowerCase());
        }

        return join(result, ';');
    }

    /**
     * Calc a hash from the body of the HTTP request using a hash function that algorithm is SHA256.
     * Take the lower case base hex encoding of the hash function output.
     * If the body is empty, use the empty string as the input to the hash function.
     *
     * @param {String|Object} body The body of the HTTP request
     * @return {String} The SHA256 hash of the body
     */
    static String createBodyHash(Map<String, Object> body) {
        String data = "";
        if (body != null) {
            data = JSON.encode(body);
        }
        return DigestUtils.sha256Hex(data);
    }

    /**
     * Returns following data.
     *
     * CanonicalRequest =
     *    HTTPRequestMethod + '\n' +
     *    CanonicalURI + '\n' +
     *    CanonicalQueryString + '\n' +
     *    CanonicalHeaders + '\n' +
     *    SignedHeaders + '\n' +
     *    HexEncode(Hash(body))
     *
     * @param method HTTP requet method (Such as GET, POST, etc)
     * @param uri URI of the request
     * @param queryString Query string of the request
     * @param headers The request header map of the request
     * @param body The body of the request
     * @return Canonical request
     */
    public static String createCanonicalRequest(String method, String uri, String queryString, Map<String, String> headers, Map<String, Object> body) {
        List<String> result = new ArrayList<String>();
        result.add(method);
        result.add(createCanonicalUri(uri));
        result.add(createCanonicalQueryString(queryString));
        result.add(createCanonicalHeaders(headers));
        result.add(createSignedHeaders(headers));
        result.add(createBodyHash(body));
        return join(result, '\n');
    }

    /**
     * Returns following data.
     *
     * StringToSign =
     *   Algorithm + '\n' +
     *   RequestDate + '\n' +
     *   HexEncode(Hash(CanonicalRequest))
     *
     * @param canonicalRequest The canonical request
     * @param requestDate The date of the request
     * @return The string to sign
     */
    public static String createStringToSign(String canonicalRequest, Date requestDate) {
        List<String> result = new ArrayList<String>();
        result.add("SHA256");
        result.add(DateUtil.formatAsISO8601(requestDate));
        result.add(DigestUtils.sha256Hex(canonicalRequest));
        return join(result, '\n');
    }

    /**
     * Create a signature from secret key and string to sign.
     *
     * SecretKey = Your Secret Key (Account secret key or application secret key)
     * DerivedSigningKey = HMAC(HMAC("TUPPARI" + SecretKey, RequestDate), Host)
     * Signature = HMAC(DerivedSigningKey, StringToSign)
     *
     * @param {String} secretKey The secret key
     * @param {String} stringToSign The string you want to sign
     * @param {Date} requestDate The date of the request
     * @param {String} host The hostname of the tuppari admin server
     * @return {String} Signature of the stringToSign
     */
    public static String createSignature(String secretKey, String stringToSign, Date requestDate, String host) {
        String derivedSigningKey = hmac(hmac("TUPPARI" + secretKey, DateUtil.formatAsISO8601(requestDate)), host);
        String signature = hmac(derivedSigningKey, stringToSign);
        return signature;
    }

    /**
     * Create signed request config.
     *
     * @param {String} method HTTP requet method (Such as GET, POST, etc)
     * @param {String} uri URI of the request
     * @param {String} operation The operation name
     * @param {Object|String} body The body of the request
     * @param {String} accessKeyId Access key id
     * @param {String} secretKey Access secret key
     * @return {Object} The request config
     */
    public static Map<String, Object> createSignedRequestConfig(String method, URI uri, String operation, Map<String, Object> body, String accessKeyId, String secretKey) {
        String hostname = uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
        String path = uri.getPath();
        String query = uri.getQuery();
        Date now = DateUtil.now();
        String formattedDate = DateUtil.formatAsRFC3339(now);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Host", hostname);
        headers.put("Content-Type", "application/json");
        headers.put("X-Tuppari-Date", formattedDate);
        headers.put("X-Tuppari-Operation", operation);
        headers.put("Authorization", createAuthorizationHeader(method, hostname, path, query, headers, body, now, accessKeyId, secretKey));

        Map<String, Object> options = new LinkedHashMap<String, Object>();
        options.put("uri", uri);
        options.put("body", JSON.encode(body));
        options.put("headers", headers);

        return options;
    }

    /**
     * Create authorization header.
     *
     * @param {String} method The HTTP method of the request
     * @param {String} hostname The host name of the API server
     * @param {String} path The absolute path of the request
     * @param {String} query The query string of the request
     * @param {Object} headers The map of HTTP request headers
     * @param {String|Object} body The body of the request
     * @param {Date} requestDate The date of the request
     * @param {String} accessKeyId Access key id
     * @param {String} secretKey Access secret key
     * @return {String} Authorization header string
     */
    public static String createAuthorizationHeader(String method, String hostname, String path, String query, Map<String, String> headers, Map<String, Object> body, Date requestDate, String accessKeyId, String secretKey) {
        String signedHeaders = createSignedHeaders(headers);
        String canonicalRequest = createCanonicalRequest(method, path, query, headers, body);
        String stringToSign = createStringToSign(canonicalRequest, requestDate);
        String signature = createSignature(secretKey, stringToSign, requestDate, hostname);
        return String.format("HMAC-SHA256 Credential=%s,SignedHeaders=%s,Signature=%s", accessKeyId, signedHeaders, signature);
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private static Map<String, List<String>> getUrlParameters(String query) {
        Map<String, List<String>> params = new HashMap<String, List<String>>();

        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            String key = urlDecode(pair[0]);
            String value = "";
            if (pair.length > 1) {
                value = urlDecode(pair[1]);
            }
            List<String> values = params.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                params.put(key, values);
            }
            values.add(value);
        }

        return params;
    }

    private static String urlDecode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String join(List<String> list, char c) {
        if (list == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = list.size(); i < len; ++i) {
            if (i != 0) {
                sb.append(c);
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

}
