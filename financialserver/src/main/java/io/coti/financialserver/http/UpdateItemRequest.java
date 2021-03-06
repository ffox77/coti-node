package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.DisputeUpdateItemData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class UpdateItemRequest extends Request {

    @NotNull
    private @Valid DisputeUpdateItemData disputeUpdateItemData;
}
