package De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.Message;


import De.Hpi.DesisMultipleKeys.LocalNode.BaselineNoOptimizer.Desis.Dao.WindowCollection;
import org.msgpack.annotation.Message;

@Message
public class MessageResult {

    private int nodeId;
    private int messageType;
    private int messageLevel;
    public WindowCollection windowCollection;


    public int getNodeId() {
        return nodeId;
    }
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
    public int getMessageType() {
        return messageType;
    }
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
    public int getMessageLevel() {
        return messageLevel;
    }
    public void setMessageLevel(int messageLevel) {
        this.messageLevel = messageLevel;
    }

}
