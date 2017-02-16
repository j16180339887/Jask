package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import views.html.ErrorPage;
import views.html.SearchPage;

public class SearchController extends Controller {
    
    public Result get(String key) {
        
        String SearchJson = null;
        String rootPath = new File("").getAbsolutePath();
        StringBuilder resultHtml = new StringBuilder();
        
        try {
            SearchJson = new String(Files.readAllBytes(Paths.get(rootPath + "\\conf\\json\\Search.json")));
            JsonNode dataJson = new ObjectMapper().readTree(SearchJson);
            
            /* Update fields */
            changeNode(dataJson.get("query"), "query", key);

            System.out.println(dataJson);

            String responce = new SearchController()
                    .getToElastic(new URL("http://localhost:9200/jask/questions/_search"), dataJson.toString());
            
            JsonNode responceJson = new ObjectMapper().readTree(responce).get("hits").get("hits");
            
            resultHtml.append("<ul>");
            if (responceJson.isArray()) {
                for (JsonNode objNode : responceJson) {
                    resultHtml.append("<li>");
                    resultHtml.append("<a href=\"/question/");
                    resultHtml.append(objNode.get("_source").get("created").textValue());
                    resultHtml.append("\">");
                    resultHtml.append(objNode.get("_source").get("title").textValue());
                    resultHtml.append("</a>");
                    resultHtml.append("</li>");
                }
            }
            resultHtml.append("</ul>");
            
            System.out.println(resultHtml);
            
        } catch (Exception e) {
            System.out.println(e);
            return Results.status(400, ErrorPage.render("伺服器出錯了 :("));
        }
        
        return ok(SearchPage.render(
                    new Html(resultHtml.toString())
                ));
    }
    
    private static void changeNode(JsonNode parent, String fieldName, String newValue) {

        if (parent.isContainerNode() && parent.has(fieldName)) {
            ((ObjectNode) parent).put(fieldName, newValue);
            return;
        }

        for (JsonNode child : parent) {
            if (child.isContainerNode() && child.has(fieldName)) {
                ((ObjectNode) child).put(fieldName, newValue);
                return;
            }
        }
    }
    
    private String getToElastic(URL url, String query) throws IOException {

        BufferedReader reader = null;
        StringBuilder stringBuilder;
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(query.toString());

        OutputStream os = connection.getOutputStream();
        os.write(query.toString().getBytes("UTF-8"));
        os.close();
        
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        stringBuilder = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null)
        {
          stringBuilder.append(line + "\n");
        }
        return stringBuilder.toString();
    }
}
