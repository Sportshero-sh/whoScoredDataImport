import sh.MatchOdds;
import utils.FilePersistConnection;
import utils.SQLServerPersistConnection;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by zhengyu on 2018/4/16.
 */
public class SHPredictionDataCreator {

    private FilePersistConnection mFileConnection;
    private SQLServerPersistConnection mDBConnection;

    public SHPredictionDataCreator() {
        mFileConnection = new FilePersistConnection();
        mDBConnection = new SQLServerPersistConnection("fhvm-dev.eastasia.cloudapp.azure.com", "footballhero_sa", "password99__**01", "FootballHero_Dev");
    }

    public int createPredictionData(String fileName, int amount) {

        if (mFileConnection.deleteFile(fileName)) {
            Map<Integer, MatchOdds> matchOddsMap =  mDBConnection.getMatchOdds(amount);

            int index = 0;
            Iterator<MatchOdds> it = matchOddsMap.values().iterator();
            while (it.hasNext()) {
                MatchOdds matchOdds = it.next();

                if (matchOdds.isValid()) {
                    mFileConnection.persistPredictionMatch(fileName, matchOdds.toString());
                    index ++;
                }
            }


            return index;
        } else {
            System.out.println("Old file cannot be deleted.");
            return 0;
        }
    }

    public void separateSampleTest(String dest, String target1, String target2, float target1Percentage, int columnNum) {
        mFileConnection.separateToFiles(dest, target1, target2, target1Percentage, columnNum);
    }
}
