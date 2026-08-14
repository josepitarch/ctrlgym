# CtrlGym Backend

## Arquitectura

El proyecto se organiza en tres módulos principales:

### Core

Contiene la lógica de dominio, controladores, servicios y repositorios. Es el módulo central del sistema y agrupa los siguientes casos de uso:

- **MemberUseCase** — Ciclo de vida del socio: alta, consulta, inicialización/cambio/baja de abono, accesos, resumen de asistencia, rutinas, entrenamientos, facturas, generación de QR de acceso y descarga de PDF de facturas.
- **GymUseCase** — Administración del gimnasio: gestión de sedes, socios, retención, facturas, planes de abono (sincronizados con Stripe), ocupación actual, ejercicios y rutinas a nivel de gimnasio.
- **DashboardUseCase** — Analítica y datos de panel: ocupación, métricas de abonos, cohortes, retención vs cancelación, gastos, flujo de caja, distribución de socios y motivos de baja.
- **ControllerUseCase** — Gestión de controladores hardware: heartbeat de las Raspberry Pi, registro de accesos de socios y estado de salud del dispositivo.

### Payments

Integración con **Stripe** para la gestión de suscripciones, facturación y cobros. Toda la parte de pagos está delegada en Stripe: creación de clientes, productos, suscripciones y emisión de facturas.

El módulo expone un endpoint de **webhook** (`POST /v1/payments/webhook`) que escucha los siguientes eventos de Stripe:

| Evento | Descripción |
|---|---|
| `setup_intent.succeeded` | Validación de IBAN completada |
| `invoice.finalized` | Creación del registro local de factura |
| `payment_intent.processing` | Pago en proceso |
| `invoice.payment_succeeded` | Pago completado — publica `InvoicePaidEvent` |
| `invoice.payment_failed` | Pago fallido definitivamente — publica `InvoiceFailedEvent` |
| `customer.subscription.updated` | Cambio de plan de suscripción |

### Verifactu

