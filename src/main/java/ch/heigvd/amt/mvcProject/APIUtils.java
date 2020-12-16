package ch.heigvd.amt.mvcProject;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Utils for the gamification API service
 */
public class APIUtils {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();
    private static final boolean DEBUG = true;

    private static String API_KEY = "";

    /**
     * Registers this application to the gamification service
     */
    public static void register() {

        HttpPost request = makePostRequest("/registration", null);

        try {
            HttpResponse response = HTTP_CLIENT.execute(request);
            JSONObject result = getJsonFromResponse(response);
            API_KEY = result.getString("value");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(DEBUG) System.out.println("Successfully registered : " + API_KEY);
    }

    /**
     * Create a new point scale
     * @param name : name of the point scale
     * @param description : description of the point scale
     */
    public static void createPointScale(String name, String description) {
        if(API_KEY.isEmpty()) {
            System.out.println("This application is not registered.");
            return;
        }

        if(name.isEmpty() || description.isEmpty()) {
            System.out.println("Invalid parameters.");
            return;
        }


        HttpPost request = makePostRequest("/registration", new ArrayList<>(Arrays.asList(
                new BasicNameValuePair("name", name),
                new BasicNameValuePair("description", description)
        )));

        try {
            HttpResponse response = HTTP_CLIENT.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Make an HTTP post request
     * @param endpoint : endpoint for the request
     * @return http post request
     */
    private static HttpPost makePostRequest(String endpoint, ArrayList<NameValuePair> postParameters) {
        HttpPost result = new HttpPost(BASE_URL + endpoint);

        // Add parameters
        if(postParameters != null && !postParameters.isEmpty()) {
            try {
                result.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if(DEBUG) System.out.println("POST Request : " + BASE_URL + endpoint);
        return result;
    }

    /**
     * Get the JSON object from an http response
     * @param response : http response
     * @return json object from response
     * @throws IOException
     */
    private static JSONObject getJsonFromResponse(HttpResponse response) throws IOException {
        StringWriter writer = new StringWriter();
        String encoding = StandardCharsets.UTF_8.name();
        IOUtils.copy(response.getEntity().getContent(), writer, encoding);
        return new JSONObject(writer.toString());
    }
}