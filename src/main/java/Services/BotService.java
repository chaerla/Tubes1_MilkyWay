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
    private UUID target;
    private List<GameObject> gameObjects;
    private List<GameObject> opponents;

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
        gameObjects = gameState.getGameObjects();
        List<GameObject> players = gameState.getPlayerGameObjects();
        opponents = new ArrayList<>(players);
        opponents.removeIf(x -> x.getId() == bot.getId());
        opponents = opponents.stream().sorted(Comparator
                .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
        PlayerAction tempAction = new PlayerAction();
        tempAction.action = playerAction.action;
        tempAction.heading = playerAction.heading;

        if (!(players.stream().anyMatch(item -> item.getId().equals(bot.getId())))) {
            System.out.println("died :(");
            System.out.println(players);
            return;
        }

        if (!gameObjects.isEmpty()) {
            var targetObject = gameObjects.stream()
                    .filter(gameObject -> gameObject.getId() == target)
                    .findFirst()
                    .orElse(null);
            if (targetObject == null){
                targetObject = opponents.stream()
                        .filter(gameObject -> gameObject.getId() == target)
                        .findFirst()
                        .orElse(null);
            }
            if (targetObject == null) {
                System.out.println("No current target");
                tempAction = resolveNewTarget();
            } else {
//                updateTargetInfo();
                if (targetObject.getSize() > bot.getSize()) {
                    tempAction = resolveNewTarget();
                } else {
                    if (targetObject.getGameObjectType() == ObjectTypes.PLAYER) {
                        tempAction.heading = getHeadingBetween(targetObject);
                        if(playerAction.action == PlayerActions.FORWARD){
                            if (checkShot(targetObject) && bot.torpedoSalvoCount >=0) {
                                tempAction.action = PlayerActions.FIRETORPEDOES;
                            } else {
                                tempAction = resolveNewTarget();
                            }
                        }
                        else{
                            if (checkShot(targetObject) && bot.torpedoSalvoCount >=0) {
                                tempAction.action = PlayerActions.FORWARD;
                            } else {
                                tempAction = resolveNewTarget();
                            }
                        }
                    } else if ((bot.effects & Effects.IsAfterburner.getValue()) == 1) {
                        if (checkCollitionWithAcc(targetObject)) {
                            tempAction.action = PlayerActions.FORWARD;
                            tempAction.heading = getHeadingBetween(targetObject);
                        } else {
                            tempAction.action = PlayerActions.STOPAFTERBURNER;
                            tempAction = resolveNewTarget();
                        }
                    } else {
                        tempAction.heading = getHeadingBetween(targetObject);
                    }
                }


                if (!checkTarget(targetObject) && tempAction.action != PlayerActions.FIRETORPEDOES && tempAction.action != PlayerActions.STARTAFTERBURNER) {
                    System.out.println("resolving target bcs target isnt safe to follow");
                    tempAction = resolveNewTarget();
                }
            }

        }
        System.out.println("===========SENDING COMMAND=================");
        this.playerAction = tempAction;
//        System.out.println("The selected target is a "+ targetObject.getGameObjectType());
        System.out.println("The selected heading is "+ this.playerAction.heading);
        System.out.println("The action is " + this.playerAction.action);
        System.out.println("============SENDING COMMAND=================");

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
        System.out.println("Resolving new target");
        PlayerAction ret = new PlayerAction();
        ret.action = playerAction.action;
        ret.heading = playerAction.heading;
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
                ret.heading = headingToOpp;
                ret.action = PlayerActions.FORWARD;
                target = opp.getId();
                return ret;
            } else if (checkCollitionWithAcc(opp)) {
                if ((bot.effects & Effects.IsAfterburner.getValue()) != 1) {
                    System.out.println("Chasing...");
                    ret.action = PlayerActions.STARTAFTERBURNER;
                } else {
                    ret.action = PlayerActions.FORWARD;
                }
                target = opp.getId();
                ret.heading = headingToOpp;
                return ret;
            }
        }

        for (GameObject food : foodList) {
            double distToFood = getDistanceBetween(bot, food);
            for (GameObject opponent : opponents) {
                double distToOpp = getDistanceBetween(bot, opponent);
                int headingToFood = getHeadingBetween(food);
                if (distToFood < distToOpp || checkTarget(food)
                        || bot.getSize() > opponent.getSize()) {
                    if (!isCrashBorder(food)) {
                        target = food.getId();
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

//    private void updateTargetInfo() {
//        System.out.println("=========== UPDATE ==============");
//        System.out.println("Target is a "+ target.getGameObjectType());
//        System.out.println("The heading is "+ playerAction.heading);
//        if (target.getGameObjectType() == ObjectTypes.PLAYER) {
//            for (GameObject opp : opponents) {
//                if (opp.getId() == target.getId()) {
//                    target = opp;
//                    return;
//                }
//            }
//        } else {
//            for (GameObject ob : gameObjects) {
//                if (ob.getId() == target.getId()) {
//                    target = ob;
//                    System.out.println("The new heading is "+ getHeadingBetween(target));
//                    System.out.println("=========== UPDATE ==============");
//                    return;
//                }
//            }
//        }
//    }

    private boolean checkTarget(GameObject t) {
        if(t.getGameObjectType() == ObjectTypes.FOOD){
            for (GameObject opp : opponents) {
                if (getDistanceBetween(t, opp) <= (opp.getSize() + bot.getSize())*1.15) {
                    for(int i = 0;i<100;i++){
                        System.out.println("AVOIDED HOREeeEe");
                    }
                    return false;
                }
            }
        }
        return !isCrashBorder(t);
    }

    private boolean checkShot(GameObject opp) {
        double distToOpp = getDistanceBetween(bot, opp);
        if (bot.torpedoSalvoCount > 0 && bot.getSize() >= 75) {
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

    // private boolean isCrash(GameObject object1, GameObject object2) {
    //     var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
    //     var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
    //     var dist = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    //     return (dist + bot.getSize()) >= object2.getSize();
    // }

    private boolean isCrashBorder(GameObject object) {
        var worldPos = gameState.getWorld().getCenterPoint();
        var triangleX = Math.abs(worldPos.x - object.getPosition().x);
        var triangleY = Math.abs(worldPos.y - object.getPosition().y);
        var dist = Math.sqrt(triangleX * triangleX + triangleY * triangleY);
        return dist * 0.8 >= gameState.getWorld().getRadius() + bot.getSize();
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
