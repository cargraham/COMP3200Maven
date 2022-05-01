package com.emailclient.emailclientv3.Controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

public class ChangeSyncFrequencyScreenController {

    @FXML
    public ChoiceBox<String> choiceBox;

    @FXML
    public Button confirmButton;

    @FXML
    public Button cancelButton;

    private MainScreenController mainScreenController;
    private long syncFrequency = 60000;

    //initialises the UI after FXML values have been injected
    @FXML
    public void initialize(){
        choiceBox.getItems().add("1 Minute");
        choiceBox.getItems().add("5 Minutes");
        choiceBox.getItems().add("10 Minutes");
        choiceBox.getItems().add("30 Minutes");
        choiceBox.getItems().add("60 Minutes");


    }

    //sets the main screen controller and imports the current settings
    public void setMainScreenController(MainScreenController mainScreenController){
        this.mainScreenController = mainScreenController;
        syncFrequency = mainScreenController.getSyncFrequency();

        switch ((int) syncFrequency) {
            case 60000 -> choiceBox.getSelectionModel().select(0);
            case 300000 -> choiceBox.getSelectionModel().select(1);
            case 600000 -> choiceBox.getSelectionModel().select(2);
            case 1800000 -> choiceBox.getSelectionModel().select(3);
            case 3600000 -> choiceBox.getSelectionModel().select(4);
        }
    }

    //sends selected choice to main screen controller on confirm button click
    @FXML
    public void confirmFrequencyChoice(Event event){
        String minutes = choiceBox.getValue();

        switch (minutes) {
            case "1 Minute" -> syncFrequency = 60000;
            case "5 Minutes" -> syncFrequency = 300000;
            case "10 Minutes" -> syncFrequency = 600000;
            case "30 Minutes" -> syncFrequency = 1800000;
            case "60 Minutes" -> syncFrequency = 3600000;
        }

        mainScreenController.setSyncFrequency(syncFrequency);

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
