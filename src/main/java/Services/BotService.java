package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private boolean afterBurnerOn = false;
    private Integer timeSinceLastAction;
    private GameObject target;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {
        List<GameObject> gameObjects = gameState.getGameObjects();

        PlayerAction tempAction = playerAction;

        if (gameObjects.stream().anyMatch(item -> item.getId().equals(bot.getId()))) {
            System.out.println("died :(");
            return;
        }

        if (!gameObjects.isEmpty()) {
            if (target == null) {
                System.out.println("No current target");
                tempAction = resolveNewTarget();
            } else {
                if (gameObjects.stream().anyMatch(item -> item.getId().equals(target.getId()))) {
                    updateTargetInfo();
                    if (target.getSize() > bot.getSize()) {
                        tempAction = resolveNewTarget();
                    } else {
                        if (playerAction.action == PlayerActions.FIRETORPEDOES) {
                            if (checkShot(target)) {
                                tempAction.action = PlayerActions.FORWARD;
                                tempAction.heading = getHeadingBetween(target);
                            } else {
                                tempAction = resolveNewTarget();
                            }
                        } else if (playerAction.action == PlayerActions.STARTAFTERBURNER) {
                            if (checkCollitionWithAcc(target)) {
                                tempAction.action = PlayerActions.FORWARD;
                                tempAction.heading = getHeadingBetween(target);
                            } else {
                                tempAction.action = PlayerActions.STOPAFTERBURNER;
                                target = null;
                            }
                        } else {
                            tempAction.heading = getHeadingBetween(target);
                            System.out.println("current target is a " + target.getGameObjectType());
                            System.out.println("Going at heading " + tempAction.heading);
                        }
                    }
                } else {
                    tempAction = resolveNewTarget();
                }

                while (!checkTarget()) {
                    tempAction = resolveNewTarget();
                }
            }

        }

        this.playerAction = tempAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    // private boolean checkCollitionWithoutAcc(GameObject object) {
    // if (afterBurner) {
    // return getDistanceBetween(bot, object) - bot.getSize() < object.speed -
    // bot.speed / 2;
    // }
    // return getDistanceBetween(bot, object) < object.speed - bot.speed;
    // }

    private PlayerAction resolveNewTarget() {
        PlayerAction ret = playerAction;
        List<GameObject> gameObjects = gameState.getGameObjects();
        List<GameObject> players = gameState.getPlayerGameObjects();
        List<GameObject> opponents = players;
        opponents = players;
        opponents.removeIf(x -> x.getId() == bot.getId());
        opponents = opponents.stream().sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
        List<GameObject> foodList = gameObjects.stream()
                .filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
        for (GameObject opp : opponents) {
            int headingToOpp = getHeadingBetween(opp);
            // priority nembak
            if (checkShot(opp)) {
                System.out.println("Firing torpedoes..");
                ret.heading = -1 * headingToOpp;
                ret.action = PlayerActions.FIRETORPEDOES;
                target = opp;
                return ret;
            } else if (checkCollitionWithAcc(opp)) {
                if ((bot.effects & Effects.IsAfterburner.getValue()) == 1) {
                    System.out.println("Chasing...");
                    ret.action = PlayerActions.STARTAFTERBURNER;
                } else {
                    ret.action = PlayerActions.FORWARD;
                }
                target = opp;
                ret.heading = headingToOpp;
                return ret;
            }
        }

        for (GameObject food : foodList) {
            double distToFood = getDistanceBetween(bot, food);
            for (GameObject opponent : opponents) {
                double distToOpp = getDistanceBetween(bot, opponent);
                int headingToFood = getHeadingBetween(food);
                target = food;
                if (distToFood < distToOpp || checkTarget()
                        || bot.getSize() > opponent.getSize()) {
                    if (!isCrashBorder(food)) {
                        System.out.println("Just eating...");
                        ret.action = PlayerActions.FORWARD;
                        ret.heading = headingToFood;
                        return ret;
                    }
                }
            }
        }

        return ret;
    }

    private void updateTargetInfo() {
        List<GameObject> gameObjects = gameState.getGameObjects();
        List<GameObject> players = gameState.getPlayerGameObjects();
        if (target.getGameObjectType() == ObjectTypes.PLAYER) {
            for (GameObject opp : players) {
                if (opp.getId() == target.getId()) {
                    target = opp;
                }
            }
        } else {
            for (GameObject ob : gameObjects) {
                if (ob.getId() == target.getId()) {
                    target = ob;
                }
            }
        }
    }

    private boolean checkTarget() {
        List<GameObject> players = gameState.getPlayerGameObjects();
        List<GameObject> opponents = players;
        opponents = players;

        for (GameObject opp : opponents) {
            if (getDistanceBetween(target, opp) <= opp.getSize() + bot.getSize()) {
                return false;
            }
        }
        return !isCrashBorder(target);
    }

    private boolean checkShot(GameObject opp) {
        double distToOpp = getDistanceBetween(bot, opp);
        if (bot.torpedoSalvoCount > 0 && bot.getSize() >= 100) {
            if ((distToOpp + opp.getSize() <= (opp.getSize() / opp.getSpeed() * 60) ||
                    bot.torpedoSalvoCount == 5)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCollitionWithAcc(GameObject object) {
        var tick = getDistanceBetween(bot, object) / (bot.speed * 2 - object.speed);
        return ((bot.speed * 2 > object.speed) && bot.getSize() - tick > object.getSize());
    }

    private boolean isCrash(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        var dist = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
        return (dist + bot.getSize()) >= object2.getSize();
    }

    private boolean isCrashBorder(GameObject object) {
        var worldPos = gameState.getWorld().getCenterPoint();
        var triangleX = Math.abs(worldPos.x - object.getPosition().x);
        var triangleY = Math.abs(worldPos.y - object.getPosition().y);
        var dist = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
        return (dist + bot.getSize()) * 0.8 >= gameState.getWorld().getRadius();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream()
                .filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        var res = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
        if (object1.gameObjectType == ObjectTypes.PLAYER && object2.gameObjectType == ObjectTypes.PLAYER) {
            res -= (object1.size + object2.size);
        }
        return res;
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

}
