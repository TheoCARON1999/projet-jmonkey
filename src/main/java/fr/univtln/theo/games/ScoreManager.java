package fr.univtln.theo.games;

import com.atr.jme.font.*;
import com.atr.jme.font.asset.*;
import com.atr.jme.font.shape.TrueTypeContainer;
import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.Style;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

public class ScoreManager {
    public static final int MAX_SCORE = 12;
    public static final int MAX_LEVEL = 5;

    private Node guiNode;

    private BitmapText scoreText1;
    private BitmapText scoreText2;
    private int score1;
    private int score2;

    private BitmapText levelText;
    private int level;

    private BitmapText winnerText;
    private BitmapFont winnerFont;
    private TrueTypeContainer winnerTextContainer;
    private StringContainer winnerStringContainer;

    public ScoreManager(Node guiNode, BitmapFont guiFont, float localTranslationX, float localTranslationY, AssetManager assetManager) {
        this.guiNode = guiNode;

        level = 1;
        levelText = new BitmapText(guiFont, false);
        levelText.setSize(guiFont.getCharSet().getRenderedSize());
        levelText.setText("Level: " + level);
        levelText.setLocalTranslation(localTranslationX+300, localTranslationY + levelText.getLineHeight(), 0);
        guiNode.attachChild(levelText);

        score1 = 0;
        scoreText1 = new BitmapText(guiFont, false);
        scoreText1.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText1.setColor(ColorRGBA.Red);
        scoreText1.setText("Score: " + score1);
        scoreText1.setLocalTranslation(localTranslationX+300, localTranslationY + (scoreText1.getLineHeight()*2), 0);
        guiNode.attachChild(scoreText1);

        score2 = 0;
        scoreText2 = new BitmapText(guiFont, false);
        scoreText2.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText2.setColor(ColorRGBA.Blue);
        scoreText2.setText("Score: " + score2);
        scoreText2.setLocalTranslation(localTranslationX+300, localTranslationY + (scoreText1.getLineHeight()*3), 0);
        guiNode.attachChild(scoreText2);

        assetManager.registerLoader(TrueTypeLoader.class, "ttf");

        winnerText = new BitmapText(guiFont, false);
        winnerText.setSize(guiFont.getCharSet().getRenderedSize()*2);
        winnerText.setColor(ColorRGBA.Yellow);
        winnerText.setText("");
        winnerText.setLocalTranslation(localTranslationX+300, localTranslationY + (winnerText.getLineHeight()*7), 0);
        TrueTypeKey ttk = new TrueTypeKeyMesh("Interface/Fonts/hydro_squad_6908407/hydrosquad.ttf", Style.Plain, 12);
        TrueTypeFont ttf = assetManager.loadAsset(ttk);
        winnerStringContainer = new StringContainer(ttf, "Winner");
        winnerTextContainer = ttf.getFormattedText(winnerStringContainer, ColorRGBA.Yellow);
        winnerTextContainer.setLocalTranslation(localTranslationX+300, localTranslationY + (winnerText.getLineHeight()*7), 0);
    }

    public int incrementScore1() {
        score1++;
        scoreText1.setText("Score: " + score1);
        return score1;
    }

    public int incrementScore2() {
        score2++;
        scoreText2.setText("Score: " + score2);
        return score2;
    }

    public void resetScores() {
        score1 = 0;
        scoreText1.setText("Score: " + score1);
        score2 = 0;
        scoreText2.setText("Score: " + score2);
    }

    public int incrementLevel() {
        level++;
        levelText.setText("Level: " + level);
        return level;
    }

    public void setWinnerText(String text) {
        winnerText.setText(text);
        winnerTextContainer.setText(text);
        //guiNode.attachChild(winnerTextContainer); marche pas
        guiNode.attachChild(winnerText);
    }
}