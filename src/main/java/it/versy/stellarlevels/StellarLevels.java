package it.versy.stellarlevels;

import it.versy.stellarlevels.commands.CommandHandler;
import it.versy.stellarlevels.events.KillEvent;
import it.versy.stellarlevels.managers.LevelManager;
import it.versy.stellarlevels.managers.PlaceholderManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class StellarLevels extends JavaPlugin {

    public static StellarLevels instance;

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    private LevelManager levelManager;
    private File rewardsFile;
    private FileConfiguration rewardsConfig;

    @Override
    public void onEnable() {

        // Inizializza Vault
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();

        instance = this;

        // Inizializza il LevelManager
        levelManager = new LevelManager(this);

        // Crea e carica config.yml e rewards.yml
        saveDefaultConfig();
        createRewardsFile();
        loadRewardsConfig();

        // Registra il gestore dei comandi
        CommandHandler commandHandler = new CommandHandler(this);
        getCommand("livello").setExecutor(commandHandler);
        getCommand("stellarlevels").setExecutor(commandHandler);
        getCommand("stellarlevels").setTabCompleter(commandHandler);
        getCommand("reloadls").setExecutor(commandHandler);

        // Registra gli eventi
        getServer().getPluginManager().registerEvents(new KillEvent(this), this);

        // Registra il PlaceholderManager
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderManager(this).register();
            getLogger().info("PlaceholderManager registrato con successo!");
        } else {
            getLogger().warning("PlaceholderAPI non trovato! I placeholders personalizzati non funzioneranno.");
        }

        getLogger().info("StellarLevels attivato!");
    }

    @Override
    public void onDisable() {
        // Chiudi la connessione al database
        if (levelManager != null) {
            levelManager.closeDatabase();
        }
        getLogger().info("StellarLevels disattivato!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    // Metodo per creare il file rewards.yml se non esiste
    private void createRewardsFile() {
        rewardsFile = new File(getDataFolder(), "rewards.yml");
        if (!rewardsFile.exists()) {
            rewardsFile.getParentFile().mkdirs();
            saveResource("rewards.yml", false);
        }
    }

    // Metodo per caricare il file rewards.yml
    private void loadRewardsConfig() {
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
    }

    // Metodo per ottenere la configurazione dei rewards
    public FileConfiguration getRewardsConfig() {
        return rewardsConfig;
    }

    public void reloadRewardsConfig() {
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    // Metodo per ottenere i messaggi personalizzati
    public String getMessage(String path) {
        return getConfig().getString("messages." + path).replace("&", "§");
    }

    public static StellarLevels getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

}
