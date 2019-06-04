package it.efekt.mcstatsplaytime;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MinecraftUUIDResolver {
    private final String ENDPOINT = "https://api.minetools.eu/uuid/";

    public String getName(String playerUUID){
        return getUsername(playerUUID).get("name").getAsString();
    }

    private JsonObject getUsername(String uuid) {
        try {
            URL url = new URL(ENDPOINT + uuid);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            if (connection.getResponseCode() != 200){
                throw new IOException(connection.getResponseMessage());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null){
                sb.append(line);
            }

            reader.close();
            connection.disconnect();

            return new JsonParser().parse(sb.toString()).getAsJsonObject();

        } catch (IOException exc){
            System.out.println("Seems like the API is offline");
            System.exit(1);
        }

        return null;
    }
}
