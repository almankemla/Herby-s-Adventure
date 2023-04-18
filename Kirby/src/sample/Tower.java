package sample;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;

public class Tower extends ImageView {
    private String towerType;
    private int towerCost;
    private int range;
    private int damage;
    private int attackSpeed;
    private boolean canAttack;

    public Tower(ImageView image) throws Exception {
        if (image.getId().equals("fighterImage")) {
            towerType = "fighter";
            towerCost = 300;
            range = 125;
        } else if (image.getId().equals("mageImage")) {
            towerType = "mage";
            towerCost = 500;
            range = 150;
        } else if (image.getId().equals("archerImage")) {
            towerType = "archer";
            towerCost = 400;
            range = 200;
        } else {
            throw new Exception("Tower type does not exist");
        }
    }

    public void placeTower(ImageView image) {
        this.setFitWidth(100);
        this.setFitHeight(100);
        this.setX(image.getBoundsInParent().getMinX());
        this.setY(image.getBoundsInParent().getMinY());
        this.setImage(image.getImage());
        canAttack = true;
    }

    public boolean collidesWithOtherTowers(ArrayList<Tower> towerList) {
        boolean collides = false;
        Bounds imageBounds = this.getBoundsInParent();
        if (!towerList.isEmpty()) {
            for (Tower tower : towerList) {
                if (imageBounds.intersects(tower.getBoundsInParent())) {
                    collides = true;
                }
            }
        }
        return collides;
    }

    public int getTowerCost() {
        return towerCost;
    }

    public int getAttackDamage() {
        return damage;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public int getRange() {
        return range;
    }

    public String getTowerType() {
        return towerType;
    }
}
