import React from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import { useTheme } from '../../contexts/ThemeContext'
import Header from './Header'
import Sidebar from './Sidebar'
import './Layout.css'

const Layout: React.FC = () => {
  const { isAuthenticated } = useAuth()
  const { isDark, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const location = useLocation()

  // 如果未认证且不在登录/注册页面，重定向到登录页
  React.useEffect(() => {
    if (!isAuthenticated && !['/login', '/register'].includes(location.pathname)) {
      navigate('/login')
    }
  }, [isAuthenticated, location.pathname, navigate])

  if (!isAuthenticated) {
    return <Outlet />
  }

  return (
    <div className={`layout ${isDark ? 'dark' : ''}`}>
      <Header toggleTheme={toggleTheme} isDark={isDark} />
      <div className="layout-content">
        <Sidebar />
        <main className="main-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default Layout