import com.google.gson.Gson;
import ws.*;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by zhengyu on 10/07/2017.
 */
public class WSDataParser {

    private FilePersistConnection mFileConnection;
    private SQLServerPersistConnection mDBConnection;

    public WSDataParser() {
        mFileConnection = new FilePersistConnection();
        mDBConnection = new SQLServerPersistConnection("fhvm-dev.eastasia.cloudapp.azure.com", "footballhero_sa", "password99__**01", "whoscored");
    }

    public void parserMatch(int id) {
        if (! mFileConnection.isMatchExist(id)) {
            return;
        }

        Gson gson = new Gson();

        String responseString = mFileConnection.getMatch(id);

        Match match = gson.fromJson(responseString, Match.class);
        mDBConnection.persistMatch(id, match);

        parserTeam(match.info.homeId, match.info.awayId);

        parserPlayers(match);
        parserMatchPlayerStats(match);
    }

    public void parserTeam(int homeId, int awayId) {
        if (! mFileConnection.isTeamExist(homeId) || ! mFileConnection.isTeamExist(awayId)) {
            return ;
        }

        Gson gson = new Gson();

        String homeString = mFileConnection.getTeam(homeId);
        String awayString = mFileConnection.getTeam(awayId);

        Team homeTeam = gson.fromJson(homeString, Team.class);
        Team awayTeam = gson.fromJson(awayString, Team.class);

        ArrayList<Team> teams = new ArrayList<>();
        teams.add(homeTeam);
        teams.add(awayTeam);

        mDBConnection.persistTeam(teams);
    }

    public void parserPlayers(Match match) {
        ArrayList<Player> players = new ArrayList<>();
        Gson gson = new Gson();


        if (match.liveMatch != null &&
                match.liveMatch.liveStatistics != null) {
            TeamLiveStatistics homeTeamLiveStats = match.liveMatch.liveStatistics.home;
            if (homeTeamLiveStats != null && homeTeamLiveStats.players != null) {

                for (int i = 0; i < homeTeamLiveStats.players.length; i ++) {
                    int playerId = homeTeamLiveStats.players[i].id;
                    if (mFileConnection.isPlayerExist(playerId)) {
                        String responseString = mFileConnection.getPlayer(playerId);

                        Player player = gson.fromJson(responseString, Player.class);
                        players.add(player);
                    }
                }
            }

            TeamLiveStatistics awayTeamLiveStats = match.liveMatch.liveStatistics.away;
            if (awayTeamLiveStats != null && awayTeamLiveStats.players != null) {

                for (int i = 0; i < awayTeamLiveStats.players.length; i ++) {
                    int playerId = awayTeamLiveStats.players[i].id;
                    if (mFileConnection.isPlayerExist(playerId)) {
                        String responseString = mFileConnection.getPlayer(playerId);

                        Player player = gson.fromJson(responseString, Player.class);
                        players.add(player);
                    }
                }
            }
        }

        mDBConnection.persistPlayer(players);
    }

    public void parserMatchPlayerStats(Match match) {
        ArrayList<PlayerLiveStatistics> stats = new ArrayList<>();

        if (match.liveMatch != null &&
                match.liveMatch.liveStatistics != null) {
            TeamLiveStatistics homeTeamLiveStats = match.liveMatch.liveStatistics.home;
            if (homeTeamLiveStats != null && homeTeamLiveStats.players != null) {

                Collections.addAll(stats, homeTeamLiveStats.players);
            }

            TeamLiveStatistics awayTeamLiveStats = match.liveMatch.liveStatistics.away;
            if (awayTeamLiveStats != null && awayTeamLiveStats.players != null) {

                Collections.addAll(stats, awayTeamLiveStats.players);
            }
        }

        mDBConnection.persistMatchPlayerStats(match.id, stats);
    }

    public void close() {
        mDBConnection.close();
    }

}
