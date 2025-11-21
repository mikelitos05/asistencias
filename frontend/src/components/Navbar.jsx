import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ROLES } from '../utils/constants';
import './Navbar.css';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!isAuthenticated) {
    return null;
  }

  const isAdmin = user?.role === ROLES.ADMIN || user?.role === ROLES.SUPER_ADMIN;
  const isSuperAdmin = user?.role === ROLES.SUPER_ADMIN;

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          <img src="/ambu_logo.png" alt="AMBU Logo" className="logo-img" />
          <span>Sistema de Asistencias</span>
        </Link>

        <div className="navbar-menu">
          {isAdmin && (
            <>
              <Link to="/admin/parques" className="navbar-link">
                Parques
              </Link>
              <Link to="/admin/programas" className="navbar-link">
                Programas
              </Link>
              {isSuperAdmin && (
                <>
                  <Link to="/admin/servidores-sociales" className="navbar-link">
                    Servidores Sociales
                  </Link>
                  <Link to="/admin/usuarios" className="navbar-link">
                    Usuarios
                  </Link>
                </>
              )}
            </>
          )}
          <div className="navbar-user">
            <span className="user-name">{user?.name}</span>
            <span className="user-role">({user?.role})</span>
            <button onClick={handleLogout} className="logout-btn">
              Cerrar Sesión
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

