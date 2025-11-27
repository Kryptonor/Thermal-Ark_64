import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import UserProfile from '../UserProfile';

// Mock API calls
jest.mock('../../services/userService', () => ({
  getUserProfile: jest.fn(),
  updateUserProfile: jest.fn(),
  changePassword: jest.fn(),
}));

import { getUserProfile, updateUserProfile, changePassword } from '../../services/userService';

describe('User Profile Component', () => {
  const mockUserProfile = {
    id: 'user-001',
    username: 'testuser',
    email: 'test@example.com',
    phone: '13812345678',
    walletAddress: '0x1234567890abcdef',
    balance: 5000.0,
    createdAt: '2024-01-01T00:00:00Z',
  };

  beforeEach(() => {
    (getUserProfile as jest.Mock).mockResolvedValue(mockUserProfile);
    (updateUserProfile as jest.Mock).mockResolvedValue({ success: true });
    (changePassword as jest.Mock).mockResolvedValue({ success: true });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('should display user profile correctly', async () => {
    render(<UserProfile />);

    // Wait for profile data to load
    await waitFor(() => {
      expect(screen.getByText('testuser')).toBeInTheDocument();
      expect(screen.getByText('test@example.com')).toBeInTheDocument();
      expect(screen.getByText('13812345678')).toBeInTheDocument();
      expect(screen.getByText('0x1234567890abcdef')).toBeInTheDocument();
      expect(screen.getByText('5000.0')).toBeInTheDocument();
    });

    // Verify API call
    expect(getUserProfile).toHaveBeenCalledTimes(1);
  });

  test('should handle profile update successfully', async () => {
    render(<UserProfile />);

    // Wait for profile to load
    await waitFor(() => {
      expect(screen.getByText('编辑资料')).toBeInTheDocument();
    });

    // Click edit button
    const editButton = screen.getByText('编辑资料');
    fireEvent.click(editButton);

    // Update phone number
    const phoneInput = screen.getByDisplayValue('13812345678');
    fireEvent.change(phoneInput, { target: { value: '13987654321' } });

    // Click save button
    const saveButton = screen.getByText('保存');
    fireEvent.click(saveButton);

    // Verify API call
    await waitFor(() => {
      expect(updateUserProfile).toHaveBeenCalledWith({
        phone: '13987654321'
      });
    });

    // Verify success message
    await waitFor(() => {
      expect(screen.getByText('资料更新成功')).toBeInTheDocument();
    });
  });

  test('should handle password change successfully', async () => {
    render(<UserProfile />);

    // Wait for profile to load
    await waitFor(() => {
      expect(screen.getByText('修改密码')).toBeInTheDocument();
    });

    // Click change password button
    const changePasswordButton = screen.getByText('修改密码');
    fireEvent.click(changePasswordButton);

    // Fill in password form
    const oldPasswordInput = screen.getByPlaceholderText('请输入当前密码');
    const newPasswordInput = screen.getByPlaceholderText('请输入新密码');
    const confirmPasswordInput = screen.getByPlaceholderText('请确认新密码');
    
    fireEvent.change(oldPasswordInput, { target: { value: 'oldpassword' } });
    fireEvent.change(newPasswordInput, { target: { value: 'newpassword' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'newpassword' } });

    // Click submit button
    const submitButton = screen.getByText('确认修改');
    fireEvent.click(submitButton);

    // Verify API call
    await waitFor(() => {
      expect(changePassword).toHaveBeenCalledWith({
        oldPassword: 'oldpassword',
        newPassword: 'newpassword'
      });
    });

    // Verify success message
    await waitFor(() => {
      expect(screen.getByText('密码修改成功')).toBeInTheDocument();
    });
  });

  test('should validate password confirmation', async () => {
    render(<UserProfile />);

    // Wait for profile to load
    await waitFor(() => {
      expect(screen.getByText('修改密码')).toBeInTheDocument();
    });

    // Click change password button
    const changePasswordButton = screen.getByText('修改密码');
    fireEvent.click(changePasswordButton);

    // Fill in password form with mismatched passwords
    const oldPasswordInput = screen.getByPlaceholderText('请输入当前密码');
    const newPasswordInput = screen.getByPlaceholderText('请输入新密码');
    const confirmPasswordInput = screen.getByPlaceholderText('请确认新密码');
    
    fireEvent.change(oldPasswordInput, { target: { value: 'oldpassword' } });
    fireEvent.change(newPasswordInput, { target: { value: 'newpassword' } });
    fireEvent.change(confirmPasswordInput, { target: { value: 'differentpassword' } });

    // Click submit button
    const submitButton = screen.getByText('确认修改');
    fireEvent.click(submitButton);

    // Verify validation message
    await waitFor(() => {
      expect(screen.getByText('两次输入的密码不一致')).toBeInTheDocument();
    });

    // Verify API was not called
    expect(changePassword).not.toHaveBeenCalled();
  });

  test('should display error when profile update fails', async () => {
    // Mock API failure
    (updateUserProfile as jest.Mock).mockRejectedValue(new Error('更新失败'));

    render(<UserProfile />);

    // Wait for profile to load
    await waitFor(() => {
      expect(screen.getByText('编辑资料')).toBeInTheDocument();
    });

    // Click edit button
    const editButton = screen.getByText('编辑资料');
    fireEvent.click(editButton);

    // Update phone number
    const phoneInput = screen.getByDisplayValue('13812345678');
    fireEvent.change(phoneInput, { target: { value: '13987654321' } });

    // Click save button
    const saveButton = screen.getByText('保存');
    fireEvent.click(saveButton);

    // Verify error message
    await waitFor(() => {
      expect(screen.getByText('资料更新失败：更新失败')).toBeInTheDocument();
    });
  });

  test('should toggle edit mode correctly', async () => {
    render(<UserProfile />);

    // Wait for profile to load
    await waitFor(() => {
      expect(screen.getByText('编辑资料')).toBeInTheDocument();
    });

    // Initially should not have edit inputs
    expect(screen.queryByDisplayValue('13812345678')).not.toBeInTheDocument();

    // Click edit button
    const editButton = screen.getByText('编辑资料');
    fireEvent.click(editButton);

    // Now should have edit inputs
    await waitFor(() => {
      expect(screen.getByDisplayValue('13812345678')).toBeInTheDocument();
    });

    // Click cancel button
    const cancelButton = screen.getByText('取消');
    fireEvent.click(cancelButton);

    // Should return to view mode
    await waitFor(() => {
      expect(screen.queryByDisplayValue('13812345678')).not.toBeInTheDocument();
      expect(screen.getByText('13812345678')).toBeInTheDocument();
    });
  });
});