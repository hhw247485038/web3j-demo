import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;

/**
 * 用于演示如何在 Java 中实现以太坊消息签名及验证。
 */
public class Signature {

    /**
     * 验证指定的消息签名是否有效，并匹配给定的钱包地址。
     *
     * @param signature     签名数据（十六进制字符串格式）
     * @param message       被签名的原始消息
     * @param walletAddress 钱包地址，用于验证签名是否属于该地址
     * @return 如果签名有效且匹配钱包地址则返回 true，否则返回 false
     */
    public static Boolean isSignatureValid(String signature, String message, String walletAddress) {
        // 检查输入参数是否为空
        if (StringUtils.isAnyBlank(signature, message, walletAddress)) {
            return false;
        }

        // 将签名字符串转换为字节数组
        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
        // 检查签名长度是否正确
        if (signatureBytes.length != CommonConstant.SIGNATURE_BYTE_LENGTH) {
            return false;
        }

        // 提取 v 值并调整其范围到标准 Ethereum 签名格式
        byte v = signatureBytes[CommonConstant.V_INDEX];
        if (v < CommonConstant.V_LOWER_BOUND) {
            v += CommonConstant.V_BASE;
        }

        // 提取 r 和 s 值
        Sign.SignatureData signatureData = new Sign.SignatureData(
                v,
                Arrays.copyOfRange(signatureBytes, CommonConstant.R_START_INDEX, CommonConstant.R_END_INDEX),
                Arrays.copyOfRange(signatureBytes, CommonConstant.S_START_INDEX, CommonConstant.S_END_INDEX)
        );

        // 根据签名恢复公钥
        BigInteger publicKey;
        try {
            publicKey = Sign.signedPrefixedMessageToKey(message.getBytes(), signatureData);
        } catch (SignatureException e) {
            return false;
        }

        // 计算钱包地址并进行比对
        String parsedAddress = CommonConstant.ADDRESS_PREFIX + Keys.getAddress(publicKey);
        return parsedAddress.equalsIgnoreCase(walletAddress);
    }

    /**
     * 使用私钥对消息进行签名，并返回签名的十六进制字符串。
     * 在签名前会对消息添加以太坊特定的前缀。
     *
     * @param privateKeyHex 私钥（十六进制字符串格式）
     * @param message       待签名的消息
     * @return 返回签名结果（十六进制字符串）
     */
    public static String signPrefixedMessage(String privateKeyHex, String message) {
        // 将私钥字符串转换为 BigInteger
        BigInteger privateKey = new BigInteger(privateKeyHex, CommonConstant.PRIVATE_KEY_RADIX);

        // 创建 ECKeyPair 对象
        ECKeyPair keyPair = ECKeyPair.create(privateKey);

        // 对带有前缀的消息进行签名
        Sign.SignatureData signatureData = Sign.signPrefixedMessage(message.getBytes(), keyPair);

        // 将签名结果拼接成十六进制字符串返回
        return Numeric.toHexStringNoPrefix(signatureData.getR()) +
                Numeric.toHexStringNoPrefix(signatureData.getS()) +
                Numeric.toHexStringNoPrefix(signatureData.getV());
    }

    /**
     * 主函数，执行以下操作：
     * 1. 定义测试用的私钥和钱包地址
     * 2. 定义需要签名的消息
     * 3. 调用 signPrefixedMessage 方法生成签名
     * 4. 调用 isSignatureValid 方法验证签名有效性
     * 5. 输出结果
     */
    public static void main(String[] args) {
        // 测试用的私钥和钱包地址（请替换为你自己的）
        String privateKeyHex = "ad6bfebb055780013c24afdd167cceb96ef89ddf9e4eb8615a99e573531407b5";
        String walletAddress = "0x4dc2739b3de594754066357e54bfce70167b3f99";

        // 定义待签名的消息
        String message = "2778f9e5-5992-4b06-8a8f-85135d687cff";

        // 使用私钥对消息进行签名
        String signature = signPrefixedMessage(privateKeyHex, message);

        // 验证签名是否有效
        boolean isValid = isSignatureValid(signature, message, walletAddress);

        // 输出结果
        System.out.println("Signature: " + signature);
        System.out.println("Is signature valid: " + isValid);
    }
}
