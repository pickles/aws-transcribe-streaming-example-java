/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.amazonaws.transcribestreaming;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.transcribestreaming.model.Alternative;
import software.amazon.awssdk.services.transcribestreaming.model.Result;
import software.amazon.awssdk.services.transcribestreaming.model.StartStreamTranscriptionResponse;
import software.amazon.awssdk.services.transcribestreaming.model.TranscriptEvent;
import software.amazon.awssdk.services.transcribestreaming.model.TranscriptResultStream;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.Mixer;

/**
 * This class primarily controls the GUI for this application. Most of the code relevant to starting and working
 * with our streaming API can be found in TranscribeStreamingClientWrapper.java, with the exception of some result
 * parsing logic in this classes method getResponseHandlerForWindow()
 */
public class WindowController {

    private TranscribeStreamingClientWrapper client;
    private TranscribeStreamingSynchronousClient synchronousClient;
    private TextArea outputTextArea;
    private Button startStopMicButton;
    private Button fileStreamButton;
    private Button saveButton;
    private TextArea finalTextArea;
    private CompletableFuture<Void> inProgressStreamingRequest;
    private String finalTranscript = "";
    private Stage primaryStage;

    private List<Mixer> mics;
    private ChoiceBox<String> micChoiceBox;

    private ChoiceBox<String> langChoiceBox;
    private CheckBox showSpeakerCheckBox;

    private ChoiceBox<String> translateFromLangChoiceBox;
    private TextArea translateFromTextArea;
    private Button translateFlipLanguageButton;
    private ChoiceBox<String> translateToLangChoiceBox;
    private TextArea translateToTextArea;
    private Button translateButton;

