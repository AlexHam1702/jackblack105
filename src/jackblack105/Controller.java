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
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

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
            nameField.setPromptText("Nom du joueur " + i);
            nameField.setMaxWidth(200);
            playerNamesContainer.getChildren().add(nameField);
        }
    }
    
    @FXML
    public void showInfo(ActionEvent event) {
        // Creates a simple pop-up dialog with the game rules
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Informations sur le jeu");
        alert.setHeaderText("Règles du Blackjack");
        alert.setContentText("Le but du jeu est de battre le croupier en obtenant un score "
                + "le plus proche possible de 21, sans jamais le dépasser.\n\n"
                + "- Les cartes numérotées valent leur chiffre.\n"
                + "- Les têtes (Valet, Dame, Roi) valent 10.\n"
                + "- L'As vaut 1 ou 11.\n\n"
                + "Appuyez sur 'Tirer' pour demander une carte, ou 'Rester' pour garder votre main.\n\n"
                + "Ce jeu a été développé au cours du parcours Robotique et numérique par\n"
                + "Baptiste Lhors, Alexandre Hamard, Axel Lupter-Vidal");
        
        alert.showAndWait();
    }

    @FXML
    public void quitGame(ActionEvent event) {
        // Safely closes the JavaFX application
        Platform.exit();
        System.exit(0);
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
        
        // Create an FXMLLoader instance instead of calling the static load() method
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gameLayout.fxml"));
        Parent gameBoard = loader.load();
        
        // Retrieve the GameController and pass the player data
        GameController controller = loader.getController();
        controller.initData(activePlayers);
        
        // Get the current window (Stage) from the button click event
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        
        // Set the new scene and show it
        stage.setScene(new Scene(gameBoard, 1920, 1080));
        stage.show();
    }
}