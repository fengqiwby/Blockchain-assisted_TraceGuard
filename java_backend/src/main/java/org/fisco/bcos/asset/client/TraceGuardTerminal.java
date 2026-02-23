package org.fisco.bcos.asset.client;

import org.fisco.bcos.asset.contract.TraceGuard;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.abi.datatypes.DynamicArray;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import java.util.UUID;

public class TraceGuardTerminal {

    // --- 1. 基础配置 ---
    private static String getContractAddress() {
        Properties prop = new Properties();
        // 指向你通过 Deployment 同步过来的或手动放好的属性文件
        String path = "dist/conf/contract.properties";
        try (FileInputStream fis = new FileInputStream(path)) {
            prop.load(fis);
            return prop.getProperty("address");
        } catch (IOException e) {
            System.err.println("❌ 无法读取合约地址文件，请确保执行了虚拟机的部署脚本并同步了文件！");
            return null;
        }
    }
    private static final String configPath = "src/main/resources/config.toml";
    private static final String privateKeyHex = "baf29ec1c59c003b4be1f06ac5f482cfe46bb64faf3bff823599c1f7de7a1317";
    private static final String exchangePath = "C:/Users/15363/PycharmProjects/PythonProject4/dl_detection/exchange_folder";
    private static final String SALT = "My_Project_Salt_2026";
    private static final String IP_MAP_FILE = "ip_map.properties";

    // --- 2. 性能统计变量 ---
    private static final ExecutorService uploadPool = Executors.newFixedThreadPool(5);
    private static final int MAX_RETRY = 3;
    private static AtomicInteger totalProcessed = new AtomicInteger(0);
    private static AtomicLong totalLatency = new AtomicLong(0);
    private static long batchStartTime = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("正在初始化 TraceGuard 综合治理终端...");
        BcosSDK sdk = BcosSDK.build(configPath);
        Client client = sdk.getClient(Integer.valueOf(1));
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair(privateKeyHex);
        String dynamicAddress = getContractAddress();
        if (dynamicAddress == null) return;

        TraceGuard traceGuard = TraceGuard.load(dynamicAddress, client, keyPair);
        System.out.println("✅ 成功连接新合约! 地址: " + dynamicAddress);

        System.out.println("✅ 区块链连接成功! 当前管理员: " + keyPair.getAddress());

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n==========================================");
            System.out.println("🛡️ TraceGuard 综合安全管理终端 (全功能优化版)");
            System.out.println("1. 开启并发证据监听 (性能优化上链)");
            System.out.println("2. 执行多维度回溯 (查询/筛选模式)");
            System.out.println("3. 管理员治理功能 (撤销/修正记录)");
            System.out.println("4. 查看区块链状态 (出块高度)");
            System.out.println("5. 生成分析看板 (可视化/性能指标)");
            System.out.println("6. 🚀 全维度性能压测 (Benchmark)"); // 正式列入菜单
            System.out.println("------------------------------------------");
            System.out.println("💡 快捷指令: 输入 'tps [数量]' 或 'benchmark [1-3] [数量]'");
            System.out.println("0. 退出系统");
            System.out.println("==========================================");
            System.out.print("请选择功能或输入指令: ");

            String choice = scanner.nextLine().trim();

// --- 【新增：tps 指令解析逻辑】 ---
            if (choice.startsWith("tps ")) {
                try {
                    int amount = Integer.parseInt(choice.substring(4));
                    runStressTest(traceGuard, amount); // 调用压测函数
                } catch (Exception e) {
                    System.out.println("❌ 格式错误，请输入: tps 500");
                }
                continue; // 执行完压测后直接跳回菜单开头
            }

            // --- 2. 快捷指令解析 (benchmark 1 500) ---
            if (choice.startsWith("benchmark ")) {
                try {
                    String[] parts = choice.split(" ");
                    int mode = Integer.parseInt(parts[1]);  // 模式：1-改, 2-删, 3-查
                    int amount = Integer.parseInt(parts[2]); // 数量
                    runFullBenchmark(traceGuard, mode, amount);
                } catch (Exception e) {
                    System.out.println("❌ 格式错误，请输入: benchmark [模式] [数量]");
                    System.out.println("💡 模式提示: 1-改(修正), 2-删(撤销), 3-查(检索)");
                }
                continue;
            }

