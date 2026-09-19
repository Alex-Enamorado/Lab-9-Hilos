package lab9.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import lab9.estructuras.CentroLogistico;
import lab9.estructuras.EstadoRepartidor;
import lab9.estructuras.ListaEnlazada;
import lab9.estructuras.Paquete;
import lab9.estructuras.Prioridad;
import lab9.hilos.HiloClasificador;
import lab9.hilos.HiloEmpaquetador;
import lab9.hilos.HiloRecepcion;
import lab9.hilos.Repartidor;

public class VentanaPrincipal extends JFrame {

    private JButton btnIniciar;
    private JButton btnPausar;
    private JButton btnReanudar;
    private JButton btnDetener;
    private JButton btnReiniciar;
    private JButton btnEstadisticas;

    private JTextPane areaRecepcion;
    private JTextPane areaAlmacen;
    private JTextArea areaClasificacion;
    private JTextArea areaEmpaquetado;
    private JTextPane areaExpedicion;
    private JTextPane areaRepartidores;
    private JTextArea areaRegistro;

    private JProgressBar barraRecepcion;
    private JProgressBar barraAlmacen;
    private JProgressBar barraEmpaquetado;
    private JProgressBar barraExpedicion;

    private CentroLogistico centro;
    private ListaEnlazada<Thread> hilos = new ListaEnlazada<>();
    private final Timer timerRefresco = new Timer(500, e -> actualizarPaneles());

    public VentanaPrincipal() {
        super("Sistema de Paqueteria");
        construirInterfaz();
        configurarBotones();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        timerRefresco.start();
    }

    private void actualizarPaneles() {
        if (centro == null) {
            return;
        }
        actualizarListaConColor(areaRecepcion, centro.listaRecepcion);
        actualizarListaConColor(areaAlmacen, centro.listaAlmacen);
        areaClasificacion.setText(formatearClasificadores());
        areaEmpaquetado.setText(formatearEmpaquetadores());
        actualizarExpedicionConColor();
        actualizarRepartidoresConColor();

        actualizarBarra(barraRecepcion, centro.listaRecepcion.tamano(), CentroLogistico.CAP_RECEPCION);
        actualizarBarra(barraAlmacen, centro.listaAlmacen.tamano(), CentroLogistico.CAP_ALMACEN);
        actualizarBarra(barraEmpaquetado, centro.listaEmpaquetado.tamano(), CentroLogistico.CAP_EMPAQUETADO);
        actualizarBarra(barraExpedicion, centro.listaExpedicion.tamano(), CentroLogistico.CAP_EXPEDICION);

        areaRegistro.setText(formatearRegistro());
        areaRegistro.setCaretPosition(areaRegistro.getDocument().getLength());
    }

    private void actualizarBarra(JProgressBar barra, int cantidad, int capacidad) {
        barra.setMaximum(capacidad);
        barra.setValue(cantidad);
        boolean lleno = cantidad >= capacidad;
        barra.setString(cantidad + "/" + capacidad + (lleno ? " [LLENO]" : ""));
        barra.setForeground(lleno ? new Color(192, 57, 43) : new Color(39, 174, 96));
    }

    private Color colorPrioridad(Prioridad prioridad) {
        switch (prioridad) {
            case URGENTE:
                return new Color(192, 57, 43);
            case ALTA:
                return new Color(230, 126, 34);
            case NORMAL:
                return new Color(184, 134, 11);
            default:
                return new Color(39, 174, 96);
        }
    }

    private void escribirColor(JTextPane pane, String texto, Color color) {
        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet atributos = new SimpleAttributeSet();
        StyleConstants.setForeground(atributos, color);
        try {
            doc.insertString(doc.getLength(), texto, atributos);
        } catch (BadLocationException e) {
            // no puede ocurrir: siempre insertamos al final del documento
        }
    }

    private void actualizarListaConColor(JTextPane pane, ListaEnlazada<Paquete> lista) {
        pane.setText("");
        lista.recorrer(p -> {
            Color color = colorPrioridad(p.getPrioridad());
            escribirColor(pane, "● ", color);
            escribirColor(pane, p.getCodigo() + "\n", Color.BLACK);
        });
    }

    private void actualizarExpedicionConColor() {
        areaExpedicion.setText("");
        String[] rutas = {"R1", "R2", "R3", "R4"};
        for (String ruta : rutas) {
            escribirColor(areaExpedicion, "Ruta " + ruta + ": ", Color.BLACK);
            centro.listaExpedicion.recorrer(p -> {
                if (ruta.equals(p.getRuta())) {
                    escribirColor(areaExpedicion, "● ", colorPrioridad(p.getPrioridad()));
                    escribirColor(areaExpedicion, p.getCodigo() + " ", Color.BLACK);
                }
            });
            escribirColor(areaExpedicion, "\n", Color.BLACK);
        }
    }

