package jackblack105;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class GameController {

    @FXML private VBox dealerArea;
    @FXML private HBox playersContainer; // An HBox in your FXML to hold all players side-by-side
    @FXML private Button btnTirer;
    @FXML private Button btnRester;

    private List<Player> players;
    private int currentPlayerIndex = 0;
    private Deck deck;
    private Player croupier;

    // The landing screen calls this method to pass the selected players
    public void initData(List<Player> selectedPlayers) {
        this.players = selectedPlayers;
        this.deck = new Deck();
        this.croupier = new Player("Croupier", 0);
        this.currentPlayerIndex = 0; // Reset index for safety

        // 1. Build the empty UI first so players and the dealer appear on the green table immediately
        buildPlayersUI();
        buildDealerUI();

        // 2. Use Platform.runLater to let JavaFX physically render the screen before opening popups
        Platform.runLater(() -> {
            demanderMises();
            demarrerManche();
        });
    }
    
    private void demanderMises() {
        for (Player p : players) {
            boolean miseValide = false;
            
            while (!miseValide) {
                TextInputDialog dialog = new TextInputDialog("10"); // Default bet of 10
                dialog.setTitle("Phase de Mise");
                dialog.setHeaderText("Tour de " + p.getNom());
                dialog.setContentText("Combien voulez-vous miser ? (Solde : " + p.getPorteMonnaie() + ")");

                Optional<String> result = dialog.showAndWait();
                
                if (result.isPresent()) {
                    try {
                        int montant = Integer.parseInt(result.get().trim());
                        
                        // Your Player.miser() method returns false if the bet is invalid
                        if (p.miser(montant)) {
                            miseValide = true;
                        } else {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setHeaderText("Mise invalide");
                            alert.setContentText("Le montant doit être supérieur à 0 et inférieur ou égal à votre solde.");
                            alert.showAndWait();
                        }
                    } catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText("Erreur de saisie");
                        alert.setContentText("Veuillez entrer un nombre entier valide.");
                        alert.showAndWait();
                    }
                } else {
                    // If the player clicks "Cancel" or closes the window, force a minimum bet
                    System.out.println("Mise annulée, mise minimale de 10 appliquée.");
                    p.miser(10);
                    miseValide = true;
                }
            }
        }
    }

    
    private void demarrerManche() {
        // Deal 2 cards to each player and the dealer
        for (int i = 0; i < 2; i++) {
            for (Player p : players) {
                p.ajouterCarte(deck.tirerCarte());
            }
            croupier.ajouterCarte(deck.tirerCarte());
        }

        // Force JavaFX to clear and redraw the UI with the new cards
        buildPlayersUI();
        buildDealerUI(); 
        updateActivePlayerUI();
        
        // Check if the first player instantly got 21 (Blackjack)
        verifierScoreActuel();
    }
        
    public void startRound(List<Player> selectedPlayers) {
        this.players = selectedPlayers;
        this.deck = new Deck();
        this.croupier = new Player("Croupier", 0); // The dealer doesn't need chips

        // Deal 2 cards to each player and the dealer
        for (int i = 0; i < 2; i++) {
            for (Player p : players) {
                p.ajouterCarte(deck.tirerCarte());
            }
            croupier.ajouterCarte(deck.tirerCarte());
        }

        buildPlayersUI();
        buildDealerUI(); // Render the dealer's area
        updateActivePlayerUI();
    }
    private void buildDealerUI() {
        dealerArea.getChildren().clear(); // Clear existing UI
        
        boolean isDealerTurn = (currentPlayerIndex >= players.size());
        
        // --- FIX: Check if the hand is empty before getting the first card ---
        int scoreAffiche = 0;
        if (!croupier.getMain().isEmpty()) {
            scoreAffiche = isDealerTurn ? croupier.calculerScore() : croupier.getMain().get(0).getValeur();
        }
        
        Label nameLabel = new Label("Croupier - Score : " + scoreAffiche);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        dealerArea.getChildren().add(nameLabel);
        
        HBox cardsBox = new HBox(10);
        cardsBox.setStyle("-fx-alignment: center; -fx-padding: 10;");
        
        // This loop is already safe because if size() is 0, it just won't run
        for (int i = 0; i < croupier.getMain().size(); i++) {
            Carte carte = croupier.getMain().get(i);
            Label cardLabel;
            
            if (i == 1 && !isDealerTurn) {
                cardLabel = new Label("Carte Cachée");
                cardLabel.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-padding: 5 15; -fx-background-radius: 5; -fx-font-weight: bold;");
            } else {
                cardLabel = new Label(carte.toString());
                cardLabel.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-padding: 5 15; -fx-background-radius: 5; -fx-font-weight: bold;");
                
                if (carte.getCouleur().equals("Cœur") || carte.getCouleur().equals("Carreau")) {
                    cardLabel.setStyle(cardLabel.getStyle() + "-fx-text-fill: red;");
                }
            }
            cardsBox.getChildren().add(cardLabel);
        }
        
        dealerArea.getChildren().add(cardsBox);
    }
    
    private void buildPlayersUI() {
        playersContainer.getChildren().clear(); // Clear existing UI to prevent duplicates

        for (Player p : players) {
            VBox playerBox = new VBox(10);
            playerBox.setStyle("-fx-border-color: white; -fx-padding: 10; -fx-alignment: center; -fx-min-width: 150;");
            
            Label nameLabel = new Label(p.getNom());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
            
            Label scoreLabel = new Label("Score : " + p.calculerScore());
            scoreLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            
            Label moneyLabel = new Label("Jetons : " + p.getPorteMonnaie());
            moneyLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 14px;");

            // --- Area to display the cards ---
            VBox cardsBox = new VBox(5);
            cardsBox.setStyle("-fx-alignment: center; -fx-padding: 10 0;");
            
            // Loop through the player's hand and create a visual label for each card
            for (Carte carte : p.getMain()) {
                Label cardLabel = new Label(carte.toString()); // "Valeur de Couleur" toString()
                
                // Look like a small white card
                cardLabel.setStyle("-fx-background-color: white; " +
                                   "-fx-text-fill: black; " +
                                   "-fx-padding: 5 15; " +
                                   "-fx-background-radius: 5; " +
                                   "-fx-font-weight: bold;");
                                   
                // Make red cards actually red
                if (carte.getCouleur().equals("Cœur") || carte.getCouleur().equals("Carreau")) {
                    cardLabel.setStyle(cardLabel.getStyle() + "-fx-text-fill: red;");
                }
                
                cardsBox.getChildren().add(cardLabel);
            }

            // Add everything to the player's box
            playerBox.getChildren().addAll(nameLabel, scoreLabel, moneyLabel, cardsBox);
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
    
    private void determinerGagnants() {
        System.out.println("--- Détermination des gagnants ---");
        
        try {
            int scoreCroupier = croupier.calculerScore();
            StringBuilder resultats = new StringBuilder("Score final du Croupier : " + scoreCroupier + "\n\n");

            for (Player p : players) {
                int scoreJoueur = p.calculerScore();
                resultats.append(p.getNom()).append(" (Score: ").append(scoreJoueur).append(") - ");

                if (scoreJoueur > 21) {
                    resultats.append("A sauté ! (Perdu)\n");
                    p.perdreMise();
                } else if (scoreCroupier > 21) {
                    resultats.append("Gagné ! (Le Croupier a sauté)\n");
                    p.gagnerMise();
                } else if (scoreJoueur > scoreCroupier) {
                    resultats.append("Gagné !\n");
                    p.gagnerMise();
                } else if (scoreJoueur == scoreCroupier) {
                    resultats.append("Égalité (Récupère sa mise)\n");
                    p.recupererMise();
                } else {
                    resultats.append("Perdu.\n");
                    p.perdreMise();
                }
            }

            buildPlayersUI();

            // --- Build the in-game overlay ---
            VBox overlay = new VBox(20);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-alignment: center; -fx-padding: 50;");
            
            Label titre = new Label("Résultats de la partie");
            titre.setStyle("-fx-text-fill: gold; -fx-font-size: 36px; -fx-font-weight: bold;");
            
            Label detail = new Label(resultats.toString());
            detail.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-text-alignment: center;");
            
            Button btnNouvelleManche = new Button("Nouvelle Manche");
            btnNouvelleManche.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 5;");
            
            overlay.getChildren().addAll(titre, detail, btnNouvelleManche);

            // --- Safely Inject overlay ---
            Scene scene = playersContainer.getScene();
            Parent currentRoot = scene.getRoot();
            
            // Check if we already wrapped the game in a StackPane during a previous round
            if (currentRoot instanceof StackPane && "gameStack".equals(currentRoot.getId())) {
                ((StackPane) currentRoot).getChildren().add(overlay);
            } else {
                // First time wrapping it: create the StackPane and make it the permanent root
                StackPane stack = new StackPane(currentRoot, overlay);
                stack.setId("gameStack");
                scene.setRoot(stack);
            }

            // --- Handle the button click ---
            btnNouvelleManche.setOnAction(e -> {
                StackPane stack = (StackPane) playersContainer.getScene().getRoot();
                stack.getChildren().remove(overlay); // Simply delete the dark overlay
                preparerNouvelleManche();
            });

            System.out.println("Overlay des résultats affiché avec succès.");
            
        } catch (Exception e) {
            System.err.println("ERREUR FATALE lors de l'affichage des résultats :");
            e.printStackTrace();
        }
    }
    
    private void preparerNouvelleManche() {
        // 1. Empty all hands
        for (Player p : players) {
            p.viderMain();
        }
        croupier.viderMain();
        
        // 2. Reset the turn index and re-enable buttons
        currentPlayerIndex = 0;
        btnTirer.setDisable(false);
        btnRester.setDisable(false);
        
        // 3. Clear the UI to show empty green table
        buildPlayersUI();
        buildDealerUI();
        
        // 4. Start the next round (ask for bets, then deal)
        Platform.runLater(() -> {
            demanderMises();
            demarrerManche();
        });
    }
    
    private void jouerCroupier() {
        System.out.println("--- Tour du Croupier ---");
        // Start the delayed drawing process
        tirerCarteCroupierAvecDelai();
    }

    private void tirerCarteCroupierAvecDelai() {
        // Condition: Dealer must draw if score is strictly less than 17
        if (croupier.calculerScore() < 17) {
            
            Carte nouvelleCarte = deck.tirerCarte();
            croupier.ajouterCarte(nouvelleCarte);
            System.out.println("Le Croupier tire : " + nouvelleCarte.toString());
            
            // Refresh the UI so the newly drawn card appears on the screen
            buildDealerUI();
            
            // Create a 1-second delay
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            
            // When the second is up, run this method again to check the score
            pause.setOnFinished(event -> tirerCarteCroupierAvecDelai());
            pause.play();
            
        } else {
            // The dealer has reached 17 or more, the turn is over
            System.out.println("Score final du Croupier : " + croupier.calculerScore());
            
            // Pause for 1.5 seconds before showing the results overlay, 
            // so players have time to process the dealer's final hand.
            PauseTransition pauseFin = new PauseTransition(Duration.seconds(1.5));
            pauseFin.setOnFinished(event -> determinerGagnants());
            pauseFin.play();
        }
    }

    @FXML
    private void handleHit() {
        // GUARD: Stop immediately if it's already the dealer's turn
        if (currentPlayerIndex >= players.size()) return;

        Player currentPlayer = players.get(currentPlayerIndex);
        Carte nouvelleCarte = deck.tirerCarte();
        currentPlayer.ajouterCarte(nouvelleCarte);

        System.out.println(currentPlayer.getNom() + " a tiré : " + nouvelleCarte.toString());

        buildPlayersUI(); 
        updateActivePlayerUI();
        
        verifierScoreActuel();
    }

    @FXML
    private void handleStand() {
        // GUARD: Stop immediately if it's already the dealer's turn
        if (currentPlayerIndex >= players.size()) return;

        currentPlayerIndex++;
        
        if (currentPlayerIndex >= players.size()) {
            System.out.println("Tour du croupier !");
            btnTirer.setDisable(true);
            btnRester.setDisable(true);
            
            buildDealerUI(); // Reveal the hidden card
            
            // Trigger the dealer's automatic play
            jouerCroupier(); 
        } else {
            updateActivePlayerUI();
            // Automatically check if the NEW player already has 21 from the initial deal
            verifierScoreActuel();
        }
    }

    private void verifierScoreActuel() {
        // GUARD: Stop if the game is over
        if (currentPlayerIndex >= players.size()) return;

        Player p = players.get(currentPlayerIndex);
        if (p.calculerScore() >= 21) {
            handleStand(); // Automatically move to the next player
        }
    }
}