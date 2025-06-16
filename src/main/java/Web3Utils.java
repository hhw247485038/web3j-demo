import okhttp3.OkHttpClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

public class Web3Utils {
    private static final String projectId = CommonConstant.PROJECT_ID;

    public static Web3j getWeb3j() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        return Web3j.build(new HttpService("https://sepolia.infura.io/v3/" + projectId, okHttpClient, false));
    }
}
