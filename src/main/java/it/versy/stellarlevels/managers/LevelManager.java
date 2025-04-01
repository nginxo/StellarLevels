package it.versy.stellarlevels.managers;

import it.versy.stellarlevels.StellarLevels;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LevelManager {

    private final StellarLevels plugin;
    private final DatabaseManager dbManager;
    private final int maxLevel;
    private final double baseExp;

    public LevelManager(StellarLevels plugin) {
        this.plugin = plugin;
        this.dbManager = new DatabaseManager(plugin); // Inizializza il DatabaseManager

        // Carica le impostazioni dal config
        this.maxLevel = plugin.getConfig().getInt("max-level", 85);
        this.baseExp = plugin.getConfig().getDouble("base-exp", 100.0);
    }

    // Metodo per ottenere il livello di un giocatore
    public int getLevel(Player player) {
        DatabaseManager.PlayerData data = dbManager.loadPlayerData(player.getUniqueId().toString());
        return data != null ? data.getLevel() : 1;
    }

    // Metodo per ottenere l'EXP corrente di un giocatore
    public double getExp(Player player) {
        DatabaseManager.PlayerData data = dbManager.loadPlayerData(player.getUniqueId().toString());
        return data != null ? data.getExp() : 0.0;
    }

    // Metodo per aggiungere EXP a un giocatore
    public void addExp(Player player, double amount) {
        String uuid = player.getUniqueId().toString();
        DatabaseManager.PlayerData data = dbManager.loadPlayerData(uuid);
        if (data == null) {
            data = new DatabaseManager.PlayerData(1, 0.0);
        }

        double newExp = data.getExp() + amount;
        int currentLevel = data.getLevel();
        player.sendMessage(plugin.getMessage("exp-gained")
                .replace("%exp%", String.format("%.2f", amount))
                .replace("%current_exp%", String.format("%.2f", newExp)));

        // Controlla se il giocatore ha abbastanza exp per il livello successivo
        while (newExp >= getExpRequired(currentLevel) && currentLevel < maxLevel) {
            newExp -= getExpRequired(currentLevel);
            currentLevel++;
            player.sendMessage(plugin.getMessage("level-up").replace("%level%", String.valueOf(currentLevel)));

            // Controlla e applica i premi per il livello corrente
            checkAndApplyRewards(player, currentLevel);
        }

        if (currentLevel >= maxLevel) {
            player.sendMessage(plugin.getMessage("max-level-reached"));
        }

        // Salva i dati aggiornati nel database
        dbManager.savePlayerData(uuid, currentLevel, newExp);
    }

    // Metodo per ottenere l'EXP richiesta per il livello successivo
    public double getExpRequired(int level) {
        return baseExp * level;
    }

    // Metodo per impostare il livello di un giocatore (ad esempio, per resettare il livello)
    public void setLevel(Player player, int level) {
        String uuid = player.getUniqueId().toString();
        dbManager.savePlayerData(uuid, level, 0.0); // Resetta l'EXP a zero
        player.sendMessage(plugin.getMessage("level-up").replace("%level%", String.valueOf(level)));
        checkAndApplyRewards(player, level);
    }

    // Metodo per chiudere la connessione al database quando il plugin viene disabilitato
    public void closeDatabase() {
        dbManager.close();
    }

    // Metodo per controllare e applicare i premi
    private void checkAndApplyRewards(Player player, int level) {
        FileConfiguration rewardsConfig = plugin.getRewardsConfig();

        // Itera attraverso tutte le sezioni nella configurazione "rewards"
        for (String key : rewardsConfig.getConfigurationSection("rewards").getKeys(false)) {
            String type = rewardsConfig.getString("rewards." + key + ".type");
            int rewardLevel = rewardsConfig.getInt("rewards." + key + ".level");

            // Se il livello del giocatore è maggiore o uguale al livello del premio, applica il premio
            if (level == rewardLevel) {
                switch (type.toLowerCase()) {
                    case "money":
                        double amount = rewardsConfig.getDouble("rewards." + key + ".value");

                        Economy econ = StellarLevels.getEconomy();
                        econ.depositPlayer(player, amount);

                        player.sendMessage("Hai ricevuto " + amount + " monete per aver raggiunto il livello " + rewardLevel + "!");
                        break;

                    case "permission":
                        String permission = rewardsConfig.getString("rewards." + key + ".value");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set " + permission);
                        player.sendMessage("Hai ricevuto il permesso: " + permission + " per aver raggiunto il livello " + rewardLevel + "!");
                        break;

                    case "item":
                        String itemType = rewardsConfig.getString("rewards." + key + ".value");
                        int amountItem = rewardsConfig.getInt("rewards." + key + ".amount");
                        ItemStack item = new ItemStack(Material.getMaterial(itemType.toUpperCase()), amountItem);
                        player.getInventory().addItem(item);
                        player.sendMessage("Hai ricevuto " + amountItem + " " + itemType + "(s) per aver raggiunto il livello " + rewardLevel + "!");
                        break;

                    case "group":
                        String group = rewardsConfig.getString("rewards." + key + ".value");
                        // Implementa qui la logica per impostare il gruppo del giocatore (richiede un plugin di permessi come LuckPerms)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " parent add " + group);
                        player.sendMessage("Sei stato aggiunto al gruppo: " + group + " per aver raggiunto il livello " + rewardLevel + "!");
                        break;

                    case "gem":
                        int gems = rewardsConfig.getInt("rewards." + key + ".value");
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gadmin add " + player.getName() + " " + gems);
                        player.sendMessage("Hai ricevuto " + gems + " gemme per aver raggiunto il livello " + rewardLevel + "!");
                        break;

                    case "command":
                        String cmd = rewardsConfig.getString("rewards." + key + ".value");
                        assert cmd != null;

                        if (cmd.startsWith("/")) {
                            cmd = cmd.substring(1);
                        }

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("%player%", player.getName()));

                    default:
                        player.sendMessage("Tipo di premio non riconosciuto: " + type);
                }
            }
        }
    }

    public void decrementLevel(Player player) {
        int currentLevel = getLevel(player);
        if (currentLevel > 1) {
            setLevel(player, currentLevel - 1);
        } else {
            player.sendMessage("Sei già al livello minimo!");
        }
    }

    public void resetLevel(Player player) {
        setLevel(player, 1);
    }

    public boolean hasReachedLevel(Player player, int level) {
        return getLevel(player) >= level;
    }
}
