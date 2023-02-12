package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

import com.fasterxml.jackson.databind.ser.impl.IndexedListSerializer;

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

                // List of heading range restriction
                List<DegreeRange> headRestric = new ArrayList<>();

                // check if near the wall
                int botX = bot.getPosition().getX();
                int botY = bot.getPosition().getY();
                int worldRadius = gameState.world.getRadius();
                
                if (Math.pow(botX,2)+Math.pow(botY,2) >= Math.pow(0.9*(worldRadius-bot.getSize()),2)){
                    int headingToCenter = (toDegrees(Math.atan2(-1 * botY, -1 * botX))+360)%360;
                    DegreeRange toAdd = new DegreeRange(headingToCenter+95,headingToCenter-95);
                    headRestric.add(toAdd);
                    System.out.println("NEAR THE WALL");
                }
                System.out.println("PASSED WALL CHECK");

                // check if is in asteroid field
                if (asteroidList.size() > 0){
                    if (isInAsteroidField(bot.effects) || getDistanceBetween(asteroidList.get(0), bot) <= bot.getSize()*1.1) {
                        int headingToAsteroid = getHeadingBetween(asteroidList.get(0));
                        double deltaHeading = asteroidList.get(0).getSize()/getDistanceBetween(asteroidList.get(0), bot);
                        int intDeltaHeading = (int) deltaHeading;
                        DegreeRange toAdd = new DegreeRange(headingToAsteroid+intDeltaHeading,headingToAsteroid-intDeltaHeading);
                        headRestric.add(toAdd);
                        System.out.println("NEAR THE ASTEROID");
                    }
                    System.out.println("PASSED ASTEROID CHECK");
                }

                // check if is in wormhole field
                if (wormholeList.size() > 0){
                    if (getDistanceBetween(wormholeList.get(0), bot) <= bot.getSize()*1.1) {
                        int headingToWormhole = getHeadingBetween(wormholeList.get(0));
                        double deltaHeading = wormholeList.get(0).getSize()/getDistanceBetween(wormholeList.get(0), bot);
                        int intDeltaHeading = (int) deltaHeading;
                        DegreeRange toAdd = new DegreeRange(headingToWormhole+intDeltaHeading,headingToWormhole-intDeltaHeading);
                        headRestric.add(toAdd);
                        System.out.println("NEAR THE WORMHOLE");
                    }
                    System.out.println("PASSED WORMHOLE CHECK");
                }

                // check if near gas cloud
                if (gasCloudList.size() > 0){
                    if (isInGasCloud(bot.effects) || getDistanceBetween(gasCloudList.get(0), bot) <= bot.getSize()*1.1) {
                        int headingToGasCloud = getHeadingBetween(gasCloudList.get(0));
                        double deltaHeading = gasCloudList.get(0).getSize()/getDistanceBetween(gasCloudList.get(0), bot);
                        int intDeltaHeading = (int) deltaHeading;
                        DegreeRange toAdd = new DegreeRange(headingToGasCloud+intDeltaHeading,headingToGasCloud-intDeltaHeading);
                        headRestric.add(toAdd);
                        System.out.println("NEAR THE GAS CLOUD");
                    }
                    System.out.println("PASSED GAS CLOUD CHECK");
                }

                double distanceToOpp = getDistanceBetween(bot, opponents.get(0)) - bot.getSize() - opponents.get(0).getSize();
                int headingToOpp = getHeadingBetween(opponents.get(0));
                boolean degreeValid = degreeValid(headingToOpp, headRestric);

                boolean strategied = false;

                // if could use torpedoes
                if (!strategied && degreeValid && bot.hasTorpedo() && (opponents.get(0).getSize() < bot.getSize() + bot.torpedoSalvoCount*10) && (distanceToOpp < 50)) {
                    System.out.println("USEEE TORPEDOESSSS");
                    playerAction.heading = headingToOpp;
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    // playerAction.action = PlayerActions.FORWARD;
                    strategied = true;
                } 
                System.out.println("PASSED USE TORPEDO CHECK");

                // if could use after burner
                if (!strategied && degreeValid && (bot.getSize() - (distanceToOpp/bot.getSpeed()) > opponents.get(0).getSize())){
                    System.out.println("USE AFTERRRRBURNERRRRRR");
                    playerAction.heading = headingToOpp;
                    if (!isAfterburner(bot.effects)) {
                        playerAction.action = PlayerActions.STARTAFTERBURNER;
                    } else {
                        playerAction.action = PlayerActions.FORWARD;
                    }
                    // playerAction.action = PlayerActions.FORWARD;
                    strategied = true;   
                }
                System.out.println("PASSED USE AFTERBURNER CHECK");

                // finding food
                if (!strategied){
                    
                    System.out.println("PASSED STOP AFTERBURNER CHECK");

                    // restrict heading to opponents < 150
                    for (GameObject opponent : opponents) {
                        if (getDistanceBetween(bot, opponent) - bot.getSize() - opponent.getSize() < 100) {
                            int headingToThisOpp = getHeadingBetween(opponent);
                            double deltaHeading = opponent.getSize()/getDistanceBetween(opponent, bot);
                            int intDeltaHeading = (int) deltaHeading;
                            DegreeRange toAdd = new DegreeRange(headingToThisOpp+intDeltaHeading,headingToThisOpp-intDeltaHeading);
                            System.out.println("HEADINGGGGG : "+headingToOpp);
                            System.out.println("STARTTT: " + toAdd.getStartDegree() + " END : " + "STARTTT: " + toAdd.getEndDegree());
                            headRestric.add(toAdd);
                        }
                    }
                    System.out.println("PASSED OPPONENT HEADING CHECK");

                    int heading = -1;
                    
                    // check possible food
                    int indexFood = -1;
                    for (int i=0; i < foodList.size(); i++) {
                        if (degreeValid(getHeadingBetween(foodList.get(i)), headRestric)){
                            indexFood = i;
                            break;
                        }
                    }

                    // check possible superfood 
                    int indexSuperFood = -1;
                    if (!hasSuperfood(bot.effects)){
                        for (int i=0; i < superFoodList.size(); i++) {
                            if (degreeValid(getHeadingBetween(superFoodList.get(i)), headRestric)){
                                indexSuperFood = i;
                                break;
                            }
                        }
                    }
                                        
                    // check whether goes to food or superfood
                    if (indexFood != -1 && indexSuperFood != -1) {
                        if (getDistanceBetween(bot, foodList.get(indexFood)) * 1.5 >= getDistanceBetween(bot, superFoodList.get(indexSuperFood))){
                            // go to superfood
                            heading = getHeadingBetween(superFoodList.get(indexSuperFood));
                        } else {
                            // go to food
                            heading = getHeadingBetween(foodList.get(indexFood));
                        }
                    } else if (indexFood != -1){
                        heading = getHeadingBetween(foodList.get(indexFood));
                    } else if (indexSuperFood != -1){
                        heading = getHeadingBetween(superFoodList.get(indexSuperFood));
                    } else {
                        // generate random heading
                        boolean thisHeadingValid = false;
                        while(!thisHeadingValid){
                            Random rand = new Random();
                            int randomHeading = rand.nextInt(359);
                            if (degreeValid(randomHeading, headRestric)){
                                heading = randomHeading;
                                thisHeadingValid = true;
                            }
                        }
                    }
                    System.out.println("PASSED FOODS CHECK");

                    // shutdown afterburner
                  
                        
                    playerAction.heading = heading;
                    if (isAfterburner(bot.effects)){
                        playerAction.action = PlayerActions.STOPAFTERBURNER;
                        System.out.println("STOPPPPPPPPPPPPPPPPPPPPPPPP");
                    } else {
                        playerAction.action = PlayerActions.FORWARD;
                    }

                }

                // use shield as defense
                if (torpedoList.size() > 0){
                    int torpedoHeading = torpedoList.get(0).currentHeading;
                    // int headingToTorpedo = getHeadingBetween(torpedoList.get(0));
                    System.out.println("TOPERDO HEADINGGG : " + torpedoHeading);
                    // System.out.println("Heading to torpedo : " + headingToTorpedo);
                    if (bot.hasShield() && !isUsingShield(bot.effects) && (getDistanceBetween(bot, torpedoList.get(0)) < bot.getSize() + 50) && heading_gap(headingToOpp, torpedoHeading) > 5) {
                        playerAction.action = PlayerActions.ACTIVATESHIELD;
                    }
                }
                System.out.println("PASSED SHIELD CHECK");

                // if (1.2*opponents.get(0).getSize() < bot.getSize() && getDistanceBetween(opponents.get(0), bot) < 100) {
                //     playerAction.action = PlayerActions.FORWARD;
                //     playerAction.heading = getHeadingBetween(opponents.get(0));
                //     playerAction.action = PlayerActions.STARTAFTERBURNER;
                //     playerAction.action = PlayerActions.FIRETORPEDOES;
                // } else {
                //     playerAction.action = PlayerActions.FORWARD;
                //     playerAction.heading = getHeadingBetween(superFoodList.get(0));
                    
                // }

                // counter tembok
                // int botX = bot.getPosition().getX();
                // int botY = bot.getPosition().getY();
                // int worldRadius = gameState.world.getRadius();
                
                // if (Math.pow(botX,2)+Math.pow(botY,2) >= Math.pow(worldRadius-bot.getSize(),2)){
                //     playerAction.heading = (toDegrees(Math.atan2(-1 * botY, -1 * botX))+360)%360;
                //     playerAction.action = PlayerActions.FORWARD;
                //     System.out.println("=====Countered wall, headed to :  " + bot.currentHeading);
                // }

                // info 
                System.out.println();
                System.out.println("Tick : " + gameState.world.currentTick);
                System.out.println("World Radius : " + worldRadius);
                System.out.println("Bot size : " + bot.getSize());
                System.out.println("Bot heading : " + bot.currentHeading);
                System.out.println("Bot X : " + botX);
                System.out.println("Bot Y : " + botY);
                System.out.println("Bot effects : " + bot.effects);
                System.out.println("Dist to opp : " + getDistanceBetween(opponents.get(0), bot));
                System.out.println("Opponents size : " + opponents.get(0).getSize());
                System.out.println("Heading to opponent : " + getHeadingBetween(opponents.get(0)));
                System.out.println("Restriction : " + headRestric.toString());
                for(DegreeRange range : headRestric) {
                    System.out.println(range.getStartDegree() + " - " + range.getEndDegree());
                }

            }

        }

        this.playerAction = playerAction;
    }

    public boolean degreeValid(int heading, List<DegreeRange> ranges) {
        for (DegreeRange range : ranges) {
            if (range.isInRange(heading)) {
                return false;
            }
        }
        return true;
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