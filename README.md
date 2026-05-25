# PECUNIA - Gestión de Finanzas Personales 🐷💸

**Pecunia** es una aplicación móvil nativa para el sistema operativo Android diseñada para empoderar al usuario en la gestión de su economía doméstica. El proyecto nace para mitigar la "desconexión financiera" provocada por los métodos de pago electrónicos y combatir el fenómeno del "gasto hormiga" mediante el registro activo y un sistema proactivo de alertas preventivas.

Proyecto Intermodular para el Ciclo de Desarrollo de Aplicaciones Multiplataforma (Curso 2025-2026).
* **Autora:** Ana Núñez Flores
* **Enlace al Repositorio:** [https://github.com/ananuflo/Pecunia-App.git](https://github.com/ananuflo/Pecunia-App.git)

---

## 🛠️ Especificaciones Técnicas y Arquitectura

La aplicación está desarrollada bajo un enfoque orientado a objetos enfocado en la robustez y escalabilidad, dividida en tres capas principales (Presentación, Negocio y Red):

* **Entorno de Desarrollo (IDE):** Android Studio.
* **Lenguaje de Programación:** Java (utilizando características de Java 8 y POO).
* **Backend y Persistencia (Serverless):** Google Firebase.
    * *Firebase Authentication:* Gestión segura de cuentas e identidades.
    * *Cloud Firestore:* Base de datos NoSQL en tiempo real vinculada mediante el UID único del usuario.
    * *Firebase Cloud Messaging (FCM):* Servicio en segundo plano para el envío de notificaciones push.
* **Servicios Externos (Web Services):** 
    * *API REST de Geoapify:* Consumo asíncrono de localizaciones de cajeros automáticos mediante la librería **OkHttp**.
    * *FusedLocationProviderClient:* Gestión eficiente de coordenadas GPS.
* **Diseño e Interfaz:** Renderizado híbrido de pantallas mediante componentes nativos y estructuras `HTML/WebView`. Identidad gráfica prototipada en Canva.

---

## 📖 MANUAL DE USUARIO (FLUJO DE LA APLICACIÓN)

A continuación se detalla la guía paso a paso para el uso y funcionamiento de **Pecunia**.

### 1. Acceso al Sistema (Login y Registro)
Al iniciar la aplicación por primera vez, el sistema presenta la pantalla de bienvenida donde podrás optar por registrar una nueva cuenta o identificarte con tus credenciales existentes.

* **Registro de cuenta:** En el formulario de registro se solicita tu nombre, fecha de nacimiento, correo electrónico y contraseña. Es el momento crucial donde deberás **elegir tu Rol de Usuario (Básico o Premium)** mediante el selector, lo cual adaptará transversalmente toda la interfaz de la aplicación.
* **Seguridad:** El sistema cuenta con filtros de validación locales (que impiden contraseñas menores a 6 caracteres o campos vacíos) y se sincroniza inmediatamente con Firebase Auth.

### 2. Menú Principal y Selección Temporal
Una vez completado el inicio de sesión con éxito, accederás al panel de control de Pecunia. Lo primero que debes hacer es seleccionar el **Mes y el Año** en el que deseas consultar o guardar tus datos. 

> 📌 **Nota técnica:** Esta selección temporal es captada automáticamente por la aplicación para que todos los movimientos financieros se indexen en su correspondiente fecha dentro de las colecciones de la base de datos.

### 3. Registro de Operaciones (Formularios)
Desde las opciones del menú podrás registrar tus movimientos de dinero de manera manual a través de dos formularios simplificados:

* **Nuevo Ingreso:** Permite introducir una descripción conceptual (ej: *Nómina*) y la cantidad monetaria.
* **Registrar Gasto:** Permite anotar en qué se ha invertido el dinero y el importe exacto.

Ambos formularios capturan la fecha actual de forma automática y añaden los datos directamente a la consola en la nube.

### 4. Sistema de Alertas y Balanza Final (Plan Básico)
Si eres usuario del plan **Pecunia Basic**, al pulsar sobre la opción de **Resumen**, la aplicación calculará matemáticamente la diferencia entre el total de ingresos y el total de gastos del mes seleccionado.

* **Notificaciones Inteligentes:** Si tras registrar un gasto tu balance total es **inferior a 50€**, Pecunia enviará de forma automática un mensaje de alerta directa al sistema de notificaciones de tu dispositivo (*"¡Aviso de Pecunia! Tu balance es de... ¡Ponte un límite!"*). Este umbral actúa como un recordatorio preventivo antes de entrar en descubierto bancario.

### 5. Características Exclusivas (Plan Premium)
Los usuarios que cuenten con el estatus **Premium** disfrutarán de una personalización estética con elementos en tonos dorados y una corona distintiva, además de acceso a módulos avanzados:

* **Clasificación por Categorías:** Permite categorizar los consumos en *Alimentación, Salud, Ocio, Transporte, Compras o Hogar*. Si un usuario Básico intenta acceder a estas opciones, el sistema bloqueará la navegación mostrando un aviso informativo (*"Categoría bloqueada. ¡Hazte Premium!"*).
* **Buscador de Cajeros Automáticos:** Mediante el permiso de ubicación de tu móvil, la app conecta con la API de Geoapify para listar en tiempo real los cajeros más cercanos en un radio de 2km, permitiendo enlazarlos con Google Maps o registrar la retirada en efectivo en el acto.
* **Balance Premium Desglosado:** Muestra una tabla visual interactiva con el gasto exacto acumulado en cada área. Al hacer clic sobre una de las categorías de la tabla, se abrirá una nueva vista detallada listando individualmente cada gasto con su respectiva descripción y fecha.

---

## 📈 Plan de Pruebas e Integridad
Para garantizar el correcto funcionamiento del software bajo condiciones reales, el sistema superó con éxito los siguientes escenarios de prueba:
* **Filtros de Seguridad:** Bloqueo de inicios de sesión ante contraseñas incorrectas o cortas.
* **Consumo Asíncrono de Red:** Prevención del error `NetworkOnMainThreadException` mediante la delegación del procesamiento JSON a hilos secundarios con callbacks asíncronos (`.enqueue()`).
* **Excepciones de Hardware:** Control del comportamiento de la app ante la desactivación manual del GPS del terminal.
