/*
 * Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
 *
 * All rights reserved.
 */
package org.jasapp;

import static spark.Spark.*;
import static spark.Spark.get;

import spark.Request;
import spark.Response;

public class TestExecutionController {
  private static final String EXECUTE = "/executeTests";
  private static final String GET_STATUS = "/getStatus";
  private static final String GET_OUTPUT = "/getOutput";
  private static final String HEALTHY = "/healthy";
  private static final String READY = "/ready";
  
  private final TestExecutionService testExecutionService;
  
  public TestExecutionController(TestExecutionService testExecutionService) {
	super();
	this.testExecutionService = testExecutionService;
}

public void init() {
    post(EXECUTE, this::startExecution);
    get(GET_STATUS, this::getStatus);
    get(GET_OUTPUT, this::getOutput);
    get(HEALTHY, (req, res) -> "OK");
    get(READY, (req, res) -> "OK");
  }

  public String startExecution(Request req, Response resp) {
    boolean newlyStarted = testExecutionService.startExecution();
    if (newlyStarted) {
      resp.status(201);
    } else {
      resp.status(208);
    }

    return "OK";
  }

  public String getStatus(Request req, Response resp) {
    TestExecutionService.TestResult lastResult = testExecutionService.getLastResult();
    if (lastResult == null) {
      resp.status(204);
      return TestExecutionService.Result.IN_PROGRESS.toString();
    } else {
      resp.status(200);
      return lastResult.getResult().toString();
    }
  }

  public String getOutput(Request req, Response resp) {
    TestExecutionService.TestResult lastResult = testExecutionService.getLastResult();
    if (lastResult == null) {
      resp.status(204);
      return TestExecutionService.Result.IN_PROGRESS.toString();
    } else {
      resp.status(200);
      return String.format("<html><body><pre>%s</pre></body></html>", lastResult.getOutput());
    }
  }
}
