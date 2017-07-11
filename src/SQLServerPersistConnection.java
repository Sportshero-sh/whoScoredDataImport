import ws.*;

import java.sql.*;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class SQLServerPersistConnection {

    private static final String QUERY_PLAYER_SQL = "SELECT * FROM player WHERE id_ws =? ";
    private static final String QUERY_PLAYERS_SQL = "SELECT * FROM player WHERE id_ws in(";
    private static final String INSERT_PLAYER_SQL = "INSERT INTO player"
            + "(id_ws, name, dateOfBirth, height, weight) VALUES"
            + "(?,?,?,?,?)";

    private static final String QUERY_TEAM_SQL = "SELECT * FROM team WHERE id_ws =? ";
    private static final String INSERT_TEAM_SQL = "INSERT INTO team"
            + "(id_ws, name) VALUES"
            + "(?,?)";

    private static final String QUERY_MATCH_SQL = "SELECT * FROM match WHERE id_ws =? ";
    private static final String INSERT_MATCH_SQL = "INSERT INTO match"
            + "(id_ws, tournamentName, home_id, away_id, home_name, away_name, home_score, away_score, startTimeUtc) VALUES"
            + "(?,?,?,?,?,?,?,?,?)";

    private static final String QUERY_MATCH_PLAYER_STATS_SQL = "SELECT * FROM match_player_stats WHERE match_id =? and player_id = ?";
    private static final String QUERY_MATCH_PLAYER_STATSES_SQL = "SELECT * FROM match_player_stats WHERE match_id =? and player_id in(";
    private static final String INSERT_MATCH_PLAYER_STATS_SQL = "INSERT INTO match_player_stats"
            + "(match_id, player_id, position, rating, isSub, isMoM) VALUES"
            + "(?,?,?,?,?,?)";

    private Connection mConnection = null;

    public SQLServerPersistConnection(String url, String login, String password, String database) {
        String connectionURL = "jdbc:sqlserver://" + url + ";user=" + login + ";password=" + password + ";database=" + database;

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            mConnection = DriverManager.getConnection(connectionURL);
            mConnection.setAutoCommit(false);

            System.out.println("Connect to database success: " + url);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isMatchExist(int id) {
        try {

            boolean isExist = false;

            PreparedStatement ps = mConnection.prepareStatement(QUERY_MATCH_SQL);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                isExist = true;
                break;
            }

            return isExist;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void persistMatch(int id, Match match) {
        System.out.println("Persist match: " + match);
        try {

            boolean isExist = false;

            PreparedStatement ps = mConnection.prepareStatement(QUERY_MATCH_SQL);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                isExist = true;
                break;
            }

            if (!isExist) {
                ps = mConnection.prepareStatement(INSERT_MATCH_SQL);
                MatchInfo info = match.info;
                ps.setInt(1, id);
                ps.setString(2, info.tournamentName);
                ps.setInt(3, info.homeId);
                ps.setInt(4, info.awayId);
                ps.setString(5, info.homeName);
                ps.setString(6, info.awayName);
                ps.setInt(7, info.homeScore);
                ps.setInt(8, info.awayScore);
                ps.setTimestamp(9, getTimeStamp(info.startTimeUtc));

                ps.executeUpdate();

                mConnection.commit();
            }

        } catch (SQLException e) {
            try {
                mConnection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public boolean isTeamExist(int id) {
        try {

            boolean isExist = false;

            PreparedStatement ps = mConnection.prepareStatement(QUERY_TEAM_SQL);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                isExist = true;
                break;
            }

            return isExist;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void persistTeam(ArrayList<Team> teams) {
        System.out.println("Persist teams.");
        try {
            boolean needCommit = false;

            for (int i = 0; i < teams.size(); i ++) {
                Team t = teams.get(i);
                boolean isExist = false;

                PreparedStatement ps = mConnection.prepareStatement(QUERY_TEAM_SQL);
                ps.setInt(1, t.id);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    isExist = true;
                    break;
                }

                if (!isExist) {
                    needCommit = true;

                    ps = mConnection.prepareStatement(INSERT_TEAM_SQL);
                    ps.setInt(1, t.id);
                    ps.setString(2, t.name);

                    ps.executeUpdate();
                }
            }

            if (needCommit) {
                mConnection.commit();
            }

        } catch (SQLException e) {
            try {
                mConnection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public boolean isPlayerExist(int id) {
        try {

            boolean isExist = false;

            PreparedStatement ps = mConnection.prepareStatement(QUERY_PLAYER_SQL);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                isExist = true;
                break;
            }

            return isExist;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void persistPlayer(ArrayList<Player> players) {
        System.out.println("Persist players.");
        try {
            // Remove those players already exists.
            PreparedStatement ps = mConnection.prepareStatement(getBatchQueryString(QUERY_PLAYERS_SQL, players.size()));
            for (int i = 0; i < players.size(); i++) {
                ps.setInt(i + 1, players.get(i).id);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int playerId = rs.getInt("id_ws");
                for (int i = 0; i < players.size(); i++) {
                    if (playerId == players.get(i).id) {
                        players.remove(i);
                        break;
                    }
                }
            }

            if (players.size() > 0) {
                System.out.println("Batch insert.");

                PreparedStatement batchInsert = mConnection.prepareStatement(INSERT_PLAYER_SQL);
                for (Player p : players) {
                    batchInsert.setInt(1, p.id);
                    batchInsert.setString(2, p.info.name);
                    batchInsert.setTimestamp(3, getTimeStamp(p.info.dateOfBirth));
                    batchInsert.setInt(4, p.info.height);
                    batchInsert.setInt(5, p.info.weight);
                    batchInsert.addBatch();
                }

                batchInsert.executeBatch();
                batchInsert.close();
                mConnection.commit();
            }
        } catch (SQLException e) {
            try {
                mConnection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Can only add player stats in one match.
     * @param matchId
     * @param stats
     */
    public void persistMatchPlayerStats(int matchId, ArrayList<PlayerLiveStatistics> stats) {
        System.out.println("Persist match player stats.");

        try  {
            // Remove those match-player-stats already exists.
            PreparedStatement ps = mConnection.prepareStatement(getBatchQueryString(QUERY_MATCH_PLAYER_STATSES_SQL, stats.size()));

            ps.setInt(1, matchId);
            for (int i = 0; i < stats.size(); i++) {
                ps.setInt(i + 2, stats.get(i).id);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int playerId = rs.getInt("player_id");
                for (int i = 0; i < stats.size(); i++) {
                    if (playerId == stats.get(i).id) {
                        stats.remove(i);
                        break;
                    }
                }
            }

            if (stats.size() > 0) {
                System.out.println("Batch insert.");

                PreparedStatement batchInsert = mConnection.prepareStatement(INSERT_MATCH_PLAYER_STATS_SQL);

                for (PlayerLiveStatistics playerLiveStatistics : stats) {
                    batchInsert.setInt(1, matchId);
                    batchInsert.setInt(2, playerLiveStatistics.id);
                    batchInsert.setString(3, playerLiveStatistics.stats.position);
                    batchInsert.setFloat(4, playerLiveStatistics.rating);
                    batchInsert.setBoolean(5, playerLiveStatistics.isSub);
                    batchInsert.setBoolean(6, playerLiveStatistics.isMoM);
                    batchInsert.addBatch();
                }

                batchInsert.executeBatch();
                batchInsert.close();
                mConnection.commit();
            }

        } catch (SQLException e) {
            try {
                mConnection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }


    public void close() {
        try {
            mConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void test() {
        Statement sta = null;
        try {
            sta = mConnection.createStatement();
            String Sql = "select * from team";
            ResultSet rs = sta.executeQuery(Sql);
            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private String getBatchQueryString(String baseSQL, int batchSize) {
        StringBuffer batchQuery = new StringBuffer();

        batchQuery.append(baseSQL);
        for (int i = 0; i < batchSize; i++) {
            batchQuery.append("?");
            if (i + 1 < batchSize) {
                batchQuery.append(",");
            }
        }
        batchQuery.append(")");

        return batchQuery.toString();
    }

    private static java.sql.Timestamp getTimeStamp(String dateString) throws ParseException {
        if (dateString != null) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            java.util.Date datetime = format.parse(dateString);
            return new java.sql.Timestamp(datetime.getTime());
        } else {
            return null;
        }
    }


}
