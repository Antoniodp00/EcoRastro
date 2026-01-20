package org.dam2.adp.ecorastro.controller;

import javafx.event.ActionEvent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import org.dam2.adp.ecorastro.DAO.HuellaDAO;
import org.dam2.adp.ecorastro.model.Huella;
import org.dam2.adp.ecorastro.service.RecomendacionService;
import org.dam2.adp.ecorastro.util.SessionManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

public class AnalisisController {
    public ComboBox<String> cmbRango;
    public DatePicker dpFecha;
    public Label lblTotalPeriodo;
    public Label lblActividadFrecuente;
    public PieChart pieChart;
    public BarChart<String, Number> barChart;
    public Label lblInsight;

    private final HuellaDAO huellaDAO = new HuellaDAO();
    private final RecomendacionService recomendacionService = new RecomendacionService();


    private List<Huella> huellasFiltradas;

    private void initialize() {

    }

    private void cargarDatosFiltrados() {
        int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();
        String rango = cmbRango.getValue();
        LocalDate referencia = dpFecha.getValue();

        if (referencia == null) referencia = LocalDate.now();

        LocalDate inicio;
        LocalDate fin;

        switch (rango) {
            case "Día Concreto":
                inicio = referencia;
                fin = referencia;
                break;
            case "Semana Seleccionada":
                // Lunes de esa semana
                inicio = referencia.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
                // Domingo de esa semana
                fin = referencia.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 7);
                break;
            case "Mes Seleccionado":
                inicio = referencia.with(TemporalAdjusters.firstDayOfMonth());
                fin = referencia.with(TemporalAdjusters.lastDayOfMonth());
                break;
            default:
                inicio = LocalDate.of(2000, 1, 1);
                fin = LocalDate.now().plusDays(1);
                break;
        }
        huellasFiltradas = huellaDAO.getHuellasPorFecha(idUsuario, inicio, fin);
        actualizarDashboard();
    }

    private void actualizarDashboard() {
        if (huellasFiltradas == null) return;
        actualizarKPIs();
        actualizarGraficoDistribucion();
        actualizarGraficoComparativo();
        generarConsejoBasadoEnFrecuencia();
    }

    private void actualizarKPIs() {

    }

    private void actualizarGraficoDistribucion() {
    }

    private void actualizarGraficoComparativo() {
    }

    private void generarConsejoBasadoEnFrecuencia() {
    }

    public void exportarReporte(ActionEvent actionEvent) {
    }
}
