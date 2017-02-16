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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.LoginPage;
import views.html.LogoutPage;

public class LoginController extends Controller {
    
    public Result get() {
        return ok(LoginPage.render());
    }
    
    public Result getlogout() {
        return ok(LogoutPage.render());
    }
    
    public Result postLogin() {
        JsonNode requestNode = request().body().asJson();
        
        String user = requestNode.get("user").textValue();
        String password = requestNode.get("password").textValue();
        
        System.out.println(user);
        System.out.println(password);
        
        if(user == null || user.trim().isEmpty()) {
            return badRequest(Json.parse("{\"message\":\"請輸入帳號\"}").toString());
        }
        
        if(password == null || password.trim().isEmpty() ) {
            return badRequest(Json.parse("{\"message\":\"請輸入密碼\"}").toString());
        }
        
        try {
            JsonNode dataJson = new ObjectMapper()
                    .readTree(getElastic(new URL("http://localhost:9200/jask/user/" + user)));

            JsonNode _source = dataJson.get("_source");
            System.out.println(_source);
            
        } catch (Exception e) {
            System.out.println(e);
            return badRequest(Json.parse("{\"message\":\"沒有這位使用者 :(\"}").toString());
        }
        
        return ok(Json.parse("{\"redirect\":\"/\"}").toString());
    }
    
    public Result postSignup() {
        JsonNode requestNode = request().body().asJson();
        
        String user = requestNode.get("user").textValue();
        String password1 = requestNode.get("password1").textValue();
        String password2 = requestNode.get("password2").textValue();
        
        if(user == null || user.trim().isEmpty()) {
            return badRequest(Json.parse("{\"message\":\"請輸入帳號\"}").toString());
        }
        
        if(password1 == null || password1.trim().isEmpty() || 
                password2 == null || password2.trim().isEmpty()) {
            return badRequest(Json.parse("{\"message\":\"請輸入密碼\"}").toString());
        }
        
        if(!password1.equals(password2)) {
            return badRequest(Json.parse("{\"message\":\"密碼不符\"}").toString());
        }
        
        String UserPostJson = "";
        String rootPath = new File("").getAbsolutePath();

        try {
            UserPostJson = new String(Files.readAllBytes(Paths.get(rootPath + "\\conf\\json\\User.json")));
            JsonNode dataJson = new ObjectMapper().readTree(UserPostJson);

            /* Update fields */
            ((ObjectNode) dataJson).put("user_id", user);
            ((ObjectNode) dataJson).put("password", password1);

            System.out.println(dataJson);

            System.out.println(new LoginController()
                    .postToElastic(new URL("http://localhost:9200/jask/user/" + user), dataJson.toString()));
        } catch (Exception e) {
            System.out.println(e);
            return badRequest(Json.parse("{\"message\":\"伺服器出錯了 :(\"}").toString());
        }

        
        return ok(Json.parse("{\"redirect\":\"/\"}").toString());
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
