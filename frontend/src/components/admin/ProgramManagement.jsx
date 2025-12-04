import React, { useState, useEffect } from 'react';
import { programService } from '../../services/programService';
import { parkService } from '../../services/parkService';
import DaySelector from './DaySelector';
import TimePicker from './TimePicker';
import './ProgramManagement.css';

const ProgramManagement = () => {
    const [programs, setPrograms] = useState([]);
    const [parks, setParks] = useState([]);
    const [showProgramModal, setShowProgramModal] = useState(false);
    const [showScheduleModal, setShowScheduleModal] = useState(false);
    const [selectedProgramId, setSelectedProgramId] = useState(null);

    // Search and Filter states
    const [searchTerm, setSearchTerm] = useState('');
    const [showAvailableOnly, setShowAvailableOnly] = useState(false);
    const [selectedParkFilter, setSelectedParkFilter] = useState('');

    // Edit states
    const [isEditingProgram, setIsEditingProgram] = useState(false);
    const [editingProgramId, setEditingProgramId] = useState(null);
    const [isEditingSchedule, setIsEditingSchedule] = useState(false);
    const [editingScheduleId, setEditingScheduleId] = useState(null);

    const [programForm, setProgramForm] = useState({
        name: '',
        parkIds: []
    });
    const [scheduleForm, setScheduleForm] = useState({
        parkIds: [],
        days: '',
        startTime: '09:00',
        endTime: '17:00',
        capacity: '',
        career: '',
        notes: ''
    });

    useEffect(() => {
        loadPrograms();
        loadParks();
    }, []);

    const loadPrograms = async () => {
        try {
            const data = await programService.getAllPrograms();
            setPrograms(data);
        } catch (error) {
            console.error('Error loading programs:', error);
        }
    };

    const loadParks = async () => {
        try {
            const data = await parkService.getAll();
            setParks(data);
        } catch (error) {
            console.error('Error loading parks:', error);
        }
    };

    const handleCreateProgram = async (e) => {
        e.preventDefault();

        if (programForm.parkIds.length === 0) {
            alert('Debe seleccionar al menos un parque');
            return;
        }

        try {
            if (isEditingProgram) {
                await programService.updateProgram(editingProgramId, programForm);
            } else {
                await programService.createProgram(programForm);
            }
            closeProgramModal();
            loadPrograms();
        } catch (error) {
            console.error('Error saving program:', error);
            alert(error.response?.data?.message || 'Error al guardar el programa');
        }
    };

    const handleAddSchedule = async (e) => {
        e.preventDefault();

        const selectedProgram = programs.find(p => p.id === selectedProgramId);

        if (!selectedProgram) return;

        if (!scheduleForm.parkIds || scheduleForm.parkIds.length === 0) {
            alert('Debe seleccionar al menos un parque');
            return;
        }

        for (const parkId of scheduleForm.parkIds) {
            const parkExists = selectedProgram.parks.some(p => p.id === parkId);
            if (!parkExists) {
                alert(`El parque con ID ${parkId} no pertenece a este programa`);
                return;
            }
        }

        const newCapacity = parseInt(scheduleForm.capacity);
        if (!newCapacity || newCapacity <= 0) {
            alert('La capacidad debe ser un número mayor a 0');
            return;
        }

        try {
            if (isEditingSchedule) {
                await programService.updateSchedule(selectedProgramId, editingScheduleId, scheduleForm);
            } else {
                await programService.addSchedule(selectedProgramId, scheduleForm);
            }
            closeScheduleModal();
            loadPrograms();
        } catch (error) {
            console.error('Error saving schedule:', error);
            alert(error.response?.data?.message || 'Error al guardar horario');
        }
    };

    const handleDeleteProgram = async (programId) => {
        if (!window.confirm('¿Estás seguro de que deseas eliminar este programa? Esta acción no se puede deshacer.')) {
            return;
        }

        try {
            const affectedServers = await programService.deleteProgram(programId);
            loadPrograms();
            if (affectedServers && affectedServers.length > 0) {
                alert(`Se eliminó el programa. Los siguientes servidores sociales fueron desvinculados:\n- ${affectedServers.join('\n- ')}`);
            } else {
                alert('Programa eliminado exitosamente');
            }
        } catch (error) {
            console.error('Error deleting program:', error);
            alert(error.response?.data?.message || 'Error al eliminar el programa');
        }
    };

    const handleDeleteSchedule = async (programId, scheduleId) => {
        if (!window.confirm('¿Estás seguro de que deseas eliminar este horario? Esta acción no se puede deshacer.')) {
            return;
        }

        try {
            const affectedServers = await programService.deleteSchedule(programId, scheduleId);
            loadPrograms();
            if (affectedServers && affectedServers.length > 0) {
                alert(`Se eliminó el horario. Los siguientes servidores sociales fueron desvinculados:\n- ${affectedServers.join('\n- ')}`);
            } else {
                alert('Horario eliminado exitosamente');
            }
        } catch (error) {
            console.error('Error deleting schedule:', error);
            alert(error.response?.data?.message || 'Error al eliminar el horario');
        }
    };

    const openCreateProgramModal = () => {
        setIsEditingProgram(false);
        setEditingProgramId(null);
        setProgramForm({ name: '', parkIds: [] });
        setShowProgramModal(true);
    };

    const openEditProgramModal = (program) => {
        setIsEditingProgram(true);
        setEditingProgramId(program.id);
        setProgramForm({
            name: program.name,
            parkIds: program.parks.map(p => p.id)
        });
        setShowProgramModal(true);
    };

    const closeProgramModal = () => {
        setShowProgramModal(false);
        setIsEditingProgram(false);
        setEditingProgramId(null);
        setProgramForm({ name: '', parkIds: [] });
    };

    const openCreateScheduleModal = (programId) => {
        setSelectedProgramId(programId);
        setIsEditingSchedule(false);
        setEditingScheduleId(null);
        setScheduleForm({ parkIds: [], days: '', startTime: '09:00', endTime: '17:00', capacity: '', career: '', notes: '' });
        setShowScheduleModal(true);
    };

    const openEditScheduleModal = (programId, schedule) => {
        setSelectedProgramId(programId);
        setIsEditingSchedule(true);
        setEditingScheduleId(schedule.id);
        setScheduleForm({
            parkIds: schedule.parkIds || [],
            days: schedule.days,
            startTime: schedule.startTime,
            endTime: schedule.endTime,
            capacity: schedule.capacity,
            career: schedule.career || '',
            notes: schedule.notes || ''
        });
        setShowScheduleModal(true);
    };

    const closeScheduleModal = () => {
        setShowScheduleModal(false);
        setIsEditingSchedule(false);
        setEditingScheduleId(null);
        setScheduleForm({ parkIds: [], days: '', startTime: '09:00', endTime: '17:00', capacity: '', career: '', notes: '' });
    };

    const formatDays = (days) => {
        if (!days) return '';
        const allWeekDays = 'Lunes, Martes, Miércoles, Jueves, Viernes';
        if (days === allWeekDays) {
            return 'Lunes a Viernes';
        }
        return days;
    };

    const handleParkToggle = (parkId) => {
        setProgramForm(prev => {
            const parkIds = [...prev.parkIds];
            const index = parkIds.indexOf(parkId);
            if (index > -1) {
                parkIds.splice(index, 1);
            } else {
                parkIds.push(parkId);
            }
            return { ...prev, parkIds };
        });
    };

    const handleScheduleParkToggle = (parkId) => {
        setScheduleForm(prev => {
            const parkIds = [...prev.parkIds];
            const index = parkIds.indexOf(parkId);
            if (index > -1) {
                parkIds.splice(index, 1);
            } else {
                parkIds.push(parkId);
            }
            return { ...prev, parkIds };
        });
    };
    // Helper to normalize text (remove accents and lowercase)
    const normalizeText = (text) => {
        return text ? text.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase() : '';
    };

    // Filter Logic
    const filteredPrograms = programs.filter(program => {
        const term = normalizeText(searchTerm);
        const matchesName = normalizeText(program.name).includes(term);

        // Check if any schedule has a matching career
        const matchesCareer = program.parks.some(park =>
            park.schedules && park.schedules.some(schedule =>
                normalizeText(schedule.career).includes(term)
            )
        );

        const matchesSearch = matchesName || matchesCareer;
        const matchesAvailability = showAvailableOnly ? program.currentCapacity > 0 : true;

        // Park Filter
        const matchesPark = selectedParkFilter ? program.parks.some(p => p.id === parseInt(selectedParkFilter)) : true;

        return matchesSearch && matchesAvailability && matchesPark;
    });

    return (
        <div className="program-management">
            <div className="header-actions">
                <h2>Gestión de Programas</h2>
                <button className="btn-create" onClick={openCreateProgramModal}>
                    Nuevo Programa
                </button>
            </div>

            {/* Search and Filter Bar */}
            <div className="filters-bar">
                <input
                    type="text"
                    className="search-input"
                    placeholder="Buscar programa..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />

                <select
                    className="filter-select"
                    value={selectedParkFilter}
                    onChange={(e) => setSelectedParkFilter(e.target.value)}
                >
                    <option value="">Todos los parques</option>
                    {parks.map(park => (
                        <option key={park.id} value={park.id}>
                            {park.parkName}
                        </option>
                    ))}
                </select>
                <label className="filter-toggle">
                    <input
                        type="checkbox"
                        checked={showAvailableOnly}
                        onChange={(e) => setShowAvailableOnly(e.target.checked)}
                    />
                    Solo con cupo disponible
                </label>
            </div>

            <div className="programs-list">
                {filteredPrograms.map(program => {
                    // Logic to group unique schedules
                    const uniqueSchedules = [];
                    const scheduleIds = new Set();
                    const parksWithSchedules = new Set();

                    program.parks.forEach(park => {
                        if (park.schedules) {
                            park.schedules.forEach(schedule => {
                                if (!scheduleIds.has(schedule.id)) {
                                    scheduleIds.add(schedule.id);
                                    uniqueSchedules.push(schedule);
                                }
                                if (schedule.parkIds && schedule.parkIds.includes(park.id)) {
                                    parksWithSchedules.add(park.id);
                                }
                            });
                        }
                    });

                    // Group schedules by park combination
                    const groupedSchedules = {};
                    uniqueSchedules.forEach(schedule => {
                        const parkKey = schedule.parkIds ? schedule.parkIds.sort().join(',') : 'unknown';
                        if (!groupedSchedules[parkKey]) {
                            groupedSchedules[parkKey] = {
                                parkIds: schedule.parkIds,
                                schedules: []
                            };
                        }
                        groupedSchedules[parkKey].schedules.push(schedule);
                    });

                    const parksWithoutSchedules = program.parks.filter(p => !parksWithSchedules.has(p.id));

                    return (
                        <div key={program.id} className="program-card">
                            <div className="program-header">
                                <div className="program-info">
                                    <h3>{program.name}</h3>
                                </div>
                                <div className="program-actions">
                                    {/* Reordering buttons removed */}
                                    <span className="capacity-badge">
                                        Capacidad Total: {program.currentCapacity}/{program.totalCapacity}
                                    </span>
                                    <button
                                        className="btn-soft-yellow"
                                        onClick={() => openEditProgramModal(program)}
                                        title="Editar Programa"
                                    >
                                        Editar
                                    </button>
                                    <button
                                        className="btn-soft-red"
                                        onClick={() => handleDeleteProgram(program.id)}
                                        title="Eliminar Programa"
                                    >
                                        Eliminar
                                    </button>
                                </div>
                            </div>

                            <div className="schedules-container">
                                {Object.keys(groupedSchedules).length > 0 ? (
                                    Object.values(groupedSchedules).map((group, groupIndex) => (
                                        <div key={groupIndex} className="schedule-group-card">
                                            <div className="schedule-parks-header">
                                                {group.parkIds && group.parkIds.map(parkId => {
                                                    const park = program.parks.find(p => p.id === parkId);
                                                    return park ? (
                                                        <span key={parkId} className="park-badge-header">{park.parkName}</span>
                                                    ) : null;
                                                })}
                                            </div>
                                            <div className="schedule-content">
                                                {group.schedules.map(schedule => (
                                                    <div key={schedule.id} className="schedule-item-grouped">
                                                        <div className="schedule-main-info">
                                                            <span>
                                                                {formatDays(schedule.days)}: {schedule.startTime} - {schedule.endTime}
                                                                {schedule.capacity && (
                                                                    <span className="schedule-capacity">
                                                                        (Cap: {schedule.currentCapacity !== undefined ? schedule.currentCapacity : schedule.capacity}/{schedule.capacity})
                                                                    </span>
                                                                )}
                                                                {schedule.career && (
                                                                    <span className="schedule-career">
                                                                        • {schedule.career}
                                                                    </span>
                                                                )}
                                                            </span>
                                                            <div className="schedule-actions">
                                                                <button
                                                                    className="btn-soft-yellow"
                                                                    onClick={() => openEditScheduleModal(program.id, schedule)}
                                                                    title="Editar Horario"
                                                                >
                                                                    Editar
                                                                </button>
                                                                <button
                                                                    className="btn-soft-red"
                                                                    onClick={() => handleDeleteSchedule(program.id, schedule.id)}
                                                                    title="Eliminar Horario"
                                                                >
                                                                    Eliminar
                                                                </button>
                                                            </div>
                                                        </div>
                                                        {schedule.notes && (
                                                            <div className="schedule-notes">
                                                                📝 {schedule.notes}
                                                            </div>
                                                        )}
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <p className="no-schedules">No hay horarios configurados.</p>
                                )}
                            </div>

                            {parksWithoutSchedules.length > 0 && (
                                <div className="unused-parks-section">
                                    <h4>Parques sin horarios:</h4>
                                    <div className="parks-badges">
                                        {parksWithoutSchedules.map(park => (
                                            <span key={park.id} className="park-badge gray">{park.parkName}</span>
                                        ))}
                                    </div>
                                </div>
                            )}

                            <button
                                className="btn-sm btn-secondary"
                                onClick={() => openCreateScheduleModal(program.id)}
                                style={{ marginTop: '15px' }}
                            >
                                Agregar Horario
                            </button>
                        </div>
                    );
                })}
            </div>

            {
                showProgramModal && (
                    <div className="modal-overlay">
                        <div className="modal-content">
                            <h3>{isEditingProgram ? 'Editar Programa' : 'Nuevo Programa'}</h3>
                            <form onSubmit={handleCreateProgram}>
                                <div className="form-group">
                                    <label>Nombre</label>
                                    <input
                                        type="text"
                                        value={programForm.name}
                                        onChange={e => setProgramForm({ ...programForm, name: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Parques (seleccione uno o más)</label>
                                    <div className="parks-checkbox-list">
                                        {parks.map(park => (
                                            <label key={park.id} className="checkbox-label">
                                                <input
                                                    type="checkbox"
                                                    checked={programForm.parkIds.includes(park.id)}
                                                    onChange={() => handleParkToggle(park.id)}
                                                />
                                                {park.parkName}
                                            </label>
                                        ))}
                                    </div>
                                </div>
                                <div className="info-message">
                                    <strong>Nota:</strong> La capacidad total se calculará automáticamente según los horarios que agregue.
                                </div>
                                <div className="modal-actions">
                                    <button type="button" onClick={closeProgramModal}>Cancelar</button>
                                    <button type="submit">Guardar</button>
                                </div>
                            </form>
                        </div>
                    </div>
                )
            }

            {
                showScheduleModal && (
                    <div className="modal-overlay">
                        <div className="modal-content">
                            <h3>{isEditingSchedule ? 'Editar Horario' : 'Agregar Horario'}</h3>
                            <form onSubmit={handleAddSchedule}>
                                <div className="form-group">
                                    <label>Parques {!isEditingSchedule && '(puede seleccionar varios)'}</label>
                                    <div className="parks-checkbox-list">
                                        {selectedProgramId && programs.find(p => p.id === selectedProgramId)?.parks.map(park => (
                                            <label key={park.id} className="checkbox-label">
                                                <input
                                                    type="checkbox"
                                                    checked={scheduleForm.parkIds.includes(park.id)}
                                                    onChange={() => handleScheduleParkToggle(park.id)}
                                                    disabled={isEditingSchedule && scheduleForm.parkIds.length > 0 && !scheduleForm.parkIds.includes(park.id)}
                                                />
                                                {park.parkName}
                                            </label>
                                        ))}
                                    </div>
                                </div>
                                <div className="form-group">
                                    <label>Días</label>
                                    <DaySelector
                                        value={scheduleForm.days}
                                        onChange={(days) => setScheduleForm({ ...scheduleForm, days })}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Hora Inicio</label>
                                    <TimePicker
                                        label="Hora Inicio"
                                        value={scheduleForm.startTime}
                                        onChange={(time) => setScheduleForm({ ...scheduleForm, startTime: time })}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Hora Fin</label>
                                    <TimePicker
                                        label="Hora Fin"
                                        value={scheduleForm.endTime}
                                        onChange={(time) => setScheduleForm({ ...scheduleForm, endTime: time })}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Capacidad</label>
                                    <input
                                        type="number"
                                        value={scheduleForm.capacity}
                                        onChange={e => setScheduleForm({ ...scheduleForm, capacity: e.target.value })}
                                        placeholder="Capacidad del horario"
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Carrera <span className="optional-label">(Opcional)</span></label>
                                    <input
                                        type="text"
                                        value={scheduleForm.career}
                                        onChange={e => setScheduleForm({ ...scheduleForm, career: e.target.value })}
                                        placeholder="Ej: Ingeniería en Sistemas"
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Notas <span className="optional-label">(Opcional)</span></label>
                                    <textarea
                                        value={scheduleForm.notes}
                                        onChange={e => setScheduleForm({ ...scheduleForm, notes: e.target.value })}
                                        placeholder="Información adicional sobre el horario..."
                                        rows="3"
                                    />
                                </div>
                                <div className="modal-actions">
                                    <button type="button" onClick={closeScheduleModal}>Cancelar</button>
                                    <button type="submit">Guardar</button>
                                </div>
                            </form>
                        </div>
                    </div>
                )
            }
        </div >
    );
};

export default ProgramManagement;
