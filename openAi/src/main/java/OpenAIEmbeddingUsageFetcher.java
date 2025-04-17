import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIEmbeddingUsageFetcher {

    private static final String API_KEY = "$apikey";
    private static final String BASE_URL = "https://api.openai.com/v1/organization/usage/embeddings";
    private static final String MODEL = "text-embedding-3-small";
    private static final int START_TIME = 1730419200; // Adjust as needed
    private static final int LIMIT = 1;

    public static void main(String[] args) {
        try {
            String nextPage = null;
            boolean hasMore = true;

            while (hasMore) {
                StringBuilder urlBuilder = new StringBuilder(BASE_URL);
                urlBuilder.append("?start_time=").append(START_TIME);
                urlBuilder.append("&limit=").append(LIMIT);
                urlBuilder.append("&model=").append(MODEL);
                if (nextPage != null) {
                    urlBuilder.append("&page=").append(nextPage);
                }

                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                conn.disconnect();

                JSONObject response = new JSONObject(content.toString());

                JSONArray dataArray = response.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject bucket = dataArray.getJSONObject(i);
                    JSONArray results = bucket.getJSONArray("results");

                    if (results.length() > 0) {
                        System.out.println("=== Bucket ===");
                        System.out.println("Start: " + bucket.getLong("start_time"));
                        System.out.println("End: " + bucket.getLong("end_time"));
                        System.out.println("Results:");
                        for (int j = 0; j < results.length(); j++) {
                            JSONObject result = results.getJSONObject(j);
                            String model = result.optString("model", "null");
                            int tokens = result.getInt("input_tokens");
                            int requests = result.getInt("num_model_requests");
                            System.out.printf(" - Model: %s | Tokens: %d | Requests: %d%n", model, tokens, requests);
                        }
                    }
                }

                hasMore = response.getBoolean("has_more");
                nextPage = response.optString("next_page", null);
                if (nextPage.isEmpty()) {
                    nextPage = null;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
