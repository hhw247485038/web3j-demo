import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 用于演示如何通过 Web3j 在 Java 中与以太坊智能合约进行交互。
 */
public class ContractInteraction {

    // 合约地址：部署在 optimism-sepolia 测试链上的 TestContract 合约
    private static final String CONTRACT_ADDRESS = CommonConstant.CONTRACT_ADDRESS;

    private static final String PRIVATE_KEY = CommonConstant.PRIVATE_KEY;

    // 获取 Web3j 实例（连接到以太坊节点）
    private static final Web3j web3j = Web3Utils.getWeb3j();

    // 根据私钥创建凭证对象
    private static final Credentials credentials = Credentials.create(PRIVATE_KEY);

    /**
     * 调用只读函数（不会修改链上状态）。
     *
     * @param functionName    合约函数名称
     * @param inputParameters 输入参数列表
     * @param outputParameters 输出参数类型
     * @return 返回调用结果
     * @throws IOException 如果调用失败
     */
    public static String callContract(String functionName, List<Type> inputParameters, List<TypeReference<?>> outputParameters) throws IOException {
        Function function = new Function(functionName, inputParameters, outputParameters);
        String encodedFunction = FunctionEncoder.encode(function);

        EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(credentials.getAddress(), CONTRACT_ADDRESS, encodedFunction),
                DefaultBlockParameterName.LATEST
        ).send();

        return response.getValue();
    }

    /**
     * 发送交易调用可变状态函数（会修改链上状态）。
     *
     * @param functionName    合约函数名称
     * @param inputParameters 输入参数列表
     * @param outputParameters 输出参数类型（通常为空）
     * @return 返回交易哈希
     * @throws Exception 如果发送失败
     */
    public static String sendTransaction(String functionName, List<Type> inputParameters, List<TypeReference<?>> outputParameters) throws Exception {
        Function function = new Function(functionName, inputParameters, outputParameters);
        String encodedFunction = FunctionEncoder.encode(function);

        // 获取链 ID
        EthChainId chainIdResponse = web3j.ethChainId().send();
        BigInteger chainId = chainIdResponse.getChainId();

        // 创建交易管理器
        RawTransactionManager transactionManager = new RawTransactionManager(web3j, credentials, chainId.longValue());

        // 发送交易
        EthSendTransaction transactionResponse = transactionManager.sendTransaction(
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                CONTRACT_ADDRESS,
                encodedFunction,
                BigInteger.ZERO
        );

        if (transactionResponse.hasError()) {
            throw new RuntimeException("Error sending transaction: " + transactionResponse.getError().getMessage());
        }

        return transactionResponse.getTransactionHash();
    }

    /**
     * 主函数，执行以下操作：
     * 1. 查询当前 value 值
     * 2. 调用 setValue 设置新值
     * 3. 等待一段时间后查询确认更新成功
     */
    public static void main(String[] args) throws Exception {
        // 获取调用 setValue 前的 value 值
        String resultBefore = callContract("getValue",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {}));
        System.out.println("Value before transaction: " + resultBefore);

        // 构造输入参数
        List<Type> inputParameters = new ArrayList<>();
        Uint256 value = new Uint256(BigInteger.valueOf(6));
        inputParameters.add(value);

        for (int i = 0; i < 100; i++) {
            // 发送 setValue 交易
            String txHash = sendTransaction("setValue",
                    inputParameters,
                    Collections.emptyList());
            System.out.println("Transaction sent! Tx Hash: " + txHash);
        }

        // 等待交易确认
        Thread.sleep(15000); // 等待 15 秒

        // 获取调用 setValue 后的 value 值
        String resultAfter = callContract("getValue",
                Collections.emptyList(),
                Arrays.asList(new TypeReference<Uint256>() {}));
        System.out.println("Value after transaction: " + resultAfter);
    }
}
