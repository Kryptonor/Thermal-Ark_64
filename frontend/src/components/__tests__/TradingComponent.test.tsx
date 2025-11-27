import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import TradingComponent from '../TradingComponent';

// Mock API calls
jest.mock('../../services/tradingService', () => ({
  getMarketData: jest.fn(),
  placeBuyOrder: jest.fn(),
  placeSellOrder: jest.fn(),
}));

import { getMarketData, placeBuyOrder, placeSellOrder } from '../../services/tradingService';

describe('Trading Component', () => {
  const mockMarketData = {
    currentPrice: 45.6,
    priceChange: 1.2,
    volume: 15000,
    high: 46.8,
    low: 44.3,
  };

  beforeEach(() => {
    (getMarketData as jest.Mock).mockResolvedValue(mockMarketData);
    (placeBuyOrder as jest.Mock).mockResolvedValue({ success: true, orderId: 'order-001' });
    (placeSellOrder as jest.Mock).mockResolvedValue({ success: true, orderId: 'order-002' });
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('should display market data correctly', async () => {
    render(<TradingComponent />);

    // Wait for market data to load
    await waitFor(() => {
      expect(screen.getByText('当前价格')).toBeInTheDocument();
      expect(screen.getByText('45.6')).toBeInTheDocument();
      expect(screen.getByText('+1.2')).toBeInTheDocument();
    });

    // Verify API call
    expect(getMarketData).toHaveBeenCalledTimes(1);
  });

  test('should handle buy order successfully', async () => {
    render(<TradingComponent />);

    // Wait for component to load
    await waitFor(() => {
      expect(screen.getByText('买入')).toBeInTheDocument();
    });

    // Fill in buy order form
    const amountInput = screen.getByPlaceholderText('请输入购买数量');
    const priceInput = screen.getByPlaceholderText('请输入价格');
    const buyButton = screen.getByText('买入');

    fireEvent.change(amountInput, { target: { value: '100' } });
    fireEvent.change(priceInput, { target: { value: '45.5' } });
    fireEvent.click(buyButton);

    // Verify API call
    await waitFor(() => {
      expect(placeBuyOrder).toHaveBeenCalledWith({
        amount: 100,
        price: 45.5,
        type: 'BUY'
      });
    });

    // Verify success message
    await waitFor(() => {
      expect(screen.getByText('买入订单提交成功')).toBeInTheDocument();
    });
  });

  test('should handle sell order successfully', async () => {
    render(<TradingComponent />);

    // Wait for component to load
    await waitFor(() => {
      expect(screen.getByText('卖出')).toBeInTheDocument();
    });

    // Fill in sell order form
    const amountInput = screen.getByPlaceholderText('请输入卖出数量');
    const priceInput = screen.getByPlaceholderText('请输入价格');
    const sellButton = screen.getByText('卖出');

    fireEvent.change(amountInput, { target: { value: '50' } });
    fireEvent.change(priceInput, { target: { value: '46.0' } });
    fireEvent.click(sellButton);

    // Verify API call
    await waitFor(() => {
      expect(placeSellOrder).toHaveBeenCalledWith({
        amount: 50,
        price: 46.0,
        type: 'SELL'
      });
    });

    // Verify success message
    await waitFor(() => {
      expect(screen.getByText('卖出订单提交成功')).toBeInTheDocument();
    });
  });

  test('should display error when buy order fails', async () => {
    // Mock API failure
    (placeBuyOrder as jest.Mock).mockRejectedValue(new Error('余额不足'));

    render(<TradingComponent />);

    // Wait for component to load
    await waitFor(() => {
      expect(screen.getByText('买入')).toBeInTheDocument();
    });

    // Fill in buy order form
    const amountInput = screen.getByPlaceholderText('请输入购买数量');
    const priceInput = screen.getByPlaceholderText('请输入价格');
    const buyButton = screen.getByText('买入');

    fireEvent.change(amountInput, { target: { value: '1000' } });
    fireEvent.change(priceInput, { target: { value: '45.5' } });
    fireEvent.click(buyButton);

    // Verify error message
    await waitFor(() => {
      expect(screen.getByText('买入失败：余额不足')).toBeInTheDocument();
    });
  });

  test('should validate form inputs', async () => {
    render(<TradingComponent />);

    // Wait for component to load
    await waitFor(() => {
      expect(screen.getByText('买入')).toBeInTheDocument();
    });

    // Try to submit with invalid inputs
    const buyButton = screen.getByText('买入');
    fireEvent.click(buyButton);

    // Verify validation messages
    await waitFor(() => {
      expect(screen.getByText('请输入有效的数量')).toBeInTheDocument();
      expect(screen.getByText('请输入有效的价格')).toBeInTheDocument();
    });

    // Verify API was not called
    expect(placeBuyOrder).not.toHaveBeenCalled();
  });

  test('should update order history after successful trade', async () => {
    render(<TradingComponent />);

    // Wait for component to load
    await waitFor(() => {
      expect(screen.getByText('买入')).toBeInTheDocument();
    });

    // Place a buy order
    const amountInput = screen.getByPlaceholderText('请输入购买数量');
    const priceInput = screen.getByPlaceholderText('请输入价格');
    const buyButton = screen.getByText('买入');

    fireEvent.change(amountInput, { target: { value: '100' } });
    fireEvent.change(priceInput, { target: { value: '45.5' } });
    fireEvent.click(buyButton);

    // Verify order history is updated
    await waitFor(() => {
      expect(screen.getByText('order-001')).toBeInTheDocument();
      expect(screen.getByText('买入')).toBeInTheDocument();
      expect(screen.getByText('100')).toBeInTheDocument();
      expect(screen.getByText('45.5')).toBeInTheDocument();
    });
  });
});