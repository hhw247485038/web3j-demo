import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 用于演示如何在 Java 中使用 Web3j 进行以太坊余额查询和 ETH 转账操作。
 */
public class Transfer {
    // 创建 Web3j 实例，连接到以太坊网络
    private static final Web3j web3j = Web3Utils.getWeb3j();

    /**
     * 获取指定地址的 ETH 余额（单位为 Ether）。
     *
     * @param address 钱包地址
     * @return 返回该地址的 ETH 余额（Ether 单位）
     * @throws IOException 如果与节点通信失败
     */
    public static BigDecimal getETHBalance(String address) throws IOException {
        // 获取账户余额（单位为 Wei）
        BigInteger balanceInWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();
        // 将 Wei 转换为 Ether（1 Ether = 10^18 Wei）
        return Convert.fromWei(new BigDecimal(balanceInWei), Convert.Unit.ETHER);
    }

    /**
     * 向目标地址转账 ETH。
     *
     * @param senderPrivateKey 发送方私钥（注意保密）
     * @param recipientAddress 接收方钱包地址
     * @param amountInEther    转账金额（单位为 Ether）
     * @return 返回交易哈希，如果失败则返回 null
     * @throws IOException 如果与节点通信失败
     */
    public static String transfer(String senderPrivateKey, String recipientAddress, BigDecimal amountInEther) throws IOException {
        // 获取当前链 ID（用于防止重放攻击）
        EthChainId chainIdResponse = web3j.ethChainId().send();
        long chainId = chainIdResponse.getChainId().longValue();

        /* 设置发送方账户信息 */
        // 根据私钥创建凭证对象
        Credentials credentials = Credentials.create(senderPrivateKey);
        String senderAddress = credentials.getAddress();
        // 获取发送方当前的 nonce 值（用于标识交易顺序）
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                senderAddress, DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        /* 设置交易参数：金额、Gas价格和Gas上限 */
        // 将转账金额从 Ether 转换为 Wei
        BigInteger value = Convert.toWei(amountInEther, Convert.Unit.ETHER).toBigInteger();
        // 获取当前 Gas 价格
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
        BigInteger gasPrice = ethGasPrice.getGasPrice();
        // 标准 ETH 转账 Gas 上限为 21000
        BigInteger gasLimit = BigInteger.valueOf(21000);

        /* 构建交易并发送 */
        // 创建原始交易对象
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce, gasPrice, gasLimit, recipientAddress, value);
        // 签名交易数据
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        // 发送签名后的交易
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
        // 返回交易哈希（可用于追踪交易状态）
        return ethSendTransaction.getTransactionHash();
    }

    /**
     * 主函数，执行以下步骤：
     * 1. 查询收款人地址的初始余额
     * 2. 向收款人地址转账 0.001 ETH
     * 3. 等待 15 秒后再次查询收款人地址余额，确认转账是否到账
     */
    public static void main(String[] args) throws Exception {
        // 发送方私钥（请替换为自己的私钥）
        String privateKey = CommonConstant.PRIVATE_KEY;
        // 收款人地址（请替换为自己的测试地址）
        String recipientAddress = CommonConstant.WALLET_ADDRESS;
        // 转账金额（0.001 ETH）
        BigDecimal amountInEther = new BigDecimal("0.001");

        // 查询转账前收款人余额
        BigDecimal before = getETHBalance(recipientAddress);
        System.out.println("Balance of recipient address before transfer: " + before);

        // 执行转账
        String transactionHash = Transfer.transfer(privateKey, recipientAddress, amountInEther);
        System.out.println("Transaction Hash: " + transactionHash);

        // 等待交易确认（可手动通过区块浏览器验证）
        Thread.sleep(15000); // 等待 15 秒

        // 查询转账后收款人余额
        BigDecimal after = getETHBalance(recipientAddress);
        System.out.println("Balance of recipient address after transfer: " + after);
    }
}
