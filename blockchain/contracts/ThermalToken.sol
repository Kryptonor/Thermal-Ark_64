// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./DigitalIdentity.sol";

/**
 * @title ThermalToken
 * @dev 热力积分合约，基于ERC20标准的代币实现
 */
contract ThermalToken {
    // ERC20标准字段
    string public name = "Thermal Ark Token";
    string public symbol = "TAT";
    uint8 public decimals = 2;
    uint256 public totalSupply;
    
    // 合约所有者（平台运营方）
    address public owner;
    
    // 数字身份合约引用
    DigitalIdentity public identityContract;
    
    // 事件定义
    event Transfer(address indexed from, address indexed to, uint256 value);
    event Approval(address indexed owner, address indexed spender, uint256 value);
    event Mint(address indexed to, uint256 value);
    event Burn(address indexed from, uint256 value);
    
    // 余额映射
    mapping(address => uint256) public balanceOf;
    mapping(address => mapping(address => uint256)) public allowance;
    
    // 修饰器
    modifier onlyOwner() {
        require(msg.sender == owner, "Only owner can call this function");
        _;
    }
    
    modifier onlyVerifiedUser(address user) {
        require(identityContract.isUserVerified(user), "User not verified");
        _;
    }
    
    constructor(address identityContractAddress) {
        owner = msg.sender;
        identityContract = DigitalIdentity(identityContractAddress);
        totalSupply = 0;
    }
    
    /**
     * @dev 铸造代币（仅平台运营方可调用）
     * @param to 接收地址
     * @param value 铸造数量（单位：分，1积分=100分）
     */
    function mint(address to, uint256 value) external onlyOwner onlyVerifiedUser(to) {
        require(to != address(0), "Mint to zero address");
        require(value > 0, "Mint value must be positive");
        
        totalSupply += value;
        balanceOf[to] += value;
        
        emit Mint(to, value);
        emit Transfer(address(0), to, value);
    }
    
    /**
     * @dev 销毁代币（仅平台运营方可调用）
     * @param from 销毁地址
     * @param value 销毁数量
     */
    function burn(address from, uint256 value) external onlyOwner onlyVerifiedUser(from) {
        require(from != address(0), "Burn from zero address");
        require(balanceOf[from] >= value, "Insufficient balance");
        require(value > 0, "Burn value must be positive");
        
        balanceOf[from] -= value;
        totalSupply -= value;
        
        emit Burn(from, value);
        emit Transfer(from, address(0), value);
    }
    
    /**
     * @dev 转账（要求用户身份验证）
     * @param to 接收地址
     * @param value 转账数量
     */
    function transfer(address to, uint256 value) external onlyVerifiedUser(msg.sender) returns (bool) {
        _transfer(msg.sender, to, value);
        return true;
    }
    
    /**
     * @dev 授权转账
     * @param spender 授权地址
     * @param value 授权数量
     */
    function approve(address spender, uint256 value) external onlyVerifiedUser(msg.sender) returns (bool) {
        allowance[msg.sender][spender] = value;
        emit Approval(msg.sender, spender, value);
        return true;
    }
    
    /**
     * @dev 从授权地址转账
     * @param from 转出地址
     * @param to 接收地址
     * @param value 转账数量
     */
    function transferFrom(address from, address to, uint256 value) external onlyVerifiedUser(from) returns (bool) {
        require(allowance[from][msg.sender] >= value, "Allowance exceeded");
        
        allowance[from][msg.sender] -= value;
        _transfer(from, to, value);
        
        return true;
    }
    
    /**
     * @dev 内部转账函数
     */
    function _transfer(address from, address to, uint256 value) internal {
        require(from != address(0), "Transfer from zero address");
        require(to != address(0), "Transfer to zero address");
        require(balanceOf[from] >= value, "Insufficient balance");
        require(value > 0, "Transfer value must be positive");
        
        balanceOf[from] -= value;
        balanceOf[to] += value;
        
        emit Transfer(from, to, value);
    }
    
    /**
     * @dev 批量转账
     * @param recipients 接收地址数组
     * @param values 转账数量数组
     */
    function batchTransfer(address[] memory recipients, uint256[] memory values) external onlyVerifiedUser(msg.sender) returns (bool) {
        require(recipients.length == values.length, "Arrays length mismatch");
        
        uint256 totalValue = 0;
        for (uint256 i = 0; i < values.length; i++) {
            totalValue += values[i];
        }
        
        require(balanceOf[msg.sender] >= totalValue, "Insufficient balance");
        
        for (uint256 i = 0; i < recipients.length; i++) {
            _transfer(msg.sender, recipients[i], values[i]);
        }
        
        return true;
    }
    
    /**
     * @dev 获取用户余额（以元为单位）
     * @param account 用户地址
     * @return 余额（元）
     */
    function getBalanceInYuan(address account) external view returns (uint256) {
        return balanceOf[account] / 100;
    }
    
    /**
     * @dev 获取用户余额（以分为单位）
     * @param account 用户地址
     * @return 余额（分）
     */
    function getBalanceInFen(address account) external view returns (uint256) {
        return balanceOf[account];
    }
}