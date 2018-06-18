package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.Cluster;
import io.coti.cotinode.service.interfaces.ICluster;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Vector;

import static org.junit.Assert.*;

public class ClusterTest {

    ICluster cluster;
    List<TransactionData> allClusterTransactions;
    @Before
    public void setUp() throws Exception {
        System.out.println("Initializing!");
        allClusterTransactions = new Vector<>();

        TransactionData transaction1 = new TransactionData(new Hash("1".getBytes()));
        TransactionData transaction2 = new TransactionData(new Hash("2".getBytes()));
        TransactionData transaction3 = new TransactionData(new Hash("3".getBytes()));
        TransactionData transaction4 = new TransactionData(new Hash("4".getBytes()));
        allClusterTransactions.add(transaction1);
        allClusterTransactions.add(transaction2);
        allClusterTransactions.add(transaction3);
        allClusterTransactions.add(transaction4);
        cluster = new Cluster();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void initCluster() {
        cluster.initCluster(allClusterTransactions);
    }
}