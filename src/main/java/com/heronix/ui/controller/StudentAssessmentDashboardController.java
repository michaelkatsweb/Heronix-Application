package com.heronix.ui.controller;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
@Component
public class StudentAssessmentDashboardController {
    @FXML private BarChart assessmentChart;
    @FXML private TableView assessmentTableView;
}
