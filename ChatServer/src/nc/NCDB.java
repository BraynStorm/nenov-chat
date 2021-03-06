package nc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NCDB {
    private static final Logger LOG = Logger.getLogger(NCDB.class.getName());

    static {
        LOG.setParent(Logger.getLogger(NCServer.class.getName()));
    }

    private final static String STMT_CREATE = "" +
            "CREATE TABLE nc_user\n" +
            "(\n" +
            "  id       integer NOT NULL\n" +
            "    PRIMARY KEY AUTOINCREMENT,\n" +
            "  email    text    NOT NULL\n" +
            "    UNIQUE,\n" +
            "  password blob    NOT NULL,\n" +
            "  CHECK (LENGTH(password) == 32)\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE nc_direct_message\n" +
            "(\n" +
            "  sender_id   int NOT NULL,\n" +
            "  receiver_id int NOT NULL,\n" +
            "  message     text,\n" +
            "  CONSTRAINT nc_direct_message_nc_users_id_id_fk\n" +
            "    FOREIGN KEY (sender_id, receiver_id) REFERENCES nc_user (id, id)\n" +
            "      ON UPDATE CASCADE ON DELETE CASCADE\n" +
            ");\n" +
            "\n" +
            "CREATE TABLE nc_friend\n" +
            "(\n" +
            "  user_a int NOT NULL\n" +
            "    REFERENCES nc_user\n" +
            "      ON UPDATE CASCADE ON DELETE CASCADE,\n" +
            "  user_b int NOT NULL\n" +
            "    REFERENCES nc_user\n" +
            "      ON UPDATE CASCADE ON DELETE CASCADE,\n" +
            "  CONSTRAINT nc_friend_pk\n" +
            "    PRIMARY KEY (user_a, user_b)\n" +
            ");\n" +
            "\n";

    private static MessageDigest HASHER;

    {
        try {
            HASHER = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.exit(1);
        }
    }

    private Connection connection;
    private PreparedStatement sqlCreateUser;
    private PreparedStatement sqlFindUser;
    private PreparedStatement sqlFindFriends;
    private PreparedStatement sqlMakeFriends;
    private PreparedStatement sqlRemoveFriends;
    private PreparedStatement sqlSendDirectMessage;
    private PreparedStatement sqlFindEmail;
    private PreparedStatement sqlFindUserID;

    public void connect() {
        connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:ncserver.sqlite");
        } catch (SQLException e) {
            LOG.severe("Can't open Database.");
            System.exit(1);
        }

        try {
            Statement createDB = connection.createStatement();
            createDB.executeUpdate(STMT_CREATE);
            createDB.close();
            LOG.info("Created database");
        } catch (SQLException e) {
            // Eat the exception
        }

        try {
            initializeStatements();
            LOG.info("Statements initialized");
        } catch (SQLException ignored) {
            System.exit(1);
        }

    }

    public void stop() {
        stop(sqlCreateUser);
        stop(sqlFindUser);

        try {
            connection.commit();
        } catch (SQLException ignored) {
        }

        try {
            connection.rollback();
            connection.close();
        } catch (SQLException ignored) {
        }
    }

    public long createUser(String email, String password) {
        try {
            String saltedPassword = password + "5b 82 6b cb fc 71 80 d7 f0 41 7c eb 1c 74 92 fd";
            byte[] hashedPassword = HASHER.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));

            sqlCreateUser.setString(1, email);
            sqlCreateUser.setBytes(2, hashedPassword);

            if (sqlCreateUser.executeUpdate() == 1) {
                return findUser(email, password);
            }
        } catch (SQLException ignored) {
            // Eat
            LOG.severe("Exception in createUser - " + ignored.getMessage());
        } finally {
            // Care for the passwords.
            try {
                sqlCreateUser.clearParameters();
            } catch (SQLException ignored) {
                // Eat
            }
        }
        return -1;
    }

    public long findUser(String email, String password) {
        long userID = -1;
        try {
            String saltedPassword = password + "5b 82 6b cb fc 71 80 d7 f0 41 7c eb 1c 74 92 fd";
            byte[] hashedPassword = HASHER.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));

            sqlFindUser.setString(1, email);
            sqlFindUser.setBytes(2, hashedPassword);

            ResultSet rs = sqlFindUser.executeQuery();
            if (rs.next())
                userID = rs.getLong(1);

            rs.close();
        } catch (SQLException ignored) {
            // Eat
        }

        return userID;
    }

    public List<Long> friendsWith(long userID) {
        List<Long> friends = new ArrayList<>();
        try {
            sqlFindFriends.setLong(1, userID);

            ResultSet rs = sqlFindFriends.executeQuery();
            while (rs.next()) {
                long friendID = rs.getLong(1);
                if (friendID == userID)
                    friendID = rs.getLong(2);

                friends.add(friendID);
            }
            rs.close();
        } catch (SQLException ignored) {
            friends.clear();
        }
        return friends;
    }

    public boolean makeFriends(long userA, long userB) {
        try {
            sqlMakeFriends.setLong(1, userA);
            sqlMakeFriends.setLong(2, userB);

            return sqlMakeFriends.executeUpdate() == 1;
        } catch (SQLException ignored) {
            // Eat
        }
        return false;
    }

    public boolean removeFriends(long userA, long userB) {
        try {
            sqlRemoveFriends.setLong(1, userA);
            sqlRemoveFriends.setLong(2, userB);

            return sqlRemoveFriends.executeUpdate() == 1;
        } catch (SQLException ignored) {
            // Eat
        }
        return false;
    }

    public boolean sendDirectMessage(long sender, long receiver, String message) {
        try {
            sqlSendDirectMessage.setLong(1, sender);
            sqlSendDirectMessage.setLong(2, receiver);
            sqlSendDirectMessage.setString(3, message);

            return sqlSendDirectMessage.executeUpdate() == 1;
        } catch (SQLException ignored) {
            // Eat
        }
        return false;
    }

    public String findEmail(long userID) {
        String result = null;
        try {
            sqlFindEmail.setLong(1, userID);

            ResultSet rs = sqlFindEmail.executeQuery();
            if (rs.next()) {
                result = rs.getString(1);
            }
            rs.close();
        } catch (SQLException ignored) {
            // Eat
        }
        return result;
    }

    public long findUserID(String email) {
        long result = -1;
        try {
            sqlFindUserID.setString(1, email);

            ResultSet rs = sqlFindUserID.executeQuery();
            if (rs.next()) {
                result = rs.getLong(1);
            }
            rs.close();
        } catch (SQLException ignored) {
            // Eat
        }
        return result;
    }

    private void stop(PreparedStatement ps) {
        try {
            ps.close();
        } catch (SQLException ignored) {
        }
    }

    private void initializeStatements() throws SQLException {
        sqlCreateUser = connection.prepareStatement("INSERT INTO nc_user (email, password) VALUES (?, ?);");
        sqlFindUser = connection.prepareStatement("SELECT id FROM nc_user WHERE email = ? AND password = ?;");
        sqlFindFriends = connection.prepareStatement("SELECT * FROM nc_friend WHERE user_a = ?1 OR user_b = ?1;");
        sqlMakeFriends = connection.prepareStatement("INSERT INTO nc_friend (user_a, user_b) VALUES (?, ?);");
        sqlRemoveFriends = connection.prepareStatement("DELETE FROM nc_friend WHERE (user_a = ?1 AND user_b = ?2) OR (user_a = ?2 AND user_b = ?1);");
        sqlFindEmail = connection.prepareStatement("SELECT email FROM nc_user WHERE id = ?;");
        sqlFindUserID = connection.prepareStatement("SELECT id FROM nc_user WHERE email = ?;");
    }


}
