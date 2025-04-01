package it.versy.stellarlevels.managers;

import it.versy.stellarlevels.StellarLevels;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final StellarLevels plugin;
    private Connection connection;
    private String databaseType;

    public DatabaseManager(StellarLevels plugin) {
        this.plugin = plugin;
        connect(); // Connette al database
        createTable(); // Crea le tabelle necessarie se non esistono
    }

    // Metodo per connettersi al database
    private void connect() {
        try {
            // Carica manualmente il driver H2
            Class.forName("org.h2.Driver");

            FileConfiguration config = plugin.getConfig();
            databaseType = config.getString("database.use", "h2").toLowerCase();

            if (databaseType.equals("mysql")) {
                // Configurazione MySQL/MariaDB
                String host = config.getString("database.mysql.host");
                int port = config.getInt("database.mysql.port");
                String database = config.getString("database.mysql.database");
                String username = config.getString("database.mysql.username");
                String password = config.getString("database.mysql.password");

                String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true";
                connection = DriverManager.getConnection(url, username, password);
            } else {
                // Configurazione H2 - Utilizza il percorso fornito
                File dbFile = new File(plugin.getDataFolder(), "db/data.db");
                // Verifica che la cartella esista, se no, la crea
                if (!dbFile.getParentFile().exists()) {
                    dbFile.getParentFile().mkdirs();
                }

                // Configura il percorso del database H2
                String url = "jdbc:h2:" + dbFile.getAbsolutePath();
                connection = DriverManager.getConnection(url, "sa", ""); // Connessione H2 senza password
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Metodo per creare la tabella dei livelli se non esiste
    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS player_levels (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "level INT NOT NULL," +
                    "exp DOUBLE NOT NULL" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo per salvare i dati di un giocatore
    public void savePlayerData(String uuid, int level, double exp) {
        String query;
        if (databaseType.equals("mysql")) {
            query = "REPLACE INTO player_levels (uuid, level, exp) VALUES (?, ?, ?);"; // REPLACE è specifico di MySQL/MariaDB
        } else {
            query = "MERGE INTO player_levels (uuid, level, exp) VALUES (?, ?, ?);"; // MERGE è specifico di H2
        }

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setInt(2, level);
            ps.setDouble(3, exp);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Metodo per caricare i dati di un giocatore
    public PlayerData loadPlayerData(String uuid) {
        String query = "SELECT * FROM player_levels WHERE uuid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int level = rs.getInt("level");
                double exp = rs.getDouble("exp");
                return new PlayerData(level, exp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Metodo per chiudere la connessione al database
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Classe per gestire i dati di un giocatore
    public static class PlayerData {
        private final int level;
        private final double exp;

        public PlayerData(int level, double exp) {
            this.level = level;
            this.exp = exp;
        }

        public int getLevel() {
            return level;
        }

        public double getExp() {
            return exp;
        }
    }
}
