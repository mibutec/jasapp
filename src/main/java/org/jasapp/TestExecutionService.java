/*
 * Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
 *
 * All rights reserved.
 */
package org.jasapp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

public class TestExecutionService {
  private static final Logger log = Logger.getLogger("System.out.logger");
  private TestResult lastResult;

  private Thread currentExecution;
  
  private final String packageNameForTests;
  
  private final String testNamePattern;
  
  

  public TestExecutionService(String packageNameForTests, String testNamePattern) {
	super();
	this.packageNameForTests = packageNameForTests;
	this.testNamePattern = testNamePattern;
}

public synchronized boolean startExecution() {
    if (currentExecution == null) {
      currentExecution = new Thread(this::executeTests);
      currentExecution.start();
      return true;
    }

    return false;
  }

  public void executeTests() {
    lastResult = null;
    PrintStream oldOut = System.out;
    PrintStream oldErr = System.err;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
      System.setOut(testOut);
      System.setErr(testOut);
      SummaryGeneratingListener listener = new SummaryGeneratingListener();
      LauncherDiscoveryRequest request =
          LauncherDiscoveryRequestBuilder.request()
              .selectors(DiscoverySelectors.selectPackage(packageNameForTests))
              .filters(ClassNameFilter.includeClassNamePatterns(testNamePattern))
              .build();
      Launcher launcher = LauncherFactory.create();
      TestPlan testPlan = launcher.discover(request);
      launcher.registerTestExecutionListeners(listener);
      launcher.registerTestExecutionListeners(
          LoggingListener.forBiConsumer(
              (th, s) -> {
                String ex = "";
                if (th != null) {
                  ex = "\n" + exception2String(th);
                }
                log.info(s.get() + ex);
              }));
      launcher.execute(testPlan);

      Result r = listener.getSummary().getFailures().isEmpty() ? Result.OK : Result.NOK;
      lastResult = new TestResult(r, summaryToString(listener) + "\n" + baos.toString());
    } catch (Exception e) {
      lastResult = new TestResult(Result.NOK, throwableToHtml(e));
    } finally {
      System.setOut(oldOut);
      System.setErr(oldErr);
      currentExecution = null;
    }
  }

  private String summaryToString(SummaryGeneratingListener listener) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    listener.getSummary().printTo(pw);
    return sw.toString();
  }

  public static String exception2String(Throwable th) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    th.printStackTrace(pw);
    return sw.toString();
  }

  public static String throwableToHtml(Throwable th) {
    String exceptionString = exception2String(th);
    int snipIndex = exceptionString.indexOf("at org.popper.gherkin");
    if (snipIndex > 0) {
      exceptionString = exceptionString.substring(0, snipIndex);
    }
    return "<pre>" + StringEscapeUtils.escapeHtml4(exceptionString).replace("\t", "  ") + "</pre>";
  }

  public TestResult getLastResult() {
    return lastResult;
  }

  public static class TestResult {
    private final Result result;

    private final String output;

    public TestResult(Result result, String output) {
      this.result = result;
      this.output = output;
    }

    public Result getResult() {
      return result;
    }

    public String getOutput() {
      return output;
    }
  }

  public enum Result {
    OK,
    NOK,
    IN_PROGRESS
  }
}
