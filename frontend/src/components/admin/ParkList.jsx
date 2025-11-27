import React from 'react';
import './ParkList.css';

const ParkList = ({ parks, onEdit, onDelete }) => {
  return (
    <div className="park-list">
      <div className="table-container">
        <table className="parks-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre del Parque</th>
              <th>Abreviatura</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {parks.length === 0 ? (
              <tr>
                <td colSpan="4" className="no-data">
                  No hay parques registrados
                </td>
              </tr>
            ) : (
              parks.map((park) => (
                <tr key={park.id}>
                  <td>{park.id}</td>
                  <td>{park.parkName}</td>
                  <td>
                    <span className="abbreviation-badge">{park.abbreviation}</span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button
                        onClick={() => onEdit(park)}
                        className="btn-soft-yellow"
                        title="Editar"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => onDelete(park.id)}
                        className="btn-soft-red"
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

export default ParkList;
