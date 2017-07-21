import com.google.gson.Gson;
import sh.MatchForPrediction;
import utils.FilePersistConnection;
import utils.SQLServerPersistConnection;
import ws.Match;

/**
 * Created by zhengyu on 11/07/2017.
 */
public class PredictionDataCreator {

    private FilePersistConnection mFileConnection;
    private SQLServerPersistConnection mDBConnection;

    public PredictionDataCreator() {
        mFileConnection = new FilePersistConnection();
        mDBConnection = new SQLServerPersistConnection("fhvm-dev.eastasia.cloudapp.azure.com", "footballhero_sa", "password99__**01", "whoscored");
    }

    public boolean createPredictionData(int id, String fileName) {
        if (! mFileConnection.isMatchExist(id)) {
            return false;
        }

        Gson gson = new Gson();

        String responseString = mFileConnection.getMatch(id);

        Match match = gson.fromJson(responseString, Match.class);

        if (match.liveMatch == null || match.liveMatch.liveStatistics == null
                || match.liveMatch.liveStatistics.home == null || match.liveMatch.liveStatistics.away == null)
        {
            System.out.println("MatchForPrediction has no rating for player: " + id);
            return false;
        }

        // Create the match prediction data.
        System.out.println("Create match for prediction: " + id);
        MatchForPrediction matchForPrediction = new MatchForPrediction();
        matchForPrediction.homeId = match.info.homeId;
        matchForPrediction.awayId = match.info.awayId;
        matchForPrediction.homeName = match.info.homeName;
        matchForPrediction.awayName = match.info.awayName;
        matchForPrediction.homeScore = match.info.homeScore;
        matchForPrediction.awayScore = match.info.awayScore;

        Match matchFromDB = mDBConnection.getMatch(id);
        matchForPrediction.homeSquad = mDBConnection.getPlayerStatsPreMatch(matchFromDB, matchFromDB.info.homeId);
        matchForPrediction.awaySquad = mDBConnection.getPlayerStatsPreMatch(matchFromDB, matchFromDB.info.awayId);

        if (matchForPrediction.homeSquad.length > 0 && matchForPrediction.awaySquad.length > 0) {

            mFileConnection.persistPredictionMatch(fileName, matchForPrediction.toString());

            return true;
        } else {
            return false;
        }
    }
}
