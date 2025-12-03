# Sistema de Monitoreo en Tiempo Real - UNISON
#### Aplicación de escritorio hecha con Java, SQLite, servidor propio y Arduino (con simulador automático).

<img src="./IMAGENES%20GITHUB/LOGO-OJO-BUHO-NEGRO-2048x1669.jpg" width="430">

## Info de proyecto
- **Lenguaje:** Java 17
- **Tipo:** Desktop App (Swing)
- **Base de datos:** SQLite
- **Estado:** Finalizado

### ¿Para qué nos sirve este proyecto?
- Monitorear en tiempo real tres variables (`x`, `y`, `z`) provenientes de un Arduino.
- Simular datos automáticamente cuando no hay Arduino conectado.
- Guardar todas las lecturas en una base de datos para consultar el **histórico**.
- Visualizar gráficos claros en tiempo real y por rangos de tiempo.
- Mostrar una “terminal” para ver todo lo que hace el servidor paso a paso.

---

# Contenido
- [INTRODUCCIÓN](#introducción)
- [SOLUCIÓN](#solución)
- [TECNOLOGÍAS](#tecnologías)
- [BASE DE DATOS Y SU SEGURIDAD](#base-de-datos-y-su-seguridad)
- [FUNCIONALIDAD POR VISTA](#funcionalidad-por-vista)
- [USO DE IA EN EL DESARROLLO](#uso-de-ia-en-el-desarrollo)
- [CÓMO HACER EL PROYECTO EJECUTABLE](#cómo-hacer-el-proyecto-ejecutable)
- [RETOS Y SOLUCIÓN](#retos-y-solución)
- [LO QUE APRENDÍ MIENTRAS HACÍA TODO EL PROYECTO](#lo-que-aprendí-mientras-hacía-todo-el-proyecto)

---

# INTRODUCCIÓN

En esta práctica se nos pidió desarrollar un **sistema de monitoreo en tiempo real** que leyera datos de un Arduino y los guardara en una base de datos, permitiendo ver tanto el monitoreo en vivo como un histórico de mediciones.

Elegí hacerlo en **Java** porque:

- Es un lenguaje muy usado para aplicaciones de escritorio.
- Tiene buenas librerías para gráficos (JFreeChart) y puertos serie (jSerialComm).
- Es relativamente sencillo empaquetar el proyecto en un `.exe` para que el profesor pueda ejecutarlo sin abrir el código.

La aplicación se centra en tres ideas principales:

1. **Monitoreo en tiempo real**  
   Gráfica que muestra continuamente los datos `x`, `y`, `z` del Arduino.  
   Si no hay Arduino detectado, la app usa un **simulador** automático.

2. **Histórico de datos**  
   Todas las lecturas se guardan en SQLite para consultar por:
    - Día completo.
    - Hora exacta.
    - Minuto exacto.
    - Filtros rápidos (minuto actual, últimos 5 minutos, etc.).

3. **Servidor + terminal**  
   Un servidor interno recibe los datos, los procesa y los guarda en la BD.  
   Además, hay una ventana de **terminal** donde se ve todo lo que va pasando.

### ¿Qué problema resuelve?

- Monitorear señales del Arduino sin depender solo del Serial Monitor.
- Tener un **historial persistente** en una base de datos.
- Poder demostrar el proyecto aunque no haya Arduino disponible (gracias al simulador).
- Tener una interfaz limpia con **modo claro y modo oscuro**.

### ¿En qué contexto se hizo?

- Proyecto final de semestre.
- Requisitos:
    - Uso de Arduino o simulación.
    - Base de datos.
    - Gráficas.
    - Servidor.
    - App ejecutable (`.exe`).

---

# SOLUCIÓN

La solución fue construir una aplicación de escritorio en Java que:

- Inicia un **servidor** interno al arrancar la app.
- Detecta automáticamente el Arduino (puerto CH340 / Arduino) con `jSerialComm`.
- Si no hay Arduino o hay error, cambia automáticamente a **modo simulador**.
- Muestra una pantalla de inicio con:
    - Logo de UNISON.
    - Botones para:
        - Monitor en tiempo real.
        - Histórico de datos.
        - Ver terminal del servidor.
    - Botón para cambiar entre **modo claro** y **modo oscuro**.

+ Tipo de solución:
    - Ejecutable de escritorio: `release/monitor-arduino.exe` (ruta sugerida)

+ Arquitectura general del proyecto:

- **Capa de interfaz (GUI):**
    - `MainApp.java` → arranque de la aplicación.
    - `HomePanel.java` → pantalla de inicio.
    - `MonitorPanel.java` → monitoreo en tiempo real.
    - `HistoricoPanel.java` → consulta de datos históricos.
    - `ServerLogWindow.java` → “terminal” del servidor.
    - `ThemeManager.java` → manejador de modo claro/oscuro.
    - `ImageUtils.java` → carga de imágenes (logo ojo búho, escudo UNISON).

- **Capa lógica / sensores:**
    - `SensorData.java` → modelo con `timestamp`, `x`, `y`, `z`.
    - `SensorDataSource.java` → interfaz para la fuente de datos.
    - `SerialDataSource.java` → lee datos reales desde Arduino.
    - `SimulatorDataSource.java` → genera datos aleatorios cuando no hay Arduino.
    - `SerialManager.java` → escanea puertos y detecta el Arduino.

- **Capa servidor + base de datos:**
    - `ServerCore.java`, `Server.java`, `ServerClient.java` → servidor TCP interno y cliente.
    - `DatabaseManager.java` → manejo de SQLite: creación de tabla, inserciones y consultas.
    - `HistoricalQuickFilter.java` / `HistoricalExactLevel.java` → enums para filtros del histórico.

---

# TECNOLOGÍAS

- **Lenguaje:** Java 17
- **Interfaz gráfica:** `Swing`
- **Gráficas:** `JFreeChart`
- **Puertos serie (Arduino):** `jSerialComm`
- **Base de datos:** `SQLite` (`sqlite-jdbc`)
- **Construcción:** Maven + `maven-shade-plugin`
- **Empaquetado a .exe:** Launch4j

### Estructura de carpetas (resumen)

- `src/main/java/mx/unison/monitor/`
    - `MainApp.java`
    - `HomePanel.java`
    - `MonitorPanel.java`
    - `HistoricoPanel.java`
    - `ServerLogWindow.java`
    - `SerialManager.java`
    - `SerialDataSource.java`
    - `SimulatorDataSource.java`
    - `DatabaseManager.java`
    - `SensorData.java`
    - `ServerCore.java`, `ServerClient.java`, etc.
- `src/main/resources/images/`
    - (imágenes internas que usa la app)
- `IMAGENES GITHUB/`
    - Capturas para este README:
        - `inicio modo dia.png`
        - `inicio modo oscuro.png`
        - `MONITOR TIEMPO REAL.png`
        - `Terminal.png`
        - `HISTORICO FUNCIONANDO.png`
- `monitorBD.db` → base de datos SQLite (se genera al ejecutar).
- `pom.xml` → configuración de Maven.
- `release/monitor-arduino.exe` → ejecutable final (sugerido).

---

# BASE DE DATOS Y SU SEGURIDAD

La base de datos es un archivo SQLite llamado, por ejemplo, `monitorBD.db`.

### Tabla principal: `datos_sensor`

- `id` (PK, autoincremental)
- `timestamp` (`TEXT` en formato ISO, por ejemplo: `2025-12-02T14:30:15.361`)
- `x` (`REAL`)
- `y` (`REAL`)
- `z` (`REAL`)

Cada vez que se recibe un dato:

1. El servidor lo convierte en un objeto `SensorData`.
2. Se ejecuta un `INSERT` en `datos_sensor`.
3. Los módulos de histórico hacen consultas con filtros de fecha/hora.

La “seguridad” aquí se centra en:

- Manejar correctamente los errores de escritura/lectura.
- Evitar que el programa se caiga si hay excepciones de BD o de puerto serie.
- Mantener un registro consistente de todos los datos.

---

# FUNCIONALIDAD POR VISTA

## 1. Pantalla de inicio (`HomePanel.java`)

### Modo día

<img src="./IMAGENES%20GITHUB/inicio%20modo%20dia.png" width="650">

### Modo oscuro

<img src="./IMAGENES%20GITHUB/inicio%20modo%20oscuro.png" width="650">

- Muestra:
    - Título del sistema.
    - Autor: **Zaid Montaño Martínez**.
    - Logo del ojo búho al centro.
- Botones:
    - **Monitor en tiempo real** → abre `MonitorPanel`.
    - **Histórico de datos** → abre `HistoricoPanel`.
    - **Ver terminal del servidor** → abre `ServerLogWindow`.
- Switch de modo **día / noche**:
    - Cambia colores de fondo, textos y botones usando `ThemeManager`.

---

## 2. Monitor en tiempo real (`MonitorPanel.java`)

<img src="./IMAGENES%20GITHUB/MONITOR%20TIEMPO%20REAL.png" width="650">

- Gráfica de líneas con las 3 series: `x`, `y`, `z`.
- Ejes:
    - X → Tiempo.
    - Y → Valor de la lectura.
- Botón **Iniciar/Detener monitoreo**:
    - Cambia texto y color según el estado.
    - Detecta automáticamente si se usa:
        - Modo **REAL (Arduino)** → por puerto serie.
        - Modo **SIMULADOR** → cuando no se encuentra Arduino o hay error.
- Botón **Volver al inicio** para regresar al `HomePanel`.

Internamente:

- Un hilo (`MonitorThread`) se encarga de:
    - Pedir el siguiente dato a la fuente (`SerialDataSource` o `SimulatorDataSource`).
    - Actualizar la gráfica.
    - Mandar el dato al servidor para que se guarde en SQLite.

---

## 3. Histórico de datos (`HistoricoPanel.java`)

<img src="./IMAGENES%20GITHUB/HISTORICO%20FUNCIONANDO.png" width="650">

Permite consultar todo lo que ya se guardó en la base de datos.

Tiene dos formas de filtrar:

1. **Filtro por fecha/hora exacta**
    - Selección de fecha.
    - Selección de hora.
    - Nivel:
        - Día completo.
        - Hora exacta.
        - Minuto exacto.
    - Botón **Consultar por fecha/hora** → arma una consulta específica en `DatabaseManager`.

2. **Filtros rápidos (basados en ahora)**
    - Combo box con opciones:
        - Minuto actual.
        - Últimos 5 minutos.
        - Últimos 10 minutos.
    - Botón **Consultar filtro rápido** → usa un enum (`HistoricalQuickFilter`) para crear la consulta.

En ambos casos, el resultado se dibuja de nuevo en la gráfica de JFreeChart.

---

## 4. Terminal del servidor (`ServerLogWindow.java`)

<img src="./IMAGENES%20GITHUB/Terminal.png" width="650">

- Es una ventana estilo consola con un `JTextArea` de solo lectura.
- Muestra mensajes como:
    - Inicio del servidor.
    - Puertos detectados.
    - Cambios de modo:
        - `*** ARDUINO DETECTADO → CAMBIO A MODO REAL ***`
        - `Se cambió a SIMULADOR por error en la fuente REAL.`
    - Errores de tiempo de espera, etc.

Toda la aplicación manda mensajes aquí usando algo tipo:


# USO DE IA EN EL DESARROLLO

Durante el desarrollo utilicé IA (por ejemplo ChatGPT) para:

- Definir la arquitectura en capas (GUI, lógica, servidor, BD).
- Crear y refinar clases como `SerialManager`, `SimulatorDataSource`, `DatabaseManager`, etc.
- Resolver problemas con:
  - jSerialComm y las librerías nativas (`.dll`).
  - Manejo de hilos sin congelar la interfaz.
  - Configuración de Maven y dependencias (JFreeChart, sqlite-jdbc, jSerialComm).
- Mejorar el diseño de la interfaz: organización de los paneles, modo oscuro, elección de colores, etc.
- Algunos comentarios del código.

La IA se usó como apoyo, pero el código final se probó, ajustó y adaptó manualmente para que el proyecto cumpliera exactamente los requisitos del profesor.

---

# CÓMO HACER EL PROYECTO EJECUTABLE

### Requisitos

- Tener **Java 17** instalado.
- Maven (si se compila desde el código fuente).

### 1. Generar el JAR “fat”

Desde la raíz del proyecto (donde está `pom.xml`):

# RETOS Y SOLUCIÓN

### 1. Problemas con jSerialComm y versiones nuevas de Java

- **Problema:**  
  Al principio probé con un JDK muy nuevo y aparecían errores de librerías nativas (`jSerialComm.dll`), accesos denegados y mensajes de que no se podía inicializar la clase `SerialPort`.
- **Solución:**  
  Cambié el proyecto a **Java 17**, configuré bien la dependencia en Maven y agregué manejo de excepciones.  
  Si jSerialComm falla, el sistema cambia automáticamente a **modo SIMULADOR** y lo avisa en la terminal, en lugar de cerrarse.

---

### 2. Detección automática del Arduino

- **Problema:**  
  El profesor no quería que se eligiera el puerto a mano. A veces el Arduino aparecía en un COM diferente o simplemente no estaba conectado.
- **Solución:**  
  Implementé la clase `SerialManager`, que:
    - Recorre todos los puertos disponibles.
    - Busca nombres como `USB-SERIAL CH340` o similares.
    - Si encuentra el Arduino → pasa a **MODO REAL** y lo muestra en los logs.
    - Si no lo encuentra o hay error → se queda en **SIMULADOR**, también avisando en la terminal.

---

### 3. Timeouts y errores al leer del puerto

- **Problema:**  
  Aparecían muchos mensajes de error como  
  `The read operation timed out before any data was returned`  
  y la app podía terminar mal si no se manejaba bien.
- **Solución:**  
  Traté esos timeouts como algo normal (a veces tarda en llegar un dato).  
  Se captura la excepción, se registra en la terminal y el programa sigue corriendo.  
  Solo si el problema es constante se cambia a modo simulador para no tronar todo.

---

### 4. Hacer que la interfaz no se “congele”

- **Problema:**  
  Si todo se hacía en el mismo hilo de la interfaz, la ventana se trababa cuando se leía del puerto o se consultaba la base de datos.
- **Solución:**  
  Usé **hilos separados** para:
    - El monitoreo en tiempo real.
    - El servidor.  
      Así la interfaz (Swing) se mantiene fluida y solo se actualiza la gráfica con `SwingUtilities.invokeLater()` cuando toca.

---

### 5. Modo oscuro y colores de UNISON

- **Problema:**  
  Al activar modo oscuro, los botones blancos se veían feos o no contrastaban bien, y algunas cosas se veían amontonadas.
- **Solución:**  
  Creé un `ThemeManager` con:
    - Fondo oscuro,
    - Gris `#BBBBBB` para algunas zonas,
    - Azul y dorado tipo UNISON para los botones principales.  
      Además reacomodé los filtros del histórico para que se vieran organizados y no todos pegados.

---

# LO QUE APRENDÍ MIENTRAS HACÍA TODO EL PROYECTO

- Cómo comunicar **Java con Arduino** usando puertos serie y la librería jSerialComm.
- Manejar **hilos** en una aplicación Swing para que la interfaz no se congele mientras se actualizan datos en tiempo real.
- Usar **SQLite** desde Java con `sqlite-jdbc`, crear tablas y hacer consultas filtradas por fecha y hora.
- Diseñar una interfaz con varias “pantallas” dentro del mismo `JFrame` usando `CardLayout` (inicio, monitor, histórico, terminal).
- Generar gráficas en tiempo real e históricas usando **JFreeChart** y actualizar las series sin perder rendimiento.
- Empaquetar un proyecto Maven en un **JAR “fat”** con todas las dependencias y convertirlo a `.exe` usando **Launch4j`.
- La importancia de tener una **terminal de logs** para entender qué está pasando con el servidor, el Arduino y los cambios entre modo real/simulador.
- En general, cómo tomar todos los requisitos del profesor (Arduino, BD, servidor, una sola app, .exe, diseño) y convertirlos en un sistema completo, funcional y presentable para entregar y subir a GitHub.
