package De.Hpi.Scotty.source.core;

import De.Hpi.Scotty.source.core.windowFunction.AggregateFunction;
import De.Hpi.Scotty.source.core.windowType.Window;

import java.io.Serializable;
import java.util.List;

public interface WindowOperator<InputType> extends Serializable {

    /**
     * Process a new element of the stream
     */
    void processElement(InputType element, long ts);

    /**
     * Process a watermark at a specific timestamp
     */
    List<AggregateWindow> processWatermark(long watermarkTs);

    /**
     * Add a window assigner to the window operator.
     */
    void addWindowAssigner(Window window);

    /**
     * Add a aggregation
     * @param windowFunction
     */
    <OutputType> void addAggregation(AggregateFunction<InputType, ?, OutputType> windowFunction);

    /**
     * Set the max lateness for the window operator.
     * LastWatermark - maxLateness is the point in time where slices get garbage collected and no further late elements are processed.
     * @param maxLateness
     */
    void setMaxLateness(long maxLateness);


}
