package me.wyno.wynogen.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.wyno.wynogen.WynoGen;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final WynoGen plugin;
    private HikariDataSource dataSource;
    private boolean isMySQL;

    public DatabaseManager(WynoGen plugin) {
        this.plugin = plugin;
        setupPool();
        createTables();
    }

    private void setupPool() {
        String type = plugin.getConfig().getString("database.type", "SQLITE").toUpperCase();
        this.isMySQL = type.equals("MYSQL");

        HikariConfig config = new HikariConfig();

        if (isMySQL) {
            String host = plugin.getConfig().getString("database.DB_HOST", "localhost");
            int port = plugin.getConfig().getInt("database.DB_PORT", 3306);
            String database = plugin.getConfig().getString("database.DB_NAME", "smp_unv_db");
            String username = plugin.getConfig().getString("database.DB_USER", "root");
            String password = plugin.getConfig().getString("database.DB_PASSWORD", "");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
        } else {
            // SQLite Fallback
            File dbFile = new File(plugin.getDataFolder(), "data.db");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setPoolName("WynoGen-SQLite");
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        try {
            this.dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Successfully initialized " + type + " database connection.");
        } catch (Exception e) {
            plugin.getLogger().severe("Could not initialize database! Check your configuration.");
            e.printStackTrace();
        }
    }

    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid VARCHAR(36) NOT NULL, " +
                "world_id VARCHAR(64) NOT NULL, " +
                "inventory LONGTEXT, " +
                "armor LONGTEXT, " +
                "ender_chest LONGTEXT, " +
                "stats TEXT, " +
                "potions TEXT, " +
                "advancements LONGTEXT, " +
                "PRIMARY KEY (uuid, world_id)" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create database tables!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("DataSource is null");
        return dataSource.getConnection();
    }

    public boolean isMySQL() {
        return isMySQL;
    }

    public String getUpsertSQL() {
        if (isMySQL) {
            return "INSERT INTO player_data (uuid, world_id, inventory, armor, ender_chest, stats, potions, advancements) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "inventory = VALUES(inventory), " +
                    "armor = VALUES(armor), " +
                    "ender_chest = VALUES(ender_chest), " +
                    "stats = VALUES(stats), " +
                    "potions = VALUES(potions), " +
                    "advancements = VALUES(advancements);";
        } else {
            // SQLite Syntax
            return "INSERT INTO player_data (uuid, world_id, inventory, armor, ender_chest, stats, potions, advancements) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(uuid, world_id) DO UPDATE SET " +
                    "inventory = EXCLUDED.inventory, " +
                    "armor = EXCLUDED.armor, " +
                    "ender_chest = EXCLUDED.ender_chest, " +
                    "stats = EXCLUDED.stats, " +
                    "potions = EXCLUDED.potions, " +
                    "advancements = EXCLUDED.advancements;";
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
