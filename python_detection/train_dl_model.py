import pandas as pd
import numpy as np
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Conv1D, Flatten, MaxPooling1D, Dropout, BatchNormalization
from tensorflow.keras.callbacks import EarlyStopping
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import hashlib
import time
import joblib
import os
import json

# ================= 配置区 =================
DATA_PATH = r'C:\Users\15363\PycharmProjects\PythonProject4\.venv\Mixed_DDoS2019_Dataset.csv'
MODEL_VERSION = "CNN-V2.0"
MODEL_SAVE_DIR = "models/"
if not os.path.exists(MODEL_SAVE_DIR): os.makedirs(MODEL_SAVE_DIR)

# ================= 1. 数据准备 =================
print("🚀 正在加载数据并进行高级特征工程...")
df = pd.read_csv(DATA_PATH)

# A. 对数变换 (解决数值波动巨大问题) - 这一步能大幅提升准确率
skewed_cols = ['Flow Duration', 'Flow IAT Mean', 'Packet Length Std']
for col in skewed_cols:
    df[col] = np.log1p(df[col])  # log(x+1)

# B. 提取特征和标签
features = ['Total Fwd Packets', 'Flow Duration', 'Flow IAT Mean', 'Packet Length Std']
X = df[features].values
y = df['Label'].apply(lambda x: 0 if x == 'Benign' else 1).values

# C. 标准化
scaler = StandardScaler()
X = scaler.fit_transform(X)
joblib.dump(scaler, os.path.join(MODEL_SAVE_DIR, "scaler_v1.pkl"))

# D. Reshape 为 3D [样本, 时间步, 特征]
X = X.reshape(X.shape[0], X.shape[1], 1)

# E. 划分数据集
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

# ================= 2. 构建加强版 CNN 模型 =================
print("🏗️ 正在构建深度卷积神经网络...")
model = Sequential([
    # 第一层卷积：提取基础空间特征
    Conv1D(64, kernel_size=3, activation='relu', input_shape=(X.shape[1], 1), padding='same'),
    BatchNormalization(),

    # 第二层卷积：提取更深层特征
    Conv1D(128, kernel_size=3, activation='relu', padding='same'),
    BatchNormalization(),
    MaxPooling1D(pool_size=2),

    Flatten(),
    # 全连接层
    Dense(128, activation='relu'),
    Dropout(0.3),
    Dense(64, activation='relu'),
    Dense(1, activation='sigmoid')  # 二分类
])

model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

# ================= 3. 训练 (带早停机制) =================
print(f"🔥 开始训练模型 {MODEL_VERSION} ...")
# 如果连续 3 轮验证集准确率不提升，则停止训练
early_stop = EarlyStopping(monitor='val_accuracy', patience=3, restore_best_weights=True)

model.fit(
    X_train, y_train,
    epochs=30,  # 增加训练轮数
    batch_size=64,
    validation_split=0.2,
    callbacks=[early_stop],
    verbose=1
)

# ================= 4. 模型评估与保存 =================
loss, acc = model.evaluate(X_test, y_test)
print(f"\n✅ 训练完成! 最终测试准确率: {acc * 100:.2f}%")

# 生成元数据
data_hash = hashlib.sha256(str(df.head(10).values).encode()).hexdigest()
timestamp = time.strftime("%Y-%m-%d %H:%M:%S")

model_filename = os.path.join(MODEL_SAVE_DIR, "ddos_cnn_v1.h5")
model.save(model_filename)

metadata = {
    "model_version": MODEL_VERSION,
    "algorithm": "Enhanced-1D-CNN",
    "train_time": timestamp,
    "data_hash_summary": data_hash,
    "accuracy": round(float(acc), 4),
    "file_path": model_filename
}

with open(os.path.join(MODEL_SAVE_DIR, "model_meta.json"), "w") as f:
    json.dump(metadata, f, indent=4)

print(f"📁 模型及指纹已保存至 {MODEL_SAVE_DIR}")