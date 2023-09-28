/*
 * Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
 *
 * All rights reserved.
 */
package org.jasapp;

import static spark.Spark.*;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class App {
  private static final Logger log = Logger.getLogger("System.out.logger");
  
  private static final String KEY_PACKAGE_NAME = "jasapp.packageNameForTests";

  public static void main(String[] args) {
    configureLogger();

    Properties properties = getProperties();
    String pattern = properties.getProperty("jasapp.testNamePattern", ".*IT");
    String packageName = properties.getProperty(KEY_PACKAGE_NAME);
    boolean runLocal = Boolean.parseBoolean(properties.getProperty("jasapp.runLocal", "false"));
    
    TestExecutionService service = new TestExecutionService(packageName, pattern);
    if (runLocal) {
      service.executeTests();
      log.fine(service.getLastResult().getOutput());
    } else {
      port(8080);
      new TestExecutionController(service).init();
    } 
  }

  private static void configureLogger() {
    log.setLevel(Level.ALL);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(new SimpleFormatter());
    handler.setLevel(Level.ALL);
    log.addHandler(handler);
  }
  
  static Properties getProperties() {
	  String propertiesClasspathName = System.getenv("JASAPP_CONFIG");
	  if (propertiesClasspathName == null) {
		  propertiesClasspathName = System.getProperty("jsapp.config");
	  }
	  
	  if (propertiesClasspathName == null) {
		  propertiesClasspathName = "/jasapp.properties";
	  }
	  
	  try {
		  Properties ret = new Properties();
		  ret.load(App.class.getResourceAsStream(propertiesClasspathName));
		  
		  if (!ret.containsKey(KEY_PACKAGE_NAME)) {
			  throw new IllegalStateException("resource " + propertiesClasspathName + " is invalid, it does not contain a definition for " + KEY_PACKAGE_NAME);
		  }
		  
		  return ret;
	  } catch (IOException ioe) {
		  throw new IllegalStateException("cannot load properties from");
	  }
  }
}
