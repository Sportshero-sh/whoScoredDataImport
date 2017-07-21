import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Created by zhengyu on 21/07/2017.
 */
public class WSMultiFetcher {

    private ExecutorService mExec;
    private ArrayList<Integer> mToFetchList;

    public WSMultiFetcher(int startAt, int length, int batchSize) {

        mToFetchList = new ArrayList<>();

        for (int i = startAt; i > startAt - length  ; i --) {
            mToFetchList.add(i);
        }

        mExec = Executors.newCachedThreadPool();

        fetchNextBatch(batchSize);
        mExec.shutdown();
    }

    private void fetchNextBatch(int batchSize) {
        if (mToFetchList.size() == 0) {
            return ;
        }
        ArrayList<Future<String>> results = new ArrayList<Future<String>>();

        for (int i = 0; i < batchSize && mToFetchList.size() > 0; i ++) {
            int id = mToFetchList.remove(0);

            WSFetcher fetcher = new WSFetcher();
            results.add(mExec.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    fetcher.fetchMatch(id);
                    return null;
                }
            }));
        }

        for (Future<String> fs: results) {
            try{
                fs.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        fetchNextBatch(batchSize);
    }
}
