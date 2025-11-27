const ThermalToken = artifacts.require("ThermalToken");
const DigitalIdentity = artifacts.require("DigitalIdentity");

contract("ThermalToken", (accounts) => {
    let tokenContract;
    let identityContract;
    const owner = accounts[0];
    const user1 = accounts[1];
    const user2 = accounts[2];
    const operator = accounts[3];
    
    beforeEach(async () => {
        // 部署数字身份合约
        identityContract = await DigitalIdentity.new({ from: owner });
        
        // 部署热力积分合约
        tokenContract = await ThermalToken.new(identityContract.address, { from: owner });
        
        // 注册并验证用户
        await identityContract.registerIdentity("hash123", { from: user1 });
        await identityContract.verifyIdentity(user1, { from: owner });
        
        await identityContract.registerIdentity("hash456", { from: user2 });
        await identityContract.verifyIdentity(user2, { from: owner });
    });
    
    describe("代币铸造功能", function() {
        it("应该正确初始化代币参数", async () => {
            const name = await tokenContract.name();
            const symbol = await tokenContract.symbol();
            const decimals = await tokenContract.decimals();
            const totalSupply = await tokenContract.totalSupply();
            
            assert.equal(name, "Thermal Ark Token", "代币名称不正确");
            assert.equal(symbol, "TAT", "代币符号不正确");
            assert.equal(decimals, 2, "小数位数不正确");
            assert.equal(totalSupply, 0, "初始总供应量应为0");
        });
        
        it("应该允许所有者铸造代币", async () => {
            const mintAmount = 10000; // 100.00元
            
            await tokenContract.mint(user1, mintAmount, { from: owner });
            
            const balance = await tokenContract.balanceOf(user1);
            const totalSupply = await tokenContract.totalSupply();
            
            assert.equal(balance.toString(), mintAmount.toString(), "用户余额不正确");
            assert.equal(totalSupply.toString(), mintAmount.toString(), "总供应量不正确");
        });
        
        it("应该拒绝非所有者铸造代币", async () => {
            const mintAmount = 10000;
            
            try {
                await tokenContract.mint(user1, mintAmount, { from: user1 });
                assert.fail("应该拒绝非所有者铸造代币");
            } catch (error) {
                assert(error.message.includes("revert"), "应该拒绝非所有者铸造代币");
            }
        });
        
        it("应该正确记录铸造事件", async () => {
            const mintAmount = 5000;
            
            const result = await tokenContract.mint(user1, mintAmount, { from: owner });
            
            // 检查事件是否被触发
            assert.equal(result.logs.length, 1, "应该触发一个事件");
            assert.equal(result.logs[0].event, "Transfer", "应该触发Transfer事件");
            assert.equal(result.logs[0].args.from, "0x0000000000000000000000000000000000000000", "铸造事件from应为零地址");
            assert.equal(result.logs[0].args.to, user1, "铸造事件to应为用户地址");
            assert.equal(result.logs[0].args.value.toString(), mintAmount.toString(), "铸造金额不正确");
        });
    });
    
    describe("代币转账功能", function() {
        it("应该允许已验证用户之间转账", async () => {
            const mintAmount = 10000;
            const transferAmount = 3000;
            
            await tokenContract.mint(user1, mintAmount, { from: owner });
            await tokenContract.transfer(user2, transferAmount, { from: user1 });
            
            const balance1 = await tokenContract.balanceOf(user1);
            const balance2 = await tokenContract.balanceOf(user2);
            
            assert.equal(balance1.toString(), (mintAmount - transferAmount).toString(), "转出方余额不正确");
            assert.equal(balance2.toString(), transferAmount.toString(), "接收方余额不正确");
        });
        
        it("应该拒绝未验证用户的转账", async () => {
            const unverifiedUser = accounts[4];
            
            try {
                await tokenContract.transfer(user1, 1000, { from: unverifiedUser });
                assert.fail("应该拒绝未验证用户的转账");
            } catch (error) {
                assert(error.message.includes("User not verified"), "错误消息不正确");
            }
        });
        
        it("应该拒绝余额不足的转账", async () => {
            const mintAmount = 1000;
            const transferAmount = 2000;
            
            await tokenContract.mint(user1, mintAmount, { from: owner });
            
            try {
                await tokenContract.transfer(user2, transferAmount, { from: user1 });
                assert.fail("应该拒绝余额不足的转账");
            } catch (error) {
                assert(error.message.includes("revert"), "应该拒绝余额不足的转账");
            }
        });
    });
    
    describe("代币销毁功能", function() {
        it("应该允许所有者销毁代币", async () => {
            const mintAmount = 10000;
            const burnAmount = 5000;
            
            await tokenContract.mint(user1, mintAmount, { from: owner });
            await tokenContract.burn(user1, burnAmount, { from: owner });
            
            const balance = await tokenContract.balanceOf(user1);
            const totalSupply = await tokenContract.totalSupply();
            
            assert.equal(balance.toString(), (mintAmount - burnAmount).toString(), "用户余额不正确");
            assert.equal(totalSupply.toString(), (mintAmount - burnAmount).toString(), "总供应量不正确");
        });
        
        it("应该拒绝销毁超过余额的代币", async () => {
            const mintAmount = 1000;
            const burnAmount = 2000;
            
            await tokenContract.mint(user1, mintAmount, { from: owner });
            
            try {
                await tokenContract.burn(user1, burnAmount, { from: owner });
                assert.fail("应该拒绝销毁超过余额的代币");
            } catch (error) {
                assert(error.message.includes("revert"), "应该拒绝销毁超过余额的代币");
            }
        });
    });
    
    describe("余额计算功能", function() {
        it("应该正确计算以元为单位的余额", async () => {
            const mintAmount = 12345; // 123.45元
            
            await tokenContract.mint(user1, mintAmount, { from: owner });
            
            const balanceInYuan = await tokenContract.getBalanceInYuan(user1);
            
            assert.equal(balanceInYuan.toString(), "123", "以元为单位的余额计算不正确");
        });
        
        it("应该正确计算以分为单位的余额", async () => {
            const mintAmount = 12345; // 123.45元
            
            await tokenContract.mint(user1, mintAmount, { from: owner });
            
            const balanceInFen = await tokenContract.getBalanceInFen(user1);
            
            assert.equal(balanceInFen.toString(), mintAmount.toString(), "以分为单位的余额计算不正确");
        });
    });
    
    describe("能源交易功能", function() {
        it("应该记录能源生产数据", async () => {
            const deviceId = "smart-heat-meter-001";
            const energyAmount = 150.5;
            const timestamp = Math.floor(Date.now() / 1000);
            
            const result = await tokenContract.recordEnergyProduction(
                deviceId, 
                Math.floor(energyAmount * 100), // 转换为整数
                timestamp, 
                { from: owner }
            );
            
            assert.equal(result.logs.length, 1, "应该触发一个事件");
            assert.equal(result.logs[0].event, "EnergyProductionRecorded", "应该触发EnergyProductionRecorded事件");
        });
        
        it("应该执行能源交易", async () => {
            const tradeId = "trade-001";
            const seller = user1;
            const buyer = user2;
            const amount = 100;
            const price = 45.5;
            const timestamp = Math.floor(Date.now() / 1000);
            
            // 先给卖家铸造代币
            await tokenContract.mint(seller, amount * 100, { from: owner });
            
            const result = await tokenContract.executeEnergyTrade(
                tradeId,
                seller,
                buyer,
                amount,
                Math.floor(price * 100), // 转换为整数
                timestamp,
                { from: owner }
            );
            
            assert.equal(result.logs.length, 1, "应该触发一个事件");
            assert.equal(result.logs[0].event, "EnergyTradeExecuted", "应该触发EnergyTradeExecuted事件");
        });
    });
});