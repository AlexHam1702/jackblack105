package jackblack105;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;

public class GameController {

    @FXML private VBox dealerArea;
    @FXML private HBox playersContainer; // An HBox in your FXML to hold all players side-by-side
    @FXML private Button btnTirer;
    @FXML private Button btnRester;

    private List<Player> players;
    private int currentPlayerIndex = 0;

    // The landing screen calls this method to pass the selected players
    public void initData(List<Player> selectedPlayers) {
        this.players = selectedPlayers;
        buildPlayersUI();
        updateActivePlayerUI();
    }

    private void buildPlayersUI() {
        playersContainer.getChildren().clear(); // Clear existing UI

        for (Player p : players) {
            VBox playerBox = new VBox(10);
            playerBox.setStyle("-fx-border-color: white; -fx-padding: 10; -fx-alignment: center;");
            
            Label nameLabel = new Label(p.getNom());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            
            Label scoreLabel = new Label("Score : " + p.calculerScore());
            scoreLabel.setStyle("-fx-text-fill: white;");
            
            Label moneyLabel = new Label("Jetons : " + p.getPorteMonnaie());
            moneyLabel.setStyle("-fx-text-fill: gold;");

            // You can add another HBox here later to display the actual Card images
            
            playerBox.getChildren().addAll(nameLabel, scoreLabel, moneyLabel);
            playersContainer.getChildren().add(playerBox);
        }
    }

    private void updateActivePlayerUI() {
        // Highlight the current player's turn
        for (int i = 0; i < playersContainer.getChildren().size(); i++) {
            VBox box = (VBox) playersContainer.getChildren().get(i);
            if (i == currentPlayerIndex) {
                box.setStyle("-fx-border-color: yellow; -fx-border-width: 3; -fx-padding: 10; -fx-alignment: center;");
            } else {
                box.setStyle("-fx-border-color: white; -fx-border-width: 1; -fx-padding: 10; -fx-alignment: center;");
            }
        }
    }

    @FXML
    private void handleHit() {
        Player currentPlayer = players.get(currentPlayerIndex);
        System.out.println(currentPlayer.getNom() + " tire une carte !");
        // Logic to draw a card goes here, then refresh the UI
    }

    @FXML
    private void handleStand() {
        System.out.println(players.get(currentPlayerIndex).getNom() + " reste.");
        currentPlayerIndex++;
        
        if (currentPlayerIndex >= players.size()) {
            System.out.println("Tour du croupier !");
            btnTirer.setDisable(true);
            btnRester.setDisable(true);
            // Logic for Dealer's turn goes here
        } else {
            updateActivePlayerUI();
        }
    }
}