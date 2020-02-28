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
import com.google.gson.Gson;
import java.util.*; 
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
    ArrayList<String> msgs = new ArrayList<String>();

    
  /*//fills an ArrayList with 3 messages
  public ArrayList makeMessage(){
    ArrayList<String> msgs = new ArrayList<String>();
    msgs.add("hi");
    msgs.add("bye");
    return msgs;
  }*/
  
  //converts the message ArrayList to Json
  private String convertToJsonUsingGson(ArrayList<String> msgs) {
    Gson gson = new Gson();
    String json = gson.toJson(msgs);
    return json;
  }
@Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Get the input from the form, add comment to the ArrayList
     String  userComment = getuserComment(request);
     msgs.add(userComment);
    //System.out.println(userComment);

    response.setContentType("text/html");  
    response.sendRedirect("/index.html");
  }
   private String getuserComment(HttpServletRequest request) {
    // Get the input from the form.
    String comment = request.getParameter("com");
    return comment;
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    String json = convertToJsonUsingGson(msgs);
    response.getWriter().println(json);
  }


}
