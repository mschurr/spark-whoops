spark-whoops
=====================
Better error pages for the [Spark Java micro-framework](http://sparkjava.com/).

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

Subclass `edu.rice.mschurr.spark_whoops.WhoopsHandler` and install your subclass instead of `WhoopsHandler`. This allows you to add your own tables that appear in the environment details section.

## Notes:

* This handler reveals server internals and possibly code. Only install it when you are developing and make sure to disable it before pushing to production.
* Finding code snippets is an imperfect art since the original file locations are not preserved in the compiled bytecode. Thus, code snippets will only be displayed if the Java source file for the corresponding exception stack frame is available within the working directory under `src/main/java` and you are properly following the Java naming and directory structure conventions.

## Credits:

Inspired by [filp/whoops](http://filp.github.io/whoops/).
