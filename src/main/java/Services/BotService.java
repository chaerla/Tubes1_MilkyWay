package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
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
        World world = gameState.getWorld();

        System.out.println("\n===========================");
        System.out.println("Tick            : " + gameState.world.currentTick);
        System.out.println("World Radius    : " + world.getRadius());
        System.out.println("Bot size        : " + bot.getSize());
        System.out.println("Bot heading     : " + bot.currentHeading);
        System.out.println("Bot X           : " + bot.getPosition().getX());
        System.out.println("Bot Y           : " + bot.getPosition().getY());
        System.out.println("Bot effects     : " + bot.effects);
        System.out.println();

        // filter opponents from players
        if (!players.isEmpty()) {
            opponents = players;
            opponents.removeIf(x -> x.getId().equals(bot.getId()));

            if (!gameState.getGameObjects().isEmpty()) {
                // sort opponent by distance with bot
                opponents.stream().sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)));

                // System.out.println(getDistanceBetween(bot, opponents.get(0)));

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
                // List<DegreeRange> headRestric = new ArrayList<>();
                DegreeRestriction headingRestriction = new DegreeRestriction();

                // check if near the wall
                // int botX = bot.getPosition().getX();
                // int botY = bot.getPosition().getY();
                // int worldRadius = world.getRadius();

                // if ((Math.pow(botX, 2) + Math.pow(botY, 2)) * 0.9 >= Math.pow((worldRadius),
                // 2)) {
                // int headingToCenter = getHeadingBetween(world.getCenterPoint());
                // // DegreeRange toAdd = new DegreeRange(headingToCenter + 95, headingToCenter
                // -
                // // 95);
                // // headRestric.add(toAdd)
                // this.playerAction.action = PlayerActions.FORWARD;
                // this.playerAction.heading = headingToCenter;
                // return;
                // }
                // System.out.println("PASSED WALL CHECK");

                // check if is in asteroid field
                if (asteroidList.size() > 0) {
                    if (checkEffect(Effects.InAsteroidField)
                            || getDistanceBetween(asteroidList.get(0), bot) - bot.getSize()
                                    - asteroidList.get(0).getSize() <= 20) {
                        int headingToAsteroid = getHeadingBetween(asteroidList.get(0));
                        // double deltaHeading = toDegrees(asteroidList.get(0).getSize()
                        // / getDistanceBetween(asteroidList.get(0), bot));
                        // int intDeltaHeading = (int) deltaHeading;
                        // DegreeRange toAdd = new DegreeRange(headingToAsteroid + intDeltaHeading,
                        // headingToAsteroid - intDeltaHeading);
                        // headRestric.add(toAdd);
                        headingRestriction.restrictRange(headingToAsteroid, getDeltaHeading(asteroidList.get(0)));
                        System.out.println("DETECTED    : NEAR THE ASTEROID with distance "
                                + getDistanceBetween(bot, asteroidList.get(0)));
                        System.out.println("    Asteroid Size   : " + asteroidList.get(0).getSize());
                    }
                }

                // check if is in wormhole field
                if (wormholeList.size() > 0) {
                    if (getDistanceBetween(wormholeList.get(0), bot) <= 20) {
                        int headingToWormhole = getHeadingBetween(wormholeList.get(0));
                        // double deltaHeading = wormholeList.get(0).getSize()
                        // / getDistanceBetween(wormholeList.get(0), bot);
                        // int intDeltaHeading = (int) deltaHeading;
                        // DegreeRange toAdd = new DegreeRange(headingToWormhole + intDeltaHeading,
                        // headingToWormhole - intDeltaHeading);
                        // headRestric.add(toAdd);
                        headingRestriction.restrictRange(headingToWormhole, getDeltaHeading(wormholeList.get(0)));
                        System.out.println("DETECTED    : NEAR THE WORMHOLE with distance "
                                + getDistanceBetween(bot, wormholeList.get(0)));
                        System.out.println("    Wormhole Size   : " + wormholeList.get(0).getSize());
                    }
                    // System.out.println("PASSED WORMHOLE CHECK");
                }

                // check if near gas cloud
                if (gasCloudList.size() > 0) {
                    if (checkEffect(Effects.InGasCloud)
                            || getDistanceBetween(gasCloudList.get(0), bot) - bot.getSize()
                                    - gasCloudList.get(0).getSize() <= 20) {
                        int headingToGasCloud = getHeadingBetween(gasCloudList.get(0));
                        // double deltaHeading = gasCloudList.get(0).getSize()
                        // / getDistanceBetween(gasCloudList.get(0), bot);
                        // int intDeltaHeading = (int) deltaHeading;
                        // DegreeRange toAdd = new DegreeRange(headingToGasCloud + intDeltaHeading,
                        // headingToGasCloud - intDeltaHeading);
                        // headRestric.add(toAdd);
                        headingRestriction.restrictRange(headingToGasCloud, getDeltaHeading(gasCloudList.get(0)));

                        System.out.println("DETECTED    : NEAR THE GAS CLOUD with distance "
                                + getDistanceBetween(bot, gasCloudList.get(0)));
                        System.out.println("    GasCloud Size   : " + gasCloudList.get(0).getSize());
                    }
                }

                double distanceToOpp = getDistanceBetween(bot, opponents.get(0)) - bot.getSize()
                        - opponents.get(0).getSize();
                int headingToOpp = getHeadingBetween(opponents.get(0));
                boolean degreeValid = headingRestriction.isDegValid(headingToOpp); // degreeValid(headingToOpp,
                                                                                   // headRestric);

                boolean strategied = false;
                boolean chase = false;

                // use teleporter
                if (!strategied && bot.hasTeleporter() && opponents.get(0).getSize() < bot.getSize() - 25) {
                    playerAction.action = PlayerActions.FIRETELEPORT;
                    playerAction.heading = headingToOpp;
                    System.out.println("FIREEEE TELEPORTERRRRRRRRRRRRR");
                    strategied = true;
                }

                // FIRST PRIORITY : if could use torpedoes, FIRE TORPEDOES
                if (!strategied && bot.hasTorpedo()
                        && bot.getSize() > 50
                        && (((foodList.size() + bot.getSize() < opponents.get(0).getSize())
                                && distanceToOpp < world.getRadius() * 0.6)
                                || (opponents.get(0).getSize() < bot.getSize() - bot.torpedoSalvoCount * 10
                                        && (distanceToOpp < 75)))) {
                    System.out.println("FIRING TORPEDOES");
                    playerAction.action = PlayerActions.FIRETORPEDOES;
                    playerAction.heading = headingToOpp;
                    // playerAction.action = PlayerActions.FORWARD;
                    strategied = true;
                    if (checkEffect(Effects.IsAfterburner)
                            && (bot.getSize() - (distanceToOpp / (checkEffect(Effects.IsAfterburner) ? bot.getSpeed()
                                    : 2 * bot.getSpeed())) > opponents.get(0).getSize() * 1.25)
                            && distanceToOpp < world.getRadius() * 0.8) {
                        chase = true;
                    }
                }
                // System.out.println("PASSED USE TORPEDO CHECK");

                // SECOND PRIORITY : if could chase, CHASE!!
                if (!strategied && degreeValid
                        && (bot.getSize() - (distanceToOpp / (checkEffect(Effects.IsAfterburner) ? bot.getSpeed()
                                : 2 * bot.getSpeed())) > opponents.get(0).getSize() * 1.25)
                        && distanceToOpp < world.getRadius() * 0.8) {
                    System.out.println("USING AFTERBURNER");
                    if (!checkEffect(Effects.IsAfterburner)) {
                        playerAction.action = PlayerActions.STARTAFTERBURNER;
                        playerAction.heading = headingToOpp;
                    } else {
                        playerAction.action = PlayerActions.FORWARD;
                        playerAction.heading = headingToOpp;
                    }
                    // playerAction.heading = headingToOpp;
                    // playerAction.action = PlayerActions.FORWARD;
                    strategied = true;
                    chase = true;
                }
                // System.out.println("PASSED USE AFTERBURNER CHECK");

                // ELSE,
                if (!strategied) {

                    // System.out.println("PASSED STOP AFTERBURNER CHECK");

                    // restrict heading to opponents < 150
                    for (GameObject opponent : opponents) {
                        if (getDistanceBetween(bot, opponent) < 100 && bot.getSize() < opponent.getSize()) {
                            int headingToThisOpp = getHeadingBetween(opponent);
                            // int deltaHeading = (int) toDegrees(opponent.getSize() /
                            // getDistanceBetween(opponent, bot));
                            // DegreeRange toAdd = new DegreeRange(headingToThisOpp + intDeltaHeading,
                            // headingToThisOpp - intDeltaHeading);
                            // headingRestriction.restrictRange(headingToThisOpp, deltaHeading);
                            // System.out.println("HEADINGGGGG : " + headingToOpp);
                            // System.out.println("STARTTT: " + toAdd.getStartDegree() + " END : " +
                            // "STARTTT: "
                            // + toAdd.getEndDegree());
                            // headRestric.add(toAdd);
                            headingRestriction.restrictRange(headingToThisOpp, getDeltaHeading(opponent));
                            System.out.println(
                                    "DETECTED    : OPPONENT with distance " + getDistanceBetween(bot, opponent));
                            // headingRestriction.restrictRange(headingToOpp, deltaHeading);
                        }
                    }
                    // System.out.println("PASSED OPPONENT HEADING CHECK");

                    int heading = -1;

                    // check possible food
                    int indexFood = -1;
                    for (int i = 0; i < foodList.size(); i++) {
                        if (headingRestriction.isDegValid(getHeadingBetween(foodList.get(i)))) {
                            indexFood = i;
                            break;
                        }
                    }

                    // check possible superfood
                    int indexSuperFood = -1;
                    if (!checkEffect(Effects.HasSuperfood)) {
                        for (int i = 0; i < superFoodList.size(); i++) {
                            if (headingRestriction.isDegValid(getHeadingBetween(superFoodList.get(i)))) {
                                indexSuperFood = i;
                                break;
                            }
                        }
                    }

                    // check whether goes to food or superfood
                    if (indexFood != -1 && indexSuperFood != -1) {
                        if (getDistanceBetween(bot, foodList.get(indexFood)) * 1.5 >= getDistanceBetween(bot,
                                superFoodList.get(indexSuperFood))) {
                            // go to superfood
                            heading = getHeadingBetween(superFoodList.get(indexSuperFood));
                            System.out.println("ACTION      : GO TO SUPERFOOD");
                        } else {
                            // go to food
                            heading = getHeadingBetween(foodList.get(indexFood));
                            System.out.println("ACTION      : GO TO FOOD");
                        }
                    } else if (indexFood != -1) {
                        heading = getHeadingBetween(foodList.get(indexFood));
                        System.out.println("ACTION      : GO TO FOOD");
                    } else if (indexSuperFood != -1) {
                        heading = getHeadingBetween(superFoodList.get(indexSuperFood));
                        System.out.println("ACTION      : GO TO SUPERFOOD");
                    } else {
                        // generate random heading
                        boolean thisHeadingValid = false;
                        while (!thisHeadingValid) {
                            System.out.println("randomizing shit");
                            Random rand = new Random();
                            int randomHeading = rand.nextInt(359);
                            if (headingRestriction.isDegValid(randomHeading)) {
                                heading = randomHeading;
                                thisHeadingValid = true;
                            }
                        }
                    }
                    // System.out.println("PASSED FOODS CHECK");

                    // shutdown afterburner
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = heading;
                    // if (checkEffect(Effects.IsAfterburner)) {
                    // playerAction.action = PlayerActions.STOPAFTERBURNER;
                    // System.out.println("ACTION : STOPPING AFTER BURNER");
                    // } else {
                    // playerAction.action = PlayerActions.FORWARD;
                    // }

                }

                // use shield as defense
                if (torpedoList.size() > 0) {
                    // int headingToTorpedo = getHeadingBetween(torpedoList.get(0));
                    // System.out.println("Heading to torpedo : " + headingToTorpedo);
                    for (GameObject torpedo : torpedoList) {
                        int torpedoHeading = torpedo.currentHeading;
                        if (bot.hasShield() && !checkEffect(Effects.HasShield)
                                && (getDistanceBetween(bot, torpedo) < bot.getSize() + 50)
                                && heading_gap(getHeadingBetween(opponents.get(0)), torpedoHeading) > 5
                                && torpedo.getSize() >= 2
                                && bot.getSize() > 50
                                && bot.getSize() < 350) {
                            playerAction.action = PlayerActions.ACTIVATESHIELD;
                            break;
                        }
                    }
                }
                // System.out.println("PASSED SHIELD CHECK");

                // if (1.2*opponents.get(0).getSize() < bot.getSize() &&
                // getDistanceBetween(opponents.get(0), bot) < 100) {
                // playerAction.action = PlayerActions.FORWARD;
                // playerAction.heading = getHeadingBetween(opponents.get(0));
                // playerAction.action = PlayerActions.STARTAFTERBURNER;
                // playerAction.action = PlayerActions.FIRETORPEDOES;
                // } else {
                // playerAction.action = PlayerActions.FORWARD;
                // playerAction.heading = getHeadingBetween(superFoodList.get(0));

                // }

                // counter tembok
                // int botX = bot.getPosition().getX();
                // int botY = bot.getPosition().getY();
                // int worldRadius = gameState.world.getRadius();

                // if (Math.pow(botX,2)+Math.pow(botY,2) >=
                // Math.pow(worldRadius-bot.getSize(),2)){
                // playerAction.heading = (toDegrees(Math.atan2(-1 * botY, -1 * botX))+360)%360;
                // playerAction.action = PlayerActions.FORWARD;
                // System.out.println("=====Countered wall, headed to : " + bot.currentHeading);
                // }

                // counter the wall
                if (!((Math.pow(bot.getPosition().x, 2) + Math.pow(bot.getPosition().y,
                        2)) < Math.pow(0.9 * (gameState.getWorld().getRadius() - bot.getSize()), 2))
                        && playerAction.action != PlayerActions.FIRETORPEDOES) {
                    System.out.println("DETECT  : WALL");
                    playerAction.action = PlayerActions.FORWARD;
                    playerAction.heading = getHeadingBetween(world.getCenterPoint());
                }

                // stop afterburner

                if (checkEffect(Effects.IsAfterburner) && !chase) {
                    playerAction.action = PlayerActions.STOPAFTERBURNER;
                    System.out.println("ACTION  : STOPPING AFTER BURNER");
                }

                // info

                // System.out.println("Dist to opp : " + getDistanceBetween(opponents.get(0),
                // bot));
                // System.out.println("Opponents size : " + opponents.get(0).getSize());
                // System.out.println("Heading to opponent : " +
                // getHeadingBetween(opponents.get(0)));
                // System.out.println("Restriction : " + headRestric.toString());
                // for (DegreeRange range : headRestric) {
                // System.out.println(range.getStartDegree() + " - " + range.getEndDegree());
                // }
                // for (int i = 0; i < 360; i++) {
                // if (!headingRestriction.isDegValid(i)) {
                // System.out.print(i + " ");
                // }
                // }
                // headingRestriction.printValidDegrees(headingRestriction);
                printRestrictedDegrees(headingRestriction);
                System.out.println("========================\n");

            }

        }

        this.playerAction = playerAction;
    }

    public void printRestrictedDegrees(DegreeRestriction restriction) {
        int start = -1;
        for (int i = 0; i < 360; i++) {
            if (!restriction.isDegValid(i)) {
                if (start == -1) {
                    start = i;
                }
            } else {
                if (start != -1) {
                    System.out.printf("%d to %d degrees\n", start, i - 1);
                    start = -1;
                }
            }
        }
        if (start != -1) {
            System.out.printf("%d to %d degrees\n", start, 359);
        }
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

    private int getDeltaHeading(GameObject obstacle) {
        double cosTheta = (2 * Math.pow(getDistanceBetween(bot, obstacle), 2)
                - Math.pow(bot.getSize() + obstacle.getSize(), 2))
                / (2
                        * Math.pow(getDistanceBetween(bot, obstacle), 2));
        return (int) Math.round(toDegrees(Math.acos(cosTheta)));
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

    private int getHeadingBetween(Position pos) {
        var direction = toDegrees(Math.atan2(pos.y - bot.getPosition().y,
                pos.x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int getHeadingBetween(GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    private int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }

    private boolean checkEffect(Effects effect) {
        return ((bot.effects & effect.getValue()) == effect.getValue());
    }
}
