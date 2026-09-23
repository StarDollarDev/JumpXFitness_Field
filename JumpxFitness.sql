-- =========================================================
-- Base de datos: jumpxfitness_db
-- Proyecto: JumpxFitness_Field
-- Nota: TarifaCancha fue eliminada como tabla independiente.
-- Su información (precio por hora) ahora vive directamente
-- dentro de reservacancha (columna precio_hora).
-- =========================================================

CREATE DATABASE IF NOT EXISTS jumpxfitness_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE jumpxfitness_db;

-- =========================================================
-- PERSONA (base de cliente y usuario)
-- =========================================================
CREATE TABLE persona (
    id_persona   INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(80)  NOT NULL,
    apellido     VARCHAR(80)  NOT NULL,
    documento    VARCHAR(20)  NOT NULL,   -- DNI, CE, etc.
    numeroDoc    VARCHAR(20)  NOT NULL,
    telefono     VARCHAR(20)
);

-- =========================================================
-- CLIENTE
-- =========================================================
CREATE TABLE cliente (
    id_cliente   INT AUTO_INCREMENT PRIMARY KEY,
    id_persona   INT NOT NULL,
    CONSTRAINT fk_cliente_persona FOREIGN KEY (id_persona)
        REFERENCES persona(id_persona) ON DELETE CASCADE
);

-- =========================================================
-- USUARIO (login del sistema)
-- =========================================================
CREATE TABLE usuario (
    id_usuario   INT AUTO_INCREMENT PRIMARY KEY,
    usuario      VARCHAR(50) NOT NULL UNIQUE,
    contraseña   VARCHAR(255) NOT NULL,       -- hash SHA-256
    rol          ENUM('ADMIN','CLIENTE') NOT NULL DEFAULT 'CLIENTE',
    id_persona   INT NOT NULL,
    CONSTRAINT fk_usuario_persona FOREIGN KEY (id_persona)
        REFERENCES persona(id_persona) ON DELETE CASCADE
);

-- =========================================================
-- HORARIO (franjas para Jumping Fitness)
-- =========================================================
CREATE TABLE horario (
    id_horario   INT AUTO_INCREMENT PRIMARY KEY,
    hora_inicio  VARCHAR(10) NOT NULL,
    hora_fin     VARCHAR(10) NOT NULL,
    activo       BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- PLAN JUMPING
-- =========================================================
CREATE TABLE planjumping (
    id_plan            INT AUTO_INCREMENT PRIMARY KEY,
    nombre             VARCHAR(100) NOT NULL,
    precio             DOUBLE NOT NULL,
    cantidad_personas  INT NOT NULL,
    dias_vigencia      INT NOT NULL,
    activo             BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================================================
-- REGISTRO JUMPING (ingreso de un cliente a una sesión)
-- =========================================================
CREATE TABLE registrojumping (
    id_registro    INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente     INT NOT NULL,
    id_plan        INT NOT NULL,
    id_horario     INT NOT NULL,
    metodo_pago    ENUM('YAPE','EFECTIVO') NOT NULL,
    fecha_ingreso  DATE NOT NULL,
    monto          DOUBLE NOT NULL,
    CONSTRAINT fk_registro_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente) ON DELETE CASCADE,
    CONSTRAINT fk_registro_plan FOREIGN KEY (id_plan)
        REFERENCES planjumping(id_plan),
    CONSTRAINT fk_registro_horario FOREIGN KEY (id_horario)
        REFERENCES horario(id_horario)
);

-- =========================================================
-- RESERVA CANCHA
-- (tarifacancha fusionada: precio_hora va directo aquí)
-- =========================================================
CREATE TABLE reservacancha (
    id_reserva       INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente       INT NOT NULL,
    deporte          ENUM('FUTBOL','VOLEY') NOT NULL,
    precio_hora      DOUBLE NOT NULL,
    metodo_pago      ENUM('YAPE','EFECTIVO'),
    fecha            DATE NOT NULL,
    hora_inicio      TIME NOT NULL,
    hora_fin         TIME NOT NULL,
    minutos_reserva  INT NOT NULL,
    monto_adelanto   DOUBLE NOT NULL DEFAULT 0,
    falta_pagar      DOUBLE NOT NULL DEFAULT 0,
    total            DOUBLE NOT NULL DEFAULT 0,
    estado_pago      ENUM('PENDIENTE','PAGADO') NOT NULL DEFAULT 'PENDIENTE',
    estado_reserva   ENUM('PENDIENTE','CONFIRMADO','PAGADA','EN_CURSO','FINALIZADA','CANCELADA') NOT NULL DEFAULT 'CONFIRMADO',
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente) ON DELETE CASCADE
);

-- =========================================================
-- CATEGORIA (de producto)
-- =========================================================
CREATE TABLE categoria (
    id_categoria  INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(80) NOT NULL
);

-- =========================================================
-- PRODUCTO
-- =========================================================
CREATE TABLE producto (
    id_producto    INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100) NOT NULL,
    descripcion    VARCHAR(255),
    precio_compra  DOUBLE NOT NULL,
    precio_venta   DOUBLE NOT NULL,
    stock          INT NOT NULL DEFAULT 0,
    id_categoria   INT,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria(id_categoria)
);

-- =========================================================
-- VENTA
-- =========================================================
CREATE TABLE venta (
    id_venta     INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente   INT NULL,                    -- puede ser venta anónima
    metodo_pago  ENUM('YAPE','EFECTIVO') NOT NULL,
    fecha        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total        DOUBLE NOT NULL,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente) ON DELETE SET NULL
);

-- =========================================================
-- DETALLE VENTA
-- =========================================================
CREATE TABLE detalleventa (
    id_detalle   INT AUTO_INCREMENT PRIMARY KEY,
    id_venta     INT NOT NULL,
    id_producto  INT NOT NULL,
    cantidad     INT NOT NULL,
    precio       DOUBLE NOT NULL,
    subtotal     DOUBLE NOT NULL,
    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta)
        REFERENCES venta(id_venta) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES producto(id_producto)
);

-- =========================================================
-- Datos base opcionales (horarios y planes de ejemplo)
-- =========================================================
INSERT INTO horario (hora_inicio, hora_fin, activo) VALUES
    ('07:00', '08:00', TRUE),
    ('08:00', '09:00', TRUE),
    ('18:00', '19:00', TRUE),
    ('19:00', '20:00', TRUE);

INSERT INTO planjumping (nombre, precio, cantidad_personas, dias_vigencia, activo) VALUES
    ('Clase Suelta', 15.00, 1, 1, TRUE),
    ('Plan Mensual', 120.00, 1, 30, TRUE);
