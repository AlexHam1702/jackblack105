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
        List<Player> activePlayers = new ArrayList<>();
        
        // Loop through the container to extract the text from each TextField
        for (Node node : playerNamesContainer.getChildren()) {
            if (node instanceof TextField) {
                TextField field = (TextField) node;
                String name = field.getText().trim();
                
                // Use the prompt text if they left it blank
                if (name.isEmpty()) {
                    name = field.getPromptText();
                }
                
                // Create a new Player object with 1000 starting chips (adjust as needed)
                activePlayers.add(new Player(name, 1000));
            }
        }
        
        System.out.println("Starting game with " + activePlayers.size() + " players!");
        
        // 1. Create an FXMLLoader instance instead of calling the static load() method
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameLayout.fxml"));
        Parent gameBoard = loader.load();
        
        // 2. Retrieve the GameController and pass the player data
        GameController controller = loader.getController();
        controller.initData(activePlayers);
        
        // 3. Get the current window (Stage) from the button click event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // 4. Set the new scene and show it
        stage.setScene(new Scene(gameBoard, 1920, 1080));
        stage.show();
    }
}