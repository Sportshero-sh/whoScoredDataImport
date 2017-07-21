import utils.ResponseException;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by zhengyu on 21/07/2017.
 */
public class WSMultiFetcher {

    private static final int MAX_RETRY_TIMES = 10;
    private ExecutorService mExec;
    private List<Integer> mToFetchList;
    private Map<Integer, Integer> mRetryMap = new HashMap<>();

    public WSMultiFetcher(int startAt, int length, int batchSize) {

        mToFetchList = Collections.synchronizedList(new ArrayList<>());

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
        ArrayList<Future<CallResult>> results = new ArrayList<Future<CallResult>>();

        for (int i = 0; i < batchSize && mToFetchList.size() > 0; i ++) {
            int id = mToFetchList.remove(0);

            WSFetcher fetcher = new WSFetcher();
            results.add(mExec.submit(new Callable<CallResult>() {
                @Override
                public CallResult call() throws Exception {
                    try {
                        fetcher.fetchMatch(id, false);
                    } catch (ResponseException e) {
                        return new CallResult(false, fetcher.getMatchId());
                    }
                    return new CallResult();
                }
            }));
        }

        for (Future<CallResult> fs: results) {
            try{
                CallResult result = fs.get();
                if (!result.success) {
                    if (!mRetryMap.containsKey(result.id)) {
                        mRetryMap.put(result.id, 0);
                    }

                    if (mRetryMap.get(result.id) < MAX_RETRY_TIMES) {
                        mToFetchList.add(result.id);
                        mRetryMap.put(result.id, mRetryMap.get(result.id) + 1);
                    } else {
                        System.out.println("Reach max retry times for match: " + result.id);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        fetchNextBatch(batchSize);
    }
}

class CallResult {
    boolean success;
    int id;

    public CallResult() {
        this(true, 0);
    }

    public CallResult(boolean success, int id) {
        this.success = success;
        this.id = id;
    }
}
