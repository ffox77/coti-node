package io.coti.financialserver.http;

import lombok.Data;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.http.data.GetDisputeResponseData;

@Data
public class GetDisputeResponse extends BaseResponse {

    private GetDisputeResponseData disputeData;

    public GetDisputeResponse(DisputeData disputeData) {
        super();
        this.disputeData = new GetDisputeResponseData(disputeData);
    }
}