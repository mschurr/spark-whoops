spark-whoops
=====================
Better error pages for the Spark Java micro-framework.

![image](http://i.imgur.com/oaHfcVO.png)

## Usage:
To utilize:
```java
package edu.rice.mschurr.demo;

import spark.Spark;
import edu.rice.mschurr.spark_whoops.WhoopsHandler;

public class WhoopsExample {
  public static void main(String[] args) {
    Spark.port(8080);

    Spark.get("/except", (req, res) -> {
      throw new Exception("Testing Handler!");
    });

    Spark.exception(Exception.class, new WhoopsHandler());

    Spark.init();
  }
}
```

## Maven:

Coming soon (hopefully).

## Advanced Usage:

Subclass `edu.rice.mschurr.spark_whoops.WhoopsHandler` and install your subclass instead of `WhoopsHandler`.

## Notes:

* This handler reveals server internals and possibly code. Only enable it when you are developing and make sure to disable it before pushing to production.
* Code snippets will only be displayed if the Java source file for the corresponding exception stack frame is available within the current working directory.
