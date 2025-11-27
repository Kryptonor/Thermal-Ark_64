const DigitalIdentity = artifacts.require("DigitalIdentity");

contract("DigitalIdentity", (accounts) => {
    let identityContract;
    const owner = accounts[0];
    const user1 = accounts[1];
    const user2 = accounts[2];
    const operator = accounts[3];
    
    beforeEach(async () => {
        identityContract = await DigitalIdentity.new({ from: owner });
    });
    
    it("应该正确初始化合约所有者", async () => {
        const contractOwner = await identityContract.owner();
        assert.equal(contractOwner, owner, "合约所有者不正确");
    });
    
    it("应该允许用户注册身份", async () => {
        const phoneHash = "0x1234567890abcdef";
        
        await identityContract.registerIdentity(phoneHash, { from: user1 });
        
        const isVerified = await identityContract.isUserVerified(user1);
        const identity = await identityContract.getUserIdentity(user1);
        
        assert.equal(isVerified, false, "新注册用户应该未验证");
        assert.equal(identity[0], user1, "用户地址不正确");
        assert.equal(identity[1], phoneHash, "手机号哈希不正确");
        assert.equal(identity[2], false, "验证状态不正确");
    });
    
    it("应该拒绝重复注册", async () => {
        const phoneHash = "0x1234567890abcdef";
        
        await identityContract.registerIdentity(phoneHash, { from: user1 });
        
        try {
            await identityContract.registerIdentity(phoneHash, { from: user1 });
            assert.fail("应该拒绝重复注册");
        } catch (error) {
            assert(error.message.includes("User already registered"), "错误消息不正确");
        }
    });
    
    it("应该拒绝重复手机号注册", async () => {
        const phoneHash = "0x1234567890abcdef";
        
        await identityContract.registerIdentity(phoneHash, { from: user1 });
        
        try {
            await identityContract.registerIdentity(phoneHash, { from: user2 });
            assert.fail("应该拒绝重复手机号注册");
        } catch (error) {
            assert(error.message.includes("Phone number already registered"), "错误消息不正确");
        }
    });
    
    it("应该允许操作员验证用户身份", async () => {
        const phoneHash = "0x1234567890abcdef";
        
        await identityContract.registerIdentity(phoneHash, { from: user1 });
        await identityContract.addOperator(operator, { from: owner });
        await identityContract.verifyIdentity(user1, { from: operator });
        
        const isVerified = await identityContract.isUserVerified(user1);
        const identity = await identityContract.getUserIdentity(user1);
        
        assert.equal(isVerified, true, "用户应该已验证");
        assert.equal(identity[2], true, "验证状态应该为true");
    });
    
    it("应该拒绝非操作员验证用户", async () => {
        const phoneHash = "0x1234567890abcdef";
        const nonOperator = accounts[4];
        
        await identityContract.registerIdentity(phoneHash, { from: user1 });
        
        try {
            await identityContract.verifyIdentity(user1, { from: nonOperator });
            assert.fail("应该拒绝非操作员验证");
        } catch (error) {
            assert(error.message.includes("Only operator can call this function"), "错误消息不正确");
        }
    });
    
    it("应该允许用户更新手机号哈希", async () => {
        const oldPhoneHash = "0x1234567890abcdef";
        const newPhoneHash = "0xfedcba0987654321";
        
        await identityContract.registerIdentity(oldPhoneHash, { from: user1 });
        await identityContract.updatePhoneHash(newPhoneHash, { from: user1 });
        
        const identity = await identityContract.getUserIdentity(user1);
        const addressByOldHash = await identityContract.getAddressByPhoneHash(oldPhoneHash);
        const addressByNewHash = await identityContract.getAddressByPhoneHash(newPhoneHash);
        
        assert.equal(identity[1], newPhoneHash, "手机号哈希未更新");
        assert.equal(addressByOldHash, "0x0000000000000000000000000000000000000000", "旧哈希映射应该被清除");
        assert.equal(addressByNewHash, user1, "新哈希映射不正确");
    });
    
    it("应该通过手机号哈希查找用户地址", async () => {
        const phoneHash = "0x1234567890abcdef";
        
        await identityContract.registerIdentity(phoneHash, { from: user1 });
        
        const address = await identityContract.getAddressByPhoneHash(phoneHash);
        
        assert.equal(address, user1, "通过手机号哈希查找的用户地址不正确");
    });
    
    it("应该正确管理操作员权限", async () => {
        await identityContract.addOperator(operator, { from: owner });
        
        let isOperator = await identityContract.operators(operator);
        assert.equal(isOperator, true, "操作员应该被添加");
        
        await identityContract.removeOperator(operator, { from: owner });
        
        isOperator = await identityContract.operators(operator);
        assert.equal(isOperator, false, "操作员应该被移除");
    });
});