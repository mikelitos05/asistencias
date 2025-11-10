import React, { useState } from "react";
import "./AttendanceForm.css"; // <- we'll create this next

function AttendanceForm() {
  const [email, setEmail] = useState("");
  const [registro, setRegistro] = useState("");
  const [image, setImage] = useState(null);
  const [message, setMessage] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();

    const formData = new FormData();
    formData.append("email", email);
    formData.append("registro", registro);
    formData.append("image", image);

    const res = await fetch("http://localhost:8080/api/attendance", {
      method: "POST",
      body: formData,
    });

    if (res.ok) setMessage("Registro enviado correctamente!");
  };

  return (
    <div className="form-container">
      <form onSubmit={handleSubmit} className="attendance-form">
        <img src="/ambu_logo.png" alt="Logo" className="logo" />

        <h2>Bienvenido al Panel de Asistencias</h2>

        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <select
          value={registro}
          onChange={(e) => setRegistro(e.target.value)}
        >
          <option value="">Registro</option>
          <option value="entrada">Entrada</option>
          <option value="salida">Salida</option>
        </select>

        <div className="image-preview">
          {image ? (
            <img
              src={URL.createObjectURL(image)}
              alt="Evidencia"
            />
          ) : (
            <span>Imagen de Evidencia</span>
          )}
        </div>

        <label className="upload-btn">
          Upload
          <input
            type="file"
            hidden
            onChange={(e) => setImage(e.target.files[0])}
          />
        </label>

        <button type="submit">Enviar</button>

        {message && <div className="message">{message}</div>}
      </form>
    </div>
  );
}

export default AttendanceForm;