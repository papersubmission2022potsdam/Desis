package De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.IntermediaNode;

import De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.Configure.Configuration;
import De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.Dao.Query;
import De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.Dao.WindowCollection;
import De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.MessageManager.IntermediatePublishMessage;
import De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.MessageManager.IntermediateSubscribeAndPublishMessage;
import De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.MessageManager.IntermediateSubscribeMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IntermediaNode {

    private Configuration conf;
    private ConcurrentLinkedQueue<Query> queryQueue;
    private ConcurrentLinkedQueue<WindowCollection> resultQueue;
    private ConcurrentLinkedQueue<WindowCollection> resultQueueFromLocal;

    private ZContext context;
    private ZMQ.Socket socketUpperPub;
    private ZMQ.Socket socketRootPub;
    private ZMQ.Socket socketLowerPub;
    private ZMQ.Socket socketLocalPub;
    private IntermediateParseAddress intermediateParseAddress;

    private ArrayList<Thread> threadsList;


    public IntermediaNode(Configuration conf, int nodeId ){
        this.conf = conf;
        this.threadsList = new ArrayList<>();
        this.conf.setNodeId(nodeId);
        this.queryQueue = new ConcurrentLinkedQueue<Query>();
        this.resultQueue = new ConcurrentLinkedQueue<WindowCollection>();
        this.resultQueueFromLocal = new ConcurrentLinkedQueue<WindowCollection>();

        this.context = new ZContext();
        this.intermediateParseAddress = new IntermediateParseAddress();


        //to test intermediate node
//        conf.localNumber = conf.GeneratorThreadNumber;

        initialIntermediaNode();
        startIntermediaNode();
    }

    public void initialIntermediaNode(){
        //initialize the publish-subscribe mode
        //from root to intermedia
        socketRootPub = context.createSocket(SocketType.SUB);
        socketRootPub.connect(intermediateParseAddress.getRootPubAddress(conf));

        //from intermedia to local
        socketLowerPub = context.createSocket(SocketType.PUB);
        socketLowerPub.bind(intermediateParseAddress.getInterLowerPubAddress(conf, conf.getNodeId()));

        //from intermedia to root
        socketUpperPub = context.createSocket(SocketType.PUB);
        socketUpperPub.bind(intermediateParseAddress.getInterUpperPubAddress(conf, conf.getNodeId()));


        //from local to intermedia
        socketLocalPub = context.createSocket(SocketType.SUB);
        intermediateParseAddress.getLocalSubAddressAll(conf, conf.getNodeId()).forEach(addr -> socketLocalPub.connect(addr));

        //initialize thread and transfer the query or message from the root node to local node
        threadsList.add(new Thread(new IntermediateSubscribeAndPublishMessage(socketRootPub,socketLowerPub,queryQueue, conf)));
        //receive the data or message from the local node
        threadsList.add(new Thread(new IntermediateSubscribeMessage(resultQueueFromLocal, conf, socketLocalPub)));
        //perform decentralized aggregation in intermediate node
        threadsList.add(new Thread(new IntermediateComputationEngine(resultQueue, resultQueueFromLocal, queryQueue ,conf)));
//        //send data from intermedia to root
        threadsList.add(new Thread(new IntermediatePublishMessage(resultQueue, socketUpperPub, conf)));

        //simulate message from local node
//        threadsList.add(new Thread(new DataGeneratorForInter(conf, resultQueueFromLocal)));

    }

    public void startIntermediaNode(){
        threadsList.forEach( thread -> thread.start());
    }

    public void stopIntermediaNode(){
        threadsList.forEach( thread -> thread.interrupt());
    }

}
