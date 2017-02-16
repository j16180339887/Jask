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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import views.html.ErrorPage;
import views.html.QuestionPage;
import views.html.SearchPage;

public class QusetionController extends Controller {
    
    final SimpleDateFormat TimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public Result get(String index) {

        JsonNode _source = null;
        String QTitle = null;
        String QBody = null;
        String QCreated = null;
        long QScore = 0;
        String QUser = null;
        StringBuilder Answers = new StringBuilder();
        TimeFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        try {

            JsonNode dataJson = new ObjectMapper()
                    .readTree(getElastic(new URL("http://localhost:9200/jask/questions/" + index)));

            _source = dataJson.get("_source");
            QTitle = _source.get("title").textValue();
            QBody = _source.get("body").textValue();
            QScore = _source.get("score").asLong();
            QCreated = TimeFormat.format(new Date(Long.parseLong(_source.get("created").textValue())));
            QUser = _source.get("user_id").textValue();
            
            final JsonNode arrNode = ((ArrayNode) _source.get("answers"));
            
            if (arrNode.isArray()) {
                for (JsonNode objNode : arrNode) {
                    Answers.append("<br/>");
                    Answers.append("<div class=\"MainBody\">");
                    Answers.append("<div class=\"markdown\">");
                    Answers.append(objNode.get("body").textValue());
                    Answers.append("</div>");
                    Answers.append("<h6 class=\"user time\">");
                    Answers.append(objNode.get("user_id").textValue());
                    Answers.append(" ");
                    Answers.append(TimeFormat.format(new Date(Long.parseLong(objNode.get("created").textValue()))));
                    Answers.append("</div>");
                }
            }
            System.out.println(Answers);

        } catch (Exception e) {
            System.out.println(e);
            return Results.status(400, ErrorPage.render("沒有該筆資料 :("));
        }

        return ok(QuestionPage.render(
                    QTitle,
                    new Html(QBody),
                    QCreated,
                    QUser,
                    QScore,
                    new Html(Answers.toString())
                ));
    }
    
    public Result getAll() {

        String SearchJson = null;
        String rootPath = new File("").getAbsolutePath();
        StringBuilder resultHtml = new StringBuilder();
        
        try {
            SearchJson = new String(Files.readAllBytes(Paths.get(rootPath + "\\conf\\json\\SearchAll.json")));
            JsonNode dataJson = new ObjectMapper().readTree(SearchJson);

            System.out.println(dataJson);

            String responce = new QusetionController()
                    .getToElastic(new URL("http://localhost:9200/jask/questions/_search"), dataJson.toString());
            
            JsonNode responceJson = new ObjectMapper().readTree(responce).get("hits");
            
            final JsonNode arrNode = responceJson.get("hits");
            resultHtml.append("<ul>");
            if (arrNode.isArray()) {
                for (JsonNode objNode : arrNode) {
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
    
    public Result vote(String index) {
        
        JsonNode requestNode = request().body().asJson();
        
        boolean up = requestNode.get("upvote").asBoolean();
        System.out.println(requestNode.get("upvote"));
        JsonNode _source = null;
        long score = 0;
        
        try {

            JsonNode dataJson = new ObjectMapper()
                    .readTree(getElastic(new URL("http://localhost:9200/jask/questions/" + index)));
            
            _source = dataJson.get("_source");
            score = _source.get("score").asLong();
            
            if (up) {
                score++;
            } else {
                score--;
            }
            
            ((ObjectNode) _source).put("score", score);
            System.out.println(_source);
            System.out.println(new QusetionController()
                    .putToElastic(new URL("http://localhost:9200/jask/questions/" + index), _source.toString()));

        } catch (Exception e) {
            System.out.println(e);
            return Results.status(400, ErrorPage.render("伺服器出錯了 :("));
        }
        
        return ok(Json.parse("{\"redirect\":\"/question/" + index + "\"}").toString());
    }
    
    public Result postAnswer(String index) {
        
        JsonNode requestNode = request().body().asJson();
        
        System.out.println(requestNode);
        
        String ABody = requestNode.get("body").textValue();
        String AUser = requestNode.get("user").textValue();
        String EpochMilliTime = String.valueOf(Instant.now().toEpochMilli());

        /* Title is empty, code 400 */
        if(ABody == null || ABody.trim().isEmpty()) {
            return badRequest(Json.parse("{\"message\":\"請輸入標題\"}").toString());
        }

        String AskPostJson = "";
        String rootPath = new File("").getAbsolutePath();

        try {
            AskPostJson = new String(Files.readAllBytes(Paths.get(rootPath + "\\conf\\json\\Answer.json")));
            JsonNode dataJson = new ObjectMapper().readTree(AskPostJson);
            
            JsonNode originalQuestion = new ObjectMapper()
                    .readTree(getElastic(new URL("http://localhost:9200/jask/questions/" + index))).get("_source");
            
            /* Update fields */
            ((ObjectNode) dataJson).put("created", EpochMilliTime);
            ((ObjectNode) dataJson).put("body", ABody);
            ((ObjectNode) dataJson).put("user_id", AUser);
            
            ((ArrayNode) originalQuestion.get("answers")).add(dataJson);
            
            System.out.println(new QusetionController()
                    .putToElastic(new URL("http://localhost:9200/jask/questions/" + index), originalQuestion.toString()));
        } catch (Exception e) {
            System.out.println(e);
            return badRequest(Json.parse("{\"message\":\"伺服器出錯了 :(\"}").toString());
        }

        return ok();
    }

    private String getFiled(JsonNode parent, String fieldName) {

        if (parent.isContainerNode() && parent.has(fieldName)) {
            return ((ObjectNode) parent).get(fieldName).toString();
        }

        for (JsonNode child : parent) {
            if (child.isContainerNode() && child.has(fieldName)) {
                return ((ObjectNode) child).get(fieldName).toString();
            }
        }

        return null;
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

    private String getElastic(URL url) throws IOException {

        BufferedReader reader = null;
        StringBuilder stringBuilder;

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        connection.setRequestMethod("GET");

        // give it 15 seconds to respond
        connection.setReadTimeout(15*1000);
        connection.connect();

        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        stringBuilder = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null)
        {
          stringBuilder.append(line + "\n");
        }
        return stringBuilder.toString();
    }
    
    private String putToElastic(URL url, String query) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(query.toString());

        OutputStream os = connection.getOutputStream();
        os.write(query.toString().getBytes("UTF-8"));
        os.close();

        return connection.getResponseMessage().toString();

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
    
    private String postToElastic(URL url, String query) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
        wr.write(query.toString());

        OutputStream os = connection.getOutputStream();
        os.write(query.toString().getBytes("UTF-8"));
        os.close();

        return connection.getResponseMessage().toString();

    }
}
