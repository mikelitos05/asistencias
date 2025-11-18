import React from 'react';
import { formatDate } from '../../utils/formatDate';
import './UserList.css';

const UserList = ({ users, onEdit, onDelete }) => {
  return (
    <div className="user-list">
      <div className="table-container">
        <table className="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Rol</th>
              <th>Fecha de Registro</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 ? (
              <tr>
                <td colSpan="7" className="no-data">
                  No hay usuarios registrados
                </td>
              </tr>
            ) : (
              users.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.name}</td>
                  <td>{user.email}</td>
                  <td>
                    <span className={`role-badge ${user.role === 'SUPER_ADMIN' ? 'super-admin' : 'admin'}`}>
                      {user.role}
                    </span>
                  </td>
                  <td>{formatDate(user.registrationDate)}</td>
                  <td>
                    <span className={`status-badge ${user.active ? 'active' : 'inactive'}`}>
                      {user.active ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        onClick={() => onEdit && onEdit(user)}
                        className="btn-edit"
                        title="Editar"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => onDelete && onDelete(user.id)}
                        className="btn-delete"
                        title="Eliminar"
                      >
                        Eliminar
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default UserList;

