package lab9.hilos;

import lab9.estructuras.CentroLogistico;
import lab9.estructuras.EstadoPaquete;
import lab9.estructuras.Paquete;

public class HiloClasificador extends Thread {

    private final CentroLogistico centro;
    private volatile Paquete actual;

    public HiloClasificador(CentroLogistico centro, String nombre) {
        super(nombre);
        this.centro = centro;
    }

    public Paquete getActual() {
        return actual;
    }

    @Override
    public void run() {
        try {
            while (centro.estaActivo()) {
                centro.esperarSiPausado();

                Paquete paquete = centro.extraerPrioridadBloqueante(centro.listaAlmacen);
                actual = paquete;
                paquete.setEstado(EstadoPaquete.CLASIFICANDO);
                centro.registrar(paquete.getCodigo() + " tomado por " + getName());

                Thread.sleep(2000);

                String ruta = determinarRuta(paquete.getCiudad());
                paquete.setRuta(ruta);
                paquete.setEstado(EstadoPaquete.CLASIFICADO);
                centro.registrar(paquete.getCodigo() + " clasificado -> " + ruta);

                centro.agregarBloqueante(centro.listaEmpaquetado, CentroLogistico.CAP_EMPAQUETADO, paquete);
                actual = null;
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    private String determinarRuta(String ciudad) {
        switch (ciudad) {
            case "Tegucigalpa":
            case "Comayaguela":
                return "R1";
            case "San Pedro Sula":
                return "R2";
            case "La Ceiba":
                return "R3";
            case "Choluteca":
                return "R4";
            default:
                return "R1";
        }
    }
}
