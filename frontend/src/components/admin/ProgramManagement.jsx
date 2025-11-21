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
    const [programForm, setProgramForm] = useState({
        name: '',
        parkId: '',
        totalCapacity: ''
    });
    const [scheduleForm, setScheduleForm] = useState({
        days: '',
        startTime: '09:00',
        endTime: '17:00'
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
        try {
            await programService.createProgram(programForm);
            setShowProgramModal(false);
            setProgramForm({ name: '', parkId: '', totalCapacity: '' });
            loadPrograms();
        } catch (error) {
            console.error('Error creating program:', error);
            alert('Error al crear el programa');
        }
    };

    const handleAddSchedule = async (e) => {
        e.preventDefault();
        try {
            await programService.addSchedule(selectedProgramId, scheduleForm);
            setShowScheduleModal(false);
            setScheduleForm({ days: '', startTime: '09:00', endTime: '17:00' });
            loadPrograms();
        } catch (error) {
            console.error('Error adding schedule:', error);
            alert('Error al agregar horario');
        }
    };

    return (
        <div className="program-management">
            <div className="header-actions">
                <h2>Gestión de Programas</h2>
                <button className="btn-create" onClick={() => setShowProgramModal(true)}>
                    Nuevo Programa
                </button>
            </div>

            <div className="programs-list">
                {programs.map(program => (
                    <div key={program.id} className="program-card">
                        <div className="program-header">
                            <h3>{program.name}</h3>
                            <span className="capacity-badge">
                                {program.currentCapacity}/{program.totalCapacity}
                            </span>
                        </div>
                        <p><strong>Parque:</strong> {program.park.parkName}</p>

                        <div className="schedules-section">
                            <h4>Horarios:</h4>
                            {program.schedules && program.schedules.length > 0 ? (
                                <ul>
                                    {program.schedules.map(schedule => (
                                        <li key={schedule.id}>
                                            {schedule.days}: {schedule.startTime} - {schedule.endTime}
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p>Sin horarios</p>
                            )}
                            <button
                                className="btn-sm btn-secondary"
                                onClick={() => {
                                    setSelectedProgramId(program.id);
                                    setShowScheduleModal(true);
                                }}
                            >
                                Agregar Horario
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {showProgramModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h3>Nuevo Programa</h3>
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
                                <label>Parque</label>
                                <select
                                    value={programForm.parkId}
                                    onChange={e => setProgramForm({ ...programForm, parkId: e.target.value })}
                                    required
                                >
                                    <option value="">Seleccione un parque</option>
                                    {parks.map(park => (
                                        <option key={park.id} value={park.id}>{park.parkName}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Capacidad Total</label>
                                <input
                                    type="number"
                                    value={programForm.totalCapacity}
                                    onChange={e => setProgramForm({ ...programForm, totalCapacity: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="modal-actions">
                                <button type="button" onClick={() => setShowProgramModal(false)}>Cancelar</button>
                                <button type="submit">Guardar</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {showScheduleModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h3>Agregar Horario</h3>
                        <form onSubmit={handleAddSchedule}>
                            <div className="form-group">
                                <label>Días</label>
                                <DaySelector
                                    value={scheduleForm.days}
                                    onChange={(days) => setScheduleForm({ ...scheduleForm, days })}
                                />
                            </div>
                            <div className="form-group">
                                <TimePicker
                                    label="Hora Inicio"
                                    value={scheduleForm.startTime}
                                    onChange={(time) => setScheduleForm({ ...scheduleForm, startTime: time })}
                                />
                            </div>
                            <div className="form-group">
                                <TimePicker
                                    label="Hora Fin"
                                    value={scheduleForm.endTime}
                                    onChange={(time) => setScheduleForm({ ...scheduleForm, endTime: time })}
                                />
                            </div>
                            <div className="modal-actions">
                                <button type="button" onClick={() => setShowScheduleModal(false)}>Cancelar</button>
                                <button type="submit">Guardar</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ProgramManagement;
