// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/**
 * @title DigitalIdentity
 * @dev 数字身份合约，管理用户身份注册与验证
 */
contract DigitalIdentity {
    // 合约所有者（平台运营方）
    address public owner;
    
    // 用户身份结构
    struct UserIdentity {
        address userAddress;
        string phoneHash;
        bool isVerified;
        uint256 registeredAt;
        uint256 verifiedAt;
    }
    
    // 事件定义
    event UserRegistered(address indexed user, string phoneHash, uint256 timestamp);
    event UserVerified(address indexed user, address indexed operator, uint256 timestamp);
    event IdentityUpdated(address indexed user, string newPhoneHash, uint256 timestamp);
    
    // 映射关系
    mapping(address => UserIdentity) public identities;
    mapping(string => address) public phoneHashToAddress;
    mapping(address => bool) public operators; // 验证操作员
    
    // 修饰器
    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this function");
        _;
    }
    
    modifier onlyOperator() {
        require(operators[msg.sender] || msg.sender == owner, "Only operator can call this function");
        _;
    }
    
    modifier onlyRegisteredUser() {
        require(identities[msg.sender].userAddress != address(0), "User not registered");
        _;
    }
    
    constructor() {
        owner = msg.sender;
        operators[msg.sender] = true;
    }
    
    /**
     * @dev 用户身份注册
     * @param phoneHash 手机号哈希值
     */
    function registerIdentity(string memory phoneHash) external {
        require(identities[msg.sender].userAddress == address(0), "User already registered");
        require(phoneHashToAddress[phoneHash] == address(0), "Phone number already registered");
        require(bytes(phoneHash).length > 0, "Phone hash cannot be empty");
        
        UserIdentity memory newIdentity = UserIdentity({
            userAddress: msg.sender,
            phoneHash: phoneHash,
            isVerified: false,
            registeredAt: block.timestamp,
            verifiedAt: 0
        });
        
        identities[msg.sender] = newIdentity;
        phoneHashToAddress[phoneHash] = msg.sender;
        
        emit UserRegistered(msg.sender, phoneHash, block.timestamp);
    }
    
    /**
     * @dev 验证用户身份（仅平台运营方可调用）
     * @param userAddress 用户地址
     */
    function verifyIdentity(address userAddress) external onlyOperator {
        UserIdentity storage identity = identities[userAddress];
        require(identity.userAddress != address(0), "User not registered");
        require(!identity.isVerified, "User already verified");
        
        identity.isVerified = true;
        identity.verifiedAt = block.timestamp;
        
        emit UserVerified(userAddress, msg.sender, block.timestamp);
    }
    
    /**
     * @dev 更新用户手机号哈希
     * @param newPhoneHash 新的手机号哈希
     */
    function updatePhoneHash(string memory newPhoneHash) external onlyRegisteredUser {
        require(bytes(newPhoneHash).length > 0, "Phone hash cannot be empty");
        require(phoneHashToAddress[newPhoneHash] == address(0), "Phone number already registered");
        
        UserIdentity storage identity = identities[msg.sender];
        
        // 清除旧映射
        phoneHashToAddress[identity.phoneHash] = address(0);
        
        // 更新新映射
        identity.phoneHash = newPhoneHash;
        phoneHashToAddress[newPhoneHash] = msg.sender;
        
        emit IdentityUpdated(msg.sender, newPhoneHash, block.timestamp);
    }
    
    /**
     * @dev 添加操作员
     * @param operator 操作员地址
     */
    function addOperator(address operator) external onlyOwner {
        operators[operator] = true;
    }
    
    /**
     * @dev 移除操作员
     * @param operator 操作员地址
     */
    function removeOperator(address operator) external onlyOwner {
        operators[operator] = false;
    }
    
    /**
     * @dev 检查用户是否已验证
     * @param userAddress 用户地址
     * @return 是否已验证
     */
    function isUserVerified(address userAddress) external view returns (bool) {
        return identities[userAddress].isVerified;
    }
    
    /**
     * @dev 获取用户身份信息
     * @param userAddress 用户地址
     * @return 用户身份信息
     */
    function getUserIdentity(address userAddress) external view returns (
        address,
        string memory,
        bool,
        uint256,
        uint256
    ) {
        UserIdentity memory identity = identities[userAddress];
        return (
            identity.userAddress,
            identity.phoneHash,
            identity.isVerified,
            identity.registeredAt,
            identity.verifiedAt
        );
    }
    
    /**
     * @dev 通过手机号哈希获取用户地址
     * @param phoneHash 手机号哈希
     * @return 用户地址
     */
    function getAddressByPhoneHash(string memory phoneHash) external view returns (address) {
        return phoneHashToAddress[phoneHash];
    }
}