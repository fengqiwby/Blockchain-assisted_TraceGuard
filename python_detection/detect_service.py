import pandas as pd
import numpy as np
import tensorflow as tf
import joblib
import json
import hashlib
import time
import os
import ipaddress  # 用于合法性校验

# ================= 配置区 =================
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_PATH = r"C:\Users\15363\PycharmProjects\PythonProject4\data_preparation\Mixed_DDoS2019_Dataset.csv"
MODEL_PATH = os.path.join(BASE_DIR, "models", "ddos_cnn_v1.h5")
SCALER_PATH = os.path.join(BASE_DIR, "models", "scaler_v1.pkl")
META_PATH = os.path.join(BASE_DIR, "models", "model_meta.json")

EXCHANGE_DIR = os.path.join(BASE_DIR, "exchange_folder")
ARCHIVE_DIR = os.path.join(BASE_DIR, "archived_evidence")

# 定义特征的合理取值范围 (防错逻辑)
RANGES = {
    "packets": (1, 1000000),  # 转发包数：1 到 100万
    "duration": (0, 120000000),  # 持续时间：0 到 120秒(微秒表示)
    "iat": (0, 120000000),  # 包间隔：0 到 120秒
    "std": (0, 10000)  # 长度标准差：0 到 1万
}

for d in [EXCHANGE_DIR, ARCHIVE_DIR]:
    if not os.path.exists(d): os.makedirs(d)

ALERT_THRESHOLD = 0.5


# ================= 辅助校验函数 =================

def get_valid_ip():
    """验证输入的 IP 是否合法"""
    while True:
        ip = input("1. 来源 IP (例如 10.0.0.1, 输入 'q' 返回): ").strip()
        if ip.lower() == 'q': return None
        try:
            ipaddress.ip_address(ip)  # 校验格式
            return ip
        except ValueError:
            print(f"❌ 错误: '{ip}' 不是有效的 IP 地址格式，请重新输入。")


def get_valid_num(prompt, min_val, max_val):
    """验证输入的数值是否在合理范围内"""
    while True:
        try:
            val = float(input(prompt))
            if min_val <= val <= max_val:
                return val
            else:
                print(f"⚠️ 警告: 数值超出合理范围 [{min_val} - {max_val}]，请重新确认输入。")
        except ValueError:
            print("❌ 错误: 请输入有效的数字。")


def hash_sensitive_info(info_str):
    salt = "My_Project_Salt_2026"
    return hashlib.sha256((info_str + salt).encode()).hexdigest()


# ================= 核心检测引擎 =================

print("正在初始化深度学习检测引擎...")
model = tf.keras.models.load_model(MODEL_PATH)
scaler = joblib.load(SCALER_PATH)
with open(META_PATH, 'r') as f:
    model_meta = json.load(f)


def detect_and_save(features, src_ip, flow_id=None):
    """
    features 传入的是原始数值列表: [Total Fwd Pkts, Flow Duration, IAT Mean, Pkt Len Std]
    """
    # --- 【关键修改点：特征预处理】 ---
    # 复制一份数据，避免修改原始特征影响后续 JSON 记录
    processed_feats = np.array(features, dtype=float).copy()

    # 对第 2, 3, 4 个特征执行同样的 Log 变换 (索引 1, 2, 3)
    # 注意：Total Fwd Packets (索引0) 在训练代码里没做 Log，这里也不做
    processed_feats[1] = np.log1p(processed_feats[1])
    processed_feats[2] = np.log1p(processed_feats[2])
    processed_feats[3] = np.log1p(processed_feats[3])

    # 执行标准化 (使用新生成的 scaler_v1.pkl)
    X_scaled = scaler.transform([processed_feats])
    X_input = X_scaled.reshape(1, 4, 1)

    # --- 【推理】 ---
    anomaly_score = float(model.predict(X_input, verbose=0)[0][0])

    # --- 【判定逻辑】 ---
    # 因为模型准确率变高了，我们可以把报警阈值调高，减少误报
    # 建议设为 0.8 或 0.9
    ALERT_THRESHOLD = 0.9

    status = "🔴 异常" if anomaly_score > ALERT_THRESHOLD else "🟢 正常"

    # 下面的逻辑保持不变 (生成 JSON 等)...
    if not flow_id:
        flow_id = hashlib.md5((str(features) + str(time.time())).encode()).hexdigest()

    print(f"\n[实时监测] IP: {src_ip} | 攻击概率: {anomaly_score:.4f} | 状态: {status}")

    if anomaly_score > ALERT_THRESHOLD:
        file_name = f"evidence_{flow_id}.json"
        if os.path.exists(os.path.join(EXCHANGE_DIR, file_name)) or \
                os.path.exists(os.path.join(ARCHIVE_DIR, file_name)):
            print(f"⚠️ 跳过：该证据 ID {flow_id[:8]} 已经在链上或归档，不重复生成。")
            return

        evidence = {
            "feature_hash": flow_id,
            "ip_hash": hash_sensitive_info(src_ip),
            "real_ip": src_ip,
            "model_version": model_meta['model_version'],
            "confidence": int(anomaly_score * 100),
            "raw_features": features
        }

        with open(os.path.join(EXCHANGE_DIR, file_name), "w") as f:
            json.dump(evidence, f, indent=4)
        print(f"✨ 证据已生成并存入交换区: {file_name}")


