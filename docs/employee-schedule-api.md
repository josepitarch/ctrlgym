# Employee Schedule API Documentation

## Overview

La API de Employee Schedule permite gestionar turnos de empleados con soporte para series recurrentes (como Outlook) y eventos aislados. Cada turno está asociado a un centro específico (gym branch).

## Base URL

```
/v1/gyms/{gymId}/branches/{branchId}/schedule
```

## Autenticación

Todos los endpoints requieren autenticación JWT con rol `MANAGER` o `EMPLOYEE`.

---

## Endpoints Disponibles

### 1. Crear Serie Recurrente

Crea una serie de turnos que se repite según un patrón (diario, semanal, mensual) para un centro específico.

**Endpoint:**
```http
POST /v1/gyms/{gymId}/branches/{branchId}/schedule/series
```

**Request Body:**
```json
{
  "employee_id": "uuid",
  "start_time": "09:00:00",
  "end_time": "17:00:00",
  "recurrence_type": "WEEKLY",
  "interval_value": 1,
  "days_of_week": [1, 3, 5],
  "series_start": "2026-09-01",
  "series_end": "2026-12-31"
}
```

**Campos:**
- `employee_id`: UUID del empleado
- `start_time`: Hora de inicio (HH:mm:ss)
- `end_time`: Hora de fin (HH:mm:ss)
- `recurrence_type`: `NONE`, `DAILY`, `WEEKLY`, `MONTHLY`
- `interval_value`: Intervalo de repetición (ej: cada 2 semanas = 2)
- `days_of_week`: Días de la semana (1=Lunes, 7=Domingo). Solo para WEEKLY
- `series_start`: Fecha de inicio de la serie
- `series_end`: Fecha de fin (opcional, si es null genera 3 meses)

**Response:**
```json
{
  "id": 1,
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "gym_id": 1,
  "gym_branch_id": 1,
  "start_time": "09:00:00",
  "end_time": "17:00:00",
  "recurrence_type": "WEEKLY",
  "interval_value": 1,
  "days_of_week": [1, 3, 5],
  "series_start": "2026-09-01",
  "series_end": "2026-12-31"
}
```

**Ejemplos de Recurrencia:**

- **Diario cada 3 días:**
  ```json
  {
    "recurrence_type": "DAILY",
    "interval_value": 3,
    "series_start": "2026-09-01",
    "series_end": "2026-09-30"
  }
  ```

- **Semanal Lunes y Miércoles cada 2 semanas:**
  ```json
  {
    "recurrence_type": "WEEKLY",
    "interval_value": 2,
    "days_of_week": [1, 3],
    "series_start": "2026-09-01",
    "series_end": "2026-12-31"
  }
  ```

- **Mensual día 15:**
  ```json
  {
    "recurrence_type": "MONTHLY",
    "interval_value": 1,
    "series_start": "2026-09-15",
    "series_end": "2027-09-15"
  }
  ```

---

### 2. Crear Evento Aislado

Crea un turno único sin recurrencia para un centro específico.

**Endpoint:**
```http
POST /v1/gyms/{gymId}/branches/{branchId}/schedule/shifts
```

**Request Body:**
```json
{
  "employee_id": "uuid",
  "shift_date": "2026-10-15",
  "start_time": "08:00:00",
  "end_time": "12:00:00"
}
```

**Response:**
```json
{
  "id": 1,
  "series_id": null,
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "gym_id": 1,
  "gym_branch_id": 1,
  "shift_date": "2026-10-15",
  "start_time": "08:00:00",
  "end_time": "12:00:00",
  "status": "SCHEDULED",
  "is_exception": false
}
```

---

### 3. Consultar Series

Obtiene todas las series de un empleado para un centro específico.

**Endpoint:**
```http
GET /v1/gyms/{gymId}/branches/{branchId}/schedule/series?employeeId={uuid}
```

**Response:**
```json
[
  {
    "id": 1,
    "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "gym_id": 1,
    "gym_branch_id": 1,
    "start_time": "09:00:00",
    "end_time": "17:00:00",
    "recurrence_type": "WEEKLY",
    "interval_value": 1,
    "days_of_week": [1, 3, 5],
    "series_start": "2026-09-01",
    "series_end": "2026-12-31"
  }
]
```

