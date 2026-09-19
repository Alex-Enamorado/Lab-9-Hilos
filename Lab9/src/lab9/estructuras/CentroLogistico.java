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

    private volatile boolean activo;
    private volatile boolean pausado;

    public CentroLogistico() {
        listaRecepcion = new ListaEnlazada<>();
        listaAlmacen = new ListaEnlazada<>();
        listaClasificacion = new ListaEnlazada<>();
        listaEmpaquetado = new ListaEnlazada<>();
        listaExpedicion = new ListaEnlazada<>();
        listaEntregados = new ListaEnlazada<>();
        listaDevueltos = new ListaEnlazada<>();
        activo = true;
        pausado = false;
    }

    public boolean estaActivo() {
        return activo;
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