# ================= 交互模式 =================

def batch_mode():
    print(f"\n--- 批量检测模式 (CSV) ---")
    if not os.path.exists(DATASET_PATH):
        print(f"❌ 错误: 找不到数据集文件 {DATASET_PATH}")
        return

    try:
        df = pd.read_csv(DATASET_PATH)
        max_rows = len(df)
        print(f"当前数据集总行数: {max_rows}")

        while True:
            try:
                line_input = input(f"请输入要检测的行数 (1-{max_rows}, 输入 'q' 返回): ")
                if line_input.lower() == 'q': return
                num_rows = int(line_input)

                if 1 <= num_rows <= max_rows:
                    break
                else:
                    print(f"❌ 错误: 输入超出范围，当前数据集最大支持 {max_rows} 行。")
            except ValueError:
                print("❌ 错误: 请输入有效的整数。")

        test_data = df.head(num_rows)
        for index, row in test_data.iterrows():
            features = [row['Total Fwd Packets'], row['Flow Duration'],
                        row['Flow IAT Mean'], row['Packet Length Std']]
            # 使用 CSV 原有的 Hash 保证上链唯一性
            flow_id = row['Flow_Hash']
            detect_and_save(features, f"192.168.1.{index + 10}", flow_id)

    except Exception as e:
        print(f"❌ 处理过程中出现意外错误: {e}")


def manual_mode():
    print(f"\n--- 手动模拟模式 ---")
    while True:
        print("\n" + "-" * 40)
        ip = get_valid_ip()
        if not ip: break  # 用户输入 q

        p1 = get_valid_num("2. 转发包总数 Total Fwd Packets: ", RANGES["packets"][0], RANGES["packets"][1])
        p2 = get_valid_num("3. 流持续时间 Flow Duration: ", RANGES["duration"][0], RANGES["duration"][1])
        p3 = get_valid_num("4. 平均包间隔 Flow IAT Mean: ", RANGES["iat"][0], RANGES["iat"][1])
        p4 = get_valid_num("5. 包长度标准差 Packet Length Std: ", RANGES["std"][0], RANGES["std"][1])

        detect_and_save([p1, p2, p3, p4], ip)


# ================= 主入口 =================

if __name__ == "__main__":
    while True:
        print("\n" + "═" * 40)
        print("🛡️ TraceGuard DDoS 实时检测终端")
        print("1. 自动化批量检测 (读取数据集 CSV)")
        print("2. 手动模拟检测 (单条录入特征)")
        print("0. 退出程序")
        print("═" * 40)

        choice = input("请选择操作: ").strip()

        if choice == '1':
            batch_mode()
        elif choice == '2':
            manual_mode()
        elif choice == '0':
            print("👋 程序已安全退出。")
            break
        else:
            print("❌ 无效选择，请输入 0, 1 或 2。")