Integración con el servicio [verifacti.com](https://www.verifacti.com/) para el envío de facturas a la AEAT bajo la regulación Verifactu (España).

Cuando se recibe un `InvoicePaidEvent` (pago completado en Stripe), el servicio construye la factura y la envía a la API de verifacti.com (`POST /create`). También permite consultar el estado de una factura ya enviada (`GET /status`) y reenviar facturas manualmente desde el panel de administración.

## Seguridad

La configuración de seguridad se encuentra en `SecurityConfig.java` y define dos cadenas de filtros:

1. **API Key** (`/v1/controllers/**`) — Autenticación por API key para los controladores hardware (Raspberry Pi). Stateless, sin CSRF.

2. **OAuth2 / JWT** — El resto de endpoints se autentican mediante tokens JWT (OAuth2 Resource Server). Los roles definidos son:

| Rol | Endpoints |
|---|---|
| `MANAGER` | `/v1/dashboard/**`, `/admin/**` |
| `MEMBER` | `/v1/users/**` |
| Autenticado (cualquier rol) | El resto de rutas |
| Público (sin autenticación) | `/public/**`, `/v1/payments/webhook`, `/v1/auth/**`, `/v1/gyms/*/routines`, `/health` |

Además, a nivel de método se usa `@PreAuthorize` para restringir el acceso dentro de `/v1/gyms/**` combinando el rol del usuario con la pertenencia al gimnasio (`#gymId == authentication.gymId`). Los roles que intervienen son:

| Rol | Operaciones permitidas |
|---|---|
| `MANAGER` | Operaciones de escritura: crear/eliminar ejercicios, crear/eliminar planes de abono, crear/eliminar rutinas |
| `EMPLOYEE` | Operaciones de lectura y consulta: listar socios, retención de socios, facturas, descargar informe PDF de facturas |
| Cualquier autenticado del gimnasio | Consultar sedes, ocupación actual, ejercicios y planes de abono |

CORS configurado para `http://localhost:5173` y `https://app.ctrlgym.es`.

## Firma asimétrica de tokens QR (acceso a gimnasios)

### Contexto

El backend genera un JWT que se codifica en un código QR. Cada Raspberry Pi
desplegada en un gimnasio escanea ese QR, verifica el token de forma **offline**
y, si es válido, activa el relé de apertura.

El token incluye:
- `sub`: id del socio
- `gym_branches`: lista de ids de gimnasio donde el token es válido
- `iat` / `exp`: fecha de emisión y expiración

### Por qué firma asimétrica y no HS256

Con HS256 la misma clave secreta firma y verifica. Como la Raspberry Pi debe
poder **verificar** el token sin conexión, esa clave tendría que vivir en el
propio dispositivo — un dispositivo con acceso físico público en un gimnasio
(SD card extraíble, carcasa desatornillable, posible acceso SSH).

Si esa clave se extrae, cualquiera puede **fabricar tokens válidos** con
cualquier `gym_branches` y cualquier fecha de expiración, sin que el backend
se entere jamás.

Con un algoritmo asimétrico (ES256), el backend firma con la **clave privada**
y la Raspberry Pi verifica con la **clave pública**. Extraer la clave pública
de un dispositivo no permite fabricar tokens nuevos — solo verificarlos.

Se elige **ES256 (EC / curva prime256v1)** en vez de RS256 (RSA) porque las
claves y firmas son mucho más pequeñas y la verificación es más rápida en un
dispositivo con poca CPU como la Raspberry Pi, y porque el JWT resultante es
más corto (más fácil de codificar en un QR legible).

### 1. Generar el par de claves (una sola vez)

Este par de claves es **estable en el tiempo** — no se regenera en cada
arranque del backend. Se genera una vez, fuera de la aplicación:

```bash
# 1. Clave privada (formato SEC1)
openssl ecparam -genkey -name prime256v1 -noout -out ec-private.pem

# 2. Convertir a PKCS8 (formato que espera KeyFactory en Java)
openssl pkcs8 -topk8 -nocrypt -in ec-private.pem -out ec-private-pkcs8.pem

# 3. Extraer la clave pública correspondiente
openssl ec -in ec-private.pem -pubout -out ec-public.pem
```

Resultado:
- `ec-private-pkcs8.pem` → **secreto**. Solo la usa el backend para firmar.
- `ec-public.pem` → no sensible. Se distribuye a todas las Raspberry Pi para
  que verifiquen los tokens.

⚠️ Una vez generado, **guarda ambos ficheros en un lugar seguro** (por
ejemplo un gestor de contraseñas o vault) antes de borrarlos del disco local.
Si se pierde la clave privada, hay que rotar el par completo y volver a
desplegar la clave pública en todas las Raspberry Pi.

### 2. Backend — despliegue de la clave privada

Se ha elegido subir la clave privada como **variable de entorno** al
proveedor de nube (en vez de como fichero en la imagen o en el repositorio).

**Nunca hacer:**
- Subir el `.pem` al repositorio Git
- Copiarlo dentro de la imagen Docker (`COPY ec-private-pkcs8.pem ...`)
- Pegarlo en texto plano en sitios sin control de acceso

**Cómo setear la variable de entorno**

El valor de la variable `JWT_PRIVATE_KEY` es el contenido completo del
fichero `ec-private-pkcs8.pem`, incluyendo las líneas `-----BEGIN/END-----`:

```bash
# Ejemplo local con Docker
docker run -e JWT_PRIVATE_KEY="$(cat ec-private-pkcs8.pem)" tu-imagen
```

En el proveedor cloud, se pega el contenido del `.pem` tal cual como valor de
la variable de entorno del servicio (la mayoría de plataformas — Kubernetes
Secrets, ECS, Railway, etc. — soportan valores multilinea sin problema).

> Nota: esto es un paso intermedio razonable. Si el proyecto pasa a manejar
> datos reales de usuarios en producción, se recomienda migrar a un gestor de
> secretos (AWS Secrets Manager, GCP Secret Manager, Azure Key Vault,
> HashiCorp Vault) en vez de variable de entorno plana.

### 3. Rotación de claves (futuro)

Si en algún momento se necesita rotar el par de claves, considerar añadir un
claim `kid` (key id) al JWT para que la Raspberry Pi pueda tener varias
claves públicas vigentes durante la transición, en vez de dejar de validar
tokens antiguos de golpe en el momento del cambio.
