package edu.rice.mschurr.spark_whoops;

import spark.Spark;

public class WhoopsExample {
  public static void main(String[] args) {
    Spark.port(8080);
    
    Spark.get("/except", (req, res) -> {
      throw new Exception("Testing Handler!");
    });
    
    WhoopsHandler.install();
    
    Spark.init();
  }
}
