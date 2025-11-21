import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';
import Login from './pages/Login';
import AttendanceForm from './pages/AttendanceForm';
import ParksPage from './pages/admin/ParksPage';
import SocialServersPage from './pages/admin/SocialServersPage';
import AttendancesPage from './pages/admin/AttendancesPage';
import ProgramsPage from './pages/admin/ProgramsPage';
import UsersPage from './pages/admin/UsersPage';
import { ROLES } from './utils/constants';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Rutas públicas */}
          <Route path="/login" element={<Login />} />
          <Route
            path="/asistencias"
            element={
              <Layout>
                <AttendanceForm />
              </Layout>
            }
          />

          {/* Rutas protegidas para ADMIN y SUPER_ADMIN */}
          <Route
            path="/admin/parques"
            element={
              <ProtectedRoute allowedRoles={[ROLES.ADMIN, ROLES.SUPER_ADMIN]}>
                <ParksPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/servidores-sociales"
            element={
              <ProtectedRoute allowedRoles={[ROLES.ADMIN, ROLES.SUPER_ADMIN]}>
                <SocialServersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/asistencias"
            element={
              <ProtectedRoute allowedRoles={[ROLES.ADMIN, ROLES.SUPER_ADMIN]}>
                <AttendancesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/programas"
            element={
              <ProtectedRoute allowedRoles={[ROLES.ADMIN, ROLES.SUPER_ADMIN]}>
                <ProgramsPage />
              </ProtectedRoute>
            }
          />

          {/* Rutas protegidas solo para SUPER_ADMIN */}
          <Route
            path="/admin/usuarios"
            element={
              <ProtectedRoute allowedRoles={[ROLES.SUPER_ADMIN]}>
                <UsersPage />
              </ProtectedRoute>
            }
          />

          {/* Ruta por defecto */}
          <Route path="/" element={<Navigate to="/asistencias" replace />} />
          <Route path="*" element={<Navigate to="/asistencias" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
