package fr.univtln.theo.games;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.screen.DefaultScreenController;

public class MainMenu extends BaseAppState {
    private Nifty nifty;

    @Override
    public void initialize(Application app) {
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                app.getAssetManager(),
                app.getInputManager(),
                app.getAudioRenderer(),
                app.getGuiViewPort());
        nifty = niftyDisplay.getNifty();

        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");

        nifty.addScreen("start", new ScreenBuilder("start") {{
            controller(new DefaultScreenController());
            layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
                panel(new PanelBuilder("panel_mid") {{
                    childLayoutVertical();
                    alignCenter();
                    height("50%");
                    width("75%");

                    // Ajout du titre
                    control(new LabelBuilder("Title", "Air Hockey Table") {{
                        alignCenter();
                        valignCenter();
                        height("40%");
                        width("100%");
                        font("Interface/Fonts/Default.fnt");
                    }});
                    // Ajout d'un espace
                    panel(new PanelBuilder() {{
                        height("5%");
                    }});

                    control(new ButtonBuilder("PvPButton", "PvP") {{
                        alignCenter();
                        valignCenter();
                        height("15%");
                        width("50%");
                        interactOnClick("startPvPGame()");
                    }});
                    control(new ButtonBuilder("PvEButton", "PvE") {{
                        alignCenter();
                        valignCenter();
                        height("15%");
                        width("50%");
                        interactOnClick("startPvEGame()");
                    }});
                    control(new ButtonBuilder("QuitButton", "Quit") {{
                        alignCenter();
                        valignCenter();
                        height("15%");
                        width("50%");
                        interactOnClick("quitGame()");
                    }});
                }});
            }});
        }}.build(nifty));

        nifty.gotoScreen("start");

        app.getGuiViewPort().addProcessor(niftyDisplay);

        nifty.addScreen("empty", new ScreenBuilder("empty") {{
            controller(new DefaultScreenController());
            layer(new LayerBuilder("background") {{
                childLayoutVertical();
            }});
        }}.build(nifty));

        app.getInputManager().addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        app.getInputManager().addListener(actionListener, "Pause");
    }

    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals("Pause") && !isPressed) {
                nifty.gotoScreen("start");
                // Code to pause the game goes here
            }
        }
    };

    public void startPvPGame() {
        nifty.gotoScreen("empty");
        // Code to start the PvP game goes here
    }

    public void startPvEGame() {
        nifty.gotoScreen("empty");
        // Code to start the PvE game goes here
    }

    public void quitGame() {
        // Code to quit the game goes here
    }

    @Override
    protected void cleanup(Application app) {}

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}
}