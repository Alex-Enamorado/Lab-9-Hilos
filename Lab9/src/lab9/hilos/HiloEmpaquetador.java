package lab9.hilos;

import lab9.estructuras.CentroLogistico;
import lab9.estructuras.EstadoPaquete;
import lab9.estructuras.Paquete;

public class HiloEmpaquetador extends Thread {

    private final CentroLogistico centro;

    public HiloEmpaquetador(CentroLogistico centro, String nombre) {
        super(nombre);
        this.centro = centro;
    }

    @Override
    public void run() {
        try {
            while (centro.estaActivo()) {
                centro.esperarSiPausado();

                Paquete paquete = centro.extraerPrioridadBloqueante(centro.listaEmpaquetado);
                paquete.setEstado(EstadoPaquete.EMPAQUETANDO);
                centro.registrar(paquete.getCodigo() + " empaquetando por " + getName());

                Thread.sleep(tiempoSegunPeso(paquete.getPeso()));

                paquete.setEstado(EstadoPaquete.EMPAQUETADO);
                centro.registrar(paquete.getCodigo() + " empaquetado");

                paquete.setEstado(EstadoPaquete.EN_EXPEDICION);
                centro.agregarBloqueante(centro.listaExpedicion, CentroLogistico.CAP_EXPEDICION, paquete);
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    private int tiempoSegunPeso(double peso) {
        if (peso <= 2) {
            return 1000;
        } else if (peso <= 5) {
            return 2000;
        } else {
            return 3000;
        }
    }
}
