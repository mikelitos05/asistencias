import React, { useState, useEffect } from 'react';
import './SocialServerList.css';

const SocialServerList = ({ socialServers, onEdit, onDelete, onViewAttendances }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [visibleColumns, setVisibleColumns] = useState({
    folio: true,
    name: true,
    email: true,
    school: true,
    park: true,
    status: true,
    type: true,
    vest: false,
    badge: false,
    startDate: false,
    endDate: false,
    totalHours: false,
    cellPhone: false,
    bloodType: false,
    allergy: false,
    birthDate: false,
    major: false,
    period: false,
    tutorName: false,
    tutorPhone: false,
    enrollmentDate: false,
    generalInductionDate: false,
    acceptanceLetterId: false,
    completionLetterId: false,
    schedule: false,
    program: false,
  });
  const [showColumnSelector, setShowColumnSelector] = useState(false);
  const columnSelectorRef = React.useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (columnSelectorRef.current && !columnSelectorRef.current.contains(event.target)) {
        setShowColumnSelector(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const allColumns = [
    { key: 'folio', label: 'Folio' },
    { key: 'name', label: 'Nombre' },
    { key: 'email', label: 'Correo' },
    { key: 'school', label: 'Escuela' },
    { key: 'park', label: 'Parque' },
    { key: 'status', label: 'Estado' },
    { key: 'type', label: 'Tipo' },
    { key: 'vest', label: 'Chaleco' },
    { key: 'badge', label: 'Gafete' },
    { key: 'startDate', label: 'Fecha Inicio' },
    { key: 'endDate', label: 'Fecha Fin' },
    { key: 'totalHours', label: 'Horas Totales' },
    { key: 'cellPhone', label: 'Celular' },
    { key: 'bloodType', label: 'Tipo Sangre' },
    { key: 'allergy', label: 'Alergias' },
    { key: 'birthDate', label: 'Fecha Nacimiento' },
    { key: 'major', label: 'Carrera' },
    { key: 'period', label: 'Periodo' },
    { key: 'tutorName', label: 'Tutor' },
    { key: 'tutorPhone', label: 'Tel. Tutor' },
    { key: 'enrollmentDate', label: 'Fecha Inscripción' },
    { key: 'generalInductionDate', label: 'Inducción General' },
    { key: 'acceptanceLetterId', label: 'Oficio Aceptación' },
    { key: 'completionLetterId', label: 'Oficio Término' },
    { key: 'schedule', label: 'Horario' },
    { key: 'program', label: 'Programa' },
  ];

  const filteredServers = socialServers.filter(
    (server) =>
      (server.name && server.name.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (server.email && server.email.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (server.school && server.school.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (server.parkName && server.parkName.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  const toggleColumn = (key) => {
    setVisibleColumns(prev => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <div className="social-server-list">
      <div className="list-header">
        <input
          type="text"
          placeholder="Buscar por nombre, correo, escuela o parque..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="search-input"
        />
        <div className="column-selector-container" ref={columnSelectorRef}>
          <button
            className="btn-columns"
            onClick={() => setShowColumnSelector(!showColumnSelector)}
          >
            Columnas ▼
          </button>
          {showColumnSelector && (
            <div className="column-selector-dropdown">
              {allColumns.map(col => (
                <label key={col.key} className="column-option">
                  <input
                    type="checkbox"
                    checked={visibleColumns[col.key]}
                    onChange={() => toggleColumn(col.key)}
                  />
                  {col.label}
                </label>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              {visibleColumns.folio && <th>Folio</th>}
              {visibleColumns.name && <th>Nombre</th>}
              {visibleColumns.email && <th>Correo</th>}
              {visibleColumns.school && <th>Escuela</th>}
              {visibleColumns.park && <th>Parque</th>}
              {visibleColumns.status && <th>Estado</th>}
              {visibleColumns.type && <th>Tipo</th>}
              {visibleColumns.vest && <th>Chaleco</th>}
              {visibleColumns.badge && <th>Gafete</th>}
              {visibleColumns.startDate && <th>Fecha Inicio</th>}
              {visibleColumns.endDate && <th>Fecha Fin</th>}
              {visibleColumns.totalHours && <th>Horas</th>}
              {visibleColumns.cellPhone && <th>Celular</th>}
              {visibleColumns.bloodType && <th>Sangre</th>}
              {visibleColumns.allergy && <th>Alergias</th>}
              {visibleColumns.birthDate && <th>Nacimiento</th>}
              {visibleColumns.major && <th>Carrera</th>}
              {visibleColumns.period && <th>Periodo</th>}
              {visibleColumns.tutorName && <th>Tutor</th>}
              {visibleColumns.tutorPhone && <th>Tel. Tutor</th>}
              {visibleColumns.enrollmentDate && <th>Fecha Inscripción</th>}
              {visibleColumns.generalInductionDate && <th>Inducción General</th>}
              {visibleColumns.acceptanceLetterId && <th>Oficio Aceptación</th>}
              {visibleColumns.completionLetterId && <th>Oficio Término</th>}
              {visibleColumns.schedule && <th>Horario</th>}
              {visibleColumns.program && <th>Programa</th>}
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredServers.map((server) => (
              <tr key={server.id}>
                {visibleColumns.folio && <td>{server.id}</td>}
                {visibleColumns.name && <td>{server.name}</td>}
                {visibleColumns.email && <td>{server.email}</td>}
                {visibleColumns.school && <td>{server.school}</td>}
                {visibleColumns.park && <td>{server.parkName}</td>}
                {visibleColumns.status && <td>{server.status}</td>}
                {visibleColumns.type && <td>{server.socialServerType}</td>}
                {visibleColumns.vest && <td>{server.vest === -1 ? 'No entregado' : server.vest}</td>}
                {visibleColumns.badge && <td>{server.badge ? 'Sí' : 'No'}</td>}
                {visibleColumns.startDate && <td>{server.startDate}</td>}
                {visibleColumns.endDate && <td>{server.endDate}</td>}
                {visibleColumns.totalHours && <td>{server.totalHoursRequired}</td>}
                {visibleColumns.cellPhone && <td>{server.cellPhone}</td>}
                {visibleColumns.bloodType && <td>{server.bloodType}</td>}
                {visibleColumns.allergy && <td>{server.allergy}</td>}
                {visibleColumns.birthDate && <td>{server.birthDate}</td>}
                {visibleColumns.major && <td>{server.major}</td>}
                {visibleColumns.period && <td>{server.periodStartDate ? `${server.periodStartDate} - ${server.periodEndDate}` : 'N/A'}</td>}
                {visibleColumns.tutorName && <td>{server.tutorName}</td>}
                {visibleColumns.tutorPhone && <td>{server.tutorPhone}</td>}
                {visibleColumns.enrollmentDate && <td>{server.enrollmentDate}</td>}
                {visibleColumns.generalInductionDate && <td>{server.generalInductionDate}</td>}
                {visibleColumns.acceptanceLetterId && <td>{server.acceptanceLetterId}</td>}
                {visibleColumns.completionLetterId && <td>{server.completionLetterId}</td>}
                {visibleColumns.schedule && <td>{server.schedule ? `${server.schedule.days} ${server.schedule.startTime} - ${server.schedule.endTime}` : 'N/A'}</td>}
                {visibleColumns.program && <td>{server.schedule?.program?.name || 'N/A'}</td>}
                <td>
                  <div className="action-buttons">
                    <button
                      onClick={() => onViewAttendances(server.id)}
                      className="btn-soft-blue"
                      title="Ver Asistencias"
                    >
                      Ver Asistencias
                    </button>
                    <button
                      onClick={() => onEdit(server)}
                      className="btn-soft-yellow"
                      title="Editar"
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => onDelete(server.id)}
                      className="btn-soft-red"
                      title="Eliminar"
                    >
                      Eliminar
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default SocialServerList;
