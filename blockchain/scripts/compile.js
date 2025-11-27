const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('🚀 开始编译热力方舟智能合约...\n');

// 检查合约目录是否存在
const contractsDir = path.join(__dirname, '..', 'contracts');
if (!fs.existsSync(contractsDir)) {
    console.error('❌ 合约目录不存在:', contractsDir);
    process.exit(1);
}

// 检查编译输出目录
const buildDir = path.join(__dirname, '..', 'build');
if (!fs.existsSync(buildDir)) {
    fs.mkdirSync(buildDir, { recursive: true });
    console.log('✅ 创建编译输出目录:', buildDir);
}

// 合约文件列表
const contractFiles = [
    'DigitalIdentity.sol',
    'ThermalToken.sol',
    'TransactionLedger.sol',
    'AutoSettlement.sol'
];

// 检查所有合约文件是否存在
const missingContracts = [];
for (const file of contractFiles) {
    const filePath = path.join(contractsDir, file);
    if (!fs.existsSync(filePath)) {
        missingContracts.push(file);
    }
}

if (missingContracts.length > 0) {
    console.error('❌ 以下合约文件缺失:', missingContracts.join(', '));
    process.exit(1);
}

console.log('📋 合约文件检查完成，开始编译...\n');

try {
    // 使用 Truffle 编译合约
    console.log('🔧 执行 Truffle 编译命令...');
    execSync('npx truffle compile', { 
        cwd: path.join(__dirname, '..'),
        stdio: 'inherit'
    });
    
    console.log('\n✅ 智能合约编译完成！');
    
    // 检查编译结果
    const artifactsDir = path.join(__dirname, '..', 'build', 'contracts');
    if (fs.existsSync(artifactsDir)) {
        const artifacts = fs.readdirSync(artifactsDir);
        console.log('📄 生成的合约文件:');
        artifacts.forEach(artifact => {
            if (artifact.endsWith('.json')) {
                console.log(`   - ${artifact}`);
            }
        });
    }
    
    console.log('\n🎉 所有智能合约编译成功！');
    
} catch (error) {
    console.error('❌ 编译过程中出现错误:');
    console.error(error.message);
    process.exit(1);
}