import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BancoApp {

    /* ==============================
       MODELO Y ENUMS
    ============================== */

    public enum TipoCuenta { CORRIENTE, AHORROS }

    // Excepción específica
    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String msg) { super(msg); }
    }

    // Transacción: registro de movimientos
    public static class Transaccion {
        private final String descripcion;
        private final double monto;
        private final LocalDateTime fecha;

        public Transaccion(String descripcion, double monto) {
            this.descripcion = descripcion;
            this.monto = monto;
            this.fecha = LocalDateTime.now();
        }

        @Override
        public String toString() {
            String f = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            return String.format("[%s] %s: %.2f", f, descripcion, monto);
        }
    }

    // Cuenta Bancaria
    public static class CuentaBancaria {
        private static final AtomicInteger SEQ = new AtomicInteger(1);
        private final int id;
        private final String cliente;
        private final TipoCuenta tipo;
        private double saldo;
        private final List<Transaccion> historial = new ArrayList<>();

        public CuentaBancaria(String cliente, TipoCuenta tipo, double saldoInicial) {
            this.id = SEQ.getAndIncrement();
            this.cliente = Objects.requireNonNull(cliente, "Cliente no puede ser null");
            this.tipo = Objects.requireNonNull(tipo, "Tipo de cuenta no puede ser null");
            this.saldo = Math.max(0.0, saldoInicial);
            historial.add(new Transaccion("Apertura de cuenta", saldoInicial));
        }

        public int getId() { return id; }
        public String getCliente() { return cliente; }
        public TipoCuenta getTipo() { return tipo; }

        public synchronized double getSaldo() { return saldo; }

        public synchronized void depositar(double cantidad) {
            if (cantidad <= 0) throw new IllegalArgumentException("Monto debe ser mayor que 0");
            saldo += cantidad;
            historial.add(new Transaccion("Depósito", cantidad));
        }

        public synchronized void retirar(double cantidad) throws InsufficientFundsException {
            if (cantidad <= 0) throw new IllegalArgumentException("Monto debe ser mayor que 0");
            if (cantidad > saldo) throw new InsufficientFundsException("Saldo insuficiente");
            saldo -= cantidad;
            historial.add(new Transaccion("Retiro", -cantidad));
        }

        public synchronized void registrarTransaccion(String descripcion, double monto) {
            historial.add(new Transaccion(descripcion, monto));
        }

        public List<Transaccion> getHistorial() {
            return Collections.unmodifiableList(historial);
        }

        @Override
        public String toString() {
            return String.format("ID:%d - %s (%s) - Saldo: %.2f", id, cliente, tipo, saldo);
        }
    }

    /* ==============================
       SERVICIOS (Principio de responsabilidad única)
    ============================== */

    // Repositorio de cuentas
    public static class Banco {
        private final Map<Integer, CuentaBancaria> cuentas = new LinkedHashMap<>();

        public CuentaBancaria crearCuenta(String cliente, TipoCuenta tipo, double saldoInicial) {
            CuentaBancaria c = new CuentaBancaria(cliente, tipo, saldoInicial);
            cuentas.put(c.getId(), c);
            return c;
        }

        public Optional<CuentaBancaria> obtenerCuenta(int id) {
            return Optional.ofNullable(cuentas.get(id));
        }

        public Collection<CuentaBancaria> listar() {
            return Collections.unmodifiableCollection(cuentas.values());
        }
    }

    // Servicio de transferencias
    public static class ServicioTransferencias {
        public void transferir(CuentaBancaria origen, CuentaBancaria destino, double monto)
                throws InsufficientFundsException {
            if (origen == null || destino == null)
                throw new IllegalArgumentException("Cuentas inválidas");
            if (monto <= 0)
                throw new IllegalArgumentException("Monto debe ser mayor que 0");
            if (origen == destino)
                throw new IllegalArgumentException("No se puede transferir a la misma cuenta");

            origen.retirar(monto);
            destino.depositar(monto);
            origen.registrarTransaccion("Transferencia a cuenta " + destino.getId(), -monto);
            destino.registrarTransaccion("Transferencia desde cuenta " + origen.getId(), monto);
        }
    }

    // Servicio de intereses/cargos
    public static class ServicioIntereses {
        public void aplicarIntereses(CuentaBancaria cuenta) {
            double tasa = (cuenta.getTipo() == TipoCuenta.AHORROS) ? 0.02 : 0.005;
            double interes = cuenta.getSaldo() * tasa;
            cuenta.depositar(interes);
            cuenta.registrarTransaccion("Interés aplicado (" + (tasa * 100) + "%)", interes);
        }
    }

    /* ==============================
       APLICACIÓN PRINCIPAL (Interfaz por consola)
    ============================== */

    public static void main(String[] args) {
        Banco banco = new Banco();
        ServicioTransferencias transferencias = new ServicioTransferencias();
        ServicioIntereses intereses = new ServicioIntereses();

        
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n========= MENÚ BANCO =========");
                System.out.println("1 - Crear cuenta");
                System.out.println("2 - Consultar saldo");
                System.out.println("3 - Retirar");
                System.out.println("4 - Depositar");
                System.out.println("5 - Listar cuentas");
                System.out.println("6 - Transferir");
                System.out.println("7 - Ver historial");
                System.out.println("8 - Aplicar intereses");
                System.out.println("9 - Salir");
                System.out.print("Seleccione opción: ");

                String linea = sc.nextLine().trim();
                if (linea.isEmpty()) continue;

                int opcion;
                try { opcion = Integer.parseInt(linea); }
                catch (NumberFormatException e) { System.out.println("Opción inválida."); continue; }

                switch (opcion) {
                    case 1 -> crearCuentaFlow(sc, banco);
                    case 2 -> consultarSaldoFlow(sc, banco);
                    case 3 -> retirarFlow(sc, banco);
                    case 4 -> depositarFlow(sc, banco);
                    case 5 -> listarFlow(banco);
                    case 6 -> transferirFlow(sc, banco, transferencias);
                    case 7 -> historialFlow(sc, banco);
                    case 8 -> aplicarInteresesFlow(sc, banco, intereses);
                    case 9 -> { System.out.println("Saliendo..."); return; }
                    default -> System.out.println("Opción no válida.");
                }
            }
        }
    }

    /* ==============================
       MÉTODOS AUXILIARES DE CONSOLA
    ============================== */

    private static void crearCuentaFlow(Scanner sc, Banco banco) {
        System.out.print("Nombre del titular: ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) { System.out.println("Nombre no puede estar vacío."); return; }

        System.out.print("Tipo (1=Corriente, 2=Ahorros): ");
        String t = sc.nextLine().trim();
        TipoCuenta tipo = "2".equals(t) ? TipoCuenta.AHORROS : TipoCuenta.CORRIENTE;

        System.out.print("Saldo inicial: ");
        double saldo;
        try { saldo = Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { System.out.println("Saldo inválido."); return; }

        CuentaBancaria c = banco.crearCuenta(nombre, tipo, saldo);
        System.out.println("Cuenta creada: " + c);
    }

    private static void consultarSaldoFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isPresent()) System.out.println("Saldo actual: " + o.get().getSaldo());
        else System.out.println("Cuenta no encontrada.");
    }

    private static void retirarFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        CuentaBancaria c = o.get();

        System.out.print("Monto a retirar: ");
        try {
            double m = Double.parseDouble(sc.nextLine());
            c.retirar(m);
            System.out.println("Retiro exitoso. Nuevo saldo: " + c.getSaldo());
        } catch (InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Entrada inválida: " + e.getMessage());
        }
    }

    private static void depositarFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        CuentaBancaria c = o.get();

        System.out.print("Monto a depositar: ");
        try {
            double m = Double.parseDouble(sc.nextLine());
            c.depositar(m);
            System.out.println("Depósito exitoso. Nuevo saldo: " + c.getSaldo());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void listarFlow(Banco banco) {
        Collection<CuentaBancaria> cuentas = banco.listar();
        if (cuentas.isEmpty()) { System.out.println("No hay cuentas."); return; }
        cuentas.forEach(System.out::println);
    }

    private static void transferirFlow(Scanner sc, Banco banco, ServicioTransferencias servicio) {
        System.out.print("ID cuenta origen: ");
        int idOrigen = Integer.parseInt(sc.nextLine());
        System.out.print("ID cuenta destino: ");
        int idDestino = Integer.parseInt(sc.nextLine());

        Optional<CuentaBancaria> o1 = banco.obtenerCuenta(idOrigen);
        Optional<CuentaBancaria> o2 = banco.obtenerCuenta(idDestino);
        if (o1.isEmpty() || o2.isEmpty()) {
            System.out.println("Alguna de las cuentas no existe."); return;
        }

        System.out.print("Monto a transferir: ");
        double monto = Double.parseDouble(sc.nextLine());
        try {
            servicio.transferir(o1.get(), o2.get(), monto);
            System.out.println("Transferencia exitosa.");
        } catch (Exception e) {
            System.out.println("Error en transferencia: " + e.getMessage());
        }
    }

    private static void historialFlow(Scanner sc, Banco banco) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        CuentaBancaria c = o.get();

        System.out.println("Historial de transacciones:");
        c.getHistorial().forEach(System.out::println);
    }

    private static void aplicarInteresesFlow(Scanner sc, Banco banco, ServicioIntereses servicio) {
        Optional<CuentaBancaria> o = obtenerCuentaPorId(sc, banco);
        if (o.isEmpty()) { System.out.println("Cuenta no encontrada."); return; }
        servicio.aplicarIntereses(o.get());
        System.out.println("Interés aplicado correctamente. Nuevo saldo: " + o.get().getSaldo());
    }

    private static Optional<CuentaBancaria> obtenerCuentaPorId(Scanner sc, Banco banco) {
        System.out.print("Ingrese ID de cuenta: ");
        try {
            int id = Integer.parseInt(sc.nextLine());
            return banco.obtenerCuenta(id);
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return Optional.empty();
        }
    }
}

    

