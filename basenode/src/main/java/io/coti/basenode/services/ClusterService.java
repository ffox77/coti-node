package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClusterService implements IClusterService {
    private List<Set<TransactionData>> sourceListsByTrustScore = new ArrayList<>();

    @Autowired
    private Transactions transactions;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private ISourceSelector sourceSelector;
    @Autowired
    private TrustChainConfirmationService trustChainConfirmationService;
    @Autowired
    private LiveViewService liveViewService;
    private boolean isStarted;
    private ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster;
    private AtomicLong totalSources = new AtomicLong(0);

    @PostConstruct
    public void init() {
        trustChainConfirmationCluster = new ConcurrentHashMap<>();
        for (int i = 0; i <= 100; i++) {
            sourceListsByTrustScore.add(Sets.newConcurrentHashSet());
        }
    }

    @Override
    public void addExistingTransactionOnInit(TransactionData transactionData) {
        if (!transactionData.isTrustChainConsensus()) {
            addTransactionToTccConfirmationCluster(transactionData);
        }
    }

    @Override
    public void addTransactionOnInit(TransactionData transactionData) {
        updateParents(transactionData);
        if (!transactionData.isTrustChainConsensus()) {
            addTransactionToTccConfirmationCluster(transactionData);
        }
    }

    @Override
    public void finalizeInit() {
        isStarted = true;
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Scheduled(fixedDelay = 3000, initialDelay = 1000)
    public void checkForTrustChainConfirmedTransaction() {
        if (!isStarted) {
            return;
        }

        trustChainConfirmationService.init(trustChainConfirmationCluster);
        List<TccInfo> transactionConsensusConfirmed = trustChainConfirmationService.getTrustChainConfirmedTransactions();

        transactionConsensusConfirmed.forEach(tccInfo -> {
            trustChainConfirmationCluster.remove(tccInfo.getHash());
            confirmationService.setTccToTrue(tccInfo);
            log.debug("TCC has been reached for transaction {}!!", tccInfo.getHash());
        });
    }

    @Override
    public void attachToCluster(TransactionData transactionData) {
        updateParents(transactionData);

        addTransactionToTccConfirmationCluster(transactionData);
    }

    private void updateParents(TransactionData transactionData) {
        if (transactionData.getChildrenTransactionHashes() == null) {
            transactionData.setChildrenTransactionHashes(new ArrayList<>());
        }

        updateSingleParent(transactionData, transactionData.getLeftParentHash());
        updateSingleParent(transactionData, transactionData.getRightParentHash());
        removeTransactionParentsFromSources(transactionData);

    }

    private void updateSingleParent(TransactionData transactionData, Hash parentHash) {
        if (parentHash != null) {
            TransactionData parentTransactionData = transactions.getByHash(parentHash);
            if (parentTransactionData != null && !parentTransactionData.getChildrenTransactionHashes().contains(transactionData.getHash())) {
                parentTransactionData.addToChildrenTransactions(transactionData.getHash());
                if (trustChainConfirmationCluster.containsKey(parentTransactionData.getHash())) {
                    trustChainConfirmationCluster.put(parentTransactionData.getHash(), parentTransactionData);
                }
                transactions.put(parentTransactionData);
            }
        }
    }

    private void removeTransactionParentsFromSources(TransactionData transactionData) {
        if (transactionData.getLeftParentHash() != null) {
            removeTransactionFromSources(transactionData.getLeftParentHash());
        }
        if (transactionData.getRightParentHash() != null) {
            removeTransactionFromSources(transactionData.getRightParentHash());
        }
    }

    private void removeTransactionFromSources(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData != null && sourceListsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).remove(transactionData)) {
            liveViewService.updateTransactionStatus(transactionData, 1);
            totalSources.decrementAndGet();
        }
    }

    private void addTransactionToTccConfirmationCluster(TransactionData transactionData) {
        trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);

        if (transactionData.isSource() && sourceListsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).add(transactionData)) {
            totalSources.incrementAndGet();
        }

        log.debug("Added New Transaction with hash:{}", transactionData.getHash());
    }

    @Override
    public void selectSources(TransactionData transactionData) {
        List<Set<TransactionData>> trustScoreToTransactionMappingSnapshot =
                Collections.unmodifiableList(sourceListsByTrustScore);

        List<TransactionData> selectedSourcesForAttachment =
                sourceSelector.selectSourcesForAttachment(
                        trustScoreToTransactionMappingSnapshot,
                        transactionData.getSenderTrustScore());

        if (selectedSourcesForAttachment.size() == 0) {
            return;
        }
        if (selectedSourcesForAttachment.size() > 0) {
            transactionData.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
        }
        if (selectedSourcesForAttachment.size() > 1) {
            transactionData.setRightParentHash(selectedSourcesForAttachment.get(1).getHash());
        }

        List<Hash> selectedSourceHashes = selectedSourcesForAttachment.stream().map(source -> source.getHash()).collect(Collectors.toList());
        log.debug("For transaction with hash: {} we found the following sources: {}", transactionData.getHash(), selectedSourceHashes);

        return;
    }

    public List<Set<TransactionData>> getSourceListsByTrustScore() {
        return sourceListsByTrustScore;
    }

    @Override
    public long getTotalSources() {
        return totalSources.get();
    }

}