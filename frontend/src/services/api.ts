import axios from 'axios'

// API基础配置
const API_BASE_URL = 'http://localhost:8080/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
})

// 请求拦截器 - 添加认证token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 认证相关API
export const authApi = {
  login: async (credentials: { username: string; password: string }) => {
    const response = await apiClient.post('/auth/login', credentials)
    return response.data
  },
  
  register: async (userData: { username: string; password: string; phone: string; email: string }) => {
    const response = await apiClient.post('/auth/register', userData)
    return response.data
  },
}

// 用户相关API
export const userApi = {
  getCurrentUser: async () => {
    const response = await apiClient.get('/users/profile')
    return response.data
  },
  
  updateUser: async (userId: number, email?: string, phone?: string) => {
    const response = await apiClient.put(`/users/${userId}`, { email, phone })
    return response.data
  },
}

// 交易市场API
export const marketApi = {
  getOrders: async () => {
    const response = await apiClient.get('/market/orders')
    return response.data
  },
  
  createOrder: async (orderData: { type: 'BUY' | 'SELL'; energyAmount: number; price: number }) => {
    const response = await apiClient.post('/market/orders', orderData)
    return response.data
  },
  
  cancelOrder: async (orderId: number) => {
    const response = await apiClient.delete(`/market/orders/${orderId}`)
    return response.data
  },
}

// 钱包相关API
export const walletApi = {
  getBalance: async () => {
    const response = await apiClient.get('/wallet/balance')
    return response.data
  },
  
  deposit: async (amount: number) => {
    const response = await apiClient.post('/wallet/deposit', { amount })
    return response.data
  },
  
  withdraw: async (amount: number) => {
    const response = await apiClient.post('/wallet/withdraw', { amount })
    return response.data
  },
  
  getTransactions: async () => {
    const response = await apiClient.get('/wallet/transactions')
    return response.data
  },
}

// 能源数据API
export const energyApi = {
  getEnergyData: async (period: string) => {
    const response = await apiClient.get(`/data/energy?period=${period}`)
    return response.data
  },
  
  getCommunityStats: async () => {
    const response = await apiClient.get('/data/community')
    return response.data
  },
  
  addIoTData: async (data: { deviceId: string; temperature: number; energyOutput: number; energyConsumption: number; efficiency: number }) => {
    const response = await apiClient.post('/data/iot', data)
    return response.data
  },
}

export default apiClient