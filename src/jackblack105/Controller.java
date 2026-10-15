package jackblack105;

import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    @FXML
    private ComboBox<Integer> playerCountComboBox;

    @FXML
    private VBox playerNamesContainer;

    @FXML
    public void initialize() {
        // Populate the menu with options for 1 to 3 players
        playerCountComboBox.getItems().addAll(1, 2, 3);
        
        // Set default value and trigger the field generation
        playerCountComboBox.setValue(1);
        updatePlayerFields();

        // Listen for changes to update text fields dynamically
        playerCountComboBox.setOnAction(event -> updatePlayerFields());
    }

    private void updatePlayerFields() {
        // Clear existing fields
        playerNamesContainer.getChildren().clear();
        
        int count = playerCountComboBox.getValue();
        
        // Generate new TextField for each player
        for (int i = 1; i <= count; i++) {
            TextField nameField = new TextField();
            nameField.setPromptText("Player " + i + " Name");
            nameField.setMaxWidth(200);
            playerNamesContainer.getChildren().add(nameField);
        }
    }

    @FXML
    public void launchGame(ActionEvent event) throws Exception {
        List<String> playerNames = new ArrayList<>();
        
        // Loop through the container to extract the text from each TextField
        for (Node node : playerNamesContainer.getChildren()) {
            if (node instanceof TextField) {
                TextField field = (TextField) node;
                String name = field.getText().trim();
                
                // Use the prompt text (e.g., "Player 1 Name") if they left it blank
                if (name.isEmpty()) {
                    name = field.getPromptText();
                }
                playerNames.add(name);
            }
        }

        // Output the collected names to the console to verify it works
        System.out.println("Starting game with players: " + playerNames);
        
        // 1. Load the new game board FXML
        Parent gameBoard = FXMLLoader.load(getClass().getResource("game_board.fxml"));
        
        // 2. Get the current window (Stage) from the button click event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // 3. Set the new scene and show it
        stage.setScene(new Scene(gameBoard, 1920, 1080));
        stage.show();
    }
}