            switch (choice) {
                case "1": runUploaderModeAsync(traceGuard); break;
                case "2": runQueryMode(traceGuard, scanner); break;
                case "3": runGovernanceMode(traceGuard, scanner); break;
                case "4": System.out.println("🧱 当前区块高度: " + client.getBlockNumber().getBlockNumber()); break;
                case "5": showStatisticsDashboard(traceGuard); break;
                case "6": // 交互式引导压测
                    System.out.println("\n[压测引导] 请输入模式 (1-改, 2-删, 3-查): ");
                    int m = Integer.parseInt(scanner.nextLine());
                    System.out.println("[压测引导] 请输入测试样本量 (150/500/2000): ");
                    int n = Integer.parseInt(scanner.nextLine());
                    runFullBenchmark(traceGuard, m, n);
                    break;
                case "0": uploadPool.shutdown(); System.exit(0);
                default: System.out.println("❌ 无效选择。");
            }
        }
    }

    /**
     * 【功能 1】异步并发上链逻辑
     */
    private static void runUploaderModeAsync(TraceGuard traceGuard) {
        String archivePath = exchangePath.replace("exchange_folder", "archived_evidence");
        File archiveDir = new File(archivePath);
        if (!archiveDir.exists()) archiveDir.mkdirs();

        File folder = new File(exchangePath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        if (files == null || files.length == 0) {
            System.out.println("目前没有新证据。");
            return;
        }

        // 1. 记录开始时间
        System.out.println("🚀 [高性能模式] 异步并行上链启动，文件总数: " + files.length);
        batchStartTime = System.currentTimeMillis();
        totalProcessed.set(0);
        totalLatency.set(0);

        for (File file : files) {
            // 使用线程池处理文件读取，避免磁盘IO阻塞
            uploadPool.execute(() -> {
                try {
                    final long txStartNano = System.nanoTime();
                    String content = new String(Files.readAllBytes(file.toPath()));
                    JsonObject json = new JsonParser().parse(content).getAsJsonObject();

                    String ipHash = json.get("ip_hash").getAsString();
                    String realIp = json.has("real_ip") ? json.get("real_ip").getAsString() : "Unknown";

                    // 【核心修改】：不再接收 TransactionReceipt，而是传入一个 TransactionCallback
                    // 注意：这里调用的 uploadEvidence 最后一个参数是回调对象
                    traceGuard.uploadEvidence(
                            json.get("feature_hash").getAsString(),
                            ipHash,
                            json.get("model_version").getAsString(),
                            new BigInteger(json.get("confidence").getAsString()),
                            new org.fisco.bcos.sdk.model.callback.TransactionCallback() {
                                @Override
                                public void onResponse(TransactionReceipt receipt) {
                                    // 只有区块链真正确认了（出块了），才会进入这个方法
                                    if ("0x0".equals(receipt.getStatus())) {
                                        totalProcessed.incrementAndGet();
                                        long latencyNano = System.nanoTime() - txStartNano;
                                        totalLatency.addAndGet(latencyNano);

                                        // 执行归档逻辑
                                        saveIpMapping(ipHash, realIp);
                                        File destFile = new File(archiveDir, file.getName());
                                        file.renameTo(destFile);

                                        // 这里可以只在每完成 10 笔时打印一次，防止刷屏影响性能
                                        if (totalProcessed.get() % 5 == 0 || totalProcessed.get() == files.length) {
                                            System.out.println("✅ [后台确认] 已完成: " + totalProcessed.get() + "/" + files.length);
                                        }
                                    } else {
                                        System.err.println("❌ 交易失败，哈希: " + receipt.getTransactionHash() + " 状态: " + receipt.getStatus());
                                    }
                                }
                            }
                    );

                    // 打印投递状态（这步非常快，不代表上链成功，只代表发给了节点）
                    // System.out.println("📤 已投递: " + file.getName());

                } catch (Exception e) {
                    System.err.println("❌ 读取文件失败: " + file.getName() + " 原因: " + e.getMessage());
                }
            });
        }
        System.out.println(">>> 所有任务已提交至节点缓冲队列，请稍后查看看板统计结果。");
    }

    /**
     * 【功能 2】多维度查询逻辑 (保留所有原分支)
     */
    private static void runQueryMode(TraceGuard traceGuard, Scanner scanner) {
        System.out.println("\n--- 🔍 查询模式选择 ---");
        System.out.println("1. 按明文 IP 精确查询");
        System.out.println("2. 组合条件筛选 (时间/置信度/模型)");
        System.out.print("请选择: ");
        String subOp = scanner.nextLine();

        try {
            List<TraceGuard.Struct0> history = null;
            if ("1".equals(subOp)) {
                System.out.print("👉 请输入攻击者 IP: ");
                String inputIp = scanner.nextLine();
                String ipHash = hashIp(inputIp.trim(), SALT);
                history = parseHistory(traceGuard.getHistoryByIp(ipHash));
            } else if ("2".equals(subOp)) {
                System.out.print("👉 起始日期 (yyyy-MM-dd, 直接回车跳过): ");
                long start = parseDate(scanner.nextLine(), 0);
                System.out.print("👉 结束日期 (yyyy-MM-dd, 直接回车跳过): ");
                long end = parseDate(scanner.nextLine(), Long.MAX_VALUE);
                System.out.print("👉 最低置信度 (0-100)，直接回车跳过: ");
                String confStr = scanner.nextLine();
                BigInteger minConf = new BigInteger(confStr.isEmpty() ? "0" : confStr);
                System.out.print("👉 模型版本号 (例如CNN-V1.0,直接回车跳过): ");
                String version = scanner.nextLine();

                history = parseHistory(traceGuard.getHistoryAdvanced(BigInteger.valueOf(start), BigInteger.valueOf(end), minConf, version));
            }
            displayHistory(history);
        } catch (Exception e) { System.err.println("❌ 查询失败: " + e.getMessage()); }
    }

    /**
     * 【功能 3】治理逻辑 (保留撤销和修正)
     */
    private static void runGovernanceMode(TraceGuard traceGuard, Scanner scanner) {
        System.out.println("\n--- ⚖️ 管理员治理中心 ---");
        System.out.println("1. 撤销/标记某条存证为无效");
        System.out.println("2. 修正存证的置信度");
        System.out.print("请选择: ");
        String op = scanner.nextLine();
        System.out.print("请输入目标 [存证 ID]: ");
        BigInteger id = new BigInteger(scanner.nextLine());
        try {
            TransactionReceipt receipt;
            if ("1".equals(op)) {
                receipt = traceGuard.revokeEvidence(id);
                if ("0x0".equals(receipt.getStatus())) System.out.println("✅ ID #" + id + " 已标记为无效。");
            } else if ("2".equals(op)) {
                System.out.print("输入新置信度 (0-100): ");
                BigInteger newConf = new BigInteger(scanner.nextLine());
                receipt = traceGuard.updateConfidence(id, newConf);
                if ("0x0".equals(receipt.getStatus())) System.out.println("✅ ID #" + id + " 置信度已更新。");
            }
        } catch (Exception e) { System.err.println("❌ 操作失败: " + e.getMessage()); }
    }

    /**
     * 【功能 5】统计与看板生成 (融合性能指标)
     */
    private static void showStatisticsDashboard(TraceGuard traceGuard) throws Exception {
        System.out.println("\n📊 正在从区块链提取数据并计算性能指标...");
        Object response = traceGuard.getHistoryAdvanced(BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE), BigInteger.ZERO, "");
        List<TraceGuard.Struct0> allData = parseHistory(response);

        if (allData == null || allData.isEmpty()) {
            System.out.println("❌ 暂无数据。");
            return;
        }

        // 基础统计
        Map<String, String> ipLookup = loadIpMappings();
        Map<String, Integer> ipRankMap = new HashMap<>();
        Map<String, Integer> timeDistMap = new TreeMap<>();
        int active = 0, invalid = 0;
        SimpleDateFormat daySdf = new SimpleDateFormat("MM-dd");

        for (TraceGuard.Struct0 row : allData) {
            if ("Active".equals(row.status)) active++; else invalid++;
            String name = ipLookup.getOrDefault(row.ipHash, row.ipHash.substring(0, 8) + "...");
            ipRankMap.put(name, ipRankMap.getOrDefault(name, Integer.valueOf(0)) + 1);

// 针对 timeDistMap 这一长串，建议分两步写，既清晰又防错：
            String dayKey = daySdf.format(new Date(row.timestamp.longValue()));
            timeDistMap.put(dayKey, timeDistMap.getOrDefault(dayKey, Integer.valueOf(0)) + 1);
        }

        // 性能指标计算
        double avgLatMs = (totalProcessed.get() == 0) ? 0 :
                (double) totalLatency.get() / totalProcessed.get() / 1_000_000.0;

        double tps = 0;
        if (totalProcessed.get() > 0) {
            tps = (double) totalProcessed.get() / ((System.currentTimeMillis() - batchStartTime) / 1000.0);
        }

        System.out.println("\n======= ⚡ 系统性能实时简报 =======");
        // 使用 %.4f 保留四位小数，展示微秒级的差异，更具科学性
        System.out.println("平均响应延迟: " + String.format("%.4f", avgLatMs) + " ms/op");
        System.out.println("系统处理吞吐: " + String.format("%.2f", tps) + " tps");
        System.out.println("链上存证总规模: " + allData.size() + " 条");

        generateHtmlDashboard(timeDistMap, ipRankMap, allData.size(), active, invalid, avgLatMs, tps);
    }

    private static void generateHtmlDashboard(Map<String, Integer> timeData, Map<String, Integer> ipData, int total, int active, int invalid, double latency, double tps) {
        try {
            String htmlTemplate = "<!DOCTYPE html><html><head><meta charset='utf-8'><title>TraceGuard 审计看板</title>"
                    + "<script src='https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js'></script>"
                    + "<style>body{background:#f0f2f5; font-family:sans-serif; padding:20px;} .card-box{display:flex; justify-content:center; gap:20px;} .card{background:#fff; padding:20px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.08); min-width:200px; text-align:center;} .chart{width:800px; height:400px; margin:30px auto; background:#fff; padding:20px; border-radius:12px; shadow:0 4px 12px rgba(0,0,0,0.08);}</style></head>"
                    + "<body><h1 style='text-align:center;'>🛡️ TraceGuard 系统性能与存证看板</h1>"
                    + "<div class='card-box'>"
                    + "  <div class='card'><h2 style='color:#5470c6;'>" + String.format("%.2f", tps) + "</h2><p>当前 TPS</p></div>"
                    + "  <div class='card'><h2 style='color:#91cc75;'>" + String.format("%.0f", latency) + "ms</h2><p>平均延迟</p></div>"
                    + "  <div class='card'><h2 style='color:#fac858;'>" + total + "</h2><p>累计存证</p></div>"
                    + "  <div class='card'><h2 style='color:#ee6666;'>" + invalid + "</h2><p>已撤销记录</p></div>"
                    + "</div>"
                    + "<div id='timeChart' class='chart'></div>"
                    + "<div id='ipChart' class='chart'></div>"
                    + "<script>"
                    + "echarts.init(document.getElementById('timeChart')).setOption({title:{text:'攻击趋势分析'},xAxis:{data:" + new Gson().toJson(timeData.keySet()) + "},yAxis:{},series:[{data:" + new Gson().toJson(timeData.values()) + ",type:'line',smooth:true,areaStyle:{},itemStyle:{color:'#5470c6'}}]});"
                    + "echarts.init(document.getElementById('ipChart')).setOption({title:{text:'攻击源Top排行 (明文)'},series:[{type:'pie',radius:'50%',data:" + formatPieData(ipData) + "}]});"
                    + "</script></body></html>";
            try (FileWriter writer = new FileWriter("dashboard.html")) { writer.write(htmlTemplate); }
            System.out.println("\n✨ 系统看板已生成: " + new File("dashboard.html").getAbsolutePath());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- 辅助方法 ---
    private static synchronized void saveIpMapping(String hash, String ip) {
        try {
            Properties prop = new Properties();
            File file = new File(IP_MAP_FILE);
            if (file.exists()) { try (FileInputStream fis = new FileInputStream(file)) { prop.load(fis); } }
            prop.setProperty(hash, ip);
            try (FileOutputStream fos = new FileOutputStream(file)) { prop.store(fos, null); }
        } catch (Exception ignored) {}
    }

    private static Map<String, String> loadIpMappings() {
        Map<String, String> map = new HashMap<>();
        try {
            Properties prop = new Properties();
            File file = new File(IP_MAP_FILE);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) { prop.load(fis); }
                for (String key : prop.stringPropertyNames()) map.put(key, prop.getProperty(key));
            }
        } catch (Exception ignored) {}
        return map;
    }

    private static String formatPieData(Map<String, Integer> data) {
        List<Map<String, Object>> list = new ArrayList<>();
        data.forEach((k, v) -> { Map<String, Object> m = new HashMap<>(); m.put("name", k); m.put("value", v); list.add(m); });
        return new Gson().toJson(list);
    }

    private static List<TraceGuard.Struct0> parseHistory(Object response) {
        if (response instanceof DynamicArray) return (List<TraceGuard.Struct0>) ((DynamicArray) response).getValue();
        else if (response instanceof List) return (List<TraceGuard.Struct0>) response;
        return null;
    }

    private static void displayHistory(List<TraceGuard.Struct0> history) {
        if (history == null || history.isEmpty()) System.out.println("❌ 未找到记录。");
        else {
            System.out.println("✅ 命中存证! 发现 " + history.size() + " 条记录：");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (TraceGuard.Struct0 row : history) {
                System.out.println("-------------------------------------------");
                System.out.println("[存证 ID]  : " + row.id);
                System.out.println("[存证状态] : " + (row.status.equals("Active") ? "🟢 有效" : "🔴 已撤销"));
                System.out.println("[检测置信度]: " + row.confidence + "%");
                System.out.println("[存证时间] : " + sdf.format(new Date(row.timestamp.longValue())));
            }
        }
    }

    private static long parseDate(String dateStr, long defaultVal) {
        if (dateStr == null || dateStr.trim().isEmpty()) return (defaultVal == Long.MAX_VALUE) ? 253402214400000L : 0;
        try { return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr).getTime(); } catch (Exception e) { return 0; }
    }

    /**
     * 【核心功能】主动压测函数
     * 不读取文件，直接在内存中生成指定数量的证据并全速投递上链
     */
    private static void runStressTest(TraceGuard traceGuard, int amount) {
        System.out.println("\n🔥 启动级压测: 目标 [" + amount + "] 笔交易");
        System.out.println("⏳ 正在预装载测试数据至内存...");

        // 1. 预生成测试数据，防止计算哈希占用测试时间
        List<String[]> mockDataList = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String flowHash = "bench_" + UUID.randomUUID().toString().substring(0, 8);
            String ipHash = "test_ip_" + i;
            mockDataList.add(new String[]{flowHash, ipHash});
        }

        // 2. 初始化计时与计数
        AtomicInteger finishedCount = new AtomicInteger(0);

        // 关键点：重置全局统计变量，确保看板数据准确
        totalProcessed.set(0);
        totalLatency.set(0);

        // 记录整批任务的开始时间（用于计算 TPS）
        batchStartTime = System.currentTimeMillis();

        System.out.println("🚀 全速投递中...");

        for (int i = 0; i < amount; i++) {
            String[] data = mockDataList.get(i);

            // 【修改点 1】：在发送交易前，记录当前这一笔交易的纳秒起始点
            final long txStartNano = System.nanoTime();

            traceGuard.uploadEvidence(
                    data[0], data[1], "BENCHMARK-V2.0", BigInteger.valueOf(99),
                    new org.fisco.bcos.sdk.model.callback.TransactionCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                            // 【修改点 2】：计算这一笔交易的纳秒耗时并累加到全局变量
                            long latencyNano = System.nanoTime() - txStartNano;
                            totalLatency.addAndGet(latencyNano);

                            int current = finishedCount.incrementAndGet();

                            // 进度条打印逻辑
                            if (amount >= 10 && current % (amount / 10) == 0) {
                                System.out.print(">");
                            }

                            // 3. 最后一笔到达时，输出科学测量报告
                            if (current == amount) {
                                // 使用总计时器计算 TPS
                                long endTime = System.currentTimeMillis();
                                double totalTimeSec = (endTime - batchStartTime) / 1000.0;
                                double tps = amount / totalTimeSec;

                                // 【修改点 3】：计算平均延迟（纳秒转毫秒）
                                double avgLatMs = ((double) totalLatency.get() / amount) / 1_000_000.0;

                                System.out.println("\n\n✅ 压测任务已全部确认完毕！");
                                System.out.println("------------------------------------------");
                                System.out.println("📈 测试档位 : " + amount + " 笔");
                                System.out.println("⏱️ 消耗时长 : " + String.format("%.2f", totalTimeSec) + " 秒");
                                System.out.println("🚀 科学 TPS  : " + String.format("%.2f", tps) + " tps");
                                System.out.println("⏱️ 平均延迟 : " + String.format("%.4f", avgLatMs) + " ms/op"); // 新增科学指标
                                System.out.println("------------------------------------------");

                                // 更新全局变量，确保功能5的看板能拿到最新数值
                                totalProcessed.set(amount);
                            }
                        }
                    }
            );
        }
        System.out.println("📢 所有交易已投递，正在等待区块链共识确认...");
    }


    private static String hashIp(String ip, String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((ip + salt).getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) { String hex = Integer.toHexString(0xff & b); if (hex.length() == 1) hexString.append('0'); hexString.append(hex); }
        return hexString.toString();
    }

    /**
     * 【核心新增】全维度性能测试引擎
     * @param mode   1: 改(update), 2: 删(revoke), 3: 查(query)
     * @param amount 测试样本量
     */
    private static void runFullBenchmark(TraceGuard traceGuard, int mode, int amount) {
        String modeName = (mode == 1 ? "改/修正" : (mode == 2 ? "删/撤销" : "查/检索"));
        System.out.println("\n🏋️ 启动专项压测 | 模式: " + modeName + " | 规模: " + amount);

        AtomicInteger finished = new AtomicInteger(0);
        totalLatency.set(0); // 重置纳秒耗时统计
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < amount; i++) {
            final int index = i; // 模拟操作不同的 ID
            final long txStartNano = System.nanoTime();

            uploadPool.execute(() -> {
                try {
                    if (mode == 1) {
                        // 压测：改 (修正 0-9 号存证的置信度为 80)
                        traceGuard.updateConfidence(BigInteger.valueOf(index % 10), BigInteger.valueOf(80),
                                new org.fisco.bcos.sdk.model.callback.TransactionCallback() {
                                    @Override public void onResponse(TransactionReceipt r) {
                                        totalLatency.addAndGet(System.nanoTime() - txStartNano);
                                        handleBenchmarkEnd(finished, amount, startTime);
                                    }
                                });
                    }
                    else if (mode == 2) {
                        // 压测：删 (撤销 0-9 号存证)
                        traceGuard.revokeEvidence(BigInteger.valueOf(index % 10),
                                new org.fisco.bcos.sdk.model.callback.TransactionCallback() {
                                    @Override public void onResponse(TransactionReceipt r) {
                                        totalLatency.addAndGet(System.nanoTime() - txStartNano);
                                        handleBenchmarkEnd(finished, amount, startTime);
                                    }
                                });
                    }
                    else if (mode == 3) {
                        // 压测：查 (不消耗 Gas，纯性能测试)
                        // 模拟查询一个已知的 IP 哈希
                        traceGuard.getHistoryByIp("d1867960425600ff4a6931fbd9028c28a4866c242a8555ae9c5f4dc20cea6b67");
                        totalLatency.addAndGet(System.nanoTime() - txStartNano);
                        handleBenchmarkEnd(finished, amount, startTime);
                    }
                } catch (Exception e) {
                    finished.incrementAndGet();
                }
            });
        }
    }

    /**
     * 压测结束检查点
     */
    private static void handleBenchmarkEnd(AtomicInteger finished, int total, long start) {
        if (finished.incrementAndGet() == total) {
            long endTime = System.currentTimeMillis();
            double totalTimeSec = (endTime - start) / 1000.0;
            double tps = total / totalTimeSec;
            double avgLatMs = ((double) totalLatency.get() / total) / 1_000_000.0;

            System.out.println("\n🏁 " + total + " 笔压测任务已全部执行完毕！");
            System.out.println("------------------------------------------");
            System.out.println("🚀 科学 TPS  : " + String.format("%.2f", tps) + " tps");
            System.out.println("⏱️ 平均延迟 : " + String.format("%.4f", avgLatMs) + " ms/op");
            System.out.println("------------------------------------------");

            // 同步数据给看板
            totalProcessed.set(total);
            batchStartTime = start;
        }
    }
}
