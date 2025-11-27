import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import './Home.css'

const Home: React.FC = () => {
  const { isAuthenticated } = useAuth()

  return (
    <div className="home">
      {/* Hero Section */}
      <section className="hero">
        <div className="hero-content">
          <h1 className="hero-title">Thermal Ark</h1>
          <h2 className="hero-subtitle">去中心化 P2P 热力交易平台</h2>
          <p className="hero-description">
            基于区块链技术的智能热力交易系统，实现能源的高效分配和价值流通
          </p>
          
          {isAuthenticated ? (
            <Link to="/app" className="cta-button btn btn-primary">
              进入控制台
            </Link>
          ) : (
            <div className="cta-buttons">
              <Link to="/login" className="cta-button btn btn-primary">
                登录账户
              </Link>
              <Link to="/register" className="cta-button btn btn-secondary">
                注册新账户
              </Link>
            </div>
          )}
        </div>
        
        <div className="hero-image">
          <div className="energy-animation">
            <div className="energy-circle"></div>
            <div className="energy-wave"></div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="features">
        <div className="container">
          <h2 className="section-title">核心特性</h2>
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon">🔗</div>
              <h3>区块链技术</h3>
              <p>基于FISCO BCOS区块链，确保交易透明、不可篡改</p>
            </div>
            
            <div className="feature-card">
              <div className="feature-icon">⚡</div>
              <h3>P2P交易</h3>
              <p>去中心化的点对点热力交易，降低中间成本</p>
            </div>
            
            <div className="feature-card">
              <div className="feature-icon">📊</div>
              <h3>智能匹配</h3>
              <p>智能算法自动匹配买卖订单，提高交易效率</p>
            </div>
            
            <div className="feature-card">
              <div className="feature-icon">🌡️</div>
              <h3>实时监控</h3>
              <p>物联网设备实时监控能源生产和消耗</p>
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="stats">
        <div className="container">
          <div className="stats-grid">
            <div className="stat-item">
              <div className="stat-number">1000+</div>
              <div className="stat-label">注册用户</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">5000+</div>
              <div className="stat-label">成功交易</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">10000+</div>
              <div className="stat-label">能源交易量(kWh)</div>
            </div>
            <div className="stat-item">
              <div className="stat-number">99.9%</div>
              <div className="stat-label">系统稳定性</div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="container">
          <p>&copy; 2024 Thermal Ark. 基于区块链的P2P热力交易平台</p>
        </div>
      </footer>
    </div>
  )
}

export default Home