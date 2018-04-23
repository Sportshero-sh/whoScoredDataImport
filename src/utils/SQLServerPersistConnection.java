package utils;

import sh.*;
import ws.*;

import java.sql.*;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class SQLServerPersistConnection {

    private static final int HOME_AWAY_STATS_MATCH_NUMBER = 5;
    private static final int LAST_MEETINGS_MATCH_NUMBER = 5;

    private static final String QUERY_STAGE_SQL = "SELECT * FROM stage WHERE id_ws =?";
    private static final String INSERT_STAGE_SQL = "INSERT INTO stage"
            + "(id_ws, name, tournament_Id) VALUES"
            + "(?,?,?)";

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
    private static final String QUERY_MATCH_PRE_SQL = "SELECT top " + HOME_AWAY_STATS_MATCH_NUMBER + " * FROM match WHERE (home_id =? or away_id =?) and startTimeUtc <? ORDER by startTimeUtc desc";
    private static final String QUERY_LAST_MEETINGS_SQL = "SELECT top " + LAST_MEETINGS_MATCH_NUMBER + " * FROM match WHERE ((home_id =? and away_id =?) or (home_id =? and away_id =?)) and startTimeUtc <? ORDER by startTimeUtc desc";
    private static final String INSERT_MATCH_SQL = "INSERT INTO match"
            + "(id_ws, tournamentName, home_id, away_id, home_name, away_name, home_score, away_score, startTimeUtc, stage_id) VALUES"
            + "(?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_MATCH_STAGE_SQL = "UPDATE match set stage_id =? where id_ws =?";


    private static final String QUERY_MATCH_PLAYER_STATS_SQL = "SELECT * FROM match_player_stats WHERE match_id =? and player_id = ?";
    private static final String QUERY_MATCH_PLAYER_STATSES_SQL = "SELECT * FROM match_player_stats WHERE match_id =? and player_id in(";
    private static final String INSERT_MATCH_PLAYER_STATS_SQL = "INSERT INTO match_player_stats"
            + "(match_id, team_id, player_id, position, rating, isSub, isMoM, man_of_the_match, formation_place, total_sub_on, total_sub_off," +
            " totalPasses, passAccuracy, aerialsWon, touches, fouls, shots, dribblesWon, tackles, saves, assist, goal_penalty, goals, goal_own," +
            " penalty_missed, minutes_played, shots_blocked, was_dribbled, interceptions, was_fouled, offsides, dispossessed, turnovers, crosses," +
            " long_balls, through_balls, shotsOnTarget, yellow, red, secondYellow, penaltySave, error_lead_to_goal, last_man_tackle, clearance_off_line, hit_woodwork) VALUES"
            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_PLAYER_STATS_SQL = "update match_player_stats set man_of_the_match=?, formation_place=?, total_sub_on=?, total_sub_off=?, totalPasses=?, passAccuracy=?, aerialsWon=?, touches=?," +
            "fouls=?, shots=?, dribblesWon=?, tackles=?, saves=?, assist=?, goal_penalty=?, goals=?, goal_own=?, penalty_missed=?, minutes_played=?, shots_blocked=?, was_dribbled=?," +
            "interceptions=?, was_fouled=?, offsides=?, dispossessed=?, turnovers=?, crosses=?, long_balls=?, through_balls=?, shotsOnTarget=?, yellow=?, red=?, secondYellow=?, " +
            "penaltySave=?, error_lead_to_goal=?, last_man_tackle=?, clearance_off_line=?, hit_woodwork=? " +
            "where match_id =? and player_id =?";
    private static final String QUERY_MATCH_STATS_PRE_SQL = "SELECT * FROM match_player_stats " +
            "WHERE isSub = 'false' and match_id in (" +
            "SELECT top " + HOME_AWAY_STATS_MATCH_NUMBER + " id_ws FROM match WHERE (home_id =? or away_id =?) and startTimeUtc <? ORDER by startTimeUtc desc" +
            ")";
    private static final String QUERY_LAST_MEETING_STATS_SQL = "SELECT * FROM match_player_stats " +
            "WHERE isSub = 'false' and match_id in (" +
            "SELECT top " + LAST_MEETINGS_MATCH_NUMBER + " id_ws FROM match WHERE ((home_id =? and away_id =?) or (home_id =? and away_id =?)) and startTimeUtc <? ORDER by startTimeUtc desc" +
            ")";
    private static final String QUERY_PLAYER_AVG_RATING_SQL =
            "select player_id, count(player_id), avg(rating) from match_player_stats " +
            "where team_id = ? and rating > 0 and match_id in (" +
                "select top 10 id_ws from match m " +
                "where (home_id = ? or away_id = ?) and startTimeUtc < ? " +
                "order by m.startTimeUtc desc" +
            ") and player_id in (" +
                "select player_id from match_player_stats where match_id = ? and team_id = ?" +
            ")" +
            "group by player_id order by count(player_id) desc";

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

    public void persistStage(Stage stage) {
        try {

            boolean isExist = false;

            PreparedStatement ps = mConnection.prepareStatement(QUERY_STAGE_SQL);
            ps.setInt(1, stage.id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                isExist = true;
                break;
            }

            if (!isExist) {
                ps = mConnection.prepareStatement(INSERT_STAGE_SQL);
                ps.setInt(1, stage.id);
                ps.setString(2, stage.name);
                ps.setInt(3, stage.tournamentId);

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
        }
    }


    public boolean isMatchExist(int id) {
        return getMatch(id) != null;
    }

    public Match getMatch(int id) {
        Match match = null;
        try {

            PreparedStatement ps = mConnection.prepareStatement(QUERY_MATCH_SQL);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                match = new Match();
                match.id = rs.getInt("id_ws");
                match.info = new MatchInfo();
                match.info.tournamentName = rs.getString("tournamentName");
                match.info.homeId = rs.getInt("home_id");
                match.info.awayId = rs.getInt("away_id");
                match.info.homeName = rs.getString("home_name");
                match.info.awayName = rs.getString("away_name");
                match.info.homeScore = rs.getInt("home_score");
                match.info.awayScore = rs.getInt("away_score");
                match.info.startTimeDate = rs.getDate("startTimeUtc");
                break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return match;
    }

    public void persistMatch(Match match, int stageId) {
        try {

            boolean isExist = false;

            PreparedStatement ps = mConnection.prepareStatement(QUERY_MATCH_SQL);
            ps.setInt(1, match.id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                isExist = true;
                break;
            }

            if (!isExist) {
                ps = mConnection.prepareStatement(INSERT_MATCH_SQL);
                MatchInfo info = match.info;
                ps.setInt(1, match.id);
                ps.setString(2, info.tournamentName);
                ps.setInt(3, info.homeId);
                ps.setInt(4, info.awayId);
                ps.setString(5, info.homeName);
                ps.setString(6, info.awayName);
                ps.setInt(7, info.homeScore);
                ps.setInt(8, info.awayScore);
                ps.setTimestamp(9, getTimeStamp(info.startTimeUtc));
                ps.setInt(10, stageId);

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

    public void updateMatchStage(Stage stage) {
        int stageId = stage.id;

        try {
            PreparedStatement batchUpdate = mConnection.prepareStatement(UPDATE_MATCH_STAGE_SQL);
            for (Fixture fixture : stage.fixtures) {

                batchUpdate.setInt(1, stageId);
                batchUpdate.setInt(2, fixture.id);

                batchUpdate.addBatch();
            }

            batchUpdate.executeBatch();
            batchUpdate.close();
            mConnection.commit();
        } catch (SQLException e) {
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
                PreparedStatement batchInsert = mConnection.prepareStatement(INSERT_MATCH_PLAYER_STATS_SQL);

                for (PlayerLiveStatistics playerLiveStatistics : stats) {
                    PlayerLiveStatisticsDetail detail = playerLiveStatistics.stats;
                    int index = 1;
                    batchInsert.setInt(index++, matchId);
                    batchInsert.setInt(index++, playerLiveStatistics.teamId);
                    batchInsert.setInt(index++, playerLiveStatistics.id);
                    batchInsert.setString(index++, detail.position);
                    batchInsert.setFloat(index++, playerLiveStatistics.rating);
                    batchInsert.setBoolean(index++, playerLiveStatistics.isSub);
                    batchInsert.setBoolean(index++, playerLiveStatistics.isMoM);
                    batchInsert.setInt(index++, detail.man_of_the_match);
                    batchInsert.setInt(index++, detail.formation_place);
                    batchInsert.setInt(index++, detail.total_sub_on);
                    batchInsert.setInt(index++, detail.total_sub_off);
                    batchInsert.setInt(index++, detail.totalPasses);
                    batchInsert.setInt(index++, detail.passAccuracy);
                    batchInsert.setInt(index++, detail.aerialsWon);
                    batchInsert.setInt(index++, detail.touches);
                    batchInsert.setInt(index++, detail.fouls);
                    batchInsert.setInt(index++, detail.shots);
                    batchInsert.setInt(index++, detail.dribblesWon);
                    batchInsert.setInt(index++, detail.tackles);
                    batchInsert.setInt(index++, detail.saves);
                    batchInsert.setInt(index++, detail.assist);
                    batchInsert.setInt(index++, detail.goal_penalty);
                    batchInsert.setInt(index++, detail.goals);
                    batchInsert.setInt(index++, detail.goal_own);
                    batchInsert.setInt(index++, detail.penalty_missed);
                    batchInsert.setInt(index++, detail.minutes_played);
                    batchInsert.setInt(index++, detail.shots_blocked);
                    batchInsert.setInt(index++, detail.was_dribbled);
                    batchInsert.setInt(index++, detail.interceptions);
                    batchInsert.setInt(index++, detail.was_fouled);
                    batchInsert.setInt(index++, detail.offsides);
                    batchInsert.setInt(index++, detail.dispossessed);
                    batchInsert.setInt(index++, detail.turnovers);
                    batchInsert.setInt(index++, detail.crosses);
                    batchInsert.setInt(index++, detail.long_balls);
                    batchInsert.setInt(index++, detail.through_balls);
                    batchInsert.setInt(index++, detail.shotsOnTarget);
                    batchInsert.setInt(index++, detail.yellow);
                    batchInsert.setInt(index++, detail.red);
                    batchInsert.setInt(index++, detail.secondYellow);
                    batchInsert.setInt(index++, detail.penaltySave);
                    batchInsert.setInt(index++, detail.error_lead_to_goal);
                    batchInsert.setInt(index++, detail.last_man_tackle);
                    batchInsert.setInt(index++, detail.clearance_off_line);
                    batchInsert.setInt(index++, detail.hit_woodwork);


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

    public ArrayList<MatchStats> getMatchStatsPreMatch(Match match, int teamId) {

        ArrayList<MatchStats> matchStats = new ArrayList<>();

        try {
            PreparedStatement ps = mConnection.prepareStatement(QUERY_MATCH_PRE_SQL);

            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            ps.setDate(3, match.info.startTimeDate);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                MatchStats stats = new MatchStats();

                stats.id = rs.getInt("id_ws");
                if (rs.getInt("home_id") == teamId) {
                    stats.goal = rs.getInt("home_score");
                    stats.conceded = rs.getInt("away_score");
                } else if (rs.getInt("away_id") == teamId) {
                    stats.goal = rs.getInt("away_score");
                    stats.conceded = rs.getInt("home_score");
                }

                matchStats.add(stats);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return if there is not enough matches.
        if (matchStats.size() < HOME_AWAY_STATS_MATCH_NUMBER) {
            return new ArrayList<MatchStats>();
        }

        try {
            PreparedStatement ps = mConnection.prepareStatement(QUERY_MATCH_STATS_PRE_SQL);
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            ps.setDate(3, match.info.startTimeDate);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                TeamSquad squad = new TeamSquad();
                squad.rating = rs.getFloat("rating");
                squad.man_of_the_match = rs.getInt("man_of_the_match");
                squad.formation_place = rs.getInt("formation_place");
                squad.totalPasses = rs.getInt("totalPasses");
                squad.passAccuracy = rs.getInt("passAccuracy");
                squad.aerialsWon = rs.getInt("aerialsWon");
                squad.touches = rs.getInt("touches");
                squad.fouls = rs.getInt("fouls");
                squad.shots = rs.getInt("shots");
                squad.dribblesWon = rs.getInt("dribblesWon");
                squad.tackles = rs.getInt("tackles");
                squad.saves = rs.getInt("saves");
                squad.assist = rs.getInt("assist");
                squad.goal_penalty = rs.getInt("goal_penalty");
                squad.goals = rs.getInt("goals");
                squad.goal_own = rs.getInt("goal_own");
                squad.penalty_missed = rs.getInt("penalty_missed");
                squad.minutes_played = rs.getInt("minutes_played");
                squad.shots_blocked = rs.getInt("shots_blocked");
                squad.was_dribbled = rs.getInt("was_dribbled");
                squad.interceptions = rs.getInt("interceptions");
                squad.was_fouled = rs.getInt("was_fouled");
                squad.offsides = rs.getInt("offsides");
                squad.dispossessed = rs.getInt("dispossessed");
                squad.turnovers = rs.getInt("turnovers");
                squad.crosses = rs.getInt("crosses");
                squad.long_balls = rs.getInt("long_balls");
                squad.through_balls = rs.getInt("through_balls");
                squad.shotsOnTarget = rs.getInt("shotsOnTarget");
                squad.yellow = rs.getInt("yellow");
                squad.red = rs.getInt("red");
                squad.secondYellow = rs.getInt("secondYellow");
                squad.penaltySave = rs.getInt("penaltySave");
                squad.error_lead_to_goal = rs.getInt("error_lead_to_goal");
                squad.last_man_tackle = rs.getInt("last_man_tackle");
                squad.clearance_off_line = rs.getInt("clearance_off_line");
                squad.hit_woodwork = rs.getInt("hit_woodwork");

                int matchId = rs.getInt("match_id");
                int thisTeamId = rs.getInt("team_id");

                for (MatchStats stats: matchStats) {
                    if (stats.id == matchId) {
                        if (thisTeamId == teamId) {
                            stats.mySquad.add(squad);
                        } else {
                            stats.againstSquad.add(squad);
                        }
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return matchStats;
    }


    public ArrayList<MatchStats> getLastMeetingsStats(Match match) {
        ArrayList<MatchStats> matchStats = new ArrayList<>();

        try {
            PreparedStatement ps = mConnection.prepareStatement(QUERY_LAST_MEETINGS_SQL);

            ps.setInt(1, match.info.homeId);
            ps.setInt(2, match.info.awayId);
            ps.setInt(3, match.info.awayId);
            ps.setInt(4, match.info.homeId);
            ps.setDate(5, match.info.startTimeDate);

            ResultSet rs = ps.executeQuery();

            int index = 0;
            while(rs.next()) {
                MatchStats stats = new MatchStats();

                stats.id = rs.getInt("id_ws");
                if (rs.getInt("home_id") == match.info.homeId) {
                    stats.goal = rs.getInt("home_score");
                    stats.conceded = rs.getInt("away_score");
                } else if (rs.getInt("away_id") == match.info.homeId) {
                    stats.goal = rs.getInt("away_score");
                    stats.conceded = rs.getInt("home_score");
                }

                matchStats.add(stats);
                index ++;
                if (index >= LAST_MEETINGS_MATCH_NUMBER) {
                    break;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return if there is not enough matches.
        if (matchStats.size() < LAST_MEETINGS_MATCH_NUMBER) {
            return new ArrayList<MatchStats>();
        }

        try {
            PreparedStatement ps = mConnection.prepareStatement(QUERY_LAST_MEETING_STATS_SQL);
            ps.setInt(1, match.info.homeId);
            ps.setInt(2, match.info.awayId);
            ps.setInt(3, match.info.awayId);
            ps.setInt(4, match.info.homeId);
            ps.setDate(5, match.info.startTimeDate);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                TeamSquad squad = new TeamSquad();
                squad.rating = rs.getFloat("rating");
                squad.man_of_the_match = rs.getInt("man_of_the_match");
                squad.formation_place = rs.getInt("formation_place");
                squad.totalPasses = rs.getInt("totalPasses");
                squad.passAccuracy = rs.getInt("passAccuracy");
                squad.aerialsWon = rs.getInt("aerialsWon");
                squad.touches = rs.getInt("touches");
                squad.fouls = rs.getInt("fouls");
                squad.shots = rs.getInt("shots");
                squad.dribblesWon = rs.getInt("dribblesWon");
                squad.tackles = rs.getInt("tackles");
                squad.saves = rs.getInt("saves");
                squad.assist = rs.getInt("assist");
                squad.goal_penalty = rs.getInt("goal_penalty");
                squad.goals = rs.getInt("goals");
                squad.goal_own = rs.getInt("goal_own");
                squad.penalty_missed = rs.getInt("penalty_missed");
                squad.minutes_played = rs.getInt("minutes_played");
                squad.shots_blocked = rs.getInt("shots_blocked");
                squad.was_dribbled = rs.getInt("was_dribbled");
                squad.interceptions = rs.getInt("interceptions");
                squad.was_fouled = rs.getInt("was_fouled");
                squad.offsides = rs.getInt("offsides");
                squad.dispossessed = rs.getInt("dispossessed");
                squad.turnovers = rs.getInt("turnovers");
                squad.crosses = rs.getInt("crosses");
                squad.long_balls = rs.getInt("long_balls");
                squad.through_balls = rs.getInt("through_balls");
                squad.shotsOnTarget = rs.getInt("shotsOnTarget");
                squad.yellow = rs.getInt("yellow");
                squad.red = rs.getInt("red");
                squad.secondYellow = rs.getInt("secondYellow");
                squad.penaltySave = rs.getInt("penaltySave");
                squad.error_lead_to_goal = rs.getInt("error_lead_to_goal");
                squad.last_man_tackle = rs.getInt("last_man_tackle");
                squad.clearance_off_line = rs.getInt("clearance_off_line");
                squad.hit_woodwork = rs.getInt("hit_woodwork");

                int matchId = rs.getInt("match_id");
                int thisTeamId = rs.getInt("team_id");

                for (MatchStats stats: matchStats) {
                    if (stats.id == matchId) {
                        if (thisTeamId == match.info.homeId) {
                            stats.mySquad.add(squad);
                        } else {
                            stats.againstSquad.add(squad);
                        }
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return matchStats;
    }

    public ArrayList<PlayerRating> getPastPlayerRating(Match match, int teamId) {
        ArrayList<PlayerRating> pastRatings = new ArrayList<>();

        try {
            PreparedStatement ps = mConnection.prepareStatement(QUERY_PLAYER_AVG_RATING_SQL);

            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            ps.setInt(3, teamId);
            ps.setDate(4, match.info.startTimeDate);
            ps.setInt(5, match.id);
            ps.setInt(6, teamId);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                PlayerRating stats = new PlayerRating();

                stats.id = rs.getInt(1);
                stats.appearance = rs.getInt(2);
                stats.avgRating = rs.getFloat(3);

                pastRatings.add(stats);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pastRatings;
    }

    public void updatePlayerStats(int matchId, ArrayList<PlayerLiveStatistics> stats) {

        try {
            PreparedStatement batchUpdate = mConnection.prepareStatement(UPDATE_PLAYER_STATS_SQL);
            for (PlayerLiveStatistics playerLiveStatistics: stats) {
                PlayerLiveStatisticsDetail detail = playerLiveStatistics.stats;

                int index = 1;
                batchUpdate.setInt(index++, detail.man_of_the_match);
                batchUpdate.setInt(index++, detail.formation_place);
                batchUpdate.setInt(index++, detail.total_sub_on);
                batchUpdate.setInt(index++, detail.total_sub_off);
                batchUpdate.setInt(index++, detail.totalPasses);
                batchUpdate.setInt(index++, detail.passAccuracy);
                batchUpdate.setInt(index++, detail.aerialsWon);
                batchUpdate.setInt(index++, detail.touches);
                batchUpdate.setInt(index++, detail.fouls);
                batchUpdate.setInt(index++, detail.shots);
                batchUpdate.setInt(index++, detail.dribblesWon);
                batchUpdate.setInt(index++, detail.tackles);
                batchUpdate.setInt(index++, detail.saves);
                batchUpdate.setInt(index++, detail.assist);
                batchUpdate.setInt(index++, detail.goal_penalty);
                batchUpdate.setInt(index++, detail.goals);
                batchUpdate.setInt(index++, detail.goal_own);
                batchUpdate.setInt(index++, detail.penalty_missed);
                batchUpdate.setInt(index++, detail.minutes_played);
                batchUpdate.setInt(index++, detail.shots_blocked);
                batchUpdate.setInt(index++, detail.was_dribbled);
                batchUpdate.setInt(index++, detail.interceptions);
                batchUpdate.setInt(index++, detail.was_fouled);
                batchUpdate.setInt(index++, detail.offsides);
                batchUpdate.setInt(index++, detail.dispossessed);
                batchUpdate.setInt(index++, detail.turnovers);
                batchUpdate.setInt(index++, detail.crosses);
                batchUpdate.setInt(index++, detail.long_balls);
                batchUpdate.setInt(index++, detail.through_balls);
                batchUpdate.setInt(index++, detail.shotsOnTarget);
                batchUpdate.setInt(index++, detail.yellow);
                batchUpdate.setInt(index++, detail.red);
                batchUpdate.setInt(index++, detail.secondYellow);
                batchUpdate.setInt(index++, detail.penaltySave);
                batchUpdate.setInt(index++, detail.error_lead_to_goal);
                batchUpdate.setInt(index++, detail.last_man_tackle);
                batchUpdate.setInt(index++, detail.clearance_off_line);
                batchUpdate.setInt(index++, detail.hit_woodwork);


                batchUpdate.setInt(39, matchId);
                batchUpdate.setInt(40, playerLiveStatistics.id);
                batchUpdate.addBatch();
            }

            batchUpdate.executeBatch();
            batchUpdate.close();
            mConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getTeamIdWithName(String name) {
        try {
            PreparedStatement ps = mConnection.prepareStatement("select id, Name from team where Name = ?");

            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public String getTranslationWithTeamId(int id) {
        try {
            PreparedStatement ps = mConnection.prepareStatement("select zh from translation where ObjectType = 'Team' and ObjectId = ?");

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                return rs.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setTranslationWithTeamId(int id, String translatedString) {
        try {
            PreparedStatement batchInsert = mConnection.prepareStatement("insert into translation (ObjectType, ObjectId, zh) values (?, ?, ?)");

            batchInsert.setString(1,"Team");
            batchInsert.setInt(2, id);
            batchInsert.setString(3, translatedString);
            batchInsert.addBatch();

            batchInsert.executeBatch();
            batchInsert.close();
            mConnection.commit();
        } catch (SQLException e) {

        }

    }


    public void close() {
        try {
            mConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, MatchOdds> getMatchOdds(int amount) {
        Map<Integer, MatchOdds> matchOdds = new HashMap<>();

        String statement = "select TOP " + amount + " * from games g\n" +
                "join leagues l on g.League_Id = l.id\n" +
                "where ScoreString is not null and l.Sport_Id = 1\n" +
                "order by StartTime DESC";

        try {
            PreparedStatement ps = mConnection.prepareStatement(statement);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                MatchOdds odds = new MatchOdds();

                odds.id = rs.getInt("id");
                odds.goal = rs.getInt("homeGoals");
                odds.conceded = rs.getInt("awayGoals");
                odds.home_id = rs.getInt("HomeTeam_Id");
                odds.away_id = rs.getInt("AwayTeam_Id");
                odds.time = rs.getTimestamp("StartTime");

                matchOdds.put(odds.id, odds);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        statement = "select * from FHOdds f\n" +
                "join markets m on f.Market_Id = m.id\n" +
                "where m.Game_Id in (\n" +
                "\tselect TOP " + amount + " g.id from games g\n" +
                "\tjoin leagues l on g.League_Id = l.id\n" +
                "\twhere ScoreString is not null and l.Sport_Id = 1\n" +
                "\torder by StartTime DESC\n" +
                ") and Outcome_Id in (1, 2, 3)";
        try {


            PreparedStatement ps = mConnection.prepareStatement(statement);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                Odd odd = new Odd();
                odd.outcome = rs.getInt("Outcome_Id");
                odd.odds = rs.getFloat("Odd");
                odd.time = rs.getTimestamp("FetchedWhen");

                int matchId = rs.getInt("Game_Id");
                if (matchOdds.containsKey(matchId)) {
                    matchOdds.get(matchId).addOdds(odd.outcome, odd);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        statement = "SELECT top 5 * FROM games " +
                "WHERE ((HomeTeam_id =? and AwayTeam_Id =?) or (HomeTeam_id =? and AwayTeam_Id =?)) " +
                "and startTime <? " +
                "ORDER by startTime desc";

        int index = 0;
        Iterator<MatchOdds> it = matchOdds.values().iterator();
        while (it.hasNext()) {
            MatchOdds odds = it.next();
            try {
                PreparedStatement ps = mConnection.prepareStatement(statement);
                ps.setInt(1, odds.home_id);
                ps.setInt(2, odds.away_id);
                ps.setInt(3, odds.away_id);
                ps.setInt(4, odds.home_id);
                ps.setTimestamp(5, odds.time);

                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    LastMeeting last = new LastMeeting();
                    last.time = rs.getTimestamp("StartTime");
                    last.goal = rs.getInt("homeGoals");
                    last.conceded = rs.getInt("awayGoals");
                    last.home_id = rs.getInt("HomeTeam_Id");
                    last.away_id = rs.getInt("AwayTeam_Id");

                    odds.addLastMeeting(last);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            index ++;
            System.out.println(index);
        }

        return matchOdds;
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
