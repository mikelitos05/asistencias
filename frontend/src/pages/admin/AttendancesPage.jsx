import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Layout from '../../components/Layout';
import AttendanceList from '../../components/admin/AttendanceList';
import { attendanceService } from '../../services/attendanceService';
import { socialServerService } from '../../services/socialServerService';
import PhotoSizeConfigModal from '../../components/admin/PhotoSizeConfigModal';
import { useAuth } from '../../context/AuthContext';
import { ROLES } from '../../utils/constants';
import './AttendancesPage.css';

const AttendancesPage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [isConfigModalOpen, setIsConfigModalOpen] = useState(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const [allAttendances, setAllAttendances] = useState([]);
  const [filteredAttendances, setFilteredAttendances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [viewMode, setViewMode] = useState('all'); // 'all' or 'user'

  const servidorId = searchParams.get('servidor');
  const emailParam = searchParams.get('email');

  useEffect(() => {
    loadAttendances();
  }, []);

  useEffect(() => {
    if (servidorId) {
      loadUserAttendances(servidorId);
    } else if (emailParam) {
      // Filtrar por email si viene como parámetro
      const userAttendances = allAttendances.filter((a) => a.email === emailParam);
      setFilteredAttendances(userAttendances);
      setViewMode('user');
      const attendance = allAttendances.find((a) => a.email === emailParam);
      if (attendance) {
        setSelectedUser({ email: attendance.email, name: attendance.socialServerName });
      }
    } else {
      setViewMode('all');
      setSelectedUser(null);
      setFilteredAttendances(allAttendances);
    }
  }, [servidorId, emailParam, allAttendances]);

  const loadAttendances = async () => {
    try {
      setLoading(true);
      const data = await attendanceService.getAll();
      setAllAttendances(data);
      setFilteredAttendances(data);
    } catch (err) {
      setError('Error al cargar las asistencias. Por favor, recarga la página.');
      console.error('Error loading attendances:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadUserAttendances = async (socialServerId) => {
    try {
      setLoading(true);
      setViewMode('user');
      const data = await attendanceService.getBySocialServerId(parseInt(socialServerId));
      setFilteredAttendances(data);
    } catch (err) {
      setError('Error al cargar las asistencias del usuario.');
      console.error('Error loading user attendances:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUserClick = async (email, userName) => {
    try {
      // Intentar obtener el ID del servidor social por email
      const socialServers = await socialServerService.getAll();
      const socialServer = socialServers.find((s) => s.email === email);

      if (socialServer && socialServer.id) {
        // Usar el endpoint específico con el ID
        setSelectedUser({ email, name: userName, id: socialServer.id });
        setSearchParams({ servidor: socialServer.id });
        await loadUserAttendances(socialServer.id);
      } else {
        // Fallback: filtrar por email
        setSelectedUser({ email, name: userName });
        setSearchParams({ email: email });
        const userAttendances = allAttendances.filter((a) => a.email === email);
        setFilteredAttendances(userAttendances);
        setViewMode('user');
      }
    } catch (err) {
      // Fallback: filtrar por email
      setSelectedUser({ email, name: userName });
      setSearchParams({ email: email });
      const userAttendances = allAttendances.filter((a) => a.email === email);
      setFilteredAttendances(userAttendances);
      setViewMode('user');
    }
  };

  const handleBackToAll = () => {
    setSearchParams({});
    setSelectedUser(null);
    setViewMode('all');
    setFilteredAttendances(allAttendances);
  };

  if (loading) {
    return (
      <Layout>
        <div className="loading">Cargando...</div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="attendances-page">
        <div className="page-header">
          <div className="header-title-actions">
            <h1>Asistencias</h1>
            {user?.role === ROLES.SUPER_ADMIN && (
              <button
                className="btn-config"
                onClick={() => setIsConfigModalOpen(true)}
              >
                Configurar Tamaño
              </button>
            )}
          </div>
          {servidorId ? (
            <button onClick={() => navigate('/admin/servidores-sociales')} className="btn-back">
              ← Volver a Servidores Sociales
            </button>
          ) : viewMode === 'user' && selectedUser && (
            <button onClick={handleBackToAll} className="btn-back">
              ← Volver a todas las asistencias
            </button>
          )}
        </div>

        {viewMode === 'user' && selectedUser && (
          <div className="user-info">
            <h2>Asistencias de: {selectedUser.name}</h2>
            <p>Correo: {selectedUser.email}</p>
          </div>
        )}

        {error && <div className="error-message">{error}</div>}

        <AttendanceList
          attendances={filteredAttendances}
          onUserClick={viewMode === 'all' ? handleUserClick : null}
          showUserColumn={viewMode === 'all'}
        />

        <PhotoSizeConfigModal
          isOpen={isConfigModalOpen}
          onClose={() => setIsConfigModalOpen(false)}
        />
      </div>
    </Layout>
  );
};

export default AttendancesPage;

