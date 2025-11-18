import React, { useState } from 'react';
import './SocialServerList.css';

const SocialServerList = ({ socialServers, onEdit, onDelete, onViewAttendances }) => {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredServers = socialServers.filter((server) => {
    const searchLower = searchTerm.toLowerCase();
    return (
      server.name?.toLowerCase().includes(searchLower) ||
      server.email?.toLowerCase().includes(searchLower) ||
      server.school?.toLowerCase().includes(searchLower) ||
      server.id?.toString().includes(searchLower)
    );
  });

  return (
    <div className="social-server-list">
      <div className="search-container">
        <input
          type="text"
          placeholder="Buscar por nombre, correo, escuela o folio..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
      </div>

      <div className="table-container">
        <table className="servers-table">
          <thead>
            <tr>
              <th>Folio</th>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Escuela</th>
              <th>Parque</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredServers.length === 0 ? (
              <tr>
                <td colSpan="6" className="no-data">
                  {searchTerm ? 'No se encontraron resultados' : 'No hay servidores sociales registrados'}
                </td>
              </tr>
            ) : (
              filteredServers.map((server) => (
                <tr key={server.id}>
                  <td>{server.id}</td>
                  <td>{server.name}</td>
                  <td>{server.email}</td>
                  <td>{server.school}</td>
                  <td>{server.parkName}</td>
                  <td>
                    <div className="action-buttons">
                      <button
                        onClick={() => onViewAttendances(server.id)}
                        className="btn-view"
                        title="Ver asistencias"
                      >
                        Ver Asistencias
                      </button>
                      <button
                        onClick={() => onEdit(server)}
                        className="btn-edit"
                        title="Editar"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => onDelete(server.id)}
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

export default SocialServerList;

