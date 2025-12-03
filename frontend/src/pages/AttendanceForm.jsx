import React, { useState, useEffect, useCallback } from 'react';
import { attendanceService } from '../services/attendanceService';
import { parkService } from '../services/parkService';
import { ATTENDANCE_TYPES, ATTENDANCE_TYPE_LABELS } from '../utils/constants';
import './AttendanceForm.css';

function AttendanceForm() {
  const [folio, setFolio] = useState('');
  const [parkId, setParkId] = useState('');
  const [type, setType] = useState('');
  const [image, setImage] = useState(null);
  const [parks, setParks] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingParks, setLoadingParks] = useState(true);
  const [location, setLocation] = useState(null); // {latitude, longitude, address}
  const [loadingLocation, setLoadingLocation] = useState(false);
  const [locationError, setLocationError] = useState('');

  useEffect(() => {
    loadParks();
  }, []);

  const loadParks = async () => {
    try {
      setLoadingParks(true);
      const parksData = await parkService.getAll();
      setParks(parksData);
    } catch (err) {
      setError('Error al cargar los parques. Por favor, recarga la página.');
    } finally {
      setLoadingParks(false);
    }
  };

  const getAddressFromCoordinates = useCallback(async (latitude, longitude) => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&accept-language=es`
      );
      const data = await response.json();

      if (data && data.display_name) {
        return data.display_name;
      }

      return `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
    } catch (error) {
      console.error('Error en reverse geocoding:', error);
      return `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;
    }
  }, []);

  const captureLocation = useCallback(() => {
    if (!navigator.geolocation) {
      setLocationError('Tu navegador no soporta geolocalización');
      return;
    }

    setLoadingLocation(true);
    setLocationError('');

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const latitude = position.coords.latitude;
        const longitude = position.coords.longitude;

        const address = await getAddressFromCoordinates(latitude, longitude);

        setLocation({
          latitude,
          longitude,
          address
        });
        setLoadingLocation(false);
      },
      (error) => {
        setLoadingLocation(false);
        switch (error.code) {
          case error.PERMISSION_DENIED:
            setLocationError('Permisos de ubicación denegados. Por favor, permite el acceso a tu ubicación.');
            break;
          case error.POSITION_UNAVAILABLE:
            setLocationError('Ubicación no disponible.');
            break;
          case error.TIMEOUT:
            setLocationError('Tiempo de espera agotado al obtener ubicación.');
            break;
          default:
            setLocationError('Error desconocido al obtener ubicación.');
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    );
  }, [getAddressFromCoordinates]);

  useEffect(() => {
    captureLocation();
  }, [captureLocation]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    if (!folio || !parkId || !type || !image) {
      setError('Por favor, complete todos los campos');
      return;
    }

    setLoading(true);

    try {
      const attendanceData = {
        id: parseInt(folio),
        parkId: parseInt(parkId),
        type: type,
      };

      // Agregar ubicación si está disponible
      if (location) {
        attendanceData.latitude = location.latitude;
        attendanceData.longitude = location.longitude;
        attendanceData.address = location.address;
      }

      const response = await attendanceService.register(attendanceData, image);
      setMessage(response.message || 'Asistencia registrada exitosamente');

      // Limpiar formulario
      setFolio('');
      setParkId('');
      setType('');
      setImage(null);

      // Recapturar ubicación para el siguiente registro
      captureLocation();
    } catch (err) {
      const errorMessage = err.response?.data?.message ||
        err.message ||
        'Error al registrar la asistencia';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="form-container">
      <form onSubmit={handleSubmit} className="attendance-form">
        <img src="/ambu_logo.png" alt="Logo" className="logo" />

        <h2>Registro de Asistencia</h2>

        <div className="form-group">
          <label htmlFor="folio">Folio</label>
          <input
            type="number"
            id="folio"
            placeholder="Ingrese su folio"
            value={folio}
            onChange={(e) => setFolio(e.target.value)}
            required
            min="1"
          />
        </div>

        <div className="form-group">
          <label htmlFor="park">Parque</label>
          {loadingParks ? (
            <select disabled>
              <option>Cargando parques...</option>
            </select>
          ) : (
            <select
              id="park"
              value={parkId}
              onChange={(e) => setParkId(e.target.value)}
              required
            >
              <option value="">Seleccione un parque</option>
              {parks.map((park) => (
                <option key={park.id} value={park.id}>
                  {park.parkName}
                </option>
              ))}
            </select>
          )}
        </div>

        <div className="form-group">
          <label>Ubicación</label>
          {loadingLocation ? (
            <div className="location-status loading">
              📍 Obteniendo ubicación... Por favor espera
            </div>
          ) : locationError ? (
            <div className="location-status error">
              ⚠️ {locationError}
              <button
                type="button"
                onClick={captureLocation}
                className="retry-location-btn"
              >
                Reintentar
              </button>
            </div>
          ) : location ? (
            <div className="location-status success">
              ✅ Ubicación obtenida:<br />
              <small>{location.address}</small>
            </div>
          ) : (
            <div className="location-status">
              Ubicación no disponible
            </div>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="type">Tipo de Registro</label>
          <select
            id="type"
            value={type}
            onChange={(e) => setType(e.target.value)}
            required
          >
            <option value="">Seleccione el tipo</option>
            <option value={ATTENDANCE_TYPES.CHECK_IN}>
              {ATTENDANCE_TYPE_LABELS.CHECK_IN}
            </option>
            <option value={ATTENDANCE_TYPES.CHECK_OUT}>
              {ATTENDANCE_TYPE_LABELS.CHECK_OUT}
            </option>
          </select>
        </div>

        <div className="image-preview">
          {image ? (
            <img
              src={URL.createObjectURL(image)}
              alt="Evidencia"
            />
          ) : (
            <span>Vista previa de la imagen</span>
          )}
        </div>

        <label className="upload-btn">
          Seleccionar Foto
          <input
            type="file"
            accept="image/*"
            hidden
            onChange={(e) => setImage(e.target.files[0])}
            required
          />
        </label>

        {error && <div className="error-message">{error}</div>}
        {message && <div className="success-message">{message}</div>}

        <button type="submit" disabled={loading || loadingLocation} className="submit-btn">
          {loading ? 'Registrando...' : loadingLocation ? 'Esperando ubicación...' : 'Registrar Asistencia'}
        </button>
      </form>
    </div>
  );
}

export default AttendanceForm;

