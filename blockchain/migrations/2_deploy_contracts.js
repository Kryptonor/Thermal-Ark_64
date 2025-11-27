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
        
    } catch (error) {
        console.error("❌ 部署过程中出现错误:", error);
        throw error;
    }
};