package io.coti.basenode.data;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class NetworkFeeData extends OutputBaseTransactionData {
    @NotNull
    private List<TrustScoreNodeResultData> networkFeeTrustScoreNodeResult;

    private NetworkFeeData() {
        super();
    }

    public NetworkFeeData(Hash addressHash, BigDecimal amount, BigDecimal originalAmount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        super(addressHash, amount, originalAmount, baseTransactionHash, signature, createTime);
    }
}
