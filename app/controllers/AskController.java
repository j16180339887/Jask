package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import play.mvc.*;
import views.html.*;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class AskController extends Controller {
    public Result get() {
        return ok(AskPage.render());
    }

    public Result postQuestion() {
        JsonNode requestNode = request().body().asJson();

        String QTitle = requestNode.get("title").textValue();
        String QBody = requestNode.get("body").textValue();
        String QUser = requestNode.get("user").textValue();
        String EpochMilliTime = String.valueOf(Instant.now().toEpochMilli());

        /* Title is empty, code 400 */
        if(QTitle == null || QTitle.trim().isEmpty()) {
            return badRequest(Json.parse("{\"message\":\"請輸入標題\"}").toString());
        }

        String AskPostJson = "";
        String rootPath = new File("").getAbsolutePath();

        try {
            AskPostJson = new String(Files.readAllBytes(Paths.get(rootPath + "\\conf\\json\\Question.json")));
            JsonNode dataJson = new ObjectMapper().readTree(AskPostJson);

            /* Update fields */
            ((ObjectNode) dataJson).put("created", EpochMilliTime);
            ((ObjectNode) dataJson).put("title", QTitle);
            ((ObjectNode) dataJson).put("body", QBody);
            ((ObjectNode) dataJson).put("user_id", QUser);

            System.out.println(dataJson);

            System.out.println(new AskController()
                    .postToElastic(new URL("http://localhost:9200/jask/questions/" + EpochMilliTime), dataJson.toString()));
        } catch (Exception e) {
            System.out.println(e);
            return badRequest(Json.parse("{\"message\":\"伺服器出錯了 :(\"}").toString());
        }

        return ok(Json.parse("{\"redirect\":\"/question/" + EpochMilliTime + "\"}").toString());
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
