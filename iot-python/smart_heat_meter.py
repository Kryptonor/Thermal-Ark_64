#!/usr/bin/env python3
"""
智能热量表模拟器 - Python版本
用于模拟热能交易系统中的物联网设备数据
"""

import json
import random
import time
import requests
import hashlib
import hmac
from datetime import datetime
from typing import Dict, Any


class SmartHeatMeter:
    """智能热量表模拟器"""
    
    def __init__(self, device_id: str, user_id: str, api_base_url: str = "http://localhost:8080"):
        self.device_id = device_id
        self.user_id = user_id
        self.api_base_url = api_base_url
        self.encryption_key = self._generate_encryption_key()
        
    def _generate_encryption_key(self) -> str:
        """生成设备加密密钥"""
        seed = f"{self.device_id}_{self.user_id}_{datetime.now().timestamp()}"
        return hashlib.sha256(seed.encode()).hexdigest()
    
    def generate_data(self) -> Dict[str, Any]:
        """生成模拟数据"""
        # 模拟真实的热能数据
        heat_produced = round(random.uniform(0, 10), 2)  # 产生的热量 (kWh)
        heat_used = round(random.uniform(0, 8), 2)       # 使用的热量 (kWh)
        water_temperature = round(random.uniform(40, 80), 1)  # 水温 (°C)
        flow_rate = round(random.uniform(0.5, 2.0), 2)        # 流量 (m³/h)
        
        # 计算能量平衡
        energy_balance = heat_produced - heat_used
        
        return {
            'device_id': self.device_id,
            'user_id': self.user_id,
            'heat_produced': heat_produced,
            'heat_used': heat_used,
            'energy_balance': energy_balance,
            'water_temperature': water_temperature,
            'flow_rate': flow_rate,
            'pressure': round(random.uniform(0.1, 0.5), 3),  # 压力 (MPa)
            'efficiency': round(random.uniform(85, 95), 1),   # 效率 (%)
            'status': self._get_device_status(),
            'timestamp': datetime.now().isoformat(),
            'location': self._get_device_location()
        }
    
    def _get_device_status(self) -> str:
        """获取设备状态"""
        status_prob = random.random()
        if status_prob < 0.85:
            return "normal"
        elif status_prob < 0.95:
            return "warning"
        else:
            return "critical"
    
    def _get_device_location(self) -> Dict[str, float]:
        """模拟设备地理位置"""
        # 中国主要城市坐标范围
        locations = {
            "beijing": (39.9, 116.4),
            "shanghai": (31.2, 121.5),
            "guangzhou": (23.1, 113.3),
            "shenzhen": (22.5, 114.1),
            "hangzhou": (30.3, 120.2)
        }
        
        city = random.choice(list(locations.keys()))
        base_lat, base_lng = locations[city]
        
        # 添加随机偏移
        lat = base_lat + random.uniform(-0.1, 0.1)
        lng = base_lng + random.uniform(-0.1, 0.1)
        
        return {
            "latitude": round(lat, 6),
            "longitude": round(lng, 6),
            "city": city
        }
    
    def encrypt(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """加密数据"""
        # 创建数据签名
        data_str = json.dumps(data, sort_keys=True)
        signature = hmac.new(
            self.encryption_key.encode(),
            data_str.encode(),
            hashlib.sha256
        ).hexdigest()
        
        return {
            'encrypted_data': data,
            'signature': signature,
            'device_id': self.device_id,
            'timestamp': datetime.now().isoformat(),
            'algorithm': 'HMAC-SHA256'
        }
    
    def upload_data(self) -> bool:
        """加密并上传数据到后端API"""
        try:
            data = self.generate_data()
            encrypted_data = self.encrypt(data)
            
            response = requests.post(
                f'{self.api_base_url}/api/iot/data',
                json=encrypted_data,
                headers={'Content-Type': 'application/json'},
                timeout=10
            )
            
            if response.status_code == 200:
                print(f"✅ 数据上传成功: {self.device_id}")
                return True
            else:
                print(f"❌ 数据上传失败: {self.device_id} - {response.status_code}")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"❌ 网络错误: {self.device_id} - {e}")
            return False
        except Exception as e:
            print(f"❌ 上传异常: {self.device_id} - {e}")
            return False


class IoTSimulatorManager:
    """IoT模拟器管理器"""
    
    def __init__(self, num_devices: int = 10, api_base_url: str = "http://localhost:8080"):
        self.num_devices = num_devices
        self.api_base_url = api_base_url
        self.devices = []
        self._initialize_devices()
    
    def _initialize_devices(self):
        """初始化设备"""
        for i in range(self.num_devices):
            device_id = f"heat-meter-{i+1:03d}"
            user_id = f"user-{(i % 5) + 1:03d}"  # 5个用户共享设备
            
            device = SmartHeatMeter(device_id, user_id, self.api_base_url)
            self.devices.append(device)
        
        print(f"✅ 初始化了 {len(self.devices)} 个智能热量表设备")
    
    def start_simulation(self, interval: int = 30):
        """启动模拟器"""
        print("🚀 启动IoT数据模拟器...")
        print(f"📊 设备数量: {len(self.devices)}")
        print(f"⏰ 数据上传间隔: {interval}秒")
        print("-" * 50)
        
        try:
            while True:
                success_count = 0
                total_count = len(self.devices)
                
                for device in self.devices:
                    if device.upload_data():
                        success_count += 1
                
                success_rate = (success_count / total_count) * 100
                print(f"📈 本轮上传完成: {success_count}/{total_count} ({success_rate:.1f}%)")
                print("-" * 50)
                
                time.sleep(interval)
                
        except KeyboardInterrupt:
            print("\n🛑 模拟器已停止")
        except Exception as e:
            print(f"❌ 模拟器异常: {e}")


def main():
    """主函数"""
    # 配置参数
    NUM_DEVICES = 10
    UPLOAD_INTERVAL = 30  # 秒
    API_BASE_URL = "http://localhost:8080"
    
    # 创建模拟器管理器
    simulator = IoTSimulatorManager(NUM_DEVICES, API_BASE_URL)
    
    # 启动模拟
    simulator.start_simulation(UPLOAD_INTERVAL)


if __name__ == "__main__":
    main()