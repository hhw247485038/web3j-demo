import org.web3j.protocol.Web3j;

public class ConnectToEthereum {

    public static Web3j web3j = Web3Utils.getWeb3j();
    public static void main(String[] args) {

        try {
            System.out.println("welcome to web3j");
            System.out.println(web3j.web3ClientVersion().send().getWeb3ClientVersion());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
