package org.game24.marketsync.game.hook;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public class AuthMeHook {

    private final JavaPlugin plugin;

    private volatile AuthMeApi api;

    public AuthMeHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isAvailable() {
        return api() != null;
    }

    public boolean isRegistered(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        AuthMeApi authMeApi = api();
        if (authMeApi == null) {
            plugin.getSLF4JLogger().error("AuthMe API is not available");
            return false;
        }

        return authMeApi.isRegistered(username.toLowerCase(Locale.ROOT));
    }

    private AuthMeApi api() {
        if (!Bukkit.getPluginManager().isPluginEnabled("AuthMe")) {
            return null;
        }

        AuthMeApi cached = api;
        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            if (api == null) {
                api = AuthMeApi.getInstance();
                if (api == null) {
                    plugin.getSLF4JLogger().warn("AuthMe is enabled but API is not initialized yet");
                }
            }
            return api;
        }
    }
}
