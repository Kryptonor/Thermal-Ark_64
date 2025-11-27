package com.thermalark.service;

import com.thermalark.entity.User;
import com.thermalark.repository.UserRepository;
import com.thermalark.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 用户注册 - 兼容SystemIntegrationTest.java的调用签名
     */
    public User registerUser(String username, String email, String password) {
        // 调用原始register方法，使用默认值填充其他参数
        return register(username, password, "", email, User.UserRole.USER);
    }

    /**
     * 用户注册
     */
    @Transactional
    public User register(String username, String password, String phone, String email, User.UserRole role) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查手机号是否已存在
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("手机号已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setIsVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * 用户登录验证
     */
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
    
    /**
     * 绑定区块链地址
     */
    @Transactional
    public User bindBlockchainAddress(Long userId, String blockchainAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查区块链地址是否已被绑定
        if (userRepository.existsByBlockchainAddress(blockchainAddress)) {
            throw new RuntimeException("区块链地址已被绑定");
        }
        
        user.setBlockchainAddress(blockchainAddress);
        user.setIsVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * 获取用户信息
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long userId, String email, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null && !phone.equals(user.getPhone())) {
            if (userRepository.existsByPhone(phone)) {
                throw new RuntimeException("手机号已存在");
            }
            user.setPhone(phone);
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 根据角色获取用户
     */
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查是否有钱包记录
        walletRepository.findByUserId(userId).ifPresent(wallet -> {
            throw new RuntimeException("用户存在钱包记录，无法删除");
        });
        
        userRepository.delete(user);
    }
    
    /**
     * 用户认证 - 兼容SystemIntegrationTest.java的调用签名
     */
    public boolean authenticateUser(String username, String password) {
        Optional<User> userOptional = authenticate(username, password);
        return userOptional.isPresent();
    }
    
    /**
     * 重置密码 - 兼容SystemIntegrationTest.java的调用签名
     */
    @Transactional
    public boolean resetPassword(String email, String newPassword) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}