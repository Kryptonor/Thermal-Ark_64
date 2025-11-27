import { configureStore } from '@reduxjs/toolkit'
import authReducer from './slices/authSlice'
import userReducer from './slices/userSlice'
import marketReducer from './slices/marketSlice'
import walletReducer from './slices/walletSlice'
import energyReducer from './slices/energySlice'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    user: userReducer,
    market: marketReducer,
    wallet: walletReducer,
    energy: energyReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
})

// 导出类型
export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch