package lab9.hilos;

import java.util.Random;
import lab9.estructuras.CentroLogistico;
import lab9.estructuras.EstadoPaquete;
import lab9.estructuras.Paquete;
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

    @Override
    public void run() {
        try {
            while (centro.estaActivo()) {
                centro.esperarSiPausado();
                Paquete paquete = generarPaquete();
                centro.agregarBloqueante(centro.listaRecepcion, CentroLogistico.CAP_RECEPCION, paquete);
                centro.registrar(String.format("%s recibido - %s, %s, %s, %.1fkg, %s",
                        paquete.getCodigo(), paquete.getCliente(), paquete.getDireccion(),
                        paquete.getCiudad(), paquete.getPeso(), paquete.getPrioridad()));
                Thread.sleep(200);

                centro.listaRecepcion.eliminar(paquete);
                paquete.setEstado(EstadoPaquete.ALMACENADO);
                centro.registrar(paquete.getCodigo() + " almacenado");
                centro.agregarBloqueante(centro.listaAlmacen, CentroLogistico.CAP_ALMACEN, paquete);

                Thread.sleep(100 + random.nextInt(400));
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    private Paquete generarPaquete() {
        String codigo = centro.generarCodigo();
        String cliente = clientes[random.nextInt(clientes.length)];
        String ciudad = ciudades[random.nextInt(ciudades.length)];
        String direccion = "Calle " + (1 + random.nextInt(50)) + ", " + ciudad;
        double peso = 0.5 + random.nextDouble() * 7.5;
        Prioridad prioridad = prioridades[random.nextInt(prioridades.length)];
        return new Paquete(codigo, cliente, direccion, ciudad, peso, prioridad);
    }
}
