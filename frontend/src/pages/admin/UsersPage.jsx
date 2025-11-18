import React, { useState, useEffect } from 'react';
import Layout from '../../components/Layout';
import UserList from '../../components/admin/UserList';
import UserForm from '../../components/admin/UserForm';
import { userService } from '../../services/userService';
import './UsersPage.css';

const UsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await userService.getAll();
      setUsers(data);
    } catch (err) {
      setError('Error al cargar los usuarios. Por favor, recarga la página.');
      console.error('Error loading users:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingUser(null);
    setShowForm(true);
  };

  const handleEdit = (user) => {
    setEditingUser(user);
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (!window.confirm('¿Está seguro de que desea eliminar este usuario?')) {
      return;
    }

    // Nota: El backend no tiene endpoint de eliminación, solo se puede crear
    // Por ahora, solo mostramos un mensaje
    alert('La funcionalidad de eliminación no está disponible en el backend.');
  };

  const handleSubmit = async (formData) => {
    try {
      if (editingUser) {
        // Nota: El backend no tiene endpoint de actualización
        alert('La funcionalidad de edición no está disponible en el backend.');
        setShowForm(false);
        setEditingUser(null);
      } else {
        await userService.create(formData);
        setShowForm(false);
        loadUsers();
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Error al guardar el usuario';
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
      <div className="users-page">
        <div className="page-header">
          <h1>Usuarios</h1>
          <button onClick={handleCreate} className="btn-create">
            + Nuevo Usuario
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}

        <UserList
          users={users}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />

        {showForm && (
          <UserForm
            user={editingUser}
            onSubmit={handleSubmit}
            onCancel={() => {
              setShowForm(false);
              setEditingUser(null);
            }}
          />
        )}
      </div>
    </Layout>
  );
};

export default UsersPage;

