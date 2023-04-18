package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import javax.sound.sampled.Clip;

public class Main extends Application {
    private static Stage primaryStage;
    private static Scene menuScene;
    private static Scene settingsScene;
    private static Scene gameScene;
    private static AudioClip menuMusic;
    private static AudioClip gameMusic;

    @Override
    public void start(Stage stage) throws Exception {
        Parent mainMenu = FXMLLoader.load(getClass().getResource("/ui/MainMenu.fxml"));
        Parent settingsMenu = FXMLLoader.load(getClass().getResource("/ui/GameSettings.fxml"));
        Parent game = FXMLLoader.load(getClass().getResource("/ui/GameScreen.fxml"));

        menuScene = new Scene(mainMenu, 1280, 720);
        settingsScene = new Scene(settingsMenu, 1280, 720);
        gameScene = new Scene(game, 1280, 720);

        stage.setTitle("Herby's Tower Defventure");
        stage.getIcons().add(new Image("herby.png"));
        stage.setScene(menuScene);
        stage.show();
        primaryStage = stage;

        {
            menuMusic = new AudioClip(this.getClass().
                    getResource("menumusic.mp3").toString());
        }
        {
            gameMusic = new AudioClip(this.getClass().
                    getResource("gamemusic.mp3").toString());
        }
        menuMusic.setVolume(.5);
        gameMusic.setVolume(.5);
        menuMusic.setCycleCount(Clip.LOOP_CONTINUOUSLY);
        gameMusic.setCycleCount(Clip.LOOP_CONTINUOUSLY);
        menuMusic.play();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Scene getMenuScene() {
        return menuScene;
    }

    public static Scene getSettingsScene() {
        return settingsScene;
    }

    public static Scene getGameScene() {
        return gameScene;
    }

    public static AudioClip getMenuMusic() { return menuMusic; }

    public static AudioClip getGameMusic() { return gameMusic; }
}
