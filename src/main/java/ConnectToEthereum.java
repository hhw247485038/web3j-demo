import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;

public class ConnectToEthereum {

    public static Web3j web3j = Web3Utils.getWeb3j();
    public static void main(String[] args) {

        try {
            System.out.println("welcome to web3j");
            System.out.println(web3j.web3ClientVersion().send().getWeb3ClientVersion());
            DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(web3j.ethBlockNumber().send().getBlockNumber());
            System.out.println(web3j.ethGetBalance(CommonConstant.WALLET_ADDRESS, defaultBlockParameter).send().getBalance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
