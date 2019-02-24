package io.coti.trustscore.services;

import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class InitializationService extends BaseNodeInitializationService {

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private INetworkService networkService;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;

    @PostConstruct
    public void init() {
        super.initDB();
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.TrustScoreNode);
        NetworkNodeData zerospendNetworkNodeData = networkService.getSingleNodeData(NodeType.ZeroSpendServer);
        if (zerospendNetworkNodeData == null) {
            log.error("Zero Spend server is down, info came from node manager");
            System.exit(-1);
        }
        networkService.setRecoveryServerAddress(zerospendNetworkNodeData.getHttpFullAddress());
        super.init();
        communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress());
        List<NetworkNodeData> dspNetworkNodeData = networkService.getShuffledNetworkNodeDataListFromMapValues(NodeType.DspNode);
        dspNetworkNodeData.forEach(node -> communicationService.addSubscription(node.getPropagationFullAddress()));
    }

    @Override
    protected NetworkNodeData createNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.TrustScoreNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash(), networkType);
        return networkNodeData;

    }
}