    private static SortedMap<String, String> TRANSLATE_LANGUAGE_CODES = new TreeMap<>();
    static {
        TRANSLATE_LANGUAGE_CODES.put("Afrikaans", "af");
        TRANSLATE_LANGUAGE_CODES.put("Albanian", "sq");
        TRANSLATE_LANGUAGE_CODES.put("Amharic", "am");
        TRANSLATE_LANGUAGE_CODES.put("Arabic", "ar");
        TRANSLATE_LANGUAGE_CODES.put("Armenian", "hy");
        TRANSLATE_LANGUAGE_CODES.put("Azerbaijani", "az");
        TRANSLATE_LANGUAGE_CODES.put("Bengali", "bn");
        TRANSLATE_LANGUAGE_CODES.put("Bosnian", "bs");
        TRANSLATE_LANGUAGE_CODES.put("Bulgarian", "bg");
        TRANSLATE_LANGUAGE_CODES.put("Catalan", "ca");
        TRANSLATE_LANGUAGE_CODES.put("Chinese (Simplified)", "zh");
        TRANSLATE_LANGUAGE_CODES.put("Chinese (Traditional)", "zh-TW");
        TRANSLATE_LANGUAGE_CODES.put("Croatian", "hr");
        TRANSLATE_LANGUAGE_CODES.put("Czech", "cs");
        TRANSLATE_LANGUAGE_CODES.put("Danish", "da");
        TRANSLATE_LANGUAGE_CODES.put("Dari", "fa-AF");
        TRANSLATE_LANGUAGE_CODES.put("Dutch", "nl");
        TRANSLATE_LANGUAGE_CODES.put("English", "en");
        TRANSLATE_LANGUAGE_CODES.put("Estonian", "et");
        TRANSLATE_LANGUAGE_CODES.put("Farsi (Persian)", "fa");
        TRANSLATE_LANGUAGE_CODES.put("Filipino, Tagalog", "tl");
        TRANSLATE_LANGUAGE_CODES.put("Finnish", "fi");
        TRANSLATE_LANGUAGE_CODES.put("French", "fr");
        TRANSLATE_LANGUAGE_CODES.put("French (Canada)", "fr-CA");
        TRANSLATE_LANGUAGE_CODES.put("Georgian", "ka");
        TRANSLATE_LANGUAGE_CODES.put("German", "de");
        TRANSLATE_LANGUAGE_CODES.put("Greek", "el");
        TRANSLATE_LANGUAGE_CODES.put("Gujarati", "gu");
        TRANSLATE_LANGUAGE_CODES.put("Haitian Creole", "ht");
        TRANSLATE_LANGUAGE_CODES.put("Hausa", "ha");
        TRANSLATE_LANGUAGE_CODES.put("Hebrew", "he");
        TRANSLATE_LANGUAGE_CODES.put("Hindi", "hi");
        TRANSLATE_LANGUAGE_CODES.put("Hungarian", "hu");
        TRANSLATE_LANGUAGE_CODES.put("Icelandic", "is");
        TRANSLATE_LANGUAGE_CODES.put("Indonesian", "id");
        TRANSLATE_LANGUAGE_CODES.put("Italian", "it");
        TRANSLATE_LANGUAGE_CODES.put("Japanese", "ja");
        TRANSLATE_LANGUAGE_CODES.put("Kannada", "kn");
        TRANSLATE_LANGUAGE_CODES.put("Kazakh", "kk");
        TRANSLATE_LANGUAGE_CODES.put("Korean", "ko");
        TRANSLATE_LANGUAGE_CODES.put("Latvian", "lv");
        TRANSLATE_LANGUAGE_CODES.put("Lithuanian", "lt");
        TRANSLATE_LANGUAGE_CODES.put("Macedonian", "mk");
        TRANSLATE_LANGUAGE_CODES.put("Malay", "ms");
        TRANSLATE_LANGUAGE_CODES.put("Malayalam", "ml");
        TRANSLATE_LANGUAGE_CODES.put("Maltese", "mt");
        TRANSLATE_LANGUAGE_CODES.put("Mongolian", "mn");
        TRANSLATE_LANGUAGE_CODES.put("Norwegian", "no");
        TRANSLATE_LANGUAGE_CODES.put("Pashto", "ps");
        TRANSLATE_LANGUAGE_CODES.put("Polish", "pl");
        TRANSLATE_LANGUAGE_CODES.put("Portuguese", "pt");
        TRANSLATE_LANGUAGE_CODES.put("Romanian", "ro");
        TRANSLATE_LANGUAGE_CODES.put("Russian", "ru");
        TRANSLATE_LANGUAGE_CODES.put("Serbian", "sr");
        TRANSLATE_LANGUAGE_CODES.put("Sinhala", "si");
        TRANSLATE_LANGUAGE_CODES.put("Slovak", "sk");
        TRANSLATE_LANGUAGE_CODES.put("Slovenian", "sl");
        TRANSLATE_LANGUAGE_CODES.put("Somali", "so");
        TRANSLATE_LANGUAGE_CODES.put("Spanish", "es");
        TRANSLATE_LANGUAGE_CODES.put("Spanish (Mexico)", "es-MX");
        TRANSLATE_LANGUAGE_CODES.put("Swahili", "sw");
        TRANSLATE_LANGUAGE_CODES.put("Swedish", "sv");
        TRANSLATE_LANGUAGE_CODES.put("Tamil", "ta");
        TRANSLATE_LANGUAGE_CODES.put("Telugu", "te");
        TRANSLATE_LANGUAGE_CODES.put("Thai", "th");
        TRANSLATE_LANGUAGE_CODES.put("Turkish", "tr");
        TRANSLATE_LANGUAGE_CODES.put("Ukrainian", "uk");
        TRANSLATE_LANGUAGE_CODES.put("Urdu", "ur");
        TRANSLATE_LANGUAGE_CODES.put("Uzbek", "uz");
        TRANSLATE_LANGUAGE_CODES.put("Vietnamese", "vi");
        TRANSLATE_LANGUAGE_CODES.put("Welsh", "cy");
    };

    private static final List<String> TRANSLATE_LANGUAGE_LABELS = new ArrayList<>();
    static {
        for(String key : TRANSLATE_LANGUAGE_CODES.keySet()) {
            TRANSLATE_LANGUAGE_LABELS.add(key);
        }
    }

    public WindowController(Stage primaryStage) {
        client = new TranscribeStreamingClientWrapper();
        synchronousClient = new TranscribeStreamingSynchronousClient(TranscribeStreamingClientWrapper.getClient());
        this.primaryStage = primaryStage;
        this.mics = AudioUtil.getAvailableMics();
        initializeWindow(primaryStage);
    }

    public void close() {
        if (inProgressStreamingRequest != null) {
            inProgressStreamingRequest.completeExceptionally(new InterruptedException());
        }
        client.close();
    }

