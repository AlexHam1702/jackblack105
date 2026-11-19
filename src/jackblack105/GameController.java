package jackblack105;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
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
    @FXML private Button btnAssurance;

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
        });
    }
    
    private void demanderMises() {
        VBox overlay = new VBox(20);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-alignment: center; -fx-padding: 50;");

        Label titre = new Label("Phase de Mise");
        titre.setStyle("-fx-text-fill: gold; -fx-font-size: 36px; -fx-font-weight: bold;");

        Label erreurLabel = new Label("");
        erreurLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 16px; -fx-font-weight: bold;");

        VBox champsMises = new VBox(15);
        champsMises.setStyle("-fx-alignment: center;");

        // Map each player to their corresponding TextField
        Map<Player, TextField> inputs = new HashMap<>();

        for (Player p : players) {
            HBox ligne = new HBox(15);
            ligne.setStyle("-fx-alignment: center;");

            Label infoJoueur = new Label(p.getNom() + " (Solde : " + p.getPorteMonnaie() + ") :");
            infoJoueur.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-min-width: 260;");

            TextField champMise = new TextField("10");
            champMise.setMaxWidth(120);
            champMise.setStyle("-fx-font-size: 16px;");

            inputs.put(p, champMise);
            ligne.getChildren().addAll(infoJoueur, champMise);
            champsMises.getChildren().add(ligne);
        }

        Button btnValider = new Button("Valider les mises");
        btnValider.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 5;");

        btnValider.setOnAction(e -> {
            // 1. Validate every input first before deducting any chips
            Map<Player, Integer> montantsValides = new HashMap<>();

            for (Player p : players) {
                try {
                    int montant = Integer.parseInt(inputs.get(p).getText().trim());
                    if (montant <= 0 || montant > p.getPorteMonnaie()) {
                        erreurLabel.setText("Mise invalide pour " + p.getNom() + " (1 à " + p.getPorteMonnaie() + ").");
                        return;
                    }
                    montantsValides.put(p, montant);
                } catch (NumberFormatException ex) {
                    erreurLabel.setText("Veuillez entrer un nombre entier pour " + p.getNom() + ".");
                    return;
                }
            }

            // 2. Apply all valid bets
            for (Map.Entry<Player, Integer> entry : montantsValides.entrySet()) {
                entry.getKey().miser(entry.getValue());
            }

            // 3. Remove the betting overlay and deal the cards
            getGameStack().getChildren().remove(overlay);
            demarrerManche();
        });

        overlay.getChildren().addAll(titre, champsMises, erreurLabel, btnValider);
        getGameStack().getChildren().add(overlay);
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
    
    private StackPane getGameStack() {
        Scene scene = playersContainer.getScene();
        Parent currentRoot = scene.getRoot();

        if (currentRoot instanceof StackPane && "gameStack".equals(currentRoot.getId())) {
            return (StackPane) currentRoot;
        } else {
            StackPane stack = new StackPane(currentRoot);
            stack.setId("gameStack");
            scene.setRoot(stack);
            return stack;
        }
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
        updateAssuranceButton();
    }
    
    private void afficherFinDePartie() {
        VBox overlay = new VBox(20);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-alignment: center; -fx-padding: 50;");

        Label titre = new Label("Partie Terminée");
        titre.setStyle("-fx-text-fill: #f44336; -fx-font-size: 40px; -fx-font-weight: bold;");

        Label message = new Label("Tous les joueurs ont perdu leurs jetons !");
        message.setStyle("-fx-text-fill: white; -fx-font-size: 22px;");

        // --- Return to Main Menu Button ---
        Button btnMenu = new Button("Retour au Menu");
        btnMenu.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 5;");
        btnMenu.setOnAction(e -> {
            try {
                // Make sure "layout.fxml" matches your landing screen's exact filename
                Parent menuRoot = FXMLLoader.load(getClass().getResource("layout.fxml"));
                playersContainer.getScene().setRoot(menuRoot);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // --- Quit Button ---
        Button btnQuitter = new Button("Quitter le jeu");
        btnQuitter.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 25; -fx-background-radius: 5;");
        btnQuitter.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        HBox boutons = new HBox(20, btnMenu, btnQuitter);
        boutons.setStyle("-fx-alignment: center;");

        overlay.getChildren().addAll(titre, message, boutons);
        getGameStack().getChildren().add(overlay);
    }
    
    private void determinerGagnants() {
        System.out.println("--- Détermination des gagnants ---");
        
        try {
        	int scoreCroupier = croupier.calculerScore();
            boolean croupierBlackjack = croupier.estBlackjack();
            StringBuilder resultats = new StringBuilder("Score final du Croupier : " + scoreCroupier + "\n\n");

            for (Player p : players) {
                int scoreJoueur = p.calculerScore();
                boolean joueurBlackjack = p.estBlackjack();
                resultats.append(p.getNom()).append(" (Score: ").append(scoreJoueur).append(") - ");

                if (scoreJoueur > 21) {
                    resultats.append("A sauté ! (Perdu)\n");
                    p.perdreMise();
                } else if (joueurBlackjack && !croupierBlackjack) {
                    resultats.append("BLACKJACK ! Gagné (3:2)\n");
                    p.gagnerBlackjack();
                } else if (!joueurBlackjack && croupierBlackjack) {
                    resultats.append("Perdu (Le Croupier a un Blackjack).\n");
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

                if (p.getPorteMonnaie() <= 0) {
                    resultats.append("   ⚠ ").append(p.getNom()).append(" n'a plus de jetons et est éliminé !\n");
                }
                if (p.getMiseAssurance() > 0) {
                    if (croupierBlackjack) {
                        resultats.append("   ✓ Assurance gagnée (+").append(p.getMiseAssurance() * 2).append(" jetons)\n");
                        p.gagnerAssurance();
                    } else {
                        resultats.append("   ✗ Assurance perdue\n");
                        p.perdreAssurance();
                    }
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
        players.removeIf(p -> p.getPorteMonnaie() <= 0);

        if (players.isEmpty()) {
            afficherFinDePartie();
            return;
        }

        
        for (Player p : players) {
            p.viderMain();
        }
        croupier.viderMain();
        
        currentPlayerIndex = 0;
        btnTirer.setDisable(false);
        btnRester.setDisable(false);
        
        buildPlayersUI();
        buildDealerUI();
        
        Platform.runLater(() -> {
            demanderMises();
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
    private void handleAssurance() {
        if (currentPlayerIndex >= players.size()) return;

        Player currentPlayer = players.get(currentPlayerIndex);
        if (currentPlayer.prendreAssurance()) {
            buildPlayersUI();
            updateActivePlayerUI();
        }
    }

    private void updateAssuranceButton() {
        if (currentPlayerIndex >= players.size() || croupier.getMain().isEmpty()) {
            btnAssurance.setDisable(true);
            return;
        }

        Player p = players.get(currentPlayerIndex);
        boolean croupierMontreAs = croupier.getMain().get(0).isAs();
        boolean peutAssurer = croupierMontreAs
                && p.getMain().size() == 2
                && p.getMiseAssurance() == 0
                && p.getMiseActuelle() >= 2
                && p.getPorteMonnaie() >= (p.getMiseActuelle() / 2);

        btnAssurance.setDisable(!peutAssurer);
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
            btnAssurance.setDisable(true);
            
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