import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { energyApi } from '../../services/api'

export interface EnergyData {
  timestamp: string
  energyOutput: number
  energyConsumption: number
  efficiency: number
  temperature: number
}

export interface CommunityStats {
  totalUsers: number
  totalEnergyTraded: number
  totalCarbonReduction: number
  averageEfficiency: number
  userRankings: UserRanking[]
}

export interface UserRanking {
  username: string
  energySaved: number
  carbonReduction: number
  rank: number
}

export interface EnergyState {
  energyData: EnergyData[]
  communityStats: CommunityStats | null
  loading: boolean
  error: string | null
}

const initialState: EnergyState = {
  energyData: [],
  communityStats: null,
  loading: false,
  error: null,
}

// 异步thunk
export const fetchEnergyData = createAsyncThunk(
  'energy/fetchEnergyData',
  async (period: string, { rejectWithValue }) => {
    try {
      const response = await energyApi.getEnergyData(period)
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '获取能源数据失败')
    }
  }
)

export const fetchCommunityStats = createAsyncThunk(
  'energy/fetchCommunityStats',
  async (_, { rejectWithValue }) => {
    try {
      const response = await energyApi.getCommunityStats()
      return response
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '获取社区统计失败')
    }
  }
)

const energySlice = createSlice({
  name: 'energy',
  initialState,
  reducers: {
    updateEnergyData: (state, action) => {
      state.energyData = action.payload
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchEnergyData.fulfilled, (state, action) => {
        state.energyData = action.payload
      })
      .addCase(fetchCommunityStats.fulfilled, (state, action) => {
        state.communityStats = action.payload
      })
  },
})

export const { updateEnergyData } = energySlice.actions
export default energySlice.reducer