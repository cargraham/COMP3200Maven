package com.emailclient.emailclientv3.Controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

public class ConcentratedSettingsScreenController {

    @FXML
    public Spinner<Integer> spinner;

    @FXML
    public Button cancelButton;

    @FXML
    public Button confirmButton;

    private MainScreenController mainScreenController;
    private int notificationThreshold;

    //sets the main screen controller and imports the current settings
    public void setMainScreenController(MainScreenController mainScreenController){
        this.mainScreenController = mainScreenController;
        notificationThreshold = mainScreenController.getNotificationThreshold();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, notificationThreshold, 1));
    }

    //sends selected choice to main screen controller on confirm button click
    @FXML
    public void confirmChoice(Event event){
        notificationThreshold = spinner.getValue();
        mainScreenController.setNotificationThreshold(notificationThreshold);

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    //closes the stage and doesn't save setting choices on cancel button click
    @FXML
    public void cancel(Event event){
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
