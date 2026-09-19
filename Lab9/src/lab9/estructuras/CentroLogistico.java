package lab9.estructuras;

public class CentroLogistico {

    public static final int CAP_RECEPCION = 10;
    public static final int CAP_ALMACEN = 20;
    public static final int CAP_CLASIFICACION = 10;
    public static final int CAP_EMPAQUETADO = 8;
    public static final int CAP_EXPEDICION = 15;

    public final ListaEnlazada<Paquete> listaRecepcion;
    public final ListaEnlazada<Paquete> listaAlmacen;
    public final ListaEnlazada<Paquete> listaClasificacion;
    public final ListaEnlazada<Paquete> listaEmpaquetado;
    public final ListaEnlazada<Paquete> listaExpedicion;
    public final ListaEnlazada<Paquete> listaEntregados;
    public final ListaEnlazada<Paquete> listaDevueltos;
    public final ListaEnlazada<String> listaRegistro;

    private volatile boolean activo;
    private volatile boolean pausado;
    private int contadorPaquetes;
    private long sumaTiemposEntrega;
    private int entregasConTiempo;

    public CentroLogistico() {
        listaRecepcion = new ListaEnlazada<>();
        listaAlmacen = new ListaEnlazada<>();
        listaClasificacion = new ListaEnlazada<>();
        listaEmpaquetado = new ListaEnlazada<>();
        listaExpedicion = new ListaEnlazada<>();
        listaEntregados = new ListaEnlazada<>();
        listaDevueltos = new ListaEnlazada<>();
        listaRegistro = new ListaEnlazada<>();
        activo = true;
        pausado = false;
        contadorPaquetes = 0;
        sumaTiemposEntrega = 0;
        entregasConTiempo = 0;
    }

    public boolean estaActivo() {
        return activo;
    }

    public void registrar(String mensaje) {
        String hora = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        listaRegistro.agregar(hora + " - " + mensaje);
    }

    public synchronized String generarCodigo() {
        contadorPaquetes++;
        return String.format("PKG-%05d", contadorPaquetes);
    }

    public synchronized int getTotalGenerados() {
        return contadorPaquetes;
    }

    public int getTotalEntregados() {
        return listaEntregados.tamano();
    }

    public int getTotalDevueltos() {
        return listaDevueltos.tamano();
    }

    public int getTotalEnProceso() {
        return listaRecepcion.tamano() + listaAlmacen.tamano() + listaClasificacion.tamano()
                + listaEmpaquetado.tamano() + listaExpedicion.tamano();
    }

    public synchronized void registrarTiempoEntrega(Paquete paquete) {
        long duracion = System.currentTimeMillis() - paquete.getTiempoCreacion();
        sumaTiemposEntrega += duracion;
        entregasConTiempo++;
    }

    public synchronized double getTiempoPromedioSegundos() {
        if (entregasConTiempo == 0) {
            return 0;
        }
        return (sumaTiemposEntrega / (double) entregasConTiempo) / 1000.0;
    }

    public void detener() {
        activo = false;
        synchronized (this) {
            notifyAll();
        }
    }

    public synchronized void pausar() {
        pausado = true;
    }

    public synchronized void reanudar() {
        pausado = false;
        notifyAll();
    }

    public synchronized void esperarSiPausado() throws InterruptedException {
        while (pausado) {
            wait();
        }
    }

    public void agregarBloqueante(ListaEnlazada<Paquete> lista, int capacidad, Paquete paquete) throws InterruptedException {
        synchronized (lista) {
            while (lista.tamano() >= capacidad) {
                lista.wait();
            }
            lista.agregar(paquete);
        }
    }

    public Paquete extraerPrioridadBloqueante(ListaEnlazada<Paquete> lista) throws InterruptedException {
        synchronized (lista) {
            while (lista.estaVacia()) {
                lista.wait();
            }
            Paquete mejor = lista.obtener(0);
            for (int i = 1; i < lista.tamano(); i++) {
                Paquete actual = lista.obtener(i);
                if (actual.getPrioridad().ordinal() < mejor.getPrioridad().ordinal()) {
                    mejor = actual;
                }
            }
            lista.eliminar(mejor);
            return mejor;
        }
    }
}
