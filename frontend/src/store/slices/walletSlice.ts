import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { walletApi } from '../../services/api'

export interface Wallet {
  id: number
  balance: number
  frozenBalance: number
  totalBalance: number
  energy: number
  frozenEnergy: number
  currency: number
  frozenCurrency: number
}

export interface Transaction {
  id: number
  type: 'DEPOSIT' | 'WITHDRAW' | 'TRADE'
  amount: number
  status: 'PENDING' | 'COMPLETED' | 'FAILED'
  createdAt: string
  description: string
}

export interface WalletState {
  wallet: Wallet | null
  transactions: Transaction[]
  loading: boolean
  error: string | null
}

const initialState: WalletState = {
  wallet: null,
  transactions: [],
  loading: false,
  error: null,
}

// 异步thunk
export const fetchWallet = createAsyncThunk(
  'wallet/fetchWallet',
  async (_, { rejectWithValue }) => {
    try {
      const response = await walletApi.getBalance()
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '获取钱包信息失败')
    }
  }
)

export const deposit = createAsyncThunk(
  'wallet/deposit',
  async (amount: number, { rejectWithValue }) => {
    try {
      const response = await walletApi.deposit(amount)
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '充值失败')
    }
  }
)

export const withdraw = createAsyncThunk(
  'wallet/withdraw',
  async (amount: number, { rejectWithValue }) => {
    try {
      const response = await walletApi.withdraw(amount)
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '提现失败')
    }
  }
)

const walletSlice = createSlice({
  name: 'wallet',
  initialState,
  reducers: {
    clearWallet: (state) => {
      state.wallet = null
      state.transactions = []
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchWallet.fulfilled, (state, action) => {
        state.wallet = action.payload
      })
      .addCase(deposit.fulfilled, (state, action) => {
        if (state.wallet) {
          state.wallet.balance += action.payload.amount
        }
      })
      .addCase(withdraw.fulfilled, (state, action) => {
        if (state.wallet) {
          state.wallet.balance -= action.payload.amount
        }
      })
  },
})

export const { clearWallet } = walletSlice.actions
export default walletSlice.reducer