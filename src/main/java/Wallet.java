import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Wallet {

    /**
     * 生成一个随机的以太坊私钥。
     *
     * @return 返回十六进制格式的随机生成的私钥字符串。
     * @throws InvalidAlgorithmParameterException 如果加密算法参数无效。
     * @throws NoSuchAlgorithmException 如果加密算法不可用。
     * @throws NoSuchProviderException 如果安全提供者不可用。
     */
    public static String createRandomPrivateKey() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        // 生成一个随机的 ECKeyPair（包含私钥和公钥）
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        // 将私钥转换为十六进制字符串并返回
        return ecKeyPair.getPrivateKey().toString(CommonConstant.PRIVATE_KEY_RADIX);
    }

    /**
     * 根据给定的私钥生成以太坊钱包地址。
     *
     * @param privateKeyHex 私钥的十六进制格式字符串。
     * @return 返回带有 "0x" 前缀的以太坊钱包地址。
     */
    public static String getWalletAddressFromPrivateKeyHex(String privateKeyHex) {
        // 将十六进制格式的私钥转换为 BigInteger 类型
        BigInteger privateKey = new BigInteger(privateKeyHex, CommonConstant.PRIVATE_KEY_RADIX);
        // 创建一个 ECKeyPair 对象（包含私钥和公钥）
        ECKeyPair keyPair = ECKeyPair.create(privateKey);
        // 从公钥中生成钱包地址，并添加 "0x" 前缀后返回
        return CommonConstant.ADDRESS_PREFIX + Keys.getAddress(keyPair.getPublicKey());
    }

    /**
     * 验证给定的以太坊钱包地址是否符合正确的格式。
     *
     * @param address 钱包地址（应该以 "0x" 开头）。
     * @return 如果地址有效，返回 true；否则返回 false。
     */
    public static boolean isValidAddress(String address) {
        return WalletUtils.isValidAddress(address);
    }

    /**
     * 验证给定的私钥是否有效。
     *
     * @param privateKey 私钥的十六进制格式字符串。
     * @return 如果私钥有效，返回 true；否则返回 false。
     */
    public static boolean isValidPrivateKey(String privateKey) {
        return WalletUtils.isValidPrivateKey(privateKey);
    }

    public static void main(String[] args) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        // 生成一个随机的私钥
        String privateKeyHex = Wallet.createRandomPrivateKey();

        // 使用私钥生成钱包地址
        String walletAddress = Wallet.getWalletAddressFromPrivateKeyHex(privateKeyHex);

        // 打印生成的私钥和对应的钱包地址
        System.out.println("私钥 [" + privateKeyHex + "] 对应的钱包地址是: [" + walletAddress + "]");

        // 验证生成的钱包地址是否有效
        System.out.println("钱包地址 [" + walletAddress + "] 是 " + (Wallet.isValidAddress(walletAddress) ? "有效的" : "无效的"));

        // 验证生成的私钥是否有效
        System.out.println("私钥 [" + privateKeyHex + "] 是 " + (Wallet.isValidPrivateKey(privateKeyHex) ? "有效的" : "无效的"));
    }
}
