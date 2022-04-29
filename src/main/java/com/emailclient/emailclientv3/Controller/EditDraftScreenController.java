package com.emailclient.emailclientv3.Controller;

import com.emailclient.emailclientv3.Model.Graph;
import com.microsoft.graph.models.*;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class EditDraftScreenController {

    @FXML
    public TextField fromTextField;

    @FXML
    public TextField toTextField;

    @FXML
    public TextField ccTextField;

    @FXML
    public TextField subjectTextField;

    @FXML
    public HBox attachmentsHBox;

    @FXML
    public TextArea bodyTextArea;

    @FXML
    public Button draftButton;

    @FXML
    public Button attachFileButton;

    @FXML
    public ToggleButton importantToggle;

    @FXML
    public Button sendButton;

    private final String SEMI_COLON = "; ";
    private String messageID;
    private Stage thisStage;
    private LinkedList<Attachment> attachments = new LinkedList<>();

    //sets the 'From' field to the user's name after FXML values have been injected
    @FXML
    public void initialize(){
        fromTextField.setText(Graph.getUser().userPrincipalName);
    }

    //loads the draft message into the editing window
    public void initialiseDraft(Message message){
        if(message != null){
            ArrayList<Recipient> recipients = new ArrayList<>();
            ArrayList<Recipient> ccRecipients = new ArrayList<>();

            if(message.toRecipients != null){
                recipients.addAll(message.toRecipients);
            }

            if(message.ccRecipients != null){
                ccRecipients.addAll(message.ccRecipients);
            }

            String subject = message.subject;
            this.messageID = message.id;

            String body = "";

            if(message.body != null){
                body = message.body.content;
            }

            toTextField.setText(buildRecipientsString(recipients));
            ccTextField.setText(buildRecipientsString(ccRecipients));
            subjectTextField.setText(subject);

            Document document = Jsoup.parse(body);
            Document.OutputSettings outputSettings = new Document.OutputSettings();
            outputSettings.prettyPrint(false);
            document.outputSettings(outputSettings);
            document.select("br").before("\\n");
            document.select("p").before("\\n");

            String originalNewLines = document.html().replaceAll("\\\\n", "\n");
            String parsedBody = Jsoup.clean(originalNewLines, "", Safelist.none(), outputSettings);

            bodyTextArea.setText(parsedBody);

            if(message.hasAttachments != null && message.hasAttachments){

                attachments = new LinkedList<>(Graph.getMessageAttachmentList(messageID));
                attachmentsHBox.setPadding(new Insets(5));
                attachmentsHBox.setSpacing(5);

                for(Attachment attachment : attachments){

                    Button attachmentButton = new Button(attachment.name);
                    attachmentsHBox.getChildren().add(attachmentButton);

                    if (Objects.equals(attachment.oDataType, "#microsoft.graph.fileAttachment")){

                        attachmentButton.setOnAction(event1 -> {

                            FileAttachment attachment1 = Graph.getMessageFileAttachment(messageID, attachment.id);
                            String home = System.getProperty("user.home");
                            File file = new File(home + File.separator + "Downloads" + File.separator + attachment1.name);

                            try(FileOutputStream outputStream = new FileOutputStream(file)){
                                if(file.createNewFile()){

                                    if(attachment1.contentBytes != null){
                                        outputStream.write(attachment1.contentBytes);
                                    }

                                    if(Desktop.isDesktopSupported()){
                                        Desktop desktop = Desktop.getDesktop();
                                        if(file.exists()){
                                            desktop.open(file);
                                        }
                                    }
                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            }

            if(message.importance == Importance.HIGH){
                importantToggle.setSelected(true);
            }
        }
    }

    //builds a string of recipients joined by a semicolon
    public String buildRecipientsString(ArrayList<Recipient> recipients){
        StringJoiner recipientJoiner = new StringJoiner(SEMI_COLON);

        for(Recipient recipient : recipients){

            String recipientAddress = "";

            if(recipient.emailAddress != null){
                recipientAddress = recipient.emailAddress.address;
            }

            if(!Objects.equals(recipientAddress, Graph.getUser().userPrincipalName)){
                recipientJoiner.add(recipientAddress);
            }
        }

        return recipientJoiner.toString();
    }

    //saves a draft message and closes window on button click
    @FXML
    public void saveDraftMessage(Event event){
        String subject = subjectTextField.getText();
        String body = bodyTextArea.getText();
        ArrayList<String> toRecipients = new ArrayList<>();
        ArrayList<String> ccRecipients = new ArrayList<>();

        if(!toTextField.getText().isEmpty()){
            toRecipients = new ArrayList<>(List.of(toTextField.getText().split(SEMI_COLON)));
        }
        if(!ccTextField.getText().isEmpty()){
            ccRecipients = new ArrayList<>(List.of(ccTextField.getText().split(SEMI_COLON)));
        }

        Message message = Graph.createMessage(subject, body, toRecipients, ccRecipients);

        if(importantToggle.isSelected()){
            message.importance = Importance.HIGH;
        }

        if(attachments.isEmpty()){
            Graph.saveDraft(message);
            Graph.deleteDraft(messageID);
        }
        else{
            Graph.saveDraftWithAttachment(message, attachments);
            Graph.deleteDraft(messageID);
        }

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    //launches file chooser to allow user to choose attachment
    @FXML
    public void attachFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        File attachment = fileChooser.showOpenDialog(thisStage);

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.name = attachment.getName();
        fileAttachment.contentBytes = Files.readAllBytes(attachment.toPath());
        fileAttachment.oDataType = "#microsoft.graph.fileAttachment";

        attachments.add(fileAttachment);

        Button attachmentButton = new Button(attachment.getName());
        attachmentsHBox.setPadding(new Insets(5));
        attachmentsHBox.setSpacing(5);
        attachmentsHBox.getChildren().add(attachmentButton);

        attachmentButton.setOnAction(event1 -> {

            Desktop desktop = Desktop.getDesktop();

            if(attachment.exists()){
                try {
                    desktop.open(attachment);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //sends message on button click
    @FXML
    public void sendMessage(Event event) throws IOException {
        String subject = subjectTextField.getText();
        String body = bodyTextArea.getText();
        ArrayList<String> toRecipients = new ArrayList<>();
        ArrayList<String> ccRecipients = new ArrayList<>();

        if(!toTextField.getText().isEmpty()){
            toRecipients = new ArrayList<>(List.of(toTextField.getText().split(SEMI_COLON)));
        }
        if(!ccTextField.getText().isEmpty()){
            ccRecipients = new ArrayList<>(List.of(ccTextField.getText().split(SEMI_COLON)));
        }

        Message message = Graph.createMessage(subject, body, toRecipients, ccRecipients);

        if(importantToggle.isSelected()){
            message.importance = Importance.HIGH;
        }

        if(attachments.isEmpty()){
            Graph.sendMessage(message);
            Graph.deleteDraft(messageID);

        }
        else{
            Graph.sendMessageWithAttachment(message, attachments);
            Graph.deleteDraft(messageID);
        }

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    //sets the stage for use with file chooser
    public void setStage(Stage thisStage){
        this.thisStage = thisStage;
    }
}
