package De.Hpi.DesisSW.LocalNode;

import De.Hpi.DesisSW.Configure.Configuration;
import De.Hpi.DesisSW.Dao.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalComputationEngine implements Runnable{

    private ArrayList<Optimizer> optimizers;
    private Configuration conf;
    private ConcurrentLinkedQueue<WindowCollection> intermediateResultQueue;
    private ConcurrentLinkedQueue<Query> queryQueue;
    private ConcurrentLinkedQueue<ArrayList<Tuple>> dataQueue;

    public LocalComputationEngine(Configuration conf, ConcurrentLinkedQueue<WindowCollection> intermediateResultQueue,
                                  ConcurrentLinkedQueue<Query> queryQueue, ConcurrentLinkedQueue<ArrayList<Tuple>> dataQueue){

        this.conf = conf;
        this.intermediateResultQueue =intermediateResultQueue;
        this.queryQueue =queryQueue;
        this.dataQueue =dataQueue;
        this.optimizers =  new ArrayList<>();
    }


    public void run() {
        //to read all queries
        queryPreProcess();
        //to increase time granularity
        long previousTimeCounter = System.currentTimeMillis();
        long previousCountCounter = 0;
        optimizers.forEach(optimizer -> optimizer.previousTimeCounter = previousTimeCounter);
//        optimizers.forEach(optimizer -> optimizer.previousCountCounter = previousCountCounter);


        System.out.println(conf.queryNumber);
        optimizers.forEach(optimizer -> {
            System.out.println("Optimizer:   " + optimizer.optimizerId);
            ArrayList<LocalTask> localTasks = optimizer.localTasks;
            System.out.println("LocalTask: " + localTasks.size());
            System.out.println("QuerySub: ");
            localTasks.forEach(localTask -> System.out.print(localTask.querySubs.size() + " "));
            System.out.println();
        });

        while(true) {
            if (!dataQueue.isEmpty()){
                ArrayList<Tuple> dataBuffer = dataQueue.poll();
                dataBuffer.forEach(tuple -> {
                    //process tuples
                    optimizers.forEach(optimizer -> optimizer.worker(tuple));
                });
            }
        }


    }

    public void queryPreProcess(){
        int queryCounter = 0;
        while(queryCounter < conf.queryNumber){
            if(!queryQueue.isEmpty()) {
                Query query = (Query) queryQueue.poll();
                queryCounter++;
                Optimizer optimizerTemp = optimizers.stream()
                        .filter(optimizer -> {
                            if(optimizer.scenario == query.getScenario())
                                return optimizer.function == query.getFunction();
                            else
                                return optimizer.scenario == query.getScenario();
                        })
                        .findFirst()
                        .orElse(null);
                if(optimizerTemp != null){
                    optimizerTemp.queryPreProcess(query);
                    continue;
                }
                Optimizer optimizer = new Optimizer(conf, intermediateResultQueue, query.getFunction(), query.getScenario());
                optimizer.optimizerId = optimizers.size() + 1;
                optimizer.queryPreProcess(query);
                optimizers.add(optimizer);
            }else{
                try {
                    Thread.sleep(conf.queryWait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