---

### 4. Consultar Turnos

Obtiene los turnos de un empleado para un centro específico, opcionalmente filtrados por rango de fechas.

**Endpoint:**
```http
GET /v1/gyms/{gymId}/branches/{branchId}/schedule/shifts?employeeId={uuid}&from={date}&to={date}
```

**Parámetros:**
- `employeeId` (requerido): UUID del empleado
- `from` (opcional): Fecha inicio (YYYY-MM-DD)
- `to` (opcional): Fecha fin (YYYY-MM-DD)

**Response:**
```json
[
  {
    "id": 1,
    "series_id": 1,
    "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "gym_id": 1,
    "gym_branch_id": 1,
    "shift_date": "2026-09-02",
    "start_time": "09:00:00",
    "end_time": "17:00:00",
    "status": "SCHEDULED",
    "is_exception": false
  }
]
```

**Estados de Turno:**
- `SCHEDULED`: Turno programado normalmente
- `CANCELLED`: Turno cancelado (soft delete)
- `MODIFIED`: Turno modificado (excepción de la serie)

---

### 5. Modificar Turno (Crear Excepción)

Modifica un turno específico de una serie sin afectar al resto. Esto crea una excepción.

**Endpoint:**
```http
PUT /v1/gyms/{gymId}/branches/{branchId}/schedule/shifts/{shiftId}
```

**Request Body:**
```json
{
  "shift_date": "2026-09-02",
  "start_time": "10:00:00",
  "end_time": "18:00:00"
}
```

**Comportamiento:**
- Si el turno pertenece a una serie, se marca como `is_exception: true` y `status: MODIFIED`
- Los demás turnos de la serie NO se ven afectados
- Puedes modificar fecha, hora de inicio y/o hora de fin

**Response:**
```json
{
  "id": 1,
  "series_id": 1,
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "gym_id": 1,
  "gym_branch_id": 1,
  "shift_date": "2026-09-02",
  "start_time": "10:00:00",
  "end_time": "18:00:00",
  "status": "MODIFIED",
  "is_exception": true
}
```

---

### 6. Eliminar Turno de Serie (Cancelar)

Cancela un turno específico de una serie (soft delete).

**Endpoint:**
```http
DELETE /v1/gyms/{gymId}/branches/{branchId}/schedule/shifts/{shiftId}
```

**Comportamiento:**
- Si el turno pertenece a una serie, se marca como `status: CANCELLED`
- El turno sigue en la base de datos pero no se muestra en consultas activas
- Los demás turnos de la serie NO se ven afectados

**Response:** `204 No Content`

---

### 7. Eliminar Evento Aislado

Elimina físicamente un turno que no pertenece a ninguna serie.

**Endpoint:**
```http
DELETE /v1/gyms/{gymId}/branches/{branchId}/schedule/shifts/{shiftId}
```

**Comportamiento:**
- Si el turno NO pertenece a una serie (`series_id: null`), se elimina físicamente
- Si pertenece a una serie, se comporta como cancelación (ver punto 6)

**Response:** `204 No Content`

---

### 8. Eliminar Serie Completa

Elimina una serie y todos sus turnos asociados.

**Endpoint:**
```http
DELETE /v1/gyms/{gymId}/branches/{branchId}/schedule/series/{seriesId}
```

**Comportamiento:**
- Elimina la serie completa
- Elimina TODOS los turnos generados por esa serie
- Operación irreversible

**Response:** `204 No Content`

---

## Casos de Uso Comunes

### Caso 1: Crear horario semanal de Lunes a Viernes para un centro

```bash
POST /v1/gyms/1/branches/1/schedule/series
{
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "start_time": "09:00:00",
  "end_time": "17:00:00",
  "recurrence_type": "WEEKLY",
  "interval_value": 1,
  "days_of_week": [1, 2, 3, 4, 5],
  "series_start": "2026-09-01",
  "series_end": "2026-12-31"
}
```

