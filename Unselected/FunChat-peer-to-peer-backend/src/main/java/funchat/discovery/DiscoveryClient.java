package funchat.discovery;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class DiscoveryClient {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl; //http://localhost:8080/discovery

    public DiscoveryClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void register(String id, int chatPort) {
        try {
            String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
            String url = baseUrl + "/register?id=" + encodedId + "&chatPort=" + chatPort;

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return "ip:port" or null if user not found or error
     */
    public String search(String id) {
        try {
            String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8);
            String url = baseUrl + "/search?id=" + encodedId;

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                if (body == null || body.equals("null") || body.isBlank()) {
                    return null;
                }
                return body.trim(); // "ip:port"
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
