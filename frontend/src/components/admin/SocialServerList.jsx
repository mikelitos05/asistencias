import React, { useState, useEffect } from 'react';
import './SocialServerList.css';

// All available columns with metadata (defined outside component)
const ALL_COLUMNS = [
  { key: 'folio', label: 'Folio', type: 'text', filterable: true },
  { key: 'name', label: 'Nombre', type: 'text', filterable: true },
  { key: 'email', label: 'Correo', type: 'text', filterable: true },
  { key: 'school', label: 'Escuela', type: 'text', filterable: true },
  { key: 'program', label: 'Programa', type: 'text', filterable: true },
  { key: 'park', label: 'Parque', type: 'text', filterable: true },
  { key: 'status', label: 'Estado', type: 'select', filterable: true, options: ['ACTIVE', 'INACTIVE', 'COMPLETED'] },
  { key: 'type', label: 'Tipo', type: 'select', filterable: true, options: ['SOCIAL_SERVICE', 'PROFESSIONAL_PRACTICES'] },
  { key: 'photo', label: 'Foto', type: 'image', filterable: false },
  { key: 'vest', label: 'Chaleco', type: 'text', filterable: true },
  { key: 'badge', label: 'Gafete', type: 'select', filterable: true, options: ['Sí', 'No'] },
  { key: 'startDate', label: 'Fecha Inicio', type: 'text', filterable: true },
  { key: 'endDate', label: 'Fecha Fin', type: 'text', filterable: true },
  { key: 'totalHours', label: 'Horas Totales', type: 'text', filterable: true },
  { key: 'cellPhone', label: 'Celular', type: 'text', filterable: true },
  { key: 'bloodType', label: 'Tipo Sangre', type: 'text', filterable: true },
  { key: 'allergy', label: 'Alergias', type: 'text', filterable: true },
  { key: 'birthDate', label: 'Fecha Nacimiento', type: 'text', filterable: true },
  { key: 'major', label: 'Carrera', type: 'text', filterable: true },
  { key: 'period', label: 'Periodo', type: 'text', filterable: true },
  { key: 'tutorName', label: 'Tutor', type: 'text', filterable: true },
  { key: 'tutorPhone', label: 'Tel. Tutor', type: 'text', filterable: true },
  { key: 'enrollmentDate', label: 'Fecha Inscripción', type: 'text', filterable: true },
  { key: 'generalInductionDate', label: 'Inducción General', type: 'text', filterable: true },
  { key: 'acceptanceLetterId', label: 'Oficio Aceptación', type: 'text', filterable: true },
  { key: 'completionLetterId', label: 'Oficio Término', type: 'text', filterable: true },
  { key: 'schedule', label: 'Horario', type: 'text', filterable: true },
];

