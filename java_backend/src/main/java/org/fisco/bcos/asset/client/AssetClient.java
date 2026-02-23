package org.fisco.bcos.asset.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fisco.bcos.asset.contract.Asset;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;

public class AssetClient {
    static Logger logger = LoggerFactory.getLogger(AssetClient.class);
    private BcosSDK bcosSDK;
    private Client client;
    private CryptoKeyPair cryptoKeyPair;

    public void initialize() throws Exception {
        // 使用 config.toml 初始化，而不是 XML
        String configFile = "conf/config.toml";
        bcosSDK = BcosSDK.build(configFile);
        client = bcosSDK.getClient(1);
        cryptoKeyPair = client.getCryptoSuite().createKeyPair();
        client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
        logger.debug("client setup success, account: " + cryptoKeyPair.getAddress());
    }

    public void deployAssetAndRecordAddr() {
        try {
            Asset asset = Asset.deploy(client, cryptoKeyPair);
            System.out.println(" deploy Asset success, contract address is " + asset.getContractAddress());
            recordAssetAddr(asset.getContractAddress());
        } catch (Exception e) {
            System.out.println(" deploy Asset failed, error: " + e.getMessage());
        }
    }

    public void recordAssetAddr(String address) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.setProperty("address", address);
        // 将地址保存在当前目录的 contract.properties 文件中
        FileOutputStream fileOutputStream = new FileOutputStream("contract.properties");
        prop.store(fileOutputStream, "contract address");
    }

    public String loadAssetAddr() throws Exception {
        Properties prop = new Properties();
        // 从当前目录读取 contract.properties
        try {
            prop.load(new FileInputStream("contract.properties"));
        } catch (FileNotFoundException e) {
            System.out.println("无法找到合约地址文件，请先执行 deploy 操作！");
            throw e;
        }
        String contractAddress = prop.getProperty("address");
        if (contractAddress == null || contractAddress.trim().equals("")) {
            throw new Exception(" load Asset contract address failed, please deploy it first. ");
        }
        return contractAddress;
    }

    public void queryAssetAmount(String assetAccount) {
        try {
            String contractAddress = loadAssetAddr();
            Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
            Tuple2<BigInteger, BigInteger> result = asset.select(assetAccount);
            if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
                System.out.printf(" asset account %s, value %s \n", assetAccount, result.getValue2());
            } else {
                System.out.printf(" %s asset account is not exist \n", assetAccount);
            }
        } catch (Exception e) {
            System.out.printf(" query failed, error: %s\n", e.getMessage());
        }
    }

    public void registerAssetAccount(String assetAccount, BigInteger amount) {
        try {
            String contractAddress = loadAssetAddr();
            Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = asset.register(assetAccount, amount);
            List<Asset.RegisterEventEventResponse> response = asset.getRegisterEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
                    System.out.printf(" register success => asset: %s, value: %s \n", assetAccount, amount);
                } else {
                    System.out.printf(" register failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found, maybe transaction not exec. ");
            }
        } catch (Exception e) {
            System.out.printf(" register failed, error: %s\n", e.getMessage());
        }
    }

    public void transferAsset(String fromAccount, String toAccount, BigInteger amount) {
        try {
            String contractAddress = loadAssetAddr();
            Asset asset = Asset.load(contractAddress, client, cryptoKeyPair);
            TransactionReceipt receipt = asset.transfer(fromAccount, toAccount, amount);
            List<Asset.TransferEventEventResponse> response = asset.getTransferEventEvents(receipt);
            if (!response.isEmpty()) {
                if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
                    System.out.printf(" transfer success => from: %s, to: %s, amount: %s \n", fromAccount, toAccount, amount);
                } else {
                    System.out.printf(" transfer failed, ret code is %s \n", response.get(0).ret.toString());
                }
            } else {
                System.out.println(" event log not found. ");
            }
        } catch (Exception e) {
            System.out.printf(" transfer failed, error: %s\n", e.getMessage());
        }
    }

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("\t bash asset_run.sh deploy");
        System.out.println("\t bash asset_run.sh query    asset_account");
        System.out.println("\t bash asset_run.sh register asset_account asset_amount");
        System.out.println("\t bash asset_run.sh transfer from_account to_account amount");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            Usage();
        }
        AssetClient client = new AssetClient();
        client.initialize();
        switch (args[0]) {
            case "deploy":
                client.deployAssetAndRecordAddr();
                break;
            case "query":
                if (args.length < 2) Usage();
                client.queryAssetAmount(args[1]);
                break;
            case "register":
                if (args.length < 3) Usage();
                client.registerAssetAccount(args[1], new BigInteger(args[2]));
                break;
            case "transfer":
                if (args.length < 4) Usage();
                client.transferAsset(args[1], args[2], new BigInteger(args[3]));
                break;
            default:
                Usage();
        }
        System.exit(0);
    }
}
