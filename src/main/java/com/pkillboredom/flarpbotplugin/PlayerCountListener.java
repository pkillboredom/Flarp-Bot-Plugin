package com.pkillboredom.flarpbotplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pkillboredom.flarpbotplugin.models.ExternalRequestHandlerResponse;
import com.pkillboredom.flarpbotplugin.models.SayInChannelRequest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.SSLHandshakeException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class PlayerCountListener implements Listener {
    private final Gson gson = new GsonBuilder().create();
    private long BusyServerExpiryTime;
    private Map<String, Object> ConfigMap;
    private Logger Logger;
    private long BusyServerCooldownSeconds;
    private boolean UrlInitializedSuccessfully;
    private boolean TimeInitializedSuccessfully;
    private URL sayEndpoint;
    private String GuildId;
    private String ChannelId;
    private String BusyServerTemplate;
    private long BusyServerMinimum;

    public PlayerCountListener(Map<String, Object> configMap, Logger logger)
    {
        super();
        Logger = logger;
        ConfigMap = configMap;

        try {
            sayEndpoint = new URL((String) ConfigMap.get("BotSayEndpoint"));
            UrlInitializedSuccessfully = true;
            GuildId = ((String) ConfigMap.get("BotSayGuild"));
            ChannelId = ((String) ConfigMap.get("BotSayChannel"));
            BusyServerTemplate = ((String) ConfigMap.get("BotSayBusyTemplate"));
            BusyServerMinimum = Long.parseLong((String) ConfigMap.get("BusyServerMinimum"));
        }
        catch (MalformedURLException e)
        {
            logger.warning("The endpoint URL specified was malformed.");
            UrlInitializedSuccessfully = false;
        }

        Object busyServerCooldown = ConfigMap.get("BusyServerCooldown");
        // We should allow strings or json numbers.
        if (busyServerCooldown instanceof String)
        {
            // Allow appending with s, m, or h, case-insensitive.
            String busyServerCooldownString = ((String) busyServerCooldown).toLowerCase(Locale.ROOT);
            try
            {
                long timePortionAsLong = Long.parseLong(busyServerCooldownString.substring(0,
                        busyServerCooldownString.length() - 1));
                if (busyServerCooldownString.charAt(busyServerCooldownString.length() - 1) == 's')
                {
                    BusyServerCooldownSeconds = timePortionAsLong;
                }
                else if (busyServerCooldownString.charAt(busyServerCooldownString.length() - 1) == 'm')
                {
                    BusyServerCooldownSeconds = timePortionAsLong * 60;
                }
                else if (busyServerCooldownString.charAt(busyServerCooldownString.length() - 1) == 'h')
                {
                    BusyServerCooldownSeconds = timePortionAsLong * 3600;
                }
                else
                {
                    BusyServerCooldownSeconds = timePortionAsLong;
                }
                TimeInitializedSuccessfully = true;
            }
            catch (Exception e)
            {
                logger.warning("The supplied cooldown duration string was invalid!");
                TimeInitializedSuccessfully = false;
            }
        }
        else if (busyServerCooldown instanceof Long)
        {
            BusyServerCooldownSeconds = (long) busyServerCooldown;
            TimeInitializedSuccessfully = true;
        }
        else if (busyServerCooldown instanceof Integer)
        {
            BusyServerCooldownSeconds = (long) busyServerCooldown;
            TimeInitializedSuccessfully = true;
        }
        else
        {
            logger.warning("The supplied cooldown duration was invalid or null!");
            TimeInitializedSuccessfully = false;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (UrlInitializedSuccessfully && TimeInitializedSuccessfully)
        {
            int playerCount = getServer().getOnlinePlayers().size();
            long time = Instant.now().getEpochSecond();
            if (time > BusyServerExpiryTime && playerCount >= BusyServerMinimum)
            {
                try {
                    // Do bot stuff.
                    HttpURLConnection con = (HttpURLConnection) sayEndpoint.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json; utf-8");
                    con.setRequestProperty("Accept", "application/json");
                    con.setDoOutput(true);

                    SayInChannelRequest request =
                            new SayInChannelRequest(UUID.randomUUID().toString(),
                                    GuildId,
                                    ChannelId,
                                    String.format(BusyServerTemplate, playerCount));

                    String requestString = gson.toJson(request);

                    try(OutputStream os = con.getOutputStream()) {
                        byte[] input = requestString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    try(BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }

                        ExternalRequestHandlerResponse result =
                                new Gson().fromJson(response.toString(), ExternalRequestHandlerResponse.class);
                        //System.out.println(response.toString());

                        // Set new cooldown expiry.
                        BusyServerExpiryTime = time + BusyServerCooldownSeconds;
                    }

                }
                catch (SSLHandshakeException exception)
                {
                    Logger.warning("The SSL Handshake failed.");
                    exception.printStackTrace();
                }
                catch (IOException exception)
                {
                    Logger.warning("Failed to connect to URL specified for Bot Say.");
                }
            }
        }
    }
}
