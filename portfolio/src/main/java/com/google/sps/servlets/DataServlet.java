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
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.gson.Gson;
import java.util.*; 
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private ArrayList<String> msgs;
  private DatastoreService datastore;

  @Override
  public void init() { //constructor
    this.msgs = new ArrayList<String>();
    this.datastore = DatastoreServiceFactory.getDatastoreService();
  }
   
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {   
    // Get the input from the form, add comment to the ArrayList
     String  userComment = getuserComment(request);
     
    if (userComment != null || userComment!= "") {
        msgs.add(userComment);
        //new Comment entity with the user's comment in it 
        Entity comEntity = new Entity("Comment");
        comEntity.setProperty("string", userComment);

        //put the comment in the database
        datastore.put(comEntity);
    
    }
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
    //gets the appropriate language code from query string 
    String code = request.getParameter("languageCode");
   
    Query query = new Query("Comment"); 
    PreparedQuery results = datastore.prepare(query);
    ArrayList<String> comList = new ArrayList<String>();

    //if there is no translation specified
    if (code == null) {
      //iterate through all the comments 
      for (Entity entity : results.asIterable()) {
        String com  = (String)entity.getProperty("string");
        comList.add(com);
      }
    }
    else {
      Translate translate = TranslateOptions.getDefaultInstance().getService();
      //iterate through all the comments and translate them 
      for (Entity entity : results.asIterable()) {
         String com  = (String)entity.getProperty("string");
         Translation translation = translate.translate(com, Translate.TranslateOption.targetLanguage(code));
         comList.add(translation.getTranslatedText());;
      }   
    }
    
    Gson gson = new Gson(); 
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comList));
  }
}
