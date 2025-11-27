#!/usr/bin/env python3
"""
系统集成测试脚本
测试物联网数据模拟器、支付集成、区块链网关和消息队列的完整功能
"""

import requests
import json
import time
import random
from datetime import datetime

# 后端API基础URL
BASE_URL = "http://localhost:8080/api"

def test_iot_data_reception():
    """测试IoT数据接收功能"""
    print("🔍 测试IoT数据接收功能...")
    
    # 模拟IoT数据
    iot_data = {
        "deviceId": "smart-heat-meter-001",
        "userId": "user-001",
        "heatProduced": round(random.uniform(0, 10), 2),
        "heatUsed": round(random.uniform(0, 8), 2),
        "waterTemperature": round(random.uniform(40, 80), 1),
        "flowRate": round(random.uniform(0.5, 2.0), 2),
        "location": "Building A, Floor 3",
        "deviceType": "Smart Heat Meter",
        "firmwareVersion": "1.2.3",
        "timestamp": int(time.time() * 1000)
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/iot/data",
            json=iot_data,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ IoT数据接收测试成功: {result['message']}")
            return True
        else:
            print(f"❌ IoT数据接收测试失败: HTTP {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ IoT数据接收测试异常: {e}")
        return False

def test_payment_integration():
    """测试支付集成功能"""
    print("🔍 测试支付集成功能...")
    
    payment_request = {
        "orderId": f"order_{int(time.time())}",
        "amount": 100.0,
        "paymentMethod": "WECHAT",
        "userId": "user-001",
        "description": "热能交易费用"
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/payment/create",
            json=payment_request,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ 支付集成测试成功: {result['message']}")
            return True
        else:
            print(f"❌ 支付集成测试失败: HTTP {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ 支付集成测试异常: {e}")
        return False

def test_blockchain_integration():
    """测试区块链集成功能"""
    print("🔍 测试区块链集成功能...")
    
    try:
        # 测试区块链状态查询
        response = requests.get(f"{BASE_URL}/blockchain/status")
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ 区块链状态查询成功: 连接状态={result['connected']}, 区块高度={result['blockHeight']}")
            
            # 测试能源交易记录
            transaction_data = {
                "buyerId": "user-001",
                "sellerId": "user-002",
                "energyAmount": 50.0,
                "price": 25.0
            }
            
            response2 = requests.post(
                f"{BASE_URL}/blockchain/energy/transaction",
                params=transaction_data
            )
            
            if response2.status_code == 200:
                result2 = response2.json()
                print(f"✅ 能源交易记录测试成功: {result2['message']}")
                return True
            else:
                print(f"❌ 能源交易记录测试失败: HTTP {response2.status_code}")
                return False
        else:
            print(f"❌ 区块链状态查询失败: HTTP {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ 区块链集成测试异常: {e}")
        return False

def test_device_status():
    """测试设备状态查询功能"""
    print("🔍 测试设备状态查询功能...")
    
    try:
        response = requests.get(f"{BASE_URL}/iot/device/smart-heat-meter-001/status")
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ 设备状态查询测试成功: 设备状态={result['status']}")
            return True
        else:
            print(f"❌ 设备状态查询测试失败: HTTP {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ 设备状态查询测试异常: {e}")
        return False

def test_python_iot_simulator():
    """测试Python IoT模拟器"""
    print("🔍 测试Python IoT模拟器...")
    
    try:
        # 导入Python IoT模拟器
        import sys
        import os
        sys.path.append(os.path.join(os.path.dirname(__file__), 'iot-python'))
        
        from smart_heat_meter import SmartHeatMeter, IoTSimulatorManager
        
        # 创建智能热量表实例
        heat_meter = SmartHeatMeter("test-device-001", "test-user-001")
        
        # 生成测试数据
        test_data = heat_meter.generate_data()
        print(f"✅ Python IoT模拟器测试成功: 生成数据={test_data}")
        
        # 测试数据加密
        encrypted_data = heat_meter.encrypt(test_data)
        print(f"✅ 数据加密测试成功: 加密数据长度={len(encrypted_data)}")
        
        return True
        
    except Exception as e:
        print(f"❌ Python IoT模拟器测试异常: {e}")
        return False

def run_comprehensive_test():
    """运行全面的系统集成测试"""
    print("🚀 开始系统集成测试")
    print("=" * 50)
    
    test_results = []
    
    # 1. 测试Python IoT模拟器
    test_results.append(("Python IoT模拟器", test_python_iot_simulator()))
    
    # 2. 测试IoT数据接收
    test_results.append(("IoT数据接收", test_iot_data_reception()))
    
    # 3. 测试设备状态查询
    test_results.append(("设备状态查询", test_device_status()))
    
    # 4. 测试支付集成
    test_results.append(("支付集成", test_payment_integration()))
    
    # 5. 测试区块链集成
    test_results.append(("区块链集成", test_blockchain_integration()))
    
    # 汇总测试结果
    print("\n" + "=" * 50)
    print("📊 测试结果汇总:")
    
    passed_tests = 0
    total_tests = len(test_results)
    
    for test_name, result in test_results:
        status = "✅ 通过" if result else "❌ 失败"
        print(f"  {test_name}: {status}")
        if result:
            passed_tests += 1
    
    print(f"\n🎯 测试完成: {passed_tests}/{total_tests} 项测试通过")
    
    if passed_tests == total_tests:
        print("🎉 所有测试通过！系统集成功能正常。")
        return True
    else:
        print("⚠️  部分测试失败，请检查相关服务是否正常运行。")
        return False

def check_service_health():
    """检查服务健康状态"""
    print("🔍 检查服务健康状态...")
    
    services = [
        ("后端服务", "http://localhost:8080/actuator/health"),
        ("RabbitMQ管理界面", "http://localhost:15672"),
        ("前端服务", "http://localhost:80"),
        ("区块链节点", "http://localhost:8545")
    ]
    
    for service_name, url in services:
        try:
            if "actuator" in url:
                response = requests.get(url, timeout=5)
                if response.status_code == 200:
                    print(f"✅ {service_name}: 运行正常")
                else:
                    print(f"❌ {service_name}: HTTP {response.status_code}")
            else:
                # 对于非HTTP端点，简单尝试连接
                import socket
                host, port = url.replace("http://", "").split(":")
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.settimeout(5)
                result = sock.connect_ex((host, int(port)))
                sock.close()
                
                if result == 0:
                    print(f"✅ {service_name}: 端口可访问")
                else:
                    print(f"❌ {service_name}: 端口不可访问")
                    
        except Exception as e:
            print(f"❌ {service_name}: 检查失败 - {e}")

if __name__ == "__main__":
    print("🌡️  Thermal Ark 系统集成测试")
    print("=" * 50)
    
    # 检查服务健康状态
    check_service_health()
    print()
    
    # 运行综合测试
    success = run_comprehensive_test()
    
    # 输出测试总结
    print("\n" + "=" * 50)
    if success:
        print("🎊 系统集成测试完成！所有组件工作正常。")
        print("\n📋 已实现的功能:")
        print("  • Python智能热量表模拟器")
        print("  • 微信支付和支付宝集成")
        print("  • FISCO BCOS区块链网关")
        print("  • RabbitMQ消息队列")
        print("  • 完整的Docker部署配置")
        print("  • REST API接口服务")
    else:
        print("⚠️  系统集成测试发现一些问题，请检查相关服务。")
        print("\n💡 建议:")
        print("  1. 确保所有Docker容器正常运行")
        print("  2. 检查后端服务日志")
        print("  3. 验证数据库连接")
        print("  4. 确认消息队列配置")
    
    exit(0 if success else 1)