import io.reactivex.Flowable;
import org.reactivestreams.Subscription;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * 扩展支持多种监听类型的以太坊事件监听器
 */
public class EventListener {

    // 测试合约地址（示例）
    private static final String CONTRACT_ADDRESS = "0xB6b0ab2e6205212FD2A4017bD0A4710b11EA55eb";

    public Subscription ethMissSubscription; //ETH交易空档事件订阅对象

    public Subscription ethSubscription;     //ETH交易事件订阅对象
    // 以太坊节点服务
    private static final Web3j web3j = Web3j.build(new HttpService("https://opt-sepolia.g.alchemy.com/v2/tow91YQzp06m0yzJ8nQ_pa6gVokpKCk5"));

    /**
     * 监听ERC20代币转账事件（从指定区块开始）
     *
     * @param contractAddress ERC20合约地址
     * @param startBlock      起始区块号
     */
    public void listenTokenTransfers(String contractAddress, BigInteger startBlock) {
        // 定义ERC20 Transfer事件
        Event transferEvent = new Event("Transfer", Arrays.asList(
                new TypeReference<Address>(true) {
                },  // from (indexed)
                new TypeReference<Address>(true) {
                },  // to (indexed)
                new TypeReference<Uint256>(false) {
                }  // value (non-indexed)
        ));

        EthFilter filter = new EthFilter(
                DefaultBlockParameter.valueOf(startBlock),
                DefaultBlockParameterName.LATEST,
                contractAddress
        );
        filter.addOptionalTopics(EventEncoder.encode(transferEvent));

        Flowable<Log> logFlowable = web3j.ethLogFlowable(filter);
        logFlowable.subscribe(
                log -> processTransferEvent(log, contractAddress, transferEvent),
                throwable -> System.err.println("Token transfer error: " + throwable.getMessage())
        );
    }

    /**
     * 回放指定区块范围的历史交易
     *
     * @param contractAddress 合约地址
     * @param fromBlock       起始区块
     * @param toBlock         结束区块
     */
    public void replayPastTransactions(String contractAddress, BigInteger fromBlock, BigInteger toBlock) {
        Event transferEvent = new Event("Transfer", Arrays.asList(
                new TypeReference<Address>(true) {
                },
                new TypeReference<Address>(true) {
                },
                new TypeReference<Uint256>(false) {
                }
        ));

        EthFilter filter = new EthFilter(
                DefaultBlockParameter.valueOf(fromBlock),
                DefaultBlockParameter.valueOf(toBlock),
                contractAddress
        );
        filter.addOptionalTopics(EventEncoder.encode(transferEvent));

        // 同步获取历史日志
        EthLog ethLog = null;
        try {
            ethLog = web3j.ethGetLogs(filter).send();
            ethLog.getLogs().forEach(log -> processTransferEvent((Log) log, contractAddress, transferEvent));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 监听未来发生的交易（从当前区块开始）
     *
     * @param contractAddress 监听的合约地址
     */
    public void listenFutureTransactions(String contractAddress) {
        Event transferEvent = new Event("Transfer", Arrays.asList(
                new TypeReference<Address>(true) {
                },
                new TypeReference<Address>(true) {
                },
                new TypeReference<Uint256>(false) {
                }
        ));

        EthFilter filter = new EthFilter(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST,
                contractAddress
        );
        filter.addOptionalTopics(EventEncoder.encode(transferEvent));

        Flowable<Log> logFlowable = web3j.ethLogFlowable(filter);
        logFlowable.subscribe(
                log -> processTransferEvent(log, contractAddress, transferEvent),
                throwable -> System.err.println("Future transaction error: " + throwable.getMessage())
        );
    }

    /**
     * 处理Transfer事件逻辑
     *
     * @param log             区块链日志
     * @param contractAddress 合约地址（用于日志区分）
     */
    private void processTransferEvent(Log log, String contractAddress, Event event) {
        if (log.getData() == null || log.getData().equals("0x")) {
            System.err.println("Empty log data for contract " + contractAddress);
            return;
        }

        // 提取indexed参数
        String from = "0x" + log.getTopics().get(1).substring(26);
        String to = "0x" + log.getTopics().get(2).substring(26);
        System.out.println(log);
        System.out.printf("Contract %s: Transfer from %s to %s%n", contractAddress, from, to);

        // 解析非indexed参数
        List<Type> decoded = FunctionReturnDecoder.decode(log.getData(), event.getNonIndexedParameters());

        if (!decoded.isEmpty()) {
            BigInteger value = (BigInteger) decoded.get(0).getValue();
            System.out.printf("Contract %s: Transfer from %s to %s, amount: %s%n",
                    contractAddress, from, to, value.toString());
        }
    }

    public static void main(String[] args) {
        EventListener listener = new EventListener();

        // 示例调用：
        // 1. 监听新代币转账
        listener.listenTokenTransfers(CONTRACT_ADDRESS, BigInteger.ZERO);

        // 2. 重放历史交易（区块1000000-1100000）
        listener.replayPastTransactions(CONTRACT_ADDRESS, BigInteger.valueOf(0), BigInteger.valueOf(400));

        // 3. 监听未来交易
        listener.listenFutureTransactions(CONTRACT_ADDRESS);

        System.out.println("Listening...");
    }
}
