package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class FundDistributionData implements IEntity{

    private Hash hash;
    private Hash hashByDate;
    private String fileName; // File name according to which this entry was created
    private DistributionEntryStatus status;

    private long id;
    private Hash receiverAddress;
    private Fund distributionPoolFund; // Expected range from ReservedAddress.isSecondaryFundDistribution()
    private BigDecimal amount;
    private Instant createTime;
    private Instant transactionTime;
    private String source; // "Source" secondary key

    public FundDistributionData(long id, Hash receiverAddress, Fund distributionPoolFund, BigDecimal amount, Instant createTime,
                                Instant transactionTime, String source) {
        this.id = id;
        this.receiverAddress = receiverAddress;
        this.distributionPoolFund = distributionPoolFund;
        this.amount = amount;
        this.createTime = createTime;
        this.transactionTime = transactionTime;
        this.source = source;
        initHashes();
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    public void initHashes() {
        byte[] concatDataFields = ArrayUtils.addAll((distributionPoolFund.getText()+ source).getBytes(),receiverAddress.getBytes());
        this.hash = CryptoHelper.cryptoHash(concatDataFields);

        this.status = DistributionEntryStatus.ONHOLD;

        LocalDateTime ldt = LocalDateTime.ofInstant(transactionTime, ZoneOffset.UTC);
        this.hashByDate = CryptoHelper.cryptoHash( (ldt.getYear() + ldt.getMonth().toString() +
                ldt.getDayOfMonth()).getBytes() );
    }

    public boolean isReadyToInitiate() {
        return this.status == DistributionEntryStatus.ACCEPTED || this.status == DistributionEntryStatus.ONHOLD;
    }

    public boolean isLockingAmount() {
        return this.status == DistributionEntryStatus.ONHOLD || this.status == DistributionEntryStatus.ACCEPTED ||
                this.status == DistributionEntryStatus.FAILED;
    }

}