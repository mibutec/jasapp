package com.sdase.epa.e2e;

import org.apache.commons.lang3.StringEscapeUtils;
import org.popper.gherkin.Narrative;
import org.popper.gherkin.listener.GherkinListener;
import org.popper.gherkin.table.Table;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class HtmlGherkinListener implements GherkinListener {
  private static StringBuilder builder = new StringBuilder();

  public void narrative(Narrative narrative) {
    builder.append("<h1>As a " +  narrative.asA() + " I want " + narrative.iWantTo() + " in order to " + narrative.inOrderTo() + "</h1>\n");
  }

  public void scenarioStarted(String scenarioTitle, Method method) {
    builder.append("<h2>" +  scenarioTitle + "</h2><table>\n");
  }

  public void stepExecutionFailed(String type, String step, Optional<Table<Map<String, String>>> table,
                                   Throwable throwable) {
    red("<b>" + step + "</b>");
  }

  public void stepExecutionSucceed(String type, String step, Optional<Table<Map<String, String>>> table) {
    green("<b>" + step + "</b>");
  }

  public void scenarioFailed(String scenarioTitle, Method method, Throwable throwable) {
    red("<b>" + scenarioTitle + " failed</b><br>" + throwableToHtml(throwable));
    builder.append("</table>\n");
  }

  public void scenarioSucceed(String scenarioTitle, Method method) {
    green("<b>" + scenarioTitle + " succeeded</b>");
    builder.append("</table>\n");
  }

  public static String createHtml(String logs) {
    return """
        <html><title>Test Results</title>
        <style>
        table {
          width: 800px;
          max-width: 800px;
        }

        .red {
          background-color: red;
        }
        .green {
          background-color: green;
        }
        </style>
        <body>""" + builder + "\n<pre>" + logs + "</pre></body></html>";
  }

  public static void reset() {
    builder = new StringBuilder();
  }

  private static void red(String text) {
    builder.append("<tr class=\"red\"><td>" + text + "</td><td><b>NOK</b></td></tr>\n");
  }

  private static void green(String text) {
    builder.append("<tr class=\"green\"><td>" + text + "</td><td><b>OK</b></td></tr>\n");
  }

  private static String throwableToHtml(Throwable th) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    th.printStackTrace(pw);
    String exceptionString = sw.toString();
    int snipIndex = exceptionString.indexOf("at org.popper.gherkin");
    if (snipIndex > 0) {
      exceptionString = exceptionString.substring(0, snipIndex);
    }
    return "<pre>" + StringEscapeUtils.escapeHtml4(exceptionString).replace("\t", "  ") + "</pre>";
//    return StringEscapeUtils.escapeHtml4(exceptionString).replace("\n", "<br>").replace(" ", "&nbsp;");
  }
}