    private void startFileTranscriptionRequest(File inputFile) {
        if (inProgressStreamingRequest == null) {
            finalTextArea.clear();
            finalTranscript = "";
            startStopMicButton.setText("Streaming...");
            startStopMicButton.setDisable(true);
            outputTextArea.clear();
            finalTextArea.clear();
            saveButton.setDisable(true);
            finalTranscript = synchronousClient.transcribeFile(inputFile);
            finalTextArea.setText(finalTranscript);
            startStopMicButton.setDisable(false);
            saveButton.setDisable(false);
            startStopMicButton.setText("Start Microphone Transcription");
        }
    }

    private void startTranscriptionRequest(File inputFile) {
        if (inProgressStreamingRequest == null) {
            finalTextArea.clear();
            finalTranscript = "";
            
            micChoiceBox.setDisable(true);
            langChoiceBox.setDisable(true);
            showSpeakerCheckBox.setDisable(true);

            startStopMicButton.setText("Connecting...");
            startStopMicButton.setDisable(true);
            outputTextArea.clear();
            finalTextArea.clear();
            saveButton.setDisable(true);

            String languageCode = langChoiceBox.getSelectionModel().getSelectedItem();
            boolean showSpeakerLabel = showSpeakerCheckBox.isSelected();

            if (inputFile != null) {
                inProgressStreamingRequest = client.startTranscription(
                    getResponseHandlerForWindow(),
                    inputFile,
                    languageCode,
                    showSpeakerLabel);
            } else {
                int index = micChoiceBox.getSelectionModel().getSelectedIndex();

                inProgressStreamingRequest = client.startTranscription(
                    getResponseHandlerForWindow(), 
                    mics.get(index), 
                    languageCode,
                    showSpeakerLabel);
            }
        }
    }

    private void initializeWindow(Stage primaryStage) {
        VBox parentPane = new VBox();
        parentPane.setPadding(new Insets(5, 5, 5, 5));
        parentPane.setSpacing(10);

        Scene scene = new Scene(parentPane);
        primaryStage.setScene(scene);

        HBox micPane = new HBox();
        micPane.setSpacing(10);
        List<String> micNames = new ArrayList<>();
        for (Mixer mic : mics) {
            micNames.add(mic.getMixerInfo().getName());
        }
        micChoiceBox = new ChoiceBox<String>(FXCollections.observableArrayList(micNames));
        micChoiceBox.setValue(micNames.get(0));
        
        startStopMicButton = new Button();
        startStopMicButton.setText("Start Microphone Transcription");
        startStopMicButton.setOnAction(__ -> {startTranscriptionRequest(null);});

        micPane.getChildren().addAll(micChoiceBox, startStopMicButton);

        HBox streamOptionsPane = new HBox();
        streamOptionsPane.setSpacing(10);
        langChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(
            "en-US", "en-GB", "en-AU",
            "es-US",
            "fr-CA", "fr-FR",
            "it-IT",
            "de-DE",
            "pt-BR",
            "ja-JP",
            "ko-KR",
            "zh-CN"
        ));
        langChoiceBox.setValue("en-US");

        showSpeakerCheckBox = new CheckBox("Show Speaker label");
        showSpeakerCheckBox.setSelected(true);
        streamOptionsPane.getChildren().addAll(langChoiceBox, showSpeakerCheckBox);
        
        fileStreamButton = new Button();
        fileStreamButton.setText("Stream From Audio File"); //TODO: what file types do we support?
        fileStreamButton.setOnAction(__ -> {
            FileChooser inputFileChooser = new FileChooser();
            inputFileChooser.setTitle("Stream Audio File");
            File inputFile = inputFileChooser.showOpenDialog(primaryStage);
            if (inputFile != null) {
                startFileTranscriptionRequest(inputFile);
            }
        });

        Text inProgressText = new Text("In Progress Transcriptions:");

        outputTextArea = new TextArea();
        outputTextArea.setWrapText(true);
        outputTextArea.setEditable(true);
 
        Text finalText = new Text("Final Transcription:");

        finalTextArea = new TextArea();
        finalTextArea.setWrapText(true);
        finalTextArea.setEditable(true);

        saveButton = new Button();
        saveButton.setDisable(true);
        saveButton.setText("Save Full Transcript");

        HBox translatePane = new HBox();
        translatePane.setSpacing(20);

