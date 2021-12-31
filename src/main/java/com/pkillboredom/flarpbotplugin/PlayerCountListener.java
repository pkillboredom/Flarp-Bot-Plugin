package com.pkillboredom.flarpbotplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

public class PlayerCountListener implements Listener {
    private long BusyServerExpiryTime;
    private Map<String, Object> ConfigMap;
    private long BusyServerCooldownSeconds;
    private boolean InitializedSuccessfully;

    public PlayerCountListener(Map<String, Object> configMap, java.util.logging.Logger logger)
    {
        super();
        ConfigMap = configMap;
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
                    BusyServerCooldownSeconds = timePortionAsLong / 60;
                }
                else if (busyServerCooldownString.charAt(busyServerCooldownString.length() - 1) == 'h')
                {
                    BusyServerCooldownSeconds = timePortionAsLong / 3600;
                }
                else
                {
                    BusyServerCooldownSeconds = timePortionAsLong;
                }
                InitializedSuccessfully = true;
            }
            catch (Exception e)
            {
                logger.warning("The supplied cooldown duration string was invalid!");
                InitializedSuccessfully = false;
            }
        }
        else if (busyServerCooldown instanceof Long)
        {
            BusyServerCooldownSeconds = (long) busyServerCooldown;
            InitializedSuccessfully = true;
        }
        else if (busyServerCooldown instanceof Integer)
        {
            BusyServerCooldownSeconds = (long) busyServerCooldown;
            InitializedSuccessfully = true;
        }
        else
        {
            logger.warning("The supplied cooldown duration was invalid or null!");
            InitializedSuccessfully = false;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if (InitializedSuccessfully)
        {
            long time = Instant.now().getEpochSecond();
            if (time > BusyServerExpiryTime)
            {
                // Do bot stuff.

                // Set new cooldown expiry.
                BusyServerExpiryTime = time + BusyServerCooldownSeconds;
            }
        }
    }
}