const SocialServerList = ({ socialServers, onEdit, onDelete, onViewAttendances }) => {
  const [searchTerm, setSearchTerm] = useState('');

  // Load column visibility from localStorage or use defaults
  const getInitialColumnVisibility = () => {
    const saved = localStorage.getItem('socialServerColumnVisibility');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        console.error('Error loading column preferences:', e);
      }
    }
    // Default visibility
    return {
      folio: true,
      name: true,
      email: true,
      school: true,
      program: true,
      park: true,
      status: true,
      type: true,
      photo: false,
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
    };
  };

  // Load column order from localStorage
  const getInitialColumnOrder = () => {
    const saved = localStorage.getItem('socialServerColumnOrder');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        console.error('Error loading column order:', e);
      }
    }
    // Default order with 'program' as 5th element
    return [
      'folio', 'name', 'email', 'school', 'program',
      'park', 'status', 'type', 'photo', 'vest',
      'badge', 'startDate', 'endDate', 'totalHours', 'cellPhone',
      'bloodType', 'allergy', 'birthDate', 'major', 'period',
      'tutorName', 'tutorPhone', 'enrollmentDate', 'generalInductionDate',
      'acceptanceLetterId', 'completionLetterId', 'schedule'
    ];
  };

  const [visibleColumns, setVisibleColumns] = useState(getInitialColumnVisibility);
  const [columnOrder, setColumnOrder] = useState(getInitialColumnOrder);
  const [columnFilters, setColumnFilters] = useState({});
  const [showColumnSelector, setShowColumnSelector] = useState(false);
  const [selectedPhoto, setSelectedPhoto] = useState(null);
  const [draggedColumn, setDraggedColumn] = useState(null);
  const [dragOverColumn, setDragOverColumn] = useState(null);
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

  // Get cell value for filtering
  const getCellValue = (server, columnKey) => {
    switch (columnKey) {
      case 'folio':
        return server.id?.toString() || '';
      case 'name':
        return server.name || '';
      case 'email':
        return server.email || '';
      case 'school':
        return server.school || '';
      case 'park':
        return server.parkName || '';
      case 'status':
        return server.status || '';
      case 'type':
        return server.socialServerType || '';
      case 'vest':
        return server.vest === -1 ? 'No entregado' : server.vest?.toString() || '';
      case 'badge':
        return server.badge ? 'Sí' : 'No';
      case 'startDate':
        return server.startDate || '';
      case 'endDate':
        return server.endDate || '';
      case 'totalHours':
        return server.totalHoursRequired?.toString() || '';
      case 'cellPhone':
        return server.cellPhone || '';
      case 'bloodType':
        return server.bloodType || '';
      case 'allergy':
        return server.allergy || '';
      case 'birthDate':
        return server.birthDate || '';
      case 'major':
        return server.major || '';
      case 'period':
        return server.periodStartDate ? `${server.periodStartDate} - ${server.periodEndDate}` : '';
      case 'tutorName':
        return server.tutorName || '';
      case 'tutorPhone':
        return server.tutorPhone || '';
      case 'enrollmentDate':
        return server.enrollmentDate || '';
      case 'generalInductionDate':
        return server.generalInductionDate || '';
      case 'acceptanceLetterId':
        return server.acceptanceLetterId?.toString() || '';
      case 'completionLetterId':
        return server.completionLetterId?.toString() || '';
      case 'schedule':
        return server.days && server.startTime && server.endTime
          ? `${server.days} ${server.startTime} - ${server.endTime}`
          : '';
      case 'program':
        return server.program || '';
      default:
        return '';
    }
  };

  // Filter servers based on search term and column filters
  const filteredServers = socialServers.filter((server) => {
    // Apply global search
    const matchesSearch = !searchTerm ||
      (server.name && server.name.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (server.email && server.email.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (server.school && server.school.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (server.parkName && server.parkName.toLowerCase().includes(searchTerm.toLowerCase()));

    if (!matchesSearch) return false;

    // Apply column filters
    for (const [columnKey, filterValue] of Object.entries(columnFilters)) {
      if (filterValue && filterValue.trim() !== '') {
        const cellValue = getCellValue(server, columnKey).toLowerCase();
        const filter = filterValue.toLowerCase();
        if (!cellValue.includes(filter)) {
          return false;
        }
      }
    }

    return true;
  });

  const toggleColumn = (key) => {
    setVisibleColumns(prev => {
      const newVisibility = { ...prev, [key]: !prev[key] };
      localStorage.setItem('socialServerColumnVisibility', JSON.stringify(newVisibility));
      return newVisibility;
    });
  };

  // Drag and drop handlers
  const handleDragStart = (e, columnKey) => {
    setDraggedColumn(columnKey);
    e.dataTransfer.effectAllowed = 'move';
    e.currentTarget.style.opacity = '0.5';
  };

  const handleDragOver = (e, columnKey) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    setDragOverColumn(columnKey);
  };

  const handleDragLeave = () => {
    setDragOverColumn(null);
  };

  const handleDrop = (e, dropColumnKey) => {
    e.preventDefault();

    if (!draggedColumn || draggedColumn === dropColumnKey) {
      return;
    }

    const newOrder = [...columnOrder];
    const draggedIndex = newOrder.indexOf(draggedColumn);
    const dropIndex = newOrder.indexOf(dropColumnKey);

    // Remove dragged item and insert at new position
    newOrder.splice(draggedIndex, 1);
    newOrder.splice(dropIndex, 0, draggedColumn);

    setColumnOrder(newOrder);
    localStorage.setItem('socialServerColumnOrder', JSON.stringify(newOrder));
    setDragOverColumn(null);
  };

  const handleDragEnd = (e) => {
    e.currentTarget.style.opacity = '1';
    setDraggedColumn(null);
    setDragOverColumn(null);
  };

  // Filter handlers
  const handleFilterChange = (columnKey, value) => {
    setColumnFilters(prev => ({
      ...prev,
      [columnKey]: value
    }));
  };

  const clearFilter = (columnKey) => {
    setColumnFilters(prev => {
      const newFilters = { ...prev };
      delete newFilters[columnKey];
      return newFilters;
    });
  };

  const clearAllFilters = () => {
    setColumnFilters({});
  };

  const hasActiveFilters = Object.keys(columnFilters).some(key => columnFilters[key] && columnFilters[key].trim() !== '');

  // Get visible columns in order
  const orderedVisibleColumns = columnOrder
    .map(key => ALL_COLUMNS.find(col => col.key === key))
    .filter(col => col && visibleColumns[col.key]);

  // Render cell content
  const renderCellContent = (server, column) => {
    switch (column.key) {
      case 'name':
        return (
          <span
            className="clickable-name"
            onClick={() => onViewAttendances(server.id)}
            title="Ver asistencias"
          >
            {server.name}
          </span>
        );
      case 'photo':
        return server.photoPath ? (
          <img
            src={`http://localhost:8081/${server.photoPath}`}
            alt="Foto"
            className="server-photo-thumb"
            onClick={() => setSelectedPhoto(`http://localhost:8081/${server.photoPath}`)}
          />
        ) : (
          <span className="no-photo">Sin foto</span>
        );
      case 'badge':
        return server.badge ? 'Sí' : 'No';
      case 'vest':
        return server.vest === -1 ? 'No entregado' : server.vest;
      case 'period':
        return server.periodStartDate ? `${server.periodStartDate} - ${server.periodEndDate}` : 'N/A';
      case 'schedule':
        return server.days && server.startTime && server.endTime
          ? `${server.days} ${server.startTime} - ${server.endTime}`
          : 'N/A';
      case 'program':
        return server.program || 'N/A';
      default:
        return getCellValue(server, column.key) || '-';
    }
  };

  // Render filter input for column
  const renderFilterInput = (column) => {
    if (!column.filterable) {
      return <div className="filter-cell"></div>;
    }

    if (column.type === 'select' && column.options) {
      return (
        <div className="filter-cell">
          <select
            className="filter-select"
            value={columnFilters[column.key] || ''}
            onChange={(e) => handleFilterChange(column.key, e.target.value)}
          >
            <option value="">Todos</option>
            {column.options.map(option => (
              <option key={option} value={option}>{option}</option>
            ))}
          </select>
          {columnFilters[column.key] && (
            <button
              className="clear-filter-btn"
              onClick={() => clearFilter(column.key)}
              title="Limpiar filtro"
            >
              ✕
            </button>
          )}
        </div>
      );
    }

    return (
      <div className="filter-cell">
        <input
          type="text"
          className="filter-input"
          placeholder="Filtrar..."
          value={columnFilters[column.key] || ''}
          onChange={(e) => handleFilterChange(column.key, e.target.value)}
        />
        {columnFilters[column.key] && (
          <button
            className="clear-filter-btn"
            onClick={() => clearFilter(column.key)}
            title="Limpiar filtro"
          >
            ✕
          </button>
        )}
      </div>
    );
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
        <div className="header-controls">
          {hasActiveFilters && (
            <button
              className="btn-clear-filters"
              onClick={clearAllFilters}
              title="Limpiar todos los filtros"
            >
              Limpiar Filtros
            </button>
          )}
          <div className="column-selector-container" ref={columnSelectorRef}>
            <button
              className="btn-columns"
              onClick={() => setShowColumnSelector(!showColumnSelector)}
            >
              Columnas ▼
            </button>
            {showColumnSelector && (
              <div className="column-selector-dropdown">
                {ALL_COLUMNS.map(col => (
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
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th className="actions-header">Acciones</th>
              {orderedVisibleColumns.map((column) => (
                <th
                  key={column.key}
                  draggable
                  onDragStart={(e) => handleDragStart(e, column.key)}
                  onDragOver={(e) => handleDragOver(e, column.key)}
                  onDragLeave={handleDragLeave}
                  onDrop={(e) => handleDrop(e, column.key)}
                  onDragEnd={handleDragEnd}
                  className={`draggable-header ${draggedColumn === column.key ? 'dragging' : ''} ${dragOverColumn === column.key ? 'drag-over' : ''}`}
                  title="Arrastra para reordenar"
                >
                  <span className="column-header-content">
                    <span className="drag-handle">⋮⋮</span>
                    {column.label}
                  </span>
                </th>
              ))}
            </tr>
            <tr className="filter-row">
              <td></td>
              {orderedVisibleColumns.map((column) => (
                <td key={`filter-${column.key}`}>
                  {renderFilterInput(column)}
                </td>
              ))}
            </tr>
          </thead>
          <tbody>
            {filteredServers.map((server) => (
              <tr key={server.id}>
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
                {orderedVisibleColumns.map((column) => (
                  <td key={`${server.id}-${column.key}`}>
                    {renderCellContent(server, column)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedPhoto && (
        <div className="modal-overlay" onClick={() => setSelectedPhoto(null)}>
          <div className="photo-modal" onClick={(e) => e.stopPropagation()}>
            <img src={selectedPhoto} alt="Foto completa" className="full-size-photo" />
            <button className="close-btn-photo" onClick={() => setSelectedPhoto(null)}>×</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default SocialServerList;
