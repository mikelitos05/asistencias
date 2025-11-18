import React, { useState, useEffect } from 'react';
import Layout from '../../components/Layout';
import ParkList from '../../components/admin/ParkList';
import ParkForm from '../../components/admin/ParkForm';
import { parkService } from '../../services/parkService';
import './ParksPage.css';

const ParksPage = () => {
  const [parks, setParks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingPark, setEditingPark] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    loadParks();
  }, []);

  const loadParks = async () => {
    try {
      setLoading(true);
      const data = await parkService.getAll();
      setParks(data);
    } catch (err) {
      setError('Error al cargar los parques. Por favor, recarga la página.');
      console.error('Error loading parks:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingPark(null);
    setShowForm(true);
  };

  const handleEdit = (park) => {
    setEditingPark(park);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de que desea eliminar este parque?')) {
      return;
    }

    try {
      await parkService.delete(id);
      setParks((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Error al eliminar el parque';
      alert(errorMessage);
      console.error('Error deleting park:', err);
    }
  };

  const handleSubmit = async (formData) => {
    try {
      if (editingPark) {
        await parkService.update(editingPark.id, formData);
      } else {
        await parkService.create(formData);
      }
      setShowForm(false);
      setEditingPark(null);
      loadParks();
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Error al guardar el parque';
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
      <div className="parks-page">
        <div className="page-header">
          <h1>Parques</h1>
          <button onClick={handleCreate} className="btn-create">
            + Nuevo Parque
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}

        <ParkList
          parks={parks}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />

        {showForm && (
          <ParkForm
            park={editingPark}
            onSubmit={handleSubmit}
            onCancel={() => {
              setShowForm(false);
              setEditingPark(null);
            }}
          />
        )}
      </div>
    </Layout>
  );
};

export default ParksPage;

