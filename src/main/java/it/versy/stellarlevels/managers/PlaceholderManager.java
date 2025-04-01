package it.versy.stellarlevels.managers;

import it.versy.stellarlevels.StellarLevels;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderManager extends PlaceholderExpansion {

    private final StellarLevels plugin;

    public PlaceholderManager(StellarLevels plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "levelSystem";
    }

    @Override
    public @NotNull String getAuthor() {
        return "VersyLiones";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Persistente attraverso i riavvii
    }

    @Override
    public boolean canRegister() {
        return true; // Questo placeholder può essere registrato
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // %levelsystem_level% -> Restituisce il livello del giocatore
        if (identifier.equalsIgnoreCase("level")) {
            return String.valueOf(plugin.getLevelManager().getLevel(player));
        }

        // %levelsystem_exp% -> Restituisce l'EXP corrente e l'EXP necessaria per il level up
        if (identifier.equalsIgnoreCase("exp")) {
            double currentExp = plugin.getLevelManager().getExp(player);
            int level = plugin.getLevelManager().getLevel(player);
            double expRequired = plugin.getLevelManager().getExpRequired(level);
            return String.format("%.2f/%.2f", currentExp, expRequired);
        }

        // Placeholder non riconosciuto
        return null;
    }
}
