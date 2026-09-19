package lab9.estructuras;

public class Paquete {

    private final String codigo;
    private final String cliente;
    private final String direccion;
    private final String ciudad;
    private final double peso;
    private final Prioridad prioridad;
    private EstadoPaquete estado;
    private String ruta;
    private int intentos;
    private final long tiempoCreacion;

    public Paquete(String codigo, String cliente, String direccion, String ciudad, double peso, Prioridad prioridad) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.direccion = direccion;
        this.ciudad = ciudad;
        this.peso = peso;
        this.prioridad = prioridad;
        this.estado = EstadoPaquete.RECIBIDO;
        this.ruta = null;
        this.intentos = 0;
        this.tiempoCreacion = System.currentTimeMillis();
    }

    public long getTiempoCreacion() {
        return tiempoCreacion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCliente() {
        return cliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public double getPeso() {
        return peso;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public EstadoPaquete getEstado() {
        return estado;
    }

    public void setEstado(EstadoPaquete nuevo) {
        if (!esTransicionValida(this.estado, nuevo)) {
            throw new IllegalStateException("Transicion invalida: " + this.estado + " -> " + nuevo);
        }
        this.estado = nuevo;
    }

    private boolean esTransicionValida(EstadoPaquete actual, EstadoPaquete nuevo) {
        switch (actual) {
            case RECIBIDO:
                return nuevo == EstadoPaquete.ALMACENADO;
            case ALMACENADO:
                return nuevo == EstadoPaquete.CLASIFICANDO;
            case CLASIFICANDO:
                return nuevo == EstadoPaquete.CLASIFICADO;
            case CLASIFICADO:
                return nuevo == EstadoPaquete.EMPAQUETANDO;
            case EMPAQUETANDO:
                return nuevo == EstadoPaquete.EMPAQUETADO;
            case EMPAQUETADO:
                return nuevo == EstadoPaquete.EN_EXPEDICION;
            case EN_EXPEDICION:
                return nuevo == EstadoPaquete.EN_REPARTO;
            case EN_REPARTO:
                return nuevo == EstadoPaquete.ENTREGADO || nuevo == EstadoPaquete.NUEVO_INTENTO;
            case NUEVO_INTENTO:
                return nuevo == EstadoPaquete.EN_EXPEDICION || nuevo == EstadoPaquete.DEVUELTO;
            default:
                return false;
        }
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public int getIntentos() {
        return intentos;
    }

    public void incrementarIntentos() {
        this.intentos++;
    }
}
