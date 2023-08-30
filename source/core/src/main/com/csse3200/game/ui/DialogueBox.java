package com.csse3200.game.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.ui.TitleBox;
import com.csse3200.game.GdxGame;

import static com.csse3200.game.screens.MainMenuScreen.logger;
import static com.csse3200.game.ui.UIComponent.skin;

public class DialogueBox extends Dialog {
    private GdxGame game;
    private TitleBox titleBox;
    private Label dialogueLabel;
    private DialogueBox dialogueBox;
    public DialogueBox(GdxGame game, DialogueBox dialogueBox,String title, Skin skin) {
        super(title, skin);
        this.game=game;
        this.dialogueBox = dialogueBox;
        this.dialogueLabel = new Label("", skin);
        this.getContentTable().add(dialogueLabel).align(Align.left);
        TextButton starttButton = new TextButton("OK", skin);
        button(starttButton, true);
        Entity entity = new Entity();
        entity.getEvents().addListener("ok", this::onOK);
        starttButton.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("OK button clicked");
                        entity.getEvents().trigger("ok");
                    }
                });
    }


    public void setDialogueText(String text) {
        dialogueLabel.setText(text);
    }
    public void showDialog(Stage stage) {
        stage.addActor(this);
    }
    private void onOK() {
        logger.info("Start game");
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);

    }
}