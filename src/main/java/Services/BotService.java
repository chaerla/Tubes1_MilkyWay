package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    private List<Integer> activeTeleporterHeadings;
    private Integer tickTorpedoShot;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
        this.activeTeleporterHeadings = new ArrayList<Integer>();
        this.tickTorpedoShot = 0;
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

        System.out.println("\n===========================");
        System.out.println("Tick            : " + gameState.world.currentTick);
        System.out.println("World Radius    : " + world.getRadius());
        System.out.println("Bot size        : " + bot.getSize());
        System.out.println("Bot heading     : " + bot.currentHeading);
        System.out.println("Bot X           : " + bot.getPosition().getX());
        System.out.println("Bot Y           : " + bot.getPosition().getY());
        System.out.println("Bot effects     : " + bot.effects);
        System.out.println();

        if (!players.isEmpty()) {
            // filter opponents from players
            opponentsByDist = players;
            opponentsByDist.removeIf(x -> x.getId().equals(bot.getId()));
            
            // sort opponent by distance and by size with bot
            opponentsByDist.stream().sorted(Comparator.comparing(item -> getDistanceBetween(bot, item)));
            opponentsBySize = opponentsByDist.stream().sorted(Comparator.comparing(item -> getDistanceBetween(bot, item))).toList();

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

            // check nearest teleporter aligned with bot's teleporterheading
            List<GameObject> teleporterList = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.TELEPORTER)
                    .sorted(Comparator
                            .comparing(item -> getDistanceBetween(bot, item)))
                    .collect(Collectors.toList());
            // food by heading gap
            List<GameObject> foodListByHeadingGap = gameObjects.stream()
                    .filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                    .sorted(Comparator
                            .comparing(item -> heading_gap(bot.currentHeading, item.currentHeading)))
                    .collect(Collectors.toList());


            // update activeTeleporterHeading
            activeTeleporterHeadings.clear();
            for (int i = 0; i < teleporterList.size(); i++) {       
                activeTeleporterHeadings.add(teleporterList.get(i).currentHeading);
            }
            
            // List of heading range restriction
            DegreeRestriction headingRestriction = new DegreeRestriction();

            // check if is in asteroid field
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

            // check if is in wormhole field
            if (wormholeList.size() > 0) {
                if (getDistanceBetween(wormholeList.get(0), bot) <= 20) {
                    int headingToWormhole = getHeadingBetween(wormholeList.get(0));
                    headingRestriction.restrictRange(headingToWormhole, getDeltaHeading(wormholeList.get(0)));

                    System.out.println("DETECTED    : NEAR THE WORMHOLE with distance "
                            + getDistanceBetween(bot, wormholeList.get(0)));
                    System.out.println("    Wormhole Size   : " + wormholeList.get(0).getSize());
                }
            }

            // check if near gas cloud
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

            // check distance and heading of bot to nearest opponent
            double distToNearestOpp = getDistanceBetween(bot, opponentsByDist.get(0)) - bot.getSize()
                    - opponentsByDist.get(0).getSize();
            int headToNearestOpp = getHeadingBetween(opponentsByDist.get(0));

            // initialize boolean for strategy
            boolean strategied = false;
            boolean chase = false;

            // FIRST PRIORITY (A) : fire teleporter (teleporter not deployed)
            if (!strategied && bot.hasTeleporter()) {
                int oppIndex = -1;
                for (int i = 0; i < opponentsBySize.size(); i++) {
                    if (opponentsBySize.get(i).getSize() < bot.getSize() - 30 && bot.getSize() > 60 && world.getCurrentTick() - tickTorpedoShot > 50) {
                        oppIndex = i;
                        break;
                    } else if (opponentsBySize.get(i).getSize() > bot.getSize()) {
                        break;
                    }
                }
                if (oppIndex != -1) {
                    playerAction.heading = getHeadingBetween(opponentsBySize.get(0));
                    playerAction.action = PlayerActions.FIRETELEPORT;
                    strategied = true;
                }
            }

            // FIRST PRIORITY (B) : use teleporter (teleporter deployed)
            if (!strategied) {
                Boolean foundValidTarget = false;
                for (GameObject tele : teleporterList) {
                    for (GameObject opponent : opponentsBySize) {
                        if (getDistanceBetween(opponent, tele) < (bot.getSize() + opponent.getSize()) * 1.1 && bot.getSize() > opponent.getSize()) {
                            playerAction.heading = getHeadingBetween(opponent);
                            playerAction.action = PlayerActions.TELEPORT;
                            System.out.println("TELEPORT~~~");
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
                                    && distToNearestOpp < world.getRadius() * 1.2 )|| (distToNearestOpp < 75))) {
                System.out.println("FIRING TORPEDOES");
                playerAction.heading = headToNearestOpp;
                playerAction.action = PlayerActions.FIRETORPEDOES;
                strategied = true;
                if (checkEffect(Effects.IsAfterburner)
                        && (bot.getSize() - (distToNearestOpp/bot.getSpeed()) > opponentsByDist.get(0).getSize()) && distToNearestOpp < world.getRadius() * 0.8) {
                    if(bot.torpedoSalvoCount <= 3){
                        playerAction.heading = headToNearestOpp;
                        playerAction.action = PlayerActions.FORWARD;
                    }
                    chase = true;
                }
            }
            
            // THIRD PRIORITY : if could chase, CHASE!!
            if (!strategied && (bot.getSize() - (distToNearestOpp/bot.getSpeed()) > opponentsByDist.get(0).getSize()*1.2) && distToNearestOpp < world.getRadius() * 0.8) {
                System.out.println("USING AFTERBURNER");
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

                // check food with closest heading gap
                for (int i = 0; i < foodListByHeadingGap.size(); i++) {
                    GameObject currFood = foodListByHeadingGap.get(i);
                    if (headingRestriction.isDegValid(getHeadingBetween(currFood)) && Math.abs(getDistanceBetween(bot, foodList.get(indexFood)) - getDistanceBetween(currFood, bot)) <= 2) {
                        indexFood = foodList.indexOf(foodList.stream().filter(item -> item.currentHeading == currFood.currentHeading).findFirst().orElse(null));
                        break;
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
                    heading = headingRestriction.getNearestValidHeading(bot.currentHeading, 1);
                }

                playerAction.heading = heading;
                playerAction.action = PlayerActions.FORWARD;
            }

            // use shield as defense
            if (torpedoList.size() > 0) {
                int torpedoHeading = torpedoList.get(0).currentHeading;
                if (bot.hasShield() && !checkEffect(Effects.HasShield)
                        && (getDistanceBetween(bot, torpedoList.get(0)) < bot.getSize() + 50)
                        && heading_gap(getHeadingBetween(opponentsByDist.get(0)), torpedoHeading) > 5
                        && torpedoList.get(0).getSize() >= 2
                        && bot.getSize() > 50
                        && bot.getSize() < 350
                        ) {
                    playerAction.action = PlayerActions.ACTIVATESHIELD;
                    System.out.println("ACTIVAAATEEE SHEIEEKDLD");
                }
            }

            // counter the wall
            if (!((Math.pow(bot.getPosition().x, 2) + Math.pow(bot.getPosition().y,
                    2)) < Math.pow(0.9 * (gameState.getWorld().getRadius() - bot.getSize()), 2))
                    && playerAction.action != PlayerActions.FIRETORPEDOES) {
                System.out.println("DETECT  : WALL");
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = getHeadingBetween(world.getCenterPoint());
            }

            // stop afterburner
            if (checkEffect(Effects.IsAfterburner) && (!chase || checkEffect(Effects.HasShield) || (getDistanceBetween(opponentsByDist.get(0), bot) < bot.getSize() * 2 && opponentsByDist.get(0).getSize() > bot.getSize()))) {
                playerAction.action = PlayerActions.STOPAFTERBURNER;
                System.out.println("ACTION  : STOPPING AFTER BURNER");
            }

            // FINAL CHECK (TELEPORTER HEADING)
            if (playerAction.action == PlayerActions.FIRETELEPORT) {
                tickTorpedoShot = world.getCurrentTick();
                saveTeleporterHeading(playerAction.heading);
            }
            if (playerAction.action == PlayerActions.TELEPORT) {
                removeTeleporterHeading(playerAction.heading);
            }
            System.out.println("========================\n");
            
        }
        this.playerAction = playerAction;
    }

    private void saveTeleporterHeading(Integer teleportHeading) {
        activeTeleporterHeadings.add(teleportHeading);
    }

    private void removeTeleporterHeading(Integer teleportHeading) {
        int teleIndex = -1;
        for (int i = 0; i < activeTeleporterHeadings.size(); i++) {
            if (activeTeleporterHeadings.get(i) == teleportHeading) {
                teleIndex = i;
                break;
            }
        }
        if (teleIndex != -1) {
            activeTeleporterHeadings.remove(teleIndex);
        }
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
