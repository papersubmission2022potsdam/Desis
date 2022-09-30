package De.Hpi.DesisIC.MessageManager;

import De.Hpi.DesisIC.Dao.Query;
import De.Hpi.DesisIC.Configure.Configuration;
import De.Hpi.DesisIC.Message.MessageQuery;
import org.msgpack.MessagePack;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RootPublishMessage implements Runnable {

    private Configuration conf;
    private ConcurrentLinkedQueue<Query> queryQueue;
    private ZMQ.Socket socketPub;
    public RootPublishMessage(Configuration conf, ConcurrentLinkedQueue<Query> queryQueue, ZMQ.Socket socketPub) {
        this.conf = conf;
        this.queryQueue =queryQueue;
        this.socketPub = socketPub;
    }

    public void run() {
//        System.out.println("Starting FromRNodeToINode ----rootNode");
        int queryCounter = 0;
        MessagePack msgpack = new MessagePack();
        while (true) {
            if(!queryQueue.isEmpty()) {
                Query query = (Query) queryQueue.poll();
                MessageQuery messageQuery = new MessageQuery();
                messageQuery.setNodeId(conf.getNodeId());
                messageQuery.setMessageType(conf.MESSAGERESULT);
                messageQuery.setMessageLevel(conf.LOCALNODEMESSAGE);
                messageQuery.query = query;
                    try {
                        byte[] raw = msgpack.write(messageQuery);
                        socketPub.send(raw);
                        if(conf.DEBUGMODE_ROOT) {
                            System.out.println("rootNode--" + conf.getNodeId() + "--sending query----" + query.getQueryId());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                queryCounter++;
                //end thread
//                if(queryCounter >= conf.queryNumber)
//                    break;
            }
        }
    }
}

