import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { userApi } from '../../services/api'

export interface User {
  id: number
  username: string
  phone: string
  email: string
  realName: string
  blockchainAddress: string
  role: 'USER' | 'ADMIN' | 'OPERATOR'
  isVerified: boolean
  createdAt: string
}

export interface UserState {
  currentUser: User | null
  users: User[]
  loading: boolean
  error: string | null
}

const initialState: UserState = {
  currentUser: null,
  users: [],
  loading: false,
  error: null,
}

// 异步thunk
export const fetchCurrentUser = createAsyncThunk(
  'user/fetchCurrentUser',
  async (_, { rejectWithValue }) => {
    try {
      const response = await userApi.getCurrentUser()
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '获取用户信息失败')
    }
  }
)

export const updateUser = createAsyncThunk(
  'user/updateUser',
  async ({ userId, email, phone }: { userId: number; email?: string; phone?: string }, { rejectWithValue }) => {
    try {
      const response = await userApi.updateUser(userId, email, phone)
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '更新用户信息失败')
    }
  }
)

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    clearUser: (state) => {
      state.currentUser = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCurrentUser.fulfilled, (state, action) => {
        state.currentUser = action.payload
      })
      .addCase(updateUser.fulfilled, (state, action) => {
        state.currentUser = action.payload
      })
  },
})

export const { clearUser } = userSlice.actions
export default userSlice.reducer