Esto genera automáticamente todos los turnos de Lunes a Viernes desde el 1 de Septiembre al 31 de Diciembre para el centro 1.

### Caso 2: Cambiar un día específico (vacaciones)

Si el empleado tiene vacaciones el día 15 de Septiembre:

```bash
# Primero obtener el turno específico
GET /v1/gyms/1/branches/1/schedule/shifts?employeeId=b2c3d4e5-f6a7-8901-bcde-f12345678901&from=2026-09-15&to=2026-09-15

# Luego eliminarlo
DELETE /v1/gyms/1/branches/1/schedule/shifts/{shiftId}
```

Esto cancela solo ese día, el resto de la serie permanece intacta.

### Caso 3: Modificar horario de un día específico

Si el empleado necesita cambiar su horario el día 20 de Septiembre:

```bash
# Obtener el turno
GET /v1/gyms/1/branches/1/schedule/shifts?employeeId=b2c3d4e5-f6a7-8901-bcde-f12345678901&from=2026-09-20&to=2026-09-20

# Modificarlo
PUT /v1/gyms/1/branches/1/schedule/shifts/{shiftId}
{
  "start_time": "10:00:00",
  "end_time": "18:00:00"
}
```

El turno se marca como excepción (`is_exception: true`, `status: MODIFIED`).

### Caso 4: Agregar turno extra fuera de la serie

Si el empleado necesita trabajar un Sábado adicional:

```bash
POST /v1/gyms/1/branches/1/schedule/shifts
{
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "shift_date": "2026-09-13",
  "start_time": "09:00:00",
  "end_time": "13:00:00"
}
```

Este turno es independiente y no afecta a la serie semanal.

### Caso 5: Cambiar horario permanente

Si necesitas cambiar el horario de toda la serie:

```bash
# Opción 1: Eliminar serie y crear nueva
DELETE /v1/gyms/1/branches/1/schedule/series/{seriesId}
POST /v1/gyms/1/branches/1/schedule/series
{
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "start_time": "10:00:00",
  "end_time": "18:00:00",
  "recurrence_type": "WEEKLY",
  "interval_value": 1,
  "days_of_week": [1, 2, 3, 4, 5],
  "series_start": "2026-10-01",
  "series_end": "2027-12-31"
}

# Opción 2: Modificar turnos individualmente (si son pocos)
# Repetir PUT para cada turno que necesites cambiar
```

### Caso 6: Empleado trabaja en múltiples centros

Si un empleado trabaja en diferentes centros con diferentes horarios:

```bash
# Centro 1 - Lunes, Miércoles, Viernes
POST /v1/gyms/1/branches/1/schedule/series
{
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "start_time": "09:00:00",
  "end_time": "17:00:00",
  "recurrence_type": "WEEKLY",
  "interval_value": 1,
  "days_of_week": [1, 3, 5],
  "series_start": "2026-09-01",
  "series_end": "2026-12-31"
}

# Centro 2 - Martes, Jueves
POST /v1/gyms/1/branches/2/schedule/series
{
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "start_time": "10:00:00",
  "end_time": "18:00:00",
  "recurrence_type": "WEEKLY",
  "interval_value": 1,
  "days_of_week": [2, 4],
  "series_start": "2026-09-01",
  "series_end": "2026-12-31"
}
```

---

## Flujo de Trabajo Recomendado

### Para el Frontend:

1. **Vista de Calendario:**
   - Consultar turnos con `GET /branches/{branchId}/schedule/shifts?from={date}&to={date}`
   - Mostrar turnos con `status: CANCELLED` tachados o en gris
   - Mostrar turnos con `status: MODIFIED` con indicador visual de excepción

2. **Crear Horario Regular:**
   - Usar `POST /branches/{branchId}/schedule/series` para crear patrones recurrentes
   - El sistema genera automáticamente todos los turnos

3. **Modificar Turno Individual:**
   - Usar `PUT /branches/{branchId}/schedule/shifts/{id}` para crear excepciones
   - El sistema marca automáticamente como excepción

