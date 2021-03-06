# Tuppari Java Client Library

This is a client library of [Tuppari](https://github.com/hakobera/tuppari) for Java.

## Maven settings

### Repository

```
<repositories>
    <repository>
        <id>tuppari-java</id>
        <name>tuppari-java repo</name>
        <url>http://hakobera.github.com/tuppari-java/releases</url>
    </repository>
</repositories>
```

### Dependency

```
<dependencies>
    <dependency>
        <groupId>tuppari</groupId>
        <artifactId>tuppari-java</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

## Code example

```java
package com.tuppari;

import com.tuppari.TuppariChannel;
import com.tuppari.TuppariClient;

import java.net.URI;
import java.util.Map;

public class TuppariExample {

    /**
     * Usage: java com.tuppari.example.TuppariExample [applicationId] [accessKeyId] [accessSecretKey]
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException("Usage: java com.tuppari.example.TuppariExample [applicationId] [accessKeyId] [accessSecretKey]");
        }

        String applicationId = args[0];
        String accessKeyId = args[1];
        String accessSecretKey = args[2];

        TuppariClient client = new TuppariClient(applicationId, accessKeyId, accessSecretKey);
        // Run on you own cloud, specify the target URL.
        // TuppariClient client = new TuppariClient(applicationId, accessKeyId, accessSecretKey, URI.create("http://localhost:5100"));
        TuppariChannel channel = client.join("your_channel");
        Map<String, String> result = channel.send("your_event", "hello");

        System.out.print(result);
    }

}
```

## License

The MIT License