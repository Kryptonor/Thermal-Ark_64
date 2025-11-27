import React from 'react'
import { NavLink } from 'react-router-dom'
import './Sidebar.css'

const Sidebar: React.FC = () => {
  const menuItems = [
    { path: '/app', icon: '📊', label: '仪表盘', exact: true },
    { path: '/app/market', icon: '💹', label: '交易市场' },
    { path: '/app/wallet', icon: '💰', label: '我的钱包' },
    { path: '/app/data', icon: '📈', label: '能源数据' },
    { path: '/app/profile', icon: '👤', label: '个人资料' },
  ]

  return (
    <aside className="sidebar">
      <nav className="sidebar-nav">
        <ul className="nav-list">
          {menuItems.map((item) => (
            <li key={item.path} className="nav-item">
              <NavLink
                to={item.path}
                className={({ isActive }) => 
                  `nav-link ${isActive ? 'active' : ''}`
                }
                end={item.exact}
              >
                <span className="nav-icon">{item.icon}</span>
                <span className="nav-label">{item.label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  )
}

export default Sidebar