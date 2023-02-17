# Tubes1_MilkyWay
This repository contains code for a starter bot named MilkyBot for Galaxio 2021 game. The bot is made for Algorithm Strategies task.

## Authors
| Nama                  | NIM      |
| --------------------- | -------- |
| Rachel Gabriela Chen  | 13521044 |
| Jeffrey Chow| 13521046 |
| Eugene Yap Jin Quan          | 13521074 |

## General Information

The game Galaxio is a multiplayer battle royale game where bots compete to be the last bot standing. More about the game and game rules can be read here: [Entelect Challenge](https://github.com/EntelectChallenge/2021-Galaxio).
The bot is coded with Java programming language and relies on Greedy Algorithm to select bot's action on each tick.

## Screenshot
![image](https://user-images.githubusercontent.com/91037907/219581684-1a82c374-3831-414d-8799-f78b15d1d105.png)

## Program Requirements

- JDK 1.8
- NET Core 3.1
- Apache Maven
- IntelliJ IDEA (Optional)

## How to build
This repository provides the pre-built package of the MilkyBot in the target folder named `MilkyBot.jar`. The bot can also be built using `mvn clean package` command in Maven or IntelliJ.

## How to run game engine
1. Download the starter-pack from [Starter Pack](https://github.com/EntelectChallenge/2021-Galaxio/releases/tag/2021.3.2)
2. On linux, you can run the game by running `./run.sh` and modifying the bot to MilkyBot.jar with the command `java -jar MilkyBot.jar`. Or, you can start game engine, game runner, game logger, and the bots used manually.
3. After the match is finished, the history of the game will be logged in logger-publish.
4. The match can be visualised using visualiser in thr `visualiser` folder.

## Implemented Algorithm
The approach that we used for this bot can be outlined to:
1. Prioritize damaging opponent's bot. The actions that can be chosen are (listed by priority): FIRETELEPORT which has great long-term implication, TELEPORT which has immediate gain as we can eliminate opponent at once and also gain size, FIRETORPEDOES, STARTAFTERBURNER, and FORWARD to chase opponents. For teleporting actions, we only consider largest bot. Meanwhile, for FIRETORPEDOES, STARTAFTERBURNER, and FORWARD we only consider the closest bot.
2. If no PlayerAction that damages opponent's bot passes the restrictions, we prioritize gaining size or defending our bot by FORWARD to get food, STOPAFTERBURNER to stop our size from decreasing, or ACTIVATESHIELD to protect bot from torpedoes.
3. The PlayerAction chosen have to pass a final check. If the bot is near the world border, we prioritize moving inside the world rather than the action chosem.
4. If no PlayerAction is considered to be optimum, we choose FORWARD with heading that gets our bot out of the obstacles.
