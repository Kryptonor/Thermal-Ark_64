const fs = require('fs');
const path = require('path');

console.log('🚀 开始部署热力方舟智能合约...\n');

// 部署脚本模板
const deployScript = `
const DigitalIdentity = artifacts.require("DigitalIdentity");
const ThermalToken = artifacts.require("ThermalToken");
const TransactionLedger = artifacts.require("TransactionLedger");
const AutoSettlement = artifacts.require("AutoSettlement");

module.exports = async function(deployer, network, accounts) {
    console.log("🌐 当前网络:", network);
    console.log("👤 部署账户:", accounts[0]);
    
    try {
        // 1. 部署数字身份合约
        console.log("📝 开始部署数字身份合约...");
        await deployer.deploy(DigitalIdentity);
        const identityContract = await DigitalIdentity.deployed();
        console.log("✅ 数字身份合约部署完成，地址:", identityContract.address);
        
        // 2. 部署热力积分合约
        console.log("💰 开始部署热力积分合约...");
        await deployer.deploy(ThermalToken, identityContract.address);
        const tokenContract = await ThermalToken.deployed();
        console.log("✅ 热力积分合约部署完成，地址:", tokenContract.address);
        
        // 3. 部署交易账本合约
        console.log("📊 开始部署交易账本合约...");
        await deployer.deploy(TransactionLedger, tokenContract.address);
        const ledgerContract = await TransactionLedger.deployed();
        console.log("✅ 交易账本合约部署完成，地址:", ledgerContract.address);
        
        // 4. 部署自动结算合约
        console.log("⚡ 开始部署自动结算合约...");
        await deployer.deploy(AutoSettlement, ledgerContract.address);
        const settlementContract = await AutoSettlement.deployed();
        console.log("✅ 自动结算合约部署完成，地址:", settlementContract.address);
        
        // 5. 配置操作员权限
        console.log("🔧 配置合约权限...");
        
        // 为交易账本合约添加操作员权限
        await ledgerContract.addOperator(accounts[0]);
        console.log("✅ 交易账本操作员配置完成");
        
        // 为自动结算合约添加操作员权限
        await settlementContract.addOperator(accounts[0]);
        console.log("✅ 自动结算操作员配置完成");
        
        // 6. 输出部署摘要
        console.log("\n🎉 热力方舟智能合约部署完成！");
        console.log("\n📋 部署摘要:");
        console.log("   - 数字身份合约:", identityContract.address);
        console.log("   - 热力积分合约:", tokenContract.address);
        console.log("   - 交易账本合约:", ledgerContract.address);
        console.log("   - 自动结算合约:", settlementContract.address);
        console.log("   - 部署账户:", accounts[0]);
        
        // 7. 保存部署信息到文件
        const deploymentInfo = {
            network: network,
            deployer: accounts[0],
            contracts: {
                DigitalIdentity: identityContract.address,
                ThermalToken: tokenContract.address,
                TransactionLedger: ledgerContract.address,
                AutoSettlement: settlementContract.address
            },
            timestamp: new Date().toISOString(),
            blockNumber: await web3.eth.getBlockNumber()
        };
        
        const deploymentFile = path.join(__dirname, '..', 'deployments', network + '-deployment.json');
        fs.mkdirSync(path.dirname(deploymentFile), { recursive: true });
        fs.writeFileSync(deploymentFile, JSON.stringify(deploymentInfo, null, 2));
        
        console.log("\n💾 部署信息已保存至:", deploymentFile);
        
    } catch (error) {
        console.error("❌ 部署过程中出现错误:", error);
        throw error;
    }
};
`;

// 创建 migrations 目录
const migrationsDir = path.join(__dirname, '..', 'migrations');
if (!fs.existsSync(migrationsDir)) {
    fs.mkdirSync(migrationsDir, { recursive: true });
}

// 创建部署脚本文件
const deployFile = path.join(migrationsDir, '2_deploy_contracts.js');
fs.writeFileSync(deployFile, deployScript);

console.log('✅ 部署脚本已创建:', deployFile);

// 创建测试部署脚本
const testDeployScript = `
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('🧪 开始测试部署热力方舟智能合约...\\n');

try {
    // 使用 Truffle 在开发网络部署
    console.log('🔧 执行 Truffle 部署命令...');
    execSync('npx truffle migrate --network development', { 
        cwd: path.join(__dirname, '..'),
        stdio: 'inherit'
    });
    
    console.log('\\n✅ 测试部署完成！');
    
    // 检查部署结果
    const deploymentFile = path.join(__dirname, '..', 'deployments', 'development-deployment.json');
    if (fs.existsSync(deploymentFile)) {
        const deploymentInfo = JSON.parse(fs.readFileSync(deploymentFile, 'utf8'));
        console.log('📋 部署信息:');
        console.log(JSON.stringify(deploymentInfo, null, 2));
    }
    
} catch (error) {
    console.error('❌ 测试部署过程中出现错误:');
    console.error(error.message);
    process.exit(1);
}
`;

const testDeployFile = path.join(__dirname, 'test-deploy.js');
fs.writeFileSync(testDeployFile, testDeployScript);

console.log('✅ 测试部署脚本已创建:', testDeployFile);

// 创建部署配置说明
const deployConfig = `
# 热力方舟智能合约部署指南

## 部署网络配置

### 1. 开发网络 (development)
用于本地测试和开发

### 2. FISCO BCOS 网络 (fisco)
用于生产环境部署

## 部署步骤

### 步骤 1: 编译合约
\`\`\`bash
npm run compile
\`\`\`

### 步骤 2: 测试部署
\`\`\`bash
npm run deploy:dev
\`\`\`

### 步骤 3: FISCO BCOS 部署
\`\`\`bash
npm run deploy:fisco
\`\`\`

## 合约部署顺序

1. DigitalIdentity - 数字身份合约
2. ThermalToken - 热力积分合约  
3. TransactionLedger - 交易账本合约
4. AutoSettlement - 自动结算合约

## 注意事项

- 确保网络连接正常
- 确保账户有足够的 Gas 费用
- 部署完成后保存合约地址信息
- 在生产环境部署前进行充分测试
`;

const deployGuideFile = path.join(__dirname, '..', 'DEPLOYMENT.md');
fs.writeFileSync(deployGuideFile, deployConfig);

console.log('✅ 部署指南已创建:', deployGuideFile);
console.log('\n🎉 智能合约部署脚本创建完成！');
console.log('\n📋 可用命令:');
console.log('   - npm run compile    # 编译合约');
console.log('   - npm run deploy:dev # 测试部署');
console.log('   - npm run deploy:fisco # FISCO BCOS 部署');
