package edu.rice.mschurr.spark_whoops;

import spark.Spark;

public class WhoopsExample {
  public static void main(String[] args) {
    Spark.port(8080);

    Spark.get("/", (req, res) -> {
      req.session().attribute("hello", "person");
      return "Hello!";
    });
    
    Spark.get("/except/:p", (req, res) -> {
      req.attribute("hello", "world");
      throw new Exception("Testing Handler!");
    });

    // Add this line to enable Whoops! error handling:
    Spark.exception(Exception.class, new WhoopsHandler());

    Spark.init();
  }
}
