// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that sends and receives comments data. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    FetchOptions fetchOptions;
    int numComments = getNumCommentsToDisplay(request);

    if (numComments >= 0) {
      fetchOptions = FetchOptions.Builder.withLimit(numComments);
    } else {
      fetchOptions = FetchOptions.Builder.withDefaults();
    }

    List<String> comments = new ArrayList();
    for (Entity entity : results.asIterable(fetchOptions)) {
      String body = (String) entity.getProperty("body");
      comments.add(body);
    }

    Gson gson = new Gson();
    response.setContentType("text/html;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Store submitted comment
    String body = getParameter(request, "comment-submission", "");
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("body", body);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /**
   * @return the number of comments to display, or -1 if all comments should be displayed
   */
  private int getNumCommentsToDisplay(HttpServletRequest request) {
    // Get the input from the form.
    String numCommentsString = getParameter(request, "num-comments", null);

    if (numCommentsString.equals("All")) {
      return -1;
    }

    // Convert the input to an int.
    int numComments;
    try {
      numComments = Integer.parseInt(numCommentsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + numCommentsString);
      return -1;
    }

    // Check that the input is non-negative.
    if (numComments < 0) {
      System.err.println("Number of comments is negative: " + numCommentsString);
      return -1;
    }

    return numComments;
  }
}
