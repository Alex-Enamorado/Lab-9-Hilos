package lab9.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class VentanaPrincipal extends JFrame {

    private JButton btnIniciar;
    private JButton btnPausar;
    private JButton btnReanudar;
    private JButton btnDetener;
    private JButton btnReiniciar;
    private JButton btnEstadisticas;

    private JTextArea areaRecepcion;
    private JTextArea areaAlmacen;
    private JTextArea areaClasificacion;
    private JTextArea areaEmpaquetado;
    private JTextArea areaExpedicion;
    private JTextArea areaRepartidores;
    private JTextArea areaRegistro;

    public VentanaPrincipal() {
        super("Sistema de Paqueteria");
        construirInterfaz();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        setLayout(new BorderLayout());
        add(crearPanelBotones(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);
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
        areaExpedicion = new JTextArea();
        areaRepartidores = new JTextArea();
        panel.add(crearPanelEtapa("EMPAQUETADO", areaEmpaquetado));
        panel.add(crearPanelEtapa("EXPEDICION", areaExpedicion));
        panel.add(crearPanelEtapa("REPARTIDORES", areaRepartidores));
        panel.add(crearPanelRegistro());
        return panel;
    }

    private JPanel crearPanelEtapasSuperiores() {
        JPanel panel = new JPanel(new GridLayout(1, 3));
        areaRecepcion = new JTextArea();
        areaAlmacen = new JTextArea();
        areaClasificacion = new JTextArea();

        panel.add(crearPanelEtapa("RECEPCION", areaRecepcion));
        panel.add(crearPanelEtapa("ALMACEN", areaAlmacen));
        panel.add(crearPanelEtapa("CLASIFICACION", areaClasificacion));
        return panel;
    }

    private JPanel crearPanelEtapa(String titulo, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(titulo));
        area.setEditable(false);
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
