# Tuppari Java Client Library

This is a client library of [Tuppari](https://github.com/hakobera/tuppari) for Java.

## Sample

```java
TuppariClient client = new TuppariClient(applicationId, accessKeyId, accessSecretKey);
TuppariChannel channel = client.join(channelName);
Map<String, String> result = channel.send(eventName, message);
```