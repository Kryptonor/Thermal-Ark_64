import React from 'react'
import { useAuth } from '../../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import './Header.css'

interface HeaderProps {
  toggleTheme: () => void
  isDark: boolean
}

const Header: React.FC<HeaderProps> = ({ toggleTheme, isDark }) => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="header">
      <div className="header-content">
        <div className="header-left">
          <h1 className="logo">Thermal Ark</h1>
          <span className="tagline">P2P 热力交易平台</span>
        </div>
        
        <div className="header-right">
          <div className="user-info">
            <span className="welcome">欢迎, {user?.realName || user?.username}</span>
            <span className="role">{user?.role === 'ADMIN' ? '管理员' : '用户'}</span>
          </div>
          
          <button 
            className="theme-toggle btn btn-secondary"
            onClick={toggleTheme}
            title={isDark ? '切换到亮色主题' : '切换到暗色主题'}
          >
            {isDark ? '☀️' : '🌙'}
          </button>
          
          <button 
            className="logout-btn btn btn-danger"
            onClick={handleLogout}
          >
            退出登录
          </button>
        </div>
      </div>
    </header>
  )
}

export default Header