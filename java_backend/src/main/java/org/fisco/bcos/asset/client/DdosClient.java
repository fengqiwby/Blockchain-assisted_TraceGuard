package org.fisco.bcos.asset.client;

import org.fisco.bcos.asset.contract.TraceGuard;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import java.io.*;
import java.util.Properties;

public class DdosClient {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: deploy | query [ip]");
            return;
        }

        // 初始化
        BcosSDK sdk = BcosSDK.build("conf/config.toml");
        Client client = sdk.getClient(1);
        // 填入你的私钥
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair("baf29ec1c59c003b4be1f06ac5f482cfe46bb64faf3bff823599c1f7de7a1317");
        
        if (args[0].equals("deploy")) {
            TraceGuard tg = TraceGuard.deploy(client, keyPair);
            String addr = tg.getContractAddress();
            System.out.println("部署成功，合约地址: " + addr);
            // 自动存入 properties 文件供全自动流程使用
            Properties prop = new Properties();
            prop.setProperty("address", addr);
            prop.store(new FileOutputStream("conf/contract.properties"), null);
        }
        System.exit(0);
    }
}
