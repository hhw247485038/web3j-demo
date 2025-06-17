import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;

import java.math.BigInteger;

/**
 * filter相关
 * 监听区块、交易
 * 所有监听都在Web3jRx中
 */
public class Filter {
    private static Web3j web3j;

    public static void main(String[] args) {
        web3j = Web3Utils.getWeb3j();
        /**
         * 新区块监听
         */
        newBlockFilter(web3j);
        /**
         * 新交易监听
         */
        newTransactionFilter(web3j);
        /**
         * 遍历旧区块、交易
         */
        replayFilter(web3j);
        /**
         * 从某一区块开始直到最新区块、交易
         */
        catchUpFilter(web3j);

        /**
         * 取消监听
         */
        //subscription.unsubscribe();
    }

    private static void newBlockFilter(Web3j web3j) {
        web3j.
                blockFlowable(false).
                subscribe(block -> {
                    System.out.println("new block come in");
                    System.out.println("block number" + block.getBlock().getNumber());
                });
    }

    private static void newTransactionFilter(Web3j web3j) {
        web3j.
                transactionFlowable().
                subscribe(transaction -> {
                    System.out.println("transaction come in");
                    System.out.println("transaction txHash " + transaction.getHash());
                });
    }

    private static void replayFilter(Web3j web3j) {
        BigInteger startBlock = BigInteger.valueOf(2000000);
        BigInteger endBlock = BigInteger.valueOf(2010000);
        /**
         * 遍历旧区块
         */
        web3j.
                replayPastBlocksFlowable(
                        DefaultBlockParameter.valueOf(startBlock),
                        DefaultBlockParameter.valueOf(endBlock),
                        false).
                subscribe(ethBlock -> {
                    System.out.println("replay block");
                    System.out.println(ethBlock.getBlock().getNumber());
                });

        /**
         * 遍历旧交易
         */
        web3j.
                replayPastTransactionsFlowable(
                        DefaultBlockParameter.valueOf(startBlock),
                        DefaultBlockParameter.valueOf(endBlock)).
                subscribe(transaction -> {
                    System.out.println("replay transaction");
                    System.out.println("txHash " + transaction.getHash());
                });
    }

    private static void catchUpFilter(Web3j web3j) {
        BigInteger startBlock = BigInteger.valueOf(2000000);

        /**
         * 遍历旧区块，监听新区块
         */
        web3j.replayPastAndFutureBlocksFlowable(
                        DefaultBlockParameter.valueOf(startBlock), false)
                .subscribe(block -> {
                    System.out.println("block");
                    System.out.println(block.getBlock().getNumber());
                });

        /**
         * 遍历旧交易，监听新交易
         */
        web3j.replayPastAndFutureTransactionsFlowable(
                        DefaultBlockParameter.valueOf(startBlock))
                .subscribe(tx -> {
                    System.out.println("transaction");
                    System.out.println(tx.getHash());
                });
    }
}