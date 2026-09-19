package lab9.hilos;

import java.util.Random;
import lab9.estructuras.CentroLogistico;
import lab9.estructuras.Prioridad;

public class HiloRecepcion extends Thread {

    private final CentroLogistico centro;
    private final Random random;

    private final String[] clientes = {"Carlos Lopez", "Maria Perez", "Juan Diaz", "Ana Ruiz", "Luis Gomez"};
    private final String[] ciudades = {"Tegucigalpa", "Comayaguela", "San Pedro Sula", "La Ceiba", "Choluteca"};
    private final Prioridad[] prioridades = {Prioridad.NORMAL, Prioridad.NORMAL, Prioridad.ALTA, Prioridad.URGENTE, Prioridad.BAJA};

    public HiloRecepcion(CentroLogistico centro) {
        this.centro = centro;
        this.random = new Random();
    }
}
