package lab9.hilos;

import java.util.Random;
import lab9.estructuras.CentroLogistico;
import lab9.estructuras.EstadoPaquete;
import lab9.estructuras.EstadoRepartidor;
import lab9.estructuras.ListaEnlazada;
import lab9.estructuras.Paquete;

public class Repartidor extends Thread {

    private final CentroLogistico centro;
    private final int capacidad;
    private final String ruta;
    private final ListaEnlazada<Paquete> cargaActual;
    private final Random random;
    private volatile EstadoRepartidor estado;
    private int entregados;

    public Repartidor(CentroLogistico centro, String nombre, int capacidad, String ruta) {
        super(nombre);
        this.centro = centro;
        this.capacidad = capacidad;
        this.ruta = ruta;
        this.cargaActual = new ListaEnlazada<>();
        this.random = new Random();
        this.estado = EstadoRepartidor.DISPONIBLE;
        this.entregados = 0;
    }

    @Override
    public void run() {
        try {
            while (centro.estaActivo()) {
                centro.esperarSiPausado();
                estado = EstadoRepartidor.DISPONIBLE;
                cargarCamion();

                if (cargaActual.estaVacia()) {
                    Thread.sleep(1000);
                    continue;
                }
                entregar();
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    private void entregar() throws InterruptedException {
        estado = EstadoRepartidor.EN_RUTA;
        centro.registrar(getName() + " sale a " + ruta + " con " + cargaActual.tamano() + " paquetes");
        Thread.sleep(2000);

        while (!cargaActual.estaVacia()) {
            Paquete paquete = cargaActual.obtener(0);
            cargaActual.eliminar(paquete);
            estado = EstadoRepartidor.ENTREGANDO;
            paquete.setEstado(EstadoPaquete.EN_REPARTO);
            Thread.sleep(500);

            boolean exito = random.nextInt(10) < 8;
            if (exito) {
                paquete.setEstado(EstadoPaquete.ENTREGADO);
                centro.listaEntregados.agregar(paquete);
                centro.registrarTiempoEntrega(paquete);
                entregados++;
                centro.registrar(paquete.getCodigo() + " entregado por " + getName());
            } else {
                paquete.incrementarIntentos();
                centro.registrar(paquete.getCodigo() + " cliente ausente (intento " + paquete.getIntentos() + ")");
                if (paquete.getIntentos() >= 3) {
                    paquete.setEstado(EstadoPaquete.DEVUELTO);
                    centro.listaDevueltos.agregar(paquete);
                    centro.registrar(paquete.getCodigo() + " devuelto");
                } else {
                    paquete.setEstado(EstadoPaquete.NUEVO_INTENTO);
                    centro.agregarBloqueante(centro.listaExpedicion, CentroLogistico.CAP_EXPEDICION, paquete);
                }
            }
        }

        estado = EstadoRepartidor.REGRESANDO;
        Thread.sleep(1000);
    }

    public EstadoRepartidor getEstado() {
        return estado;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getCargaActual() {
        return cargaActual.tamano();
    }

    public int getEntregados() {
        return entregados;
    }

    private void cargarCamion() {
        estado = EstadoRepartidor.CARGANDO;
        synchronized (centro.listaExpedicion) {
            while (cargaActual.tamano() < capacidad) {
                Paquete paquete = centro.listaExpedicion.buscar(p -> p.getRuta().equals(ruta));
                if (paquete == null) {
                    break;
                }
                centro.listaExpedicion.eliminar(paquete);
                cargaActual.agregar(paquete);
            }
        }
    }
}
