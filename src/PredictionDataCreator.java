import com.google.gson.Gson;
import sh.MatchForPrediction;
import utils.FilePersistConnection;
import utils.SQLServerPersistConnection;
import ws.Fixture;
import ws.Match;
import ws.Stage;

import java.util.ArrayList;

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
        MatchForPrediction matchForPrediction = new MatchForPrediction();
        matchForPrediction.homeId = match.info.homeId;
        matchForPrediction.awayId = match.info.awayId;
        matchForPrediction.homeName = match.info.homeName;
        matchForPrediction.awayName = match.info.awayName;
        matchForPrediction.homeScore = match.info.homeScore;
        matchForPrediction.awayScore = match.info.awayScore;

        Match matchFromDB = mDBConnection.getMatch(id);
        matchForPrediction.homePlayerRating = mDBConnection.getPastPlayerRating(matchFromDB, matchFromDB.info.homeId);
        matchForPrediction.awayPlayerRating = mDBConnection.getPastPlayerRating(matchFromDB, matchFromDB.info.awayId);

        if (matchForPrediction.isValid()) {
            System.out.println("Create match for prediction: " + id);
            mFileConnection.persistPredictionMatch(fileName, matchForPrediction.toString());

            return true;
        } else {
            return false;
        }
    }

    public int createPredictionDataByStage(String fileName) {
        ArrayList<String> stageStrings = mFileConnection.getAllStages();
        Gson gson = new Gson();

        int totalNumber = 0;
        for (String stageString : stageStrings) {
            Stage stage = gson.fromJson(stageString, Stage.class);
            for (Fixture fixture : stage.fixtures) {
                if (createPredictionData(fixture.id, fileName)){
                    totalNumber ++;

                    // TODO to remove.
                    if (totalNumber >= 1000) {
                        return totalNumber;
                    }
                }
            }
        }

        return totalNumber;
    }



    public void separateSampleTest(String dest, String target1, String target2, float target1Percentage, int itemNumber) {
        mFileConnection.separateToFiles(dest, target1, target2, target1Percentage, itemNumber);
    }

}
