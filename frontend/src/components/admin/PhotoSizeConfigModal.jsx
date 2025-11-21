import React, { useState, useEffect } from 'react';
import { appConfigService } from '../../services/appConfigService';
import './PhotoSizeConfigModal.css';

const PhotoSizeConfigModal = ({ isOpen, onClose }) => {
    const [limit, setLimit] = useState(10);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    useEffect(() => {
        if (isOpen) {
            loadLimit();
        }
    }, [isOpen]);

    const loadLimit = async () => {
        try {
            setLoading(true);
            const data = await appConfigService.getPhotoSizeLimit();
            setLimit(data.value);
        } catch (err) {
            setError('Error al cargar la configuración.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        try {
            setLoading(true);
            setError('');
            setSuccess('');
            await appConfigService.updatePhotoSizeLimit(parseInt(limit));
            setSuccess('Configuración guardada exitosamente.');
            setTimeout(() => {
                onClose();
                setSuccess('');
            }, 1500);
        } catch (err) {
            setError('Error al guardar la configuración.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <h2>Configurar Tamaño de Fotos</h2>

                <div className="form-group">
                    <label htmlFor="sizeLimit">Límite de tamaño (MB):</label>
                    <input
                        type="number"
                        id="sizeLimit"
                        value={limit}
                        onChange={(e) => setLimit(e.target.value)}
                        min="1"
                        max="50"
                    />
                    <p className="help-text">
                        Las fotos mayores a este límite serán comprimidas automáticamente.
                    </p>
                </div>

                {error && <div className="error-message">{error}</div>}
                {success && <div className="success-message">{success}</div>}

                <div className="modal-actions">
                    <button onClick={onClose} className="btn-cancel" disabled={loading}>
                        Cancelar
                    </button>
                    <button onClick={handleSave} className="btn-save" disabled={loading}>
                        {loading ? 'Guardando...' : 'Guardar'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PhotoSizeConfigModal;
