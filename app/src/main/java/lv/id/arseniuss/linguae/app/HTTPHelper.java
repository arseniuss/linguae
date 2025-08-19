package lv.id.arseniuss.linguae.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;

public class HTTPHelper {

    public static Single<GetResponse> GetRequestWithHeaders(String urlString) {
        return Single.fromCallable(() -> {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try {
                GetResponse response = new GetResponse();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP error code: " + responseCode);
                }

                response.Headers = connection.getHeaderFields();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();

                response.Content = result.toString();

                return response;
            } finally {
                connection.disconnect();
            }
        });
    }

    public static class GetResponse {
        public Map<String, List<String>> Headers;
        public String Content;
    }
}
