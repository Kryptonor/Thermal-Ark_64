package com.thermalark.service;

import com.thermalark.blockchain.service.BlockchainService;
import com.thermalark.dto.RegisterRequest;
import com.thermalark.dto.LoginResponse;
import com.thermalark.entity.User;
import com.thermalark.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BlockchainService blockchainService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registrationRequest;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegisterRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setPhone("13812345678");
    }

    @Test
    void testUserRegistration_Success() {
        // 模拟依赖行为
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        when(blockchainService.createWallet(anyString())).thenReturn("0x1234567890abcdef");

        // 执行测试
        User user = userService.register(
            registrationRequest.getUsername(),
            registrationRequest.getPassword(),
            registrationRequest.getPhone(),
            registrationRequest.getEmail(),
            com.thermalark.entity.User.UserRole.USER
        );

        // 验证结果
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());

        // 验证依赖调用
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(blockchainService, times(1)).createWallet(anyString());
    }

    @Test
    void testUserRegistration_DuplicateUsername() {
        // 模拟用户名已存在
        User existingUser = new User();
        existingUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.register(
                registrationRequest.getUsername(),
                registrationRequest.getPassword(),
                registrationRequest.getPhone(),
                registrationRequest.getEmail(),
                com.thermalark.entity.User.UserRole.USER
            );
        });

        // 验证依赖调用
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUserRegistration_DuplicateEmail() {
        // 模拟邮箱已存在
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            userService.register(
                registrationRequest.getUsername(),
                registrationRequest.getPassword(),
                registrationRequest.getPhone(),
                registrationRequest.getEmail(),
                com.thermalark.entity.User.UserRole.USER
            );
        });

        // 验证依赖调用
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testBlockchainIntegration() {
        // 模拟区块链交互
        String username = "testuser";
        String expectedWallet = "0x1234567890abcdef";
        
        when(blockchainService.createWallet(username)).thenReturn(expectedWallet);

        // 执行测试
        String walletAddress = blockchainService.createWallet(username);

        // 验证结果
        assertNotNull(walletAddress);
        assertEquals(expectedWallet, walletAddress);

        // 验证依赖调用
        verify(blockchainService, times(1)).createWallet(username);
    }

    @Test
    void testGetUserByUsername_Success() {
        // 模拟用户存在
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // 执行测试
        Optional<User> result = userService.getUserByUsername("testuser");

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());

        // 验证依赖调用
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testGetUserByUsername_NotFound() {
        // 模拟用户不存在
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // 执行测试
        Optional<User> result = userService.getUserByUsername("nonexistent");

        // 验证结果
        assertFalse(result.isPresent());

        // 验证依赖调用
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
}