        VBox translateFromPane = new VBox();
        translateFromPane.setSpacing(10);
        List<String> fromLangList = new ArrayList<>(TRANSLATE_LANGUAGE_LABELS);
        fromLangList.add(0, "Auto");
        translateFromLangChoiceBox = new ChoiceBox<>(
            FXCollections.observableArrayList(fromLangList));
        translateFromLangChoiceBox.setValue("Auto");
        translateFromLangChoiceBox.setOnAction(__ -> {
            if (translateFromLangChoiceBox.getSelectionModel().getSelectedItem().equals("Auto")) {
                translateFlipLanguageButton.setDisable(true);
            } else {
                translateFlipLanguageButton.setDisable(false);
            }
        });
        translateFromTextArea = new TextArea();
        translateFromTextArea.setWrapText(true);
        translateFromPane.getChildren().addAll(
            new HBox(new Text("Source Language: "), translateFromLangChoiceBox),
            translateFromTextArea);
        VBox.setVgrow(translateFromTextArea, Priority.ALWAYS);

        translateFlipLanguageButton = new Button("⇄");
        translateFlipLanguageButton.setDisable(true);
        translateFlipLanguageButton.setOnAction(__ -> {
            String from = translateFromLangChoiceBox.getSelectionModel().getSelectedItem();
            String to = translateToLangChoiceBox.getSelectionModel().getSelectedItem();
            translateFromLangChoiceBox.setValue(to);
            translateToLangChoiceBox.setValue(from);
        });

        VBox translateToPane = new VBox();
        translateToPane.setSpacing(10);
        translateToLangChoiceBox = new ChoiceBox<>(
            FXCollections.observableArrayList(TRANSLATE_LANGUAGE_LABELS));
        translateToLangChoiceBox.setValue("English");
        translateToTextArea = new TextArea();
        translateToTextArea.setWrapText(true);
        translateToPane.getChildren().addAll(
            new HBox(new Text("Target Language: "), translateToLangChoiceBox),
            translateToTextArea);
        VBox.setVgrow(translateToTextArea, Priority.ALWAYS);

        translatePane.getChildren().addAll(
            translateFromPane, 
            translateFlipLanguageButton, 
            translateToPane);
        HBox.setHgrow(translateFromPane, Priority.ALWAYS);
        HBox.setHgrow(translateToPane, Priority.ALWAYS);

        translateButton = new Button("Translate");
        translateButton.setOnAction(__->{translate();});
        parentPane.getChildren().addAll(
            micPane,
            streamOptionsPane,
            fileStreamButton,
            inProgressText,
            outputTextArea,
            finalText,
            finalTextArea,
            saveButton,
            new Text("Translate -----"),
            translatePane,
            translateButton);

