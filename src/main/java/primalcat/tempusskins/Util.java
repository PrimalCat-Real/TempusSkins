package primalcat.tempusskins;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
public class Util {
    private static final Logger logger = TempusSkins.plugin.getLogger();
    private static Connection connection;

    public static void connect() {
        String dbPath = "jdbc:sqlite:" + TempusSkins.plugin.getDataFolder().getAbsolutePath() + "/skins.db";
        File dbFile = new File(dbPath);
        boolean isNewDb = !dbFile.exists();

        try {
            connection = DriverManager.getConnection(dbPath);

            if (isNewDb) {
                createTable();
            }
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }
    }

    public static void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }
    }

    private static void createTable() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS skins (" + "player_nickname TEXT, " + "skin_name TEXT, " + "skin_value TEXT, " + "skin_signature TEXT, " + "PRIMARY KEY(player_nickname, skin_name)" + ")");
            statement.close();
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }
    }

    public static LinkedHashMap<String, String> getPlayerSkins(String playerNickname) {
        LinkedHashMap<String, String> skins = new LinkedHashMap<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT skin_name, skin_value, skin_signature FROM skins WHERE player_nickname = ?");
            statement.setString(1, playerNickname);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String skinName = resultSet.getString("skin_name");
                String skinValue = resultSet.getString("skin_value");
                String skinSignature = resultSet.getString("skin_signature");
                skins.put(skinName, skinValue + "|" + skinSignature);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }

        return skins;
    }

    public static String getSkinByName(String playerNickname, String skinName, DataType type) {
        String skinData = null;

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT skin_value, skin_signature FROM skins WHERE player_nickname = ? AND skin_name = ?");
            statement.setString(1, playerNickname);
            statement.setString(2, skinName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                skinData = resultSet.getString(type == DataType.VALUE ? "skin_value" : "skin_signature");
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }

        return skinData;
    }

    public static void saveSkinByName(String playerNickname, String skinName, String skinValue, String skinSignature) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO skins(player_nickname, skin_name, skin_value, skin_signature) VALUES (?, ?, ?, ?)");
            statement.setString(1, playerNickname);
            statement.setString(2, skinName);
            statement.setString(3, skinValue);
            statement.setString(4, skinSignature);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }
    }

    public static void removeSkinByName(String playerNickname, String skinName) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM skins WHERE player_nickname = ? AND skin_name = ?");
            statement.setString(1, playerNickname);
            statement.setString(2, skinName);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }
    }


    enum DataType {
        VALUE, SIGNATURE
    }
}
