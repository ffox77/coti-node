package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.data.NodeActivityData;
import lombok.Data;

@Data
public class GetNodeActivityInSecondsResponse extends BaseResponse {

    private long activityUpTimeInSeconds;

    public GetNodeActivityInSecondsResponse(NodeActivityData nodeActivityData) {
        this.activityUpTimeInSeconds = nodeActivityData.getActivityUpTimeInSeconds();
    }
}