    private Color colorEstadoRepartidor(EstadoRepartidor estado) {
        switch (estado) {
            case DISPONIBLE:
                return new Color(39, 174, 96);
            case FUERA_DE_SERVICIO:
                return new Color(192, 57, 43);
            case EN_RUTA:
            case ENTREGANDO:
                return new Color(41, 128, 185);
            default:
                return Color.BLACK;
        }
    }

    private void actualizarRepartidoresConColor() {
        areaRepartidores.setText("");
        hilos.recorrer(h -> {
            if (h instanceof Repartidor repartidor) {
                boolean lleno = repartidor.getCargaActual() >= repartidor.getCapacidad() && repartidor.getCargaActual() > 0;
                String linea = "🚚 " + repartidor.getName() + " (" + repartidor.getRuta() + ") - " + repartidor.getEstado()
                        + " - Paquetes: " + repartidor.getCargaActual() + "/" + repartidor.getCapacidad()
                        + (lleno ? " [LLENO]" : "")
                        + " - Entregados: " + repartidor.getEntregados() + "\n";
                escribirColor(areaRepartidores, linea, colorEstadoRepartidor(repartidor.getEstado()));
            }
        });
    }

    private String formatearClasificadores() {
        StringBuilder sb = new StringBuilder();
        hilos.recorrer(h -> {
            if (h instanceof HiloClasificador clasificador) {
                Paquete p = clasificador.getActual();
                sb.append(clasificador.getName()).append(" -> ").append(p == null ? "(libre)" : p.getCodigo()).append("\n");
            }
        });
        return sb.toString();
    }

    private String formatearEmpaquetadores() {
        StringBuilder sb = new StringBuilder();
        hilos.recorrer(h -> {
            if (h instanceof HiloEmpaquetador empaquetador) {
                Paquete p = empaquetador.getActual();
                sb.append(empaquetador.getName()).append(" -> ").append(p == null ? "(libre)" : p.getCodigo()).append("\n");
            }
        });
        return sb.toString();
    }

    private String formatearRegistro() {
        StringBuilder sb = new StringBuilder();
        centro.listaRegistro.recorrer(linea -> sb.append(linea).append("\n"));
        return sb.toString();
    }

    private void configurarBotones() {
        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(false);
        btnDetener.setEnabled(false);
        btnIniciar.addActionListener(e -> iniciar());
        btnPausar.addActionListener(e -> pausar());
        btnReanudar.addActionListener(e -> reanudar());
        btnDetener.addActionListener(e -> detener());
        btnReiniciar.addActionListener(e -> reiniciar());
        btnEstadisticas.addActionListener(e -> mostrarEstadisticas());
    }

    private void pausar() {
        centro.pausar();
        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(true);
    }

    private void reanudar() {
        centro.reanudar();
        btnPausar.setEnabled(true);
        btnReanudar.setEnabled(false);
    }

    private void detener() {
        centro.detener();
        hilos.recorrer(Thread::interrupt);

        btnIniciar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(false);
        btnDetener.setEnabled(false);
    }

    private void reiniciar() {
        if (centro != null && centro.estaActivo()) {
            detener();
        }
        centro = null;
        hilos = new ListaEnlazada<>();

        areaRecepcion.setText("");
        areaAlmacen.setText("");
        areaClasificacion.setText("");
        areaEmpaquetado.setText("");
        areaExpedicion.setText("");
        areaRepartidores.setText("");
        areaRegistro.setText("");

        for (JProgressBar barra : new JProgressBar[]{barraRecepcion, barraAlmacen, barraEmpaquetado, barraExpedicion}) {
            barra.setValue(0);
            barra.setString("0/0");
        }

        btnIniciar.setEnabled(true);
        btnPausar.setEnabled(false);
        btnReanudar.setEnabled(false);
        btnDetener.setEnabled(false);
    }

