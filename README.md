# Pacman AI (Partially Observable Environment)

This project implements an AI agent capable of playing a variant of Pacman in a partially observable environment.

The objective is simple: maximize the score while computing each move as fast as possible.

## Core Idea

Pacman does not always see the ghosts.  
If a wall blocks the line of sight, the ghost is invisible, making the game a **Partially Observable Markov Decision Process**.

To handle uncertainty, the AI reasons over belief states rather than a single game state and uses the AND-OR search algorithm to select optimal actions.

## Game Rules

- Pac-Man moves: `UP`, `DOWN`, `LEFT`, `RIGHT`
- Walls block movement and vision
- Dots (`.`): +10 points  
- Power gums (`*`):
  - Turn ghosts blue (fear mode)
  - Allow Pac-Man to eat ghosts (+100 points per ghost)
- Ghosts respawn after being eaten
- Once all dots are consumed, the next level starts. The game contains 3 levels in total; once Pacman completes all of them, he returns to level 1.

## Architecture Overview

The provided engine handles the game logic and rendering.  
The AI interacts with it through the following key classes:

### `BeliefState`
Represents a set of possible world states consistent with what Pacman has perceived so far.

Provides:
- Score, lives, remaining dots
- Possible ghost positions
- Comparison and state expansion utilities

### `Result`
Encapsulates multiple belief states resulting from the same action, differentiated by possible percepts.

### `Plans`
Stores all possible action → Result mappings from a belief state.

### `AI`
Contains AI logic for the agent.

The goal of the project was to implement the following method:

```java
String findNextMove(BeliefState beliefState)
```

To do so, we were allowed to create as many classes as needed, provided they were all contained within the same file.  
As a result, the entire project completed by my group and me is located in `src/logic/AI.java`.

The rest of the code was written by **Rolland Guillaume** and **Fréret Rémi**:  
https://github.com/rollandguillaume/pacman/tree/master  

and later modified by our professor **Julien Lesca**:  
https://sites.google.com/site/julienlesca/home

## Strategy

The algorithm used to decide the next move is based on an **AND-OR tree search** approach, with the search depth currently set to 3.

