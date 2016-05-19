package main.java.server.json;

import com.google.gson.Gson;
import spark.ResponseTransformer;

/**
 * Transforms the response into a JSON object
 */

public class JsonTransformer implements ResponseTransformer {
    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }
}