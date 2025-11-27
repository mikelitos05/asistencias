import React, { useState, useEffect } from 'react';
import Layout from '../../components/Layout';
import SocialServerList from '../../components/admin/SocialServerList';
import SocialServerForm from '../../components/admin/SocialServerForm';
import PhotoSizeConfigModal from '../../components/admin/PhotoSizeConfigModal';
import { socialServerService } from '../../services/socialServerService';
import { parkService } from '../../services/parkService';
import { programService } from '../../services/programService';
import { useAuth } from '../../context/AuthContext';
import { ROLES } from '../../utils/constants';
import './SocialServersPage.css';

const SocialServersPage = () => {
  const [socialServers, setSocialServers] = useState([]);
  const [parks, setParks] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingServer, setEditingServer] = useState(null);
  const [error, setError] = useState('');
  const [showPhotoConfig, setShowPhotoConfig] = useState(false);

  const { user } = useAuth();
  const isSuperAdmin = user?.role === ROLES.SUPER_ADMIN;

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [serversData, parksData, programsData] = await Promise.all([
        socialServerService.getAll(),
        parkService.getAll(),
        programService.getAllPrograms(),
      ]);
      setSocialServers(serversData);
      setParks(parksData);
      setPrograms(programsData);
    } catch (err) {
      setError('Error al cargar los datos. Por favor, recarga la página.');
      console.error('Error loading data:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingServer(null);
    setShowForm(true);
  };

  const handleEdit = (server) => {
    setEditingServer(server);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de que desea eliminar este servidor social?')) {
      return;
    }

    try {
      await socialServerService.delete(id);
      setSocialServers((prev) => prev.filter((s) => s.id !== id));
    } catch (err) {
      alert('Error al eliminar el servidor social');
      console.error('Error deleting:', err);
    }
  };

  const handleExport = async () => {
    try {
      const blob = await socialServerService.export();
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'servidores_sociales.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error('Error exporting:', err);
      alert('Error al exportar el archivo');
    }
  };

  const handleImport = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    try {
      setLoading(true);
      await socialServerService.import(file);
      alert('Importación exitosa');
      loadData(); // Reload data
    } catch (err) {
      console.error('Error importing:', err);
      alert('Error al importar el archivo');
    } finally {
      setLoading(false);
      event.target.value = ''; // Reset input
    }
  };

  const handleViewAttendances = (id) => {
    window.location.href = `/admin/asistencias?servidor=${id}`;
  };

  const handleSubmit = async (formData) => {
    try {
      if (editingServer) {
        await socialServerService.update(editingServer.id, formData);
      } else {
        await socialServerService.create(formData);
      }
      setShowForm(false);
      setEditingServer(null);
      loadData();
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Error al guardar el servidor social';
      alert(errorMessage);
      throw err;
    }
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
      <div className="social-servers-page">
        <div className="page-header">
          <h1>Servidores Sociales</h1>
          <div className="header-buttons">
            <input
              type="file"
              id="import-excel"
              style={{ display: 'none' }}
              accept=".xlsx, .xls"
              onChange={handleImport}
            />
            <button onClick={() => document.getElementById('import-excel').click()} className="btn-secondary">
              Importar Excel
            </button>
            <button onClick={handleExport} className="btn-secondary">
              Exportar Excel
            </button>
            <button onClick={handleCreate} className="btn-create">
              + Nuevo Servidor Social
            </button>
            {isSuperAdmin && (
              <button onClick={() => setShowPhotoConfig(true)} className="btn-config">
                ⚙️ Configurar Tamaño de Fotos
              </button>
            )}
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        <SocialServerList
          socialServers={socialServers}
          onEdit={handleEdit}
          onDelete={handleDelete}
          onViewAttendances={handleViewAttendances}
        />

        {showForm && (
          <SocialServerForm
            socialServer={editingServer}
            parks={parks}
            programs={programs}
            onSubmit={handleSubmit}
            onCancel={() => {
              setShowForm(false);
              setEditingServer(null);
            }}
          />
        )}

        <PhotoSizeConfigModal
          isOpen={showPhotoConfig}
          onClose={() => setShowPhotoConfig(false)}
        />
      </div>
    </Layout>
  );
};

export default SocialServersPage;
