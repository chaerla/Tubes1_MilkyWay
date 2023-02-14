package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

import com.fasterxml.jackson.databind.ser.impl.IndexedListSerializer;

/**
 * reff: chow-v1.0
 */

public class BotService extends CheckEffect{
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

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
        List<GameObject> players = gameState.getPlayerGameObjects();
        List<GameObject> opponents;

        // filter opponents from players
        if (!players.isEmpty()) {
            opponents = players;
            opponents.removeIf(x -> x.getId() == bot.getId());

            if (!gameState.getGameObjects().isEmpty()) {
                /* *** LIST AND SORT GAMEOBJECTS BY DISTANCE *** */
                // sort opponent by distance with bot
                opponents.stream().sorted(Comparator.comparing(item -> getDistanceBetween(bot,item)));
                System.out.println(getDistanceBetween(bot,opponents.get(0)));

                // check nearest food with bot
                List<GameObject> foodList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                // check nearest wormhole
                List<GameObject> wormholeList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.WORMHOLE)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                // check nearest gasCloud
                List<GameObject> gasCloudList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                // check nearest asteroidField
                List<GameObject> asteroidList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                // check nearest asteroidField
                List<GameObject> torpedoList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                // check nearest super food with bot
                List<GameObject> superFoodList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList()); 
                // check nearest supernovapickup with bot
                List<GameObject> superNovaPickupList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList()); 
                // check nearest supernovabomb with bot
                List<GameObject> superNovaBombList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());  
                // check teleporters
                List<GameObject> teleporterList = gameObjects.stream()
                        .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                        .sorted(Comparator
                                .comparing(item -> getDistanceBetween(bot, item)))
                        .collect(Collectors.toList());
                
                /* *** MAP RADAR OF CLOSEST OBSTACLE; RESTRICTS BOT FROM ENTERING OBSTACLES *** */
                // List of heading range restriction
                List<DegreeRange> headingRestriction = new ArrayList<>();

                // check if near world border
                int botXPos = bot.getPosition().getX();
                int botYPos = bot.getPosition().getY();
                int worldRadius = gameState.world.getRadius();
                
                // worldRadius - botPos - botsize
                if (worldRadius - Math.sqrt(Math.pow(botXPos,2) + Math.pow(botYPos,2)) - bot.getSize() < 2){
                    int headingToCenter = (toDegrees(Math.atan2(-1 * botYPos, -1 * botXPos)) + 360) % 360;
                    DegreeRange toAdd = new DegreeRange(headingToCenter + 95, headingToCenter - 95);
                    headingRestriction.add(toAdd);
                    System.out.println("BOT IS NEAR WORLD BORDER");
                }
                System.out.println("BORDER CHECK OK");

                // check if is in asteroid field
                if (asteroidList.size() > 0){
                    if (isInAsteroidField(bot.effects) || getOuterDistanceBetween(asteroidList.get(0), bot) < 2) {
                        int headingToAsteroid = getHeadingBetween(asteroidList.get(0));
                        int deltaHeading = (int) (asteroidList.get(0).getSize()/getDistanceBetween(asteroidList.get(0), bot));
                        DegreeRange toAdd = new DegreeRange(headingToAsteroid+deltaHeading,headingToAsteroid-deltaHeading);
                        headingRestriction.add(toAdd);
                        System.out.println("BOT IS NEAR ASTEROIDS");
                    } else {
                        System.out.println("ASTEROID CHECK OK");
                    }
                }

                // check if near gas cloud
                if (gasCloudList.size() > 0){
                    if (isInGasCloud(bot.effects) || getOuterDistanceBetween(gasCloudList.get(0), bot) < 2) {
                        int headingToGasCloud = getHeadingBetween(gasCloudList.get(0));
                        int deltaHeading = (int) (gasCloudList.get(0).getSize()/getDistanceBetween(gasCloudList.get(0), bot));
                        DegreeRange toAdd = new DegreeRange(headingToGasCloud+deltaHeading,headingToGasCloud-deltaHeading);
                        headingRestriction.add(toAdd);
                        System.out.println("BOT IS NEAR GASCLOUD");
                    } else {
                        System.out.println("GASCLOUD CHECK OK");
                    }
                }

                // check nearest torpedo
                if (torpedoList.size() > 0) {
                    if (getDistanceBetween(bot, torpedoList.get(0)) < bot.getSize() + 80) {
                        int headingToTorpedo = getHeadingBetween(torpedoList.get(0));
                        DegreeRange toAdd = new DegreeRange(headingToTorpedo + 30,headingToTorpedo - 30);
                        headingRestriction.add(toAdd);
                        System.out.println("BOT IS NEAR TORPEDO");
                    } else {
                        System.out.println("ENEMY TORPEDO CHECK OK");
                    }
                }



                /* *** OPPONENT CHECKS AND STRATEGY/COMMAND SELECTION *** */
                double distanceToOpp = getOuterDistanceBetween(bot, opponents.get(0));
                double distanceToTor;
                if (torpedoList.size() > 0) {
                    distanceToTor = getOuterDistanceBetween(bot, torpedoList.get(0));
                } else {
                    distanceToTor = 2 * worldRadius;
                }
                int headingToOpp = getHeadingBetween(opponents.get(0));

                /*
                 * if (near torp or near enemy) {
                 * if near enemy try attack
                 * if near torpedo try defence
                 * } else {
                 * gather food
                 * }
                 */
                if (distanceToOpp < 60 || distanceToTor < 60) {        /* COMBAT MODE */
                    System.out.println("COMBAT MODE");
                    boolean hasChosenStrat = false;
                    if (!hasChosenStrat && bot.hasTorpedo() && bot.getSize() > 20) {  /* try torpedo */
                        System.out.println("TORPEDO FIRED");
                        playerAction.heading = headingToOpp;
                        playerAction.action = PlayerActions.FIRETORPEDOES;
                        hasChosenStrat = true;
                    }
                    if (!hasChosenStrat && (bot.getSize() - (distanceToOpp/bot.getSpeed()) > opponents.get(0).getSize())){  /* try afterburner charge */
                        playerAction.heading = headingToOpp;
                        if (!isAfterburner(bot.effects) && bot.getSize() > 10) {
                            playerAction.action = PlayerActions.STARTAFTERBURNER;
                            System.out.println("START AFTERBURNER");
                        } else {
                            playerAction.action = PlayerActions.FORWARD;
                        }
                        // playerAction.action = PlayerActions.FORWARD;
                        hasChosenStrat = true;   
                    }
                    // if (!hasChosenStrat && bot.getSize() < opponents.get(0).getSize()) {    /* evade */
                    //     System.out.println("EVADE OPPONENT");
                    //     DegreeRange oppRange = new DegreeRange((int) (headingToOpp + distanceToOpp/opponents.get(0).getSize()), (int) (headingToOpp - distanceToOpp/opponents.get(0).getSize()));
                    //     headingRestriction.add(oppRange);
                    //     playerAction.heading = randValidHeading(headingRestriction);
                    //     if (!isAfterburner(bot.effects) && bot.getSize() > 10) {
                    //         playerAction.action = PlayerActions.STARTAFTERBURNER;
                    //     } else {
                    //         playerAction.action = PlayerActions.FORWARD;
                    //     }
                    //     hasChosenStrat = true;
                    // }

                    // use shield as defense
                    if (torpedoList.size() > 0){
                        System.out.println("DEFENCE CHECK");
                        int torpedoHeading = torpedoList.get(0).currentHeading;
                        System.out.println("TORPEDO HEADING : " + torpedoHeading);
                        if (distanceToTor < 64 && heading_gap(headingToOpp, torpedoHeading) > 5) {
                            if (bot.hasShield() && !isUsingShield(bot.effects) && bot.getSize() > 25) {
                                playerAction.action = PlayerActions.ACTIVATESHIELD;
                                System.out.println("SHIELD ACTIVATED");
                            }
                        }
                    }
                } else {        /* SAFE MODE */
                    System.out.println("SAFE MODE");

                    // System.out.println("PASSED STOP AFTERBURNER CHECK");

                    // restrict heading to opponents < 100
                    for (GameObject opponent : opponents) {
                        if (getOuterDistanceBetween(bot, opponent) < 100) {
                            int headingToThisOpp = getHeadingBetween(opponent);
                            int deltaHeading = (int) (opponent.getSize()/getDistanceBetween(opponent, bot));
                            DegreeRange toAdd = new DegreeRange(headingToThisOpp+deltaHeading,headingToThisOpp-deltaHeading);
                            System.out.println("HEADINGGGGG : "+headingToOpp);
                            System.out.println("STARTTT: " + toAdd.getStartDegree() + " END : " + "STARTTT: " + toAdd.getEndDegree());
                            headingRestriction.add(toAdd);
                        }
                    }
                    System.out.println("OPPONENT HEADING CHECK DONE");

                    int heading = -1;
                    
                    // check possible food
                    int indexFood = -1;
                    for (int i=0; i < foodList.size(); i++) {
                        if (isHeadingUnrestricted(getHeadingBetween(foodList.get(i)), headingRestriction)){
                            indexFood = i;
                            break;
                        }
                    }
                    // check possible superfood 
                    int indexSuperFood = -1;
                    if (!hasSuperfood(bot.effects)){
                        for (int i=0; i < superFoodList.size(); i++) {
                            if (isHeadingUnrestricted(getHeadingBetween(superFoodList.get(i)), headingRestriction)){
                                indexSuperFood = i;
                                break;
                            }
                        }
                    }
                    // check possible supernova charge 
                    int indexNovaPickup = -1;
                    for (int i=0; i < superNovaPickupList.size(); i++) {
                        if (isHeadingUnrestricted(getHeadingBetween(superNovaPickupList.get(i)), headingRestriction)){
                            System.out.println("SUPERNOVA PICKUP DETECTED");
                            indexNovaPickup = i;
                            break;
                        }
                    }
                                        
                    // check whether goes to food or superfood or supernova pickup
                    double distToFood = (indexFood != -1) ? getDistanceBetween(bot, foodList.get(indexFood)) : worldRadius * 2;
                    double distToSFood = (indexSuperFood != -1) ? getDistanceBetween(bot, superFoodList.get(indexSuperFood)) : worldRadius * 2;
                    double distToSNova = (indexNovaPickup != -1) ? getDistanceBetween(bot, superNovaPickupList.get(indexNovaPickup)) : worldRadius * 2;

                    if (distToSFood/4 <= distToFood && distToSFood/2 <= distToSNova && distToSFood < worldRadius * 2) {
                        heading = getHeadingBetween(superFoodList.get(indexSuperFood));
                        System.out.println("GO TO SUPERFOOD");
                    } else if (distToSNova/2 <= distToFood * 1.5 && distToSNova <= distToSFood/2 && distToSNova < worldRadius * 2) {
                        heading = getHeadingBetween(superNovaPickupList.get(indexNovaPickup));
                        System.out.println("GO TO SUPERNOVA PICKUP");
                    } else if (distToFood <= distToSFood/4 && distToFood <= distToSNova/2 && distToFood < worldRadius * 2) {
                        heading = getHeadingBetween(foodList.get(indexFood));
                        System.out.println("GO TO FOOD");
                    } else {
                        heading = randValidHeading(headingRestriction);
                        System.out.println("RANDOM");
                    }

                    playerAction.heading = heading;
                    if (isAfterburner(bot.effects)){
                        playerAction.action = PlayerActions.STOPAFTERBURNER;
                        System.out.println("STOPPED AFTERBURNER");
                    } else {
                        playerAction.action = PlayerActions.FORWARD;
                    }
                }

                /* chow */

                /* CHOICE 1: ATTACK */
                // if (isHeadingUnrestricted(headingToOpp, headingRestriction)) {
                //     System.out.println("TRY ATTACK");
                //     // if could use torpedoes
                //     if (!hasChosenStrat && bot.hasTorpedo() && (opponents.get(0).getSize() < bot.getSize() + bot.torpedoSalvoCount*10) && (distanceToOpp < 50) && bot.getSize() > 20) {
                //         System.out.println("TORPEDO FIRED");
                //         playerAction.heading = headingToOpp;
                //         playerAction.action = PlayerActions.FIRETORPEDOES;
                //         // playerAction.action = PlayerActions.FORWARD;
                //         hasChosenStrat = true;
                //     } 
                //     System.out.println("PASSED USE TORPEDO CHECK");
    
                //     // if could use after burner
                //     if (!hasChosenStrat && (bot.getSize() - (distanceToOpp/bot.getSpeed()) > opponents.get(0).getSize())){
                //         System.out.println("USE AFTERBURNER");
                //         playerAction.heading = headingToOpp;
                //         if (!isAfterburner(bot.effects) && bot.getSize() > 10) {
                //             playerAction.action = PlayerActions.STARTAFTERBURNER;
                //         } else {
                //             playerAction.action = PlayerActions.FORWARD;
                //         }
                //         // playerAction.action = PlayerActions.FORWARD;
                //         hasChosenStrat = true;   
                //     }
                //     System.out.println("PASSED USE AFTERBURNER CHECK");

                // }
                // // if could use existing tele
                // if (!hasChosenStrat && bot.getActiveTeleHead() != -1) {
                //     GameObject activeTeleporter = teleporterList.get(0);
                //     for (GameObject teleporters : teleporterList) {
                //         if (teleporters.getCurrHeading() == bot.getActiveTeleHead()) {
                //             activeTeleporter = teleporters;
                //             break;
                //         }
                //     }
                //     if (getDistanceBetween(activeTeleporter, opponents.get(0)) <= opponents.get(0).getSize() + 30 && bot.getSize() >= 1.5 * opponents.get(0).getSize()) {
                //         playerAction.action = PlayerActions.TELEPORT;
                //         hasChosenStrat = true;
                //     }
                // }
                // // if could use new teleporter 
                // if (!hasChosenStrat && bot.hasTeleporter() && (opponents.get(0).getSize() < bot.getSize() + 120) && (distanceToOpp < 60) && bot.getSize() > 40) {
                //     System.out.println("FIRE TELEPORTER");
                //     playerAction.heading = headingToOpp;
                //     bot.setActiveTeleHead(headingToOpp);
                //     playerAction.action = PlayerActions.FIRETELEPORT;
                //     hasChosenStrat = true;
                // }

                // CHOICE 2: GATHER RESOURCES
        //         if (!hasChosenStrat){
        //             System.out.println("TRY GATHER AMMO");

        //             // System.out.println("PASSED STOP AFTERBURNER CHECK");

        //             // restrict heading to opponents < 150
        //             for (GameObject opponent : opponents) {
        //                 if (getDistanceBetween(bot, opponent) - bot.getSize() - opponent.getSize() < 100) {
        //                     int headingToThisOpp = getHeadingBetween(opponent);
        //                     int deltaHeading = (int) (opponent.getSize()/getDistanceBetween(opponent, bot));
        //                     DegreeRange toAdd = new DegreeRange(headingToThisOpp+deltaHeading,headingToThisOpp-deltaHeading);
        //                     System.out.println("HEADINGGGGG : "+headingToOpp);
        //                     System.out.println("STARTTT: " + toAdd.getStartDegree() + " END : " + "STARTTT: " + toAdd.getEndDegree());
        //                     headingRestriction.add(toAdd);
        //                 }
        //             }
        //             System.out.println("OPPONENT HEADING CHECK DONE");

        //             int heading = -1;
                    
        //             // check possible food
        //             int indexFood = -1;
        //             for (int i=0; i < foodList.size(); i++) {
        //                 if (isHeadingUnrestricted(getHeadingBetween(foodList.get(i)), headingRestriction)){
        //                     indexFood = i;
        //                     break;
        //                 }
        //             }
        //             // check possible superfood 
        //             int indexSuperFood = -1;
        //             if (!hasSuperfood(bot.effects)){
        //                 for (int i=0; i < superFoodList.size(); i++) {
        //                     if (isHeadingUnrestricted(getHeadingBetween(superFoodList.get(i)), headingRestriction)){
        //                         indexSuperFood = i;
        //                         break;
        //                     }
        //                 }
        //             }
        //             // check possible supernova charge 
        //             int indexNovaPickup = -1;
        //             for (int i=0; i < superNovaPickupList.size(); i++) {
        //                 if (isHeadingUnrestricted(getHeadingBetween(superNovaPickupList.get(i)), headingRestriction)){
        //                     indexNovaPickup = i;
        //                     break;
        //                 }
        //             }
                                        
        //             // check whether goes to food or superfood or supernova pickup
        //             double distToFood = (indexFood != -1) ? getDistanceBetween(bot, foodList.get(indexFood)) : worldRadius * 2;
        //             double distToSFood = (indexSuperFood != -1) ? getDistanceBetween(bot, superFoodList.get(indexSuperFood)) : worldRadius * 2;
        //             double distToSNova = (indexNovaPickup != -1) ? getDistanceBetween(bot, superNovaPickupList.get(indexNovaPickup)) : worldRadius * 2;
        //             if (distToSNova < worldRadius * 2) {
        //                 System.out.println("SUPERNOVA PICKUP DETECTED");
        //             }

        //             if (distToSFood/4 <= distToFood && distToSFood/2 <= distToSNova && distToSFood < worldRadius * 2) {
        //                 heading = getHeadingBetween(superFoodList.get(indexSuperFood));
        //             } else if (distToSNova/2 <= distToFood * 1.5 && distToSNova <= distToSFood/2 && distToSNova < worldRadius * 2) {
        //                 heading = getHeadingBetween(superNovaPickupList.get(indexNovaPickup));
        //             } else if (distToFood <= distToSFood/4 && distToFood <= distToSNova/2 && distToFood < worldRadius * 2) {
        //                 heading = getHeadingBetween(foodList.get(indexFood));
        //             } else {
        //                 // generate random heading
        //                 heading = randValidHeading(headingRestriction);
        //             }
                    
        //             // shutdown afterburner
                                          
        //             playerAction.heading = heading;
        //             if (isAfterburner(bot.effects)){
        //                 playerAction.action = PlayerActions.STOPAFTERBURNER;
        //                 System.out.println("STOPPED AFTERBURNER");
        //             } else {
        //                 playerAction.action = PlayerActions.FORWARD;
        //             }

        //         }

        //         /* *** DEFENCE MECHANISM *** */
        //         // use shield as defense
        //         if (torpedoList.size() > 0){
        //             System.out.println("DEFENCE CHECK");
        //             int torpedoHeading = torpedoList.get(0).currentHeading;
        //             System.out.println("TORPEDO HEADING : " + torpedoHeading);
        //             if ((getDistanceBetween(bot, torpedoList.get(0)) < bot.getSize() + 50) && heading_gap(headingToOpp, torpedoHeading) > 5) {
        //                 if (bot.hasShield() && !isUsingShield(bot.effects)) {
        //                     playerAction.action = PlayerActions.ACTIVATESHIELD;
        //                     System.out.println("SHIELD ACTIVATED");
        //                 }
        //             }
        //         }
        //         System.out.println("DEFENCE CHECK DONE");

                /* FINAL CHECK */
                System.out.println("DESTRUCTIVE AFTERBURNER CHECK");
                if (isAfterburner(bot.effects) && bot.getSize()<16) {
                    playerAction.action = PlayerActions.STOPAFTERBURNER;
                    System.out.println("AFTERBURNER DISABLED");
                }
                // if (playerAction.action == PlayerActions.TELEPORT) {
                //     bot.setActiveTeleHead(-1);
                // }

                // info 
                System.out.println("Tick : " + gameState.world.currentTick);
                System.out.println("World Radius : " + worldRadius);
                System.out.println("Active Command : " + playerAction.action.name());
                System.out.println("Bot size : " + bot.getSize());
                System.out.println("Bot heading : " + bot.currentHeading);
                System.out.println("Bot position : (" + botXPos + "," + botYPos + ")");
                System.out.println("Bot effects : " + bot.effects);
                System.out.println("Dist to opp : " + getDistanceBetween(opponents.get(0), bot));
                System.out.println("Opponents size : " + opponents.get(0).getSize());
                System.out.println("Heading to opponent : " + getHeadingBetween(opponents.get(0)));
                System.out.println("Restriction : " + headingRestriction.toString());
                for(DegreeRange range : headingRestriction) {
                    System.out.println(range.getStartDegree() + " - " + range.getEndDegree());
                }
                System.out.println();

            }

        }

        this.playerAction = playerAction;
    }

    public boolean isHeadingUnrestricted(int heading, List<DegreeRange> restrictedRanges) {
        for (DegreeRange range : restrictedRanges) {
            if (range.isInRange(heading)) {
                return false;
            }
        }
        return true;
    }

    public int randValidHeading (List<DegreeRange> headingRestriction) {
        // generate random heading
        Random rand = new Random();
        int heading = rand.nextInt(359);
        while (!isHeadingUnrestricted(heading, headingRestriction)) {
            rand = new Random();
            heading = rand.nextInt(359);
        }
        return heading;
    }

    public int heading_gap(int heading1, int heading2) {
        // Normalize headings to the range [0, 360)
        heading1 = heading1 % 360;
        heading2 = heading2 % 360;
    
        int gap = Math.abs(heading2 - heading1);
        gap = Math.min(gap, 360 - gap);
    
        return gap;
    }
    

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
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

    private double getOuterDistanceBetween(GameObject object1, GameObject object2) {
        return getDistanceBetween(object1, object2) - object1.getSize() - object2.getSize();
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

class DegreeRange {
    private int startDegree;
    private int endDegree;
  
    public DegreeRange(int startDegree, int endDegree) {
      this.startDegree = normalizeDegree(startDegree);
      this.endDegree = normalizeDegree(endDegree);
    }
  
    public int getStartDegree() {
      return startDegree;
    }
  
    public int getEndDegree() {
      return endDegree;
    }
  
    public void setStartDegree(int startDegree) {
      this.startDegree = normalizeDegree(startDegree);
    }
  
    public void setEndDegree(int endDegree) {
      this.endDegree = normalizeDegree(endDegree);
    }
  
    public boolean isInRange(int degree) {
      degree = normalizeDegree(degree);
      if (startDegree <= endDegree) {
        return degree >= startDegree && degree <= endDegree;
      } else {
        return degree >= startDegree || degree <= endDegree;
      }
    }
  
    private int normalizeDegree(int degree) {
      degree %= 360;
      if (degree < 0) {
        degree += 360;
      }
      return degree;
    }
  }

class CheckEffect {
    private List<Integer> numbers = new ArrayList<>(Arrays.asList(1,2,4,8,16));

    public List<Integer> effectsUsed (Integer effect){
        List<Integer> effects = new ArrayList<>();
        for (int i = numbers.size() - 1; i >= 0; i--) {
          while (effect >= numbers.get(i)) {
            effect -= numbers.get(i);
            effects.add(numbers.get(i));
          }
        }
        return effects;
    }

    public boolean isAfterburner(Integer value) {
        return effectsUsed(value).contains(1);
    }

    public boolean isInAsteroidField(Integer value) {
        return effectsUsed(value).contains(2);
    }

    public boolean isInGasCloud(Integer value) {
        return effectsUsed(value).contains(4);
    }

    public boolean hasSuperfood(Integer value) {
        return effectsUsed(value).contains(8);
    }

    public boolean isUsingShield(Integer value) {
        return effectsUsed(value).contains(16);
    }
  }
  
  
  
// for (GameObject food : superFoodList) {
                    //     for (GameObject opponent : opponents) {
                    //         double distToFood = getDistanceBetween(bot, food);
                    //         double distToOpp = getDistanceBetween(bot, opponent);
    
                    //         if (distToFood < distToOpp) {
                    //             playerAction.action = PlayerActions.FORWARD;
                    //             playerAction.heading = getHeadingBetween(food);
                    //             break;
                    //         } 
                    //     }
                    // }


// public void gameInfo() {
//     List<GameObject> gameObjects = gameState.getGameObjects();
//     List<GameObject> players = gameState.getPlayerGameObjects();
//     List<GameObject> opponents = players;
//     opponents.removeIf(x -> x.getId() == bot.getId());
//     opponents.stream().sorted(Comparator.comparing(item -> getDistanceBetween(bot,item)));

//     Map<String, List<List<Object>>> GameInfo = new HashMap<>();
    
//     for (ObjectTypes type : ObjectTypes.values()) {
//         List<GameObject> gameObject = gameObjects.stream()
//                     .filter(item -> item.getGameObjectType() == type)
//                     .sorted(Comparator
//                             .comparing(item -> getDistanceBetween(bot, item)))
//                     .collect(Collectors.toList());

//         List<List<Object>> gameObjectDetails = new ArrayList<>();

//         for(GameObject object : gameObject) {
//             List<Object> objectDatas = new ArrayList<>();
//             objectDatas.add(object);
//             objectDatas.add(getDistanceBetween(bot, object));
//             objectDatas.add(getHeadingBetween(object));

//             gameObjectDetails.add(objectDatas);
//         }

//         GameInfo.put(type.toString(), gameObjectDetails);
//     }



// }