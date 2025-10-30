# # 🏦 Proyecto: Cuenta Bancaria Mejorada

## 📘 Descripción
Este proyecto amplía el sistema base de **Cuenta Bancaria** aplicando los **principios de Programación Orientada a Objetos (POO)** —*encapsulación, responsabilidad única, apertura/cierre y dependencia invertida*— para crear una aplicación más modular, robusta y mantenible.  

El programa permite gestionar cuentas bancarias desde consola, con operaciones como depósitos, retiros, transferencias, aplicación de intereses y visualización del historial de transacciones.

---

## 🎯 Objetivos del Proyecto
- Aplicar los principios fundamentales de la **POO**.
- Separar responsabilidades mediante clases especializadas.
- Manejar errores con excepciones personalizadas.
- Añadir nuevas funcionalidades que amplíen la lógica del negocio.

---

## 🧩 Nuevas Funcionalidades Implementadas

### 1️⃣ Transferencias entre cuentas
Permite mover dinero de una cuenta a otra, validando:
- Que ambas cuentas existan.  
- Que la cuenta origen tenga saldo suficiente.  
- Que el monto sea positivo.  

**Clase usada:** `ServicioTransferencias`

---

### 2️⃣ Historial de transacciones
Cada movimiento (depósito, retiro, transferencia o interés aplicado) se guarda con:
- Fecha y hora.  
- Descripción de la acción.  
- Monto afectado.  

**Clase usada:** `Transaccion`  
**Beneficio:** Permite auditar las operaciones de cada cuenta.

---

### 3️⃣ Aplicación de intereses
Aplica una tasa según el tipo de cuenta:
- **Ahorros:** 2%  
- **Corriente:** 0.5%

**Clase usada:** `ServicioIntereses`

El interés se suma automáticamente al saldo y se registra en el historial.

---

## 🧱 Estructura del Proyecto
BancoApp.java
├── enum TipoCuenta
├── class InsufficientFundsException
├── class Transaccion
├── class CuentaBancaria
├── class Banco
├── class ServicioTransferencias
├── class ServicioIntereses
└── main() → Interfaz de consola

Cada clase cumple un rol único, promoviendo **responsabilidad única** y **bajo acoplamiento**.

---

## ⚙️ Requisitos de ejecución

**Versión de Java:** 8 o superior  
**Compatibilidad:** Windows, Linux, macOS, y Android (usando apps como *Jvdroid* o *Dcoder*).  

---

## 📱 Ejecución en Android (Redmi)

1. Instala la app **Jvdroid** desde Play Store.  
2. Crea un nuevo proyecto Java.  
3. Crea un archivo llamado `BancoApp.java`.  
4. Copia y pega el código completo del proyecto.  
5. Pulsa **Run ▶️** para ejecutar.  
6. Usa el menú para operar con las cuentas:  

==== BANCO APP ====

Crear cuenta

Consultar saldo

Retirar

Depositar

Listar cuentas

Transferir

Ver historial

Aplicar intereses

Salir


---

## 🧠 Principios POO Aplicados

| Principio | Aplicación en el código |
|------------|-------------------------|
| **Encapsulación** | Los atributos de `CuentaBancaria` son privados, accedidos mediante métodos públicos seguros. |
| **Responsabilidad única** | Cada clase tiene una única función (e.g., `ServicioTransferencias` solo gestiona transferencias). |
| **Abierto/Cerrado** | El código puede extenderse (nuevos servicios) sin modificar las clases base. |
| **Inversión de dependencias** | Los servicios (`Transferencias`, `Intereses`) dependen de abstracciones (`CuentaBancaria`), no de implementaciones concretas. |

---

## ⚠️ Manejo de errores
El sistema cuenta con validaciones y excepciones personalizadas:
- `InsufficientFundsException`: saldo insuficiente.
- `IllegalArgumentException`: montos o entradas inválidas.
- Validaciones de ID y tipo de cuenta.

Mensajes claros guían al usuario ante errores comunes.

---



