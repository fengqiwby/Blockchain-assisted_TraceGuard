package org.fisco.bcos.asset.client; // 【核心修复 1】：匹配你的文件路径

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.fisco.bcos.asset.contract.TraceGuard;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.DynamicArray;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;

import java.io.*;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.*;

public class TrafficWebServer {
    private static TraceGuard traceGuard;
    private static Client client;
    private static final Gson gson = new Gson();
    private static final String SALT = "My_Project_Salt_2026";

    public static void main(String[] args) throws Exception {
        // 1. 初始化区块链 (请确认 dist/conf 目录下有这些文件)
        BcosSDK sdk = BcosSDK.build("dist/conf/config.toml");
        client = sdk.getClient(Integer.valueOf(1));
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair("baf29ec1c59c003b4be1f06ac5f482cfe46bb64faf3bff823599c1f7de7a1317");

        Properties prop = new Properties();
        prop.load(new FileInputStream("dist/conf/contract.properties"));
        traceGuard = TraceGuard.load(prop.getProperty("address"), client, keyPair);

        // 2. 启动服务
        HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/list", new ListHandler());
        server.createContext("/api/query", new QueryHandler());
        server.createContext("/api/add", new AddHandler());
        server.createContext("/api/revoke", new RevokeHandler());
        server.createContext("/api/update", new UpdateHandler());
        server.createContext("/api/stats", new StatsHandler());

        server.setExecutor(null);
        System.out.println("✅ TraceGuard 全功能 Web 后端已启动：http://localhost:9000");
        server.start();
    }

    static class ListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Object response = traceGuard.getHistoryAdvanced(BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ZERO, "");
                sendResponse(exchange, gson.toJson(serializeList(response)), 200);
            } catch (Exception e) { sendResponse(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 500); }
        }
    }

    static class QueryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = getParams(exchange);
            try {
                List<Map<String, Object>> resultList;

                // 情况 1：如果传了 IP，执行精确哈希查询
                if (params.containsKey("ip") && !params.get("ip").isEmpty()) {
                    String ipHash = hashIp(params.get("ip"), SALT);
                    Object res = traceGuard.getHistoryByIp(ipHash);
                    resultList = serializeList(res);
                }
                // 情况 2：执行组合条件查询
                else {
                    long start = parseTimestamp(params.get("start"), 0);
                    long end = parseTimestamp(params.get("end"), Long.MAX_VALUE);
                    int minConf = Integer.parseInt(params.getOrDefault("minConf", "0"));
                    String version = params.getOrDefault("version", "");

                    Object res = traceGuard.getHistoryAdvanced(
                            BigInteger.valueOf(start),
                            BigInteger.valueOf(end),
                            BigInteger.valueOf(minConf),
                            version
                    );
                    resultList = serializeList(res);
                }

                sendResponse(exchange, gson.toJson(resultList), 200);
            } catch (Exception e) {
                sendResponse(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 500);
            }
        }

        private long parseTimestamp(String dateStr, long defaultVal) {
            if (dateStr == null || dateStr.isEmpty()) return defaultVal;
            try {
                // 网页传入 yyyy-MM-dd，转为毫秒时间戳
                return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime();
            } catch (Exception e) { return 0; }
        }
    }

    static class AddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = getParams(exchange);
            try {
                String ip = params.get("ip");
                String conf = params.get("confidence");
                String ipHash = hashIp(ip, SALT);
                String featHash = "web_manual_" + System.currentTimeMillis();

                TransactionReceipt receipt = traceGuard.uploadEvidence(featHash, ipHash, "Web-Manual", new BigInteger(conf));
                sendResponse(exchange, "{\"msg\":\"上链成功\",\"hash\":\""+receipt.getTransactionHash()+"\"}", 200);
            } catch (Exception e) { sendResponse(exchange, "{\"error\":\""+e.getMessage()+"\"}", 500); }
        }
    }

    static class RevokeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = getParams(exchange);
            try {
                BigInteger id = new BigInteger(params.get("id"));
                TransactionReceipt receipt = traceGuard.revokeEvidence(id);
                sendResponse(exchange, "{\"status\":\""+receipt.getStatus()+"\"}", 200);
            } catch (Exception e) { sendResponse(exchange, "{\"error\":\""+e.getMessage()+"\"}", 500); }
        }
    }

    static class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = getParams(exchange);
            try {
                BigInteger id = new BigInteger(params.get("id"));
                BigInteger conf = new BigInteger(params.get("confidence"));
                TransactionReceipt receipt = traceGuard.updateConfidence(id, conf);
                sendResponse(exchange, "{\"status\":\""+receipt.getStatus()+"\"}", 200);
            } catch (Exception e) { sendResponse(exchange, "{\"error\":\""+e.getMessage()+"\"}", 500); }
        }
    }

    // --- 辅助方法区 ---
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> serializeList(Object response) {
        List<TraceGuard.Struct0> history;
        if (response instanceof DynamicArray) {
            history = (List<TraceGuard.Struct0>) ((DynamicArray<?>) response).getValue();
        } else {
            history = (List<TraceGuard.Struct0>) response;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (history != null) {
            for (TraceGuard.Struct0 row : history) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", row.id.toString());
                map.put("evidenceHash", row.evidenceHash);
                map.put("ipHash", row.ipHash);
                map.put("modelVersion", row.modelVersion);
                map.put("timestamp", row.timestamp.toString());
                map.put("blockHeight", row.blockHeight.toString());
                map.put("confidence", row.confidence.toString());
                map.put("status", row.status);
                result.add(map);
            }
        }
        return result;
    }

    private static Map<String, String> getParams(HttpExchange exchange) {
        Map<String, String> result = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) result.put(entry[0], entry[1]);
        }
        return result;
    }

    private static String hashIp(String ip, String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((ip + salt).getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    static class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String bh = client.getBlockNumber().getBlockNumber().toString();
            sendResponse(exchange, "{\"blockHeight\":\"" + bh + "\"}", 200);
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File("src/main/resources/web/index.html");
            if (!file.exists()) {
                sendResponse(exchange, "<h1>404 Not Found</h1>", 404);
                return;
            }
            byte[] bytes = Files.readAllBytes(file.toPath());
            String content = new String(bytes, StandardCharsets.UTF_8);
            sendResponse(exchange, content, 200);
        }
    }

    // 【核心修复 2】：统一参数类型为 (HttpExchange, String, int)
    private static void sendResponse(HttpExchange exchange, String content, int code) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        // 根据 code 自动设置 Content-Type
        String contentType = (code == 200 && content.startsWith("{")) ? "application/json" : "text/html";
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}