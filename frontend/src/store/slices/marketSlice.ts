import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { marketApi } from '../../services/api'

export interface Order {
  id: number
  type: 'BUY' | 'SELL'
  energyAmount: number
  price: number
  totalAmount: number
  status: 'PENDING' | 'MATCHED' | 'EXECUTING' | 'COMPLETED' | 'CANCELLED'
  createdAt: string
}

export interface MarketState {
  orders: Order[]
  buyOrders: Order[]
  sellOrders: Order[]
  priceHistory: PriceData[]
  loading: boolean
  error: string | null
}

export interface PriceData {
  timestamp: string
  price: number
  volume: number
}

const initialState: MarketState = {
  orders: [],
  buyOrders: [],
  sellOrders: [],
  priceHistory: [],
  loading: false,
  error: null,
}

// 异步thunk
export const fetchOrders = createAsyncThunk(
  'market/fetchOrders',
  async (_, { rejectWithValue }) => {
    try {
      const response = await marketApi.getOrders()
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '获取订单失败')
    }
  }
)

export const createOrder = createAsyncThunk(
  'market/createOrder',
  async (orderData: { type: 'BUY' | 'SELL'; energyAmount: number; price: number }, { rejectWithValue }) => {
    try {
      const response = await marketApi.createOrder(orderData)
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '创建订单失败')
    }
  }
)

const marketSlice = createSlice({
  name: 'market',
  initialState,
  reducers: {
    updateOrderBook: (state, action) => {
      state.orders = action.payload
      state.buyOrders = action.payload.filter((order: Order) => order.type === 'BUY')
      state.sellOrders = action.payload.filter((order: Order) => order.type === 'SELL')
    },
    updatePriceHistory: (state, action) => {
      state.priceHistory = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchOrders.fulfilled, (state, action) => {
        state.orders = action.payload
        state.buyOrders = action.payload.filter((order: Order) => order.type === 'BUY')
        state.sellOrders = action.payload.filter((order: Order) => order.type === 'SELL')
      })
      .addCase(createOrder.fulfilled, (state, action) => {
        state.orders.push(action.payload)
      })
  },
})

export const { updateOrderBook, updatePriceHistory } = marketSlice.actions
export default marketSlice.reducer