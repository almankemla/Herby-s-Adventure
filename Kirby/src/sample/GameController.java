package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class GameController {

    // UI Variables
    final Delta dragDelta = new Delta();
    private Stage primaryStage;
    private Scene menuScene;
    private Scene settingsScene;
    private Scene gameScene;
    private Label moneyDisplay;
    private Label monumentHealthDisplay;
    private AudioClip menuMusic;

    // Game Variables
    private final ArrayList<Tower> towerList = new ArrayList<>();
    private String playerName;
    private String gameDifficulty;
    private int moneyBalance = 0;
    private int monumentHealth = 0;
    private double costMultiplier;

    public void getUIObjects() {
        primaryStage = Main.getPrimaryStage();
        menuScene = Main.getMenuScene();
        settingsScene = Main.getSettingsScene();
        gameScene = Main.getGameScene();
        menuMusic = Main.getMenuMusic();
    }

    public void pressStart() {
        getUIObjects();
        primaryStage.setScene(settingsScene);
    }

    public void pressExit() {
        System.exit(0);
    }

    public void pressSettingsConfirm() throws Exception {
        getUIObjects();

        ChoiceBox difficultyInput = (ChoiceBox) settingsScene.lookup("#difficultyInput");
        TextField nameInput = (TextField) settingsScene.lookup("#nameInput");
        moneyDisplay = (Label) gameScene.lookup("#moneyDisplay");
        monumentHealthDisplay = (Label) gameScene.lookup("#monumentHealth");

        playerName = nameInput.getCharacters().toString();
        setGameDifficulty((String) difficultyInput.getValue());
        if (gameDifficulty == null || playerName.isBlank()) {
            createWarning(4, "Must have a valid name and difficulty selected!");
        } else if (gameDifficulty != null && !playerName.isEmpty()) {
            menuMusic.stop();
            iniatilizeGame();
            primaryStage.setScene(gameScene);
        }
    }

    public void setupDragEvent(String imageID) throws Exception {
        getUIObjects();
        ImageView image = (ImageView) gameScene.lookup(imageID);
        Tower dragTower = new Tower(image);
        Pane gamePane = (Pane) gameScene.lookup("#gamePane");

        Circle rangeCircle = new Circle();
        rangeCircle.setVisible(false);
        rangeCircle.setRadius(dragTower.getRange());
        rangeCircle.setOpacity(.4);
        rangeCircle.setFill(javafx.scene.paint.Color.BLACK);
        gamePane.getChildren().add(rangeCircle);

        image.toFront();

        image.setOnMousePressed(mouseEvent -> {
            dragDelta.x = image.getLayoutX() - mouseEvent.getSceneX();
            dragDelta.y = image.getLayoutY() - mouseEvent.getSceneY();
            image.setCursor(Cursor.MOVE);
        });
        image.setOnMouseReleased(mouseEvent -> {
            image.setCursor(Cursor.HAND);
            try {
                boolean collisionIssue = hasCollisionIssue(image);
                if (!collisionIssue) {
                    Tower purchaseTower = new Tower(image);
                    if (moneyBalance >= (purchaseTower.getTowerCost() * costMultiplier)) {
                        buyTower(purchaseTower, image);
                        gamePane.getChildren().add(purchaseTower);
                        updateUIs();
                    } else {
                        createWarning(2, "Insufficient funds!");
                    }
                } else {
                    createWarning(2, "Invalid placement location!");
                }
                rangeCircle.setVisible(false);
                returnPosition(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        image.setOnMouseDragged(mouseEvent -> {
            image.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
            image.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            try {
                updateRangeCircle(rangeCircle, image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        image.setOnMouseEntered(mouseEvent -> image.setCursor(Cursor.HAND));
    }

    private void updateRangeCircle(Circle rangeCircle, ImageView image) {
        rangeCircle.setCenterX(image.getBoundsInParent().getCenterX());
        rangeCircle.setCenterY(image.getBoundsInParent().getCenterY());
        if (hasCollisionIssue(image)) {
            rangeCircle.setFill(javafx.scene.paint.Color.RED);
        } else {
            rangeCircle.setFill(javafx.scene.paint.Color.BLACK);
        }

        if (!rangeCircle.isVisible()) {
            rangeCircle.setVisible(true);
        }
    }

    public void buyTower(Tower newTower, ImageView cloneImage) throws Exception {
        if (moneyBalance >= (newTower.getTowerCost() * costMultiplier)) {
            setMoney((int) (moneyBalance - (newTower.getTowerCost() * costMultiplier)));
            towerList.add(newTower);
            newTower.placeTower(cloneImage);
        } else {
            throw new Exception("Insufficient funds!");
        }
    }

    public boolean hasCollisionIssue(ImageView image) {
        boolean returnValue = false;
        getUIObjects();

        ImageView backgroundImage = (ImageView) primaryStage.getScene().lookup("#backgroundImage");
        Polygon menuCollision = (Polygon) gameScene.lookup("#menuCollision");
        Polygon trackCollision = (Polygon) gameScene.lookup("#trackCollision");
        Polygon waterCollision = (Polygon) gameScene.lookup("#waterCollision");

        Rectangle imageShape = new Rectangle();
        imageShape.setWidth(image.getFitWidth());
        imageShape.setHeight(image.getFitHeight());
        imageShape.setLayoutX(image.getBoundsInParent().getMinX());
        imageShape.setLayoutY(image.getBoundsInParent().getMinY());

        Shape menuCollisionShape = Shape.intersect(imageShape, menuCollision);
        Shape trackCollisionShape = Shape.intersect(imageShape, trackCollision);
        Shape waterCollisionShape = Shape.intersect(imageShape, waterCollision);

        Bounds backgroundImageBounds = backgroundImage.getBoundsInLocal();
        Bounds imageBounds = image.getBoundsInParent();

        if (!menuCollisionShape.getBoundsInLocal().isEmpty() ||
                !trackCollisionShape.getBoundsInLocal().isEmpty()||
                !waterCollisionShape.getBoundsInLocal().isEmpty() ||
                !(backgroundImageBounds.intersects(imageBounds))) {
            returnValue = true;
        } if (!towerList.isEmpty()) {
            for (Tower tower : towerList) {
                if (imageBounds.intersects(tower.getBoundsInParent())) {
                    returnValue = true;
                }
            }
        }

        return returnValue;
    }

    private void createWarning(int duration, String message) {
        getUIObjects();

        Label warningLabel = (Label) primaryStage.getScene().lookup("#warningLabel");
        ImageView warningImage = (ImageView) primaryStage.getScene().lookup("#warningImage");

        final KeyFrame showLabel = new KeyFrame(Duration.seconds(0), e -> {
            warningLabel.setText(message);
            warningImage.toFront();
            warningLabel.toFront();
            warningLabel.setVisible(true);
            warningImage.setVisible(true);
        });

        final KeyFrame removeLabel = new KeyFrame(Duration.seconds(duration), e -> {
            warningLabel.setVisible(false);
            warningImage.setVisible(false);
        });

        final Timeline timeline = new Timeline(showLabel, removeLabel);
        Platform.runLater(timeline::play);
    }

    private void returnPosition(ImageView image) throws Exception {
        if (image.getId().equals("fighterImage")) {
            image.setLayoutX(450);
            image.setLayoutY(599);
        } else if (image.getId().equals("mageImage")) {
            image.setLayoutX(580);
            image.setLayoutY(596);
        } else if (image.getId().equals("archerImage")) {
            image.setLayoutX(733);
            image.setLayoutY(601);
        } else {
            throw new Exception("Tower type does not exist");
        }
    }

    private void iniatilizeGame() throws Exception {
        getUIObjects();
        Main.getGameMusic().play();
        updateUIs();

        setupDragEvent("#fighterImage");
        setupDragEvent("#archerImage");
        setupDragEvent("#mageImage");

        updatePriceLabels();
    }

    private void updatePriceLabels() throws Exception {
        Label fighterPriceLabel = (Label) gameScene.lookup("#fighterPrice");
        Label magePriceLabel = (Label) gameScene.lookup("#magePrice");
        Label archerPriceLabel = (Label) gameScene.lookup("#archerPrice");

        ImageView fighterImage = (ImageView) gameScene.lookup("#fighterImage");
        ImageView mageImage = (ImageView) gameScene.lookup("#mageImage");
        ImageView archerImage = (ImageView) gameScene.lookup("#archerImage");

        Tower fighterTower = new Tower(fighterImage);
        Tower mageTower = new Tower(mageImage);
        Tower archerTower = new Tower(archerImage);

        String fighterTowerCost = String.valueOf((int) (fighterTower.getTowerCost() * costMultiplier));
        String mageTowerCost = String.valueOf((int) (mageTower.getTowerCost() * costMultiplier));
        String archerTowerCost = String.valueOf((int) (archerTower.getTowerCost() * costMultiplier));

        fighterPriceLabel.setText(fighterTowerCost);
        magePriceLabel.setText(mageTowerCost);
        archerPriceLabel.setText(archerTowerCost);

        fighterPriceLabel.toFront();
        magePriceLabel.toFront();
        archerPriceLabel.toFront();
    }

    private void updateUIs() {
        moneyDisplay.setText(String.valueOf(moneyBalance));
        monumentHealthDisplay.setText(String.valueOf(monumentHealth));
    }

    public void setMoney(int value) {
        moneyBalance = value;
    }

    public void setHealth(int value) {
        monumentHealth = value;
    }

    public void setGameDifficulty (String difficulty) {
        gameDifficulty = difficulty;

        switch (gameDifficulty) {
            case "Easy":
                setMoney(1000);
                setHealth(1000);
                costMultiplier = 0.5;
                break;
            case "Medium":
                setMoney(750);
                setHealth(750);
                costMultiplier = 1.0;
                break;
            case "Hard":
                setMoney(500);
                setHealth(500);
                costMultiplier = 1.5;
                break;
            default:
                throw new InvalidParameterException("Invalid game difficulty!");
        }
    }

    public int getMoneyBalance() {
        return moneyBalance;
    }

    public int getMonumentHealth() {
        return monumentHealth;
    }

    static class Delta { double x, y; }
}