        VBox.setVgrow(outputTextArea, Priority.ALWAYS);
        VBox.setVgrow(finalTextArea, Priority.ALWAYS);
        VBox.setVgrow(translatePane, Priority.ALWAYS);
    }

    private void stopTranscription() {
        if (inProgressStreamingRequest != null) {
            try {
                saveButton.setDisable(true);
                client.stopTranscription();
                inProgressStreamingRequest.get();
            } catch (ExecutionException | InterruptedException e) {
                System.out.println("error closing stream");
            } finally {
                inProgressStreamingRequest = null;
                startStopMicButton.setText("Start Microphone Transcription");
                startStopMicButton.setOnAction(__ -> startTranscriptionRequest(null));
                startStopMicButton.setDisable(false);
                micChoiceBox.setDisable(false);
                langChoiceBox.setDisable(false);
                showSpeakerCheckBox.setDisable(false);
            }

        }
    }

    /**
     * A StartStreamTranscriptionResponseHandler class listens to events from Transcribe streaming service that return
     * transcriptions, and decides what to do with them. This example displays the transcripts in the GUI window, and
     * combines the transcripts together into a final transcript at the end.
     */
    private StreamTranscriptionBehavior getResponseHandlerForWindow() {
        return new StreamTranscriptionBehavior() {

            //This will handle errors being returned from AWS Transcribe in your response. Here we just print the exception.
            @Override
            public void onError(Throwable e) {
                System.out.println(e.getMessage());
                Throwable cause = e.getCause();
                while (cause != null) {
                    System.out.println("Caused by: " + cause.getMessage());
                    Arrays.stream(cause.getStackTrace()).forEach(l -> System.out.println("  " + l));
                    if (cause.getCause() != cause) { //Look out for circular causes
                        cause = cause.getCause();
                    } else {
                        cause = null;
                    }
                }
                System.out.println("Error Occurred: " + e);
            }

            /*
            This handles each event being received from the Transcribe service. In this example we are displaying the
            transcript as it is updated, and when we receive a "final" transcript, we append it to our finalTranscript
            which is returned at the end of the microphone streaming.
             */
            @Override
            public void onStream(TranscriptResultStream event) {
                List<Result> results = ((TranscriptEvent) event).transcript().results();
                if(results.size()>0) {
                    Result firstResult = results.get(0);
                    if (firstResult.alternatives().size() > 0 && !firstResult.alternatives().get(0).transcript().isEmpty()) {
                        String transcript = firstResult.alternatives().get(0).transcript();
                        // Alternative firstAlternative = firstResult.alternatives().get(0);
                        // System.out.println(firstAlternative);
                        // System.out.println("Speaker: " + firstAlternative.items().get(0).speaker());
                        if(!transcript.isEmpty()) {
                            System.out.print(transcript);
                            String displayText;
                            if (!firstResult.isPartial()) {
                                finalTranscript += transcript + "\n";
                                displayText = finalTranscript;
                                System.out.println("(complete)");
                            } else {
                                displayText = finalTranscript + " " + transcript;
                                System.out.println("(partial)");
                            }
                            Platform.runLater(() -> {
                                outputTextArea.setText(displayText);
                                outputTextArea.setScrollTop(Double.MAX_VALUE);
                            });
                        }
                    }

                }
            }

            /*
            This handles the initial response from the AWS Transcribe service, generally indicating the streams have
            successfully been opened. Here we just print that we have received the initial response and do some
            UI updates.
             */
            @Override
            public void onResponse(StartStreamTranscriptionResponse r) {
                System.out.println(String.format("=== Received Initial response. Request Id: %s ===", r.requestId()));
                Platform.runLater(() -> {
                    startStopMicButton.setText("Stop Transcription");
                    startStopMicButton.setOnAction(__ -> stopTranscription());
                    startStopMicButton.setDisable(false);
                });
            }

            /*
            This method is called when the stream is terminated without error. In our case we will use this opportunity
            to display the final, total transcript we've been aggregating during the transcription period and activates
            the save button.
             */
            @Override
            public void onComplete() {
                System.out.println("=== All records streamed successfully ===");
                Platform.runLater(() -> {
                    finalTextArea.setText(finalTranscript);
                    saveButton.setDisable(false);
                    saveButton.setOnAction(__ -> {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Save Transcript");
                        File file = fileChooser.showSaveDialog(primaryStage);
                        if (file != null) {
                            try {
                                FileWriter writer = new FileWriter(file);
                                writer.write(finalTranscript);
                                writer.close();
                            } catch (IOException e) {
                                System.out.println("Error saving transcript to file: " + e);
                            }
                        }
                    });

                });
            }
        };
    }

    private void translate() {
        translateButton.setText("Translating...");
        translateButton.setDisable(true);

        AwsCredentialsProvider credentials = DefaultCredentialsProvider.create();
        TranslateClient client = new TranslateClient(credentials);
        String text = translateFromTextArea.getText();
        String fromLang = translateFromLangChoiceBox.getSelectionModel().getSelectedItem();
        String toLang = translateToLangChoiceBox.getSelectionModel().getSelectedItem();

        String fromLangCode = TRANSLATE_LANGUAGE_CODES.getOrDefault(fromLang, "auto");
        String toLangCode = TRANSLATE_LANGUAGE_CODES.getOrDefault(toLang, "en-US");

        CompletableFuture<TranslateTextResponse> result = 
            client.translate(text, fromLangCode, toLangCode);
        
        result.whenCompleteAsync((response, err) -> {
            Platform.runLater(() -> {
                translateButton.setText("Translate");
                translateButton.setDisable(false);
            });

            if (err != null) {
                System.out.println("Error!");
                err.printStackTrace();
                return;
            }
            String translatedText = response.translatedText();
            Platform.runLater(() -> {
                translateToTextArea.setText(translatedText);
            });
        });
    }

}
