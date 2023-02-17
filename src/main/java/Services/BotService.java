package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private Integer lastTeleporterTick; // added attribute to check the teleporter we shot

    private final Integer FIRE_TELEPORTER_COST = 20;
    private final Integer ACTIVATE_SHIELD_COST = 20;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
        this.lastTeleporterTick = 0;
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
        List<GameObject> opponentsByDist;
        List<GameObject> opponentsBySize;
        World world = gameState.getWorld();

        // INFO
        System.out.println("\n================MilkyBot================");
        System.out.println("Tick            : " + gameState.world.currentTick);
        System.out.println("World Radius    : " + world.getRadius());
        System.out.println("Bot size        : " + bot.getSize());
        System.out.println("Bot heading     : " + bot.currentHeading);
        System.out.println("Bot X           : " + bot.getPosition().getX());
        System.out.println("Bot Y           : " + bot.getPosition().getY());
        System.out.println("Bot effects     : " + bot.effects);
        System.out.println();

        if (!players.isEmpty()) {
            /* *** MAPPING GAME OBJECTS *** */
            // filter opponents from players
            opponentsByDist = players;
            opponentsByDist.removeIf(x -> x.getId().equals(bot.getId()));

            // map opponents in world (sorted by distance and by size with bot)
            opponentsByDist.stream().sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)));

            opponentsBySize = opponentsByDist.stream()
                    .sorted(Comparator.comparing(item -> getDistanceBetween(bot, item), Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            // map food items (sorted by distance to bot)
            List<GameObject> foodList = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            // map wormholes (sorted by distance to bot)
            List<GameObject> wormholeList = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.WORMHOLE)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            // map gas clouds (sorted by distance to bot)
            List<GameObject> gasCloudList = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.GASCLOUD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            // map asteroid fields (sorted by distance to bot)
            List<GameObject> asteroidList = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.ASTEROIDFIELD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            // map active torpedos (sorted by distance to bot)
            List<GameObject> torpedoList = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.TORPEDOSALVO)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            // map super food items (sorted by distance to bot)
            List<GameObject> superFoodList = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            // map all teleporters (sorted by distance to bot)
            List<GameObject> teleporterList = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());

            // map all food items (sorted by difference of heading to bot's heading)
            List<GameObject> foodListByHeadingGap = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                            .comparing(item -> headingGap(bot.currentHeading, item.currentHeading)))
                    .collect(Collectors.toList());

            GameObject supernovaPickup = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP)
                    .findFirst().orElse(null);

            GameObject superNovaBomb = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                    .findFirst().orElse(null);

            /* ***** INITIALIZE HEADING RESTRICTIONS -- HANDLE OBSTACLES ***** */
            // list of degree restrictions; prevents bot from entering obstacles in next
            // tick
            DegreeRestriction headingRestriction = new DegreeRestriction();

            // check if is in/near asteroid field, RESTRICT HEADINGS TO ASTEROID -- HANDLE
            // ONLY CLOSEST ONE
            if (asteroidList.size() > 0) {
                if (checkEffect(Effects.InAsteroidField)
                        || getDistanceBetween(asteroidList.get(0), bot) - bot.getSize()
                                - asteroidList.get(0).getSize() <= 20) {
                    int headingToAsteroid = getHeadingBetween(asteroidList.get(0));
                    headingRestriction.restrictRange(headingToAsteroid, getDeltaHeading(asteroidList.get(0)));

                    System.out.println("DETECTED    : NEAR THE ASTEROID with distance "
                            + getDistanceBetween(bot, asteroidList.get(0)));
                    System.out.println("    Asteroid Size   : " + asteroidList.get(0).getSize());
                }
            }

            // check if is in/near wormhole, RESTRICT HEADINGS TO WORMHOLE -- HANDLE ONLY
            // CLOSEST ONE
            if (wormholeList.size() > 0) {
                if (getDistanceBetween(wormholeList.get(0), bot) <= 20) {
                    int headingToWormhole = getHeadingBetween(wormholeList.get(0));
                    headingRestriction.restrictRange(headingToWormhole, getDeltaHeading(wormholeList.get(0)));

                    System.out.println("DETECTED    : NEAR THE WORMHOLE with distance "
                            + getDistanceBetween(bot, wormholeList.get(0)));
                    System.out.println("    Wormhole Size   : " + wormholeList.get(0).getSize());
                }
            }

            // check if nis in/near gas cloud, RESTRICT HEADING TO GASCLOUD -- HANDLE ONLY
            // CLOSEST ONE
            if (gasCloudList.size() > 0) {
                if (checkEffect(Effects.InGasCloud)
                        || getDistanceBetween(gasCloudList.get(0), bot) - bot.getSize()
                                - gasCloudList.get(0).getSize() <= 20) {
                    int headingToGasCloud = getHeadingBetween(gasCloudList.get(0));
                    headingRestriction.restrictRange(headingToGasCloud, getDeltaHeading(gasCloudList.get(0)));

                    System.out.println("DETECTED    : NEAR THE GAS CLOUD with distance "
                            + getDistanceBetween(bot, gasCloudList.get(0)));
                    System.out.println("    GasCloud Size   : " + gasCloudList.get(0).getSize());
                }
            }

            /*
             * *********************************************
             *
             * GREEDY BY DAMAGE TO OPPONENT
             * maximize damage on strategy selection
             * 
             * *********************************************
             */
            // FIND NEAREST OPPONENT
            GameObject nearestOpp = opponentsByDist.get(0);
            double distToNearestOpp = getDistanceBetween(bot, nearestOpp);
            int headToNearestOpp = getHeadingBetween(nearestOpp);

            // initialize boolean for strategy selection
            boolean strategied = false;
            boolean chase = false;

            /* *** STRATEGY SELECTION *** */
            // FIRE SUPERNOVA
            if (!strategied && bot.hasSupernova()
                    && getDistanceBetween(bot, opponentsBySize.get(0)) < 1.2 * world.getRadius()) {
                playerAction.heading = getHeadingBetween(opponentsBySize.get(0));
                playerAction.action = PlayerActions.FIRESUPERNOVA;
            }
            // FIRST PRIORITY (A) : fire teleporter (teleporter not deployed)
            if (!strategied && bot.hasTeleporter()) {
                int oppIndex = -1;
                if (supernovaPickup != null) {
                    playerAction.heading = getHeadingBetween(supernovaPickup);
                    playerAction.action = PlayerActions.FIRETELEPORT;
                    System.out.println("ACTION  : DEPLOY TELEPORTER TO SUPERNOVA");
                    strategied = true;
                } else {
                    for (int i = 0; i < opponentsBySize.size(); i++) {
                        if (opponentsBySize.get(i).getSize() < bot.getSize() - (FIRE_TELEPORTER_COST + 10)
                                && bot.getSize() > FIRE_TELEPORTER_COST * 3
                                && world.getCurrentTick() - lastTeleporterTick > 50) {
                            oppIndex = i;
                            break;
                        } else if (opponentsBySize.get(i).getSize() > bot.getSize()) {
                            break;
                        }
                    }
                    if (oppIndex != -1) {
                        playerAction.heading = getHeadingBetween(opponentsBySize.get(0));
                        playerAction.action = PlayerActions.FIRETELEPORT;
                        System.out.println("ACTION  : DEPLOY TELEPORTER??");
                        strategied = true;
                    }
                }
            }

            // FIRST PRIORITY (B) : use teleporter (teleporter deployed)
            if (!strategied) {
                Boolean foundValidTarget = false;
                for (GameObject tele : teleporterList) {
                    if (superNovaBomb != null) {
                        if (getDistanceBetween(superNovaBomb, tele) < bot.getSize() * 1.1) {
                            playerAction.heading = getHeadingBetween(tele);
                            playerAction.action = PlayerActions.TELEPORT;
                            strategied = true;
                            break;
                        }
                    }
                    for (GameObject opponent : opponentsBySize) {
                        if (getDistanceBetween(opponent, tele) < (bot.getSize() + opponent.getSize()) * 1.1
                                && bot.getSize() > opponent.getSize()) {
                            playerAction.heading = getHeadingBetween(opponent);
                            playerAction.action = PlayerActions.TELEPORT;
                            System.out.println("ACTION  : TELEPORT~~~?");
                            strategied = true;
                            foundValidTarget = true;
                            break;
                        }
                    }
                    if (foundValidTarget) {
                        break;
                    }
                }
            }

            // SECOND PRIORITY : if could use torpedoes, FIRE TORPEDOES
            if (!strategied && bot.hasTorpedo()
                    && bot.getSize() > 50
                    && (((foodList.size() + bot.getSize() < opponentsByDist.get(0).getSize())
                            && distToNearestOpp < world.getRadius() * 0.4)
                            || (opponentsByDist.get(0).getSize() < bot.getSize() - bot.torpedoSalvoCount * 10
                                    && distToNearestOpp < world.getRadius() * 1.2)
                            || (distToNearestOpp < 75))) {
                System.out.println("ACTION  : FIRING TORPEDOES?!?");
                playerAction.heading = headToNearestOpp;
                playerAction.action = PlayerActions.FIRETORPEDOES;
                strategied = true;
                if (checkEffect(Effects.IsAfterburner)
                        && (bot.getSize() - (distToNearestOpp / bot.getSpeed()) > opponentsByDist.get(0).getSize())
                        && distToNearestOpp < world.getRadius() * 0.8) {
                    if (bot.torpedoSalvoCount <= 3) {
                        playerAction.heading = headToNearestOpp;
                        playerAction.action = PlayerActions.FORWARD;
                    }
                    chase = true;
                }
            }

            // THIRD PRIORITY : if could chase, CHASE!!
            if (!strategied
                    && (bot.getSize() - (distToNearestOpp / bot.getSpeed()) > opponentsByDist.get(0).getSize() * 1.2)
                    && distToNearestOpp < world.getRadius() * 0.8) {
                System.out.println("ACTION  : USE AFTERBURNER?");
                if (!checkEffect(Effects.IsAfterburner)) {
                    playerAction.heading = headToNearestOpp;
                    playerAction.action = PlayerActions.STARTAFTERBURNER;
                } else {
                    playerAction.heading = headToNearestOpp;
                    playerAction.action = PlayerActions.FORWARD;
                }
                strategied = true;
                chase = true;
            }

            // LAST PRIORITY : FARMING,
            if (!strategied) {
                for (GameObject opponent : opponentsByDist) {
                    if (getDistanceBetween(bot, opponent) < 100 && bot.getSize() < opponent.getSize()) {
                        int headingToThisOpp = getHeadingBetween(opponent);
                        headingRestriction.restrictRange(headingToThisOpp, getDeltaHeading(opponent));
                        System.out.println(
                                "DETECTED    : OPPONENT with distance " + getDistanceBetween(bot, opponent));
                    }
                }

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

                // check food with closest heading gap
                for (int i = 0; i < foodListByHeadingGap.size(); i++) {
                    GameObject currFood = foodListByHeadingGap.get(i);
                    if (headingRestriction.isDegValid(getHeadingBetween(currFood))
                            && Math.abs(getDistanceBetween(bot, foodList.get(indexFood))
                                    - getDistanceBetween(currFood, bot)) <= 2) {
                        indexFood = foodList.indexOf(
                                foodList.stream().filter(item -> item.currentHeading == currFood.currentHeading)
                                        .findFirst().orElse(null));
                        break;
                    }
                }

                // check whether goes to food or superfood
                int heading;
                if (indexFood != -1 && indexSuperFood != -1) {
                    if (getDistanceBetween(bot, foodList.get(indexFood)) * 1.5 >= getDistanceBetween(bot,
                            superFoodList.get(indexSuperFood))) {
                        // go to superfood
                        heading = getHeadingBetween(superFoodList.get(indexSuperFood));
                        System.out.println("ACTION  : GO TO SUPERFOOD?");
                    } else {
                        // go to food
                        heading = getHeadingBetween(foodList.get(indexFood));
                        System.out.println("ACTION  : GO TO FOOD?");
                    }
                } else if (indexFood != -1) {
                    heading = getHeadingBetween(foodList.get(indexFood));
                    System.out.println("ACTION  : GO TO FOOD?");
                } else if (indexSuperFood != -1) {
                    heading = getHeadingBetween(superFoodList.get(indexSuperFood));
                    System.out.println("ACTION  : GO TO SUPERFOOD?");
                } else {
                    heading = headingRestriction.getNearestValidHeading(bot.currentHeading, 1);
                }

                playerAction.heading = heading;
                playerAction.action = PlayerActions.FORWARD;
            }

            /* *** CHECK FOR SUDDEN HAZARDS and FINALIZE COMMAND *** */
            // DEFENCE CHECK (LOW PRIORITY): use shield as defense from torpedo
            if (torpedoList.size() > 0) {
                int torpedoHeading = torpedoList.get(0).currentHeading;
                if (bot.hasShield() && !checkEffect(Effects.HasShield)
                        && (getDistanceBetween(bot, torpedoList.get(0)) < bot.getSize() + 50)
                        && headingGap(getHeadingBetween(opponentsByDist.get(0)), torpedoHeading) > 5
                        && torpedoList.get(0).getSize() >= 2
                        && bot.getSize() > ACTIVATE_SHIELD_COST * 2.5
                        && bot.getSize() < 350) {
                    playerAction.action = PlayerActions.ACTIVATESHIELD;
                    System.out.println("ACTION  : ACTIVAAATEEE SHEIEEKDLD???");
                }
            }

            // BORDER CHECK (MEDIUM PRIORITY): counter the wall
            if (!((Math.pow(bot.getPosition().x, 2) + Math.pow(bot.getPosition().y,
                    2)) < Math.pow(0.9 * (gameState.getWorld().getRadius() - bot.getSize()), 2))
                    && playerAction.action != PlayerActions.FIRETORPEDOES) {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingBetween(world.getCenterPoint());
                System.out.println("ACTION  : EVADE WALL??");
            }

            // FUEL CHECK (HIGH PRIORITY): stop afterburner; prevents self destruct from
            // using afterburner
            if (checkEffect(Effects.IsAfterburner) && (!chase || checkEffect(Effects.HasShield)
                    || (getDistanceBetween(opponentsByDist.get(0), bot) < bot.getSize() * 2
                            && opponentsByDist.get(0).getSize() > bot.getSize()))) {
                playerAction.action = PlayerActions.STOPAFTERBURNER;
                System.out.println("ACTION  : STOPPING AFTER BURNER");
            }

            // TELEPORTER CHECK (N/A): record teleporter information
            if (playerAction.action == PlayerActions.FIRETELEPORT) {
                lastTeleporterTick = world.getCurrentTick();
            }
            System.out.println("FINAL ACTION  : " + playerAction.action.name());
            System.out.println("FINAL HEADING : " + playerAction.heading);
            System.out.println("========================================\n");

        }
        /* *** SEND COMMAND *** */
        this.playerAction = playerAction;
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    public int headingGap(int heading1, int heading2) {
        // Normalize headings to the range [0, 360)
        heading1 = heading1 % 360;
        heading2 = heading2 % 360;

        int gap = Math.abs(heading2 - heading1);
        gap = Math.min(gap, 360 - gap);

        return gap;
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream()
                .filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private int getDeltaHeading(GameObject obstacle) {
        double cosTheta = (2 * Math.pow(getDistanceBetween(bot, obstacle), 2)
                - Math.pow(bot.getSize() + obstacle.getSize(), 2))
                / (2
                        * Math.pow(getDistanceBetween(bot, obstacle), 2));
        return (int) Math.round(toDegrees(Math.acos(cosTheta)));
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
