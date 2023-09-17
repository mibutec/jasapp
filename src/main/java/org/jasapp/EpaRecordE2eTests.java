/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.jasapp;

import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import spark.Request;
import spark.Response;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static spark.Spark.get;
import static spark.Spark.port;

public class EpaRecordE2eTests {
  public static void main(String[] args) {
    port(8080);
    get("/executeTests", EpaRecordE2eTests::executeTests);
  }

  public static String executeTests(Request req, Response resp) {
    PrintStream oldOut = System.out;
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream testOut = new PrintStream(baos, true, StandardCharsets.UTF_8);
      System.setOut(testOut);
      HtmlGherkinListener.reset();
      SummaryGeneratingListener listener = new SummaryGeneratingListener();
      LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
          .selectors(DiscoverySelectors.selectPackage(EpaRecordE2eTests.class.getPackageName()))
          .filters(ClassNameFilter.includeClassNamePatterns(".*Test"))
          .build();
      Launcher launcher = LauncherFactory.create();
      TestPlan testPlan = launcher.discover(request);
      launcher.registerTestExecutionListeners(listener);
      launcher.execute(request);
      int statusCode = listener.getSummary().getFailures().isEmpty() ? 200 : 400;
      resp.status(statusCode);
      return getGherkinReport(baos.toString());
    } finally {
      System.setOut(oldOut);
    }
  }

  private static String getGherkinReport(String logs) {
    return HtmlGherkinListener.createHtml(logs);
  }
}