    private void mostrarEstadisticas() {
        if (centro == null) {
            JOptionPane.showMessageDialog(this, "Inicia la simulacion primero.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Paquetes generados: ").append(centro.getTotalGenerados()).append("\n");
        sb.append("Entregados: ").append(centro.getTotalEntregados()).append("\n");
        sb.append("Devueltos: ").append(centro.getTotalDevueltos()).append("\n");
        sb.append("En proceso: ").append(calcularEnProceso()).append("\n");
        sb.append("Pendientes: ").append(centro.getTotalPendientes()).append("\n");
        sb.append(String.format("Tiempo promedio: %.1f s%n", centro.getTiempoPromedioSegundos()));
        sb.append("\n");
        hilos.recorrer(h -> {
            if (h instanceof Repartidor repartidor) {
                sb.append(repartidor.getName()).append(": ").append(repartidor.getEntregados()).append(" entregados\n");
            }
        });
        JOptionPane.showMessageDialog(this, sb.toString(), "Estadisticas", JOptionPane.INFORMATION_MESSAGE);
    }

    private int calcularEnProceso() {
        int[] contador = {0};
        hilos.recorrer(h -> {
            if (h instanceof HiloClasificador clasificador && clasificador.getActual() != null) {
                contador[0]++;
            } else if (h instanceof HiloEmpaquetador empaquetador && empaquetador.getActual() != null) {
                contador[0]++;
            } else if (h instanceof Repartidor repartidor) {
                contador[0] += repartidor.getCargaActual();
            }
        });
        return contador[0];
    }

    private void iniciar() {
        centro = new CentroLogistico();

        hilos.agregar(new HiloRecepcion(centro));
        hilos.agregar(new HiloClasificador(centro, "Clasificador-1"));
        hilos.agregar(new HiloClasificador(centro, "Clasificador-2"));
        hilos.agregar(new HiloClasificador(centro, "Clasificador-3"));
        hilos.agregar(new HiloEmpaquetador(centro, "Empaquetador-1"));
        hilos.agregar(new HiloEmpaquetador(centro, "Empaquetador-2"));
        hilos.agregar(new Repartidor(centro, "Repartidor-1", 5, "R1"));
        hilos.agregar(new Repartidor(centro, "Repartidor-2", 4, "R2"));
        hilos.agregar(new Repartidor(centro, "Repartidor-3", 6, "R3"));
        hilos.agregar(new Repartidor(centro, "Repartidor-4", 5, "R4"));

        hilos.recorrer(Thread::start);

        btnIniciar.setEnabled(false);
        btnPausar.setEnabled(true);
        btnDetener.setEnabled(true);
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout());
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(crearEncabezado(), BorderLayout.NORTH);
        panelSuperior.add(crearPanelBotones(), BorderLayout.CENTER);
        add(panelSuperior, BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
    }

    private JLabel crearEncabezado() {
        JLabel titulo = new JLabel("📦 CENTRO LOGISTICO", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        return titulo;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel();
        btnIniciar = new JButton("INICIAR");
        btnPausar = new JButton("PAUSAR");
        btnReanudar = new JButton("REANUDAR");
        btnDetener = new JButton("DETENER");
        btnReiniciar = new JButton("REINICIAR");
        btnEstadisticas = new JButton("ESTADISTICAS");

        panel.add(btnIniciar);
        panel.add(btnPausar);
        panel.add(btnReanudar);
        panel.add(btnDetener);
        panel.add(btnReiniciar);
        panel.add(btnEstadisticas);
        return panel;
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(5, 1));
        panel.add(crearPanelEtapasSuperiores());
        areaEmpaquetado = new JTextArea();
        areaExpedicion = new JTextPane();
        areaRepartidores = new JTextPane();
        barraEmpaquetado = new JProgressBar();
        barraExpedicion = new JProgressBar();
        panel.add(crearPanelEtapa("EMPAQUETADO", areaEmpaquetado, barraEmpaquetado));
        panel.add(crearPanelEtapa("EXPEDICION", areaExpedicion, barraExpedicion));
        panel.add(crearPanelEtapa("REPARTIDORES", areaRepartidores));
        panel.add(crearPanelRegistro());
        return panel;
    }

    private JPanel crearPanelEtapasSuperiores() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        areaRecepcion = new JTextPane();
        areaAlmacen = new JTextPane();
        areaClasificacion = new JTextArea();
        barraRecepcion = new JProgressBar();
        barraAlmacen = new JProgressBar();

        panel.add(crearPanelEtapa("RECEPCION", areaRecepcion, barraRecepcion));
        panel.add(crearPanelEtapa("ALMACEN", areaAlmacen, barraAlmacen));
        panel.add(crearPanelEtapa("CLASIFICACION", areaClasificacion));
        return panel;
    }

    private JPanel crearPanelEtapa(String titulo, JTextComponent area) {
        return crearPanelEtapa(titulo, area, null);
    }

    private JPanel crearPanelEtapa(String titulo, JTextComponent area, JProgressBar barra) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(titulo));
        area.setEditable(false);
        if (barra != null) {
            barra.setStringPainted(true);
            panel.add(barra, BorderLayout.NORTH);
        }
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelRegistro() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("REGISTRO DEL SISTEMA"));
        areaRegistro = new JTextArea(8, 20);
        areaRegistro.setEditable(false);
        panel.add(new JScrollPane(areaRegistro), BorderLayout.CENTER);
        return panel;
    }
}
