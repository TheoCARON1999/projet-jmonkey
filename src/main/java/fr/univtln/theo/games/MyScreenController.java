package fr.univtln.theo.games;

import de.lessvoid.nifty.screen.DefaultScreenController;

public class MyScreenController extends DefaultScreenController {
    private Game game;

    public MyScreenController(Game game) {
        this.game = game;
    }

    public void startPvEGame() {
        game.startPvEGame();
    }

    public void startPvPGame() {
        game.startPvPGame();
    }

    public void quitGame() {
        game.quitGame();
    }
}