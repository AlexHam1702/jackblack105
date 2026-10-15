package jackblack105;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Controller {

    @FXML
    private Label label;

    public void initialize() {
        label.setText("Hello, Player \n Let's play some blackjack");
    }
}