4. **Cancelar Turno:**
   - Usar `DELETE /branches/{branchId}/schedule/shifts/{id}` para cancelaciones individuales
   - El sistema hace soft delete si pertenece a serie

5. **Gestionar Series:**
   - Listar series con `GET /branches/{branchId}/schedule/series`
   - Eliminar series completas con `DELETE /branches/{branchId}/schedule/series/{id}`

---

## Notas Importantes

1. **Centro Específico:** Todos los endpoints requieren `gymId` y `branchId` en la URL. Los turnos están asociados a un centro específico.

2. **Generación Automática:** Al crear una serie, el sistema genera automáticamente todos los turnos hasta `series_end` (o 3 meses si es null).

3. **Excepciones:** Los turnos modificados mantienen su `series_id` pero se marcan con `is_exception: true`.

4. **Cancelaciones:** Los turnos cancelados de series no se eliminan físicamente, se marcan con `status: CANCELLED`.

5. **Turnos Aislados:** Los turnos sin serie (`series_id: null`) se eliminan físicamente al hacer DELETE.

6. **Días de Semana:** Usar ISO 8601 (1=Lunes, 7=Domingo).

7. **Horizonte Temporal:** Si `series_end` es null, el sistema genera turnos por 3 meses.

8. **Multi-centro:** Un empleado puede tener series en diferentes centros. Las consultas son siempre por centro.

---

## Códigos de Estado

- `200 OK`: Operación exitosa (GET, PUT)
- `201 Created`: Recurso creado (POST)
- `204 No Content`: Eliminación exitosa (DELETE)
- `400 Bad Request`: Datos inválidos
- `401 Unauthorized`: No autenticado
- `403 Forbidden`: Sin permisos
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error del servidor

---

## Ejemplo Completo de Flujo

```bash
# 1. Crear serie semanal Lun-Mié-Vie para el centro 1
POST /v1/gyms/1/branches/1/schedule/series
{
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "start_time": "09:00:00",
  "end_time": "17:00:00",
  "recurrence_type": "WEEKLY",
  "interval_value": 1,
  "days_of_week": [1, 3, 5],
  "series_start": "2026-09-01",
  "series_end": "2026-09-30"
}
# Response: { "id": 1, "gym_id": 1, "gym_branch_id": 1, ... }

# 2. Consultar turnos de Septiembre para el centro 1
GET /v1/gyms/1/branches/1/schedule/shifts?employeeId=b2c3d4e5-f6a7-8901-bcde-f12345678901&from=2026-09-01&to=2026-09-30
# Response: 13 turnos generados automáticamente

# 3. Cancelar turno del día 5 (viernes)
DELETE /v1/gyms/1/branches/1/schedule/shifts/5
# El turno se marca como CANCELLED

# 4. Modificar turno del día 10 (miércoles) para que sea más corto
PUT /v1/gyms/1/branches/1/schedule/shifts/10
{
  "start_time": "09:00:00",
  "end_time": "13:00:00"
}
# El turno se marca como MODIFIED con is_exception: true

# 5. Agregar turno extra el sábado 13
POST /v1/gyms/1/branches/1/schedule/shifts
{
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "shift_date": "2026-09-13",
  "start_time": "10:00:00",
  "end_time": "14:00:00"
}
# Se crea un turno independiente sin serie

# 6. Consultar nuevamente
GET /v1/gyms/1/branches/1/schedule/shifts?employeeId=b2c3d4e5-f6a7-8901-bcde-f12345678901&from=2026-09-01&to=2026-09-30
# Response: 14 turnos (13 originales - 1 cancelado + 1 extra)

# 7. Crear serie para el mismo empleado en otro centro
POST /v1/gyms/1/branches/2/schedule/series
{
  "employee_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "start_time": "14:00:00",
  "end_time": "18:00:00",
  "recurrence_type": "WEEKLY",
  "interval_value": 1,
  "days_of_week": [2, 4],
  "series_start": "2026-09-01",
  "series_end": "2026-09-30"
}
# El empleado ahora tiene horarios en ambos centros
```
