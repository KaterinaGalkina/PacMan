package logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * class used to represent plan. It will provide for a given set of results an action to perform in each result
 */
class Plans {
    ArrayList < Result > results;
    ArrayList < ArrayList < String >> actions;

    /**
     * construct an empty plan
     */
    public Plans() {
        this.results = new ArrayList < Result > ();
        this.actions = new ArrayList < ArrayList < String >> ();
    }

    /**
     * add a new pair of belief-state and corresponding (equivalent) actions
     * @param beliefBeliefState the belief state to add
     * @param action a list of alternative actions to perform. Only one of them is chosen but their results should be similar
     */
    public void addPlan(Result beliefBeliefState, ArrayList < String > action) {
        this.results.add(beliefBeliefState);
        this.actions.add(action);
    }

    /**
     * return the number of belief-states/actions pairs
     * @return the number of belief-states/actions pairs
     */
    public int size() {
        return this.results.size();
    }

    /**
     * return one of the belief-state of the plan
     * @param index index of the belief-state
     * @return the belief-state corresponding to the index
     */
    public Result getResult(int index) {
        return this.results.get(index);
    }

    /**
     * return the list of actions performed for a given belief-state
     * @param index index of the belief-state
     * @return the set of actions to perform for the belief-state corresponding to the index
     */
    public ArrayList < String > getAction(int index) {
        return this.actions.get(index);
    }
}

/**
 * class used to represent a transition function i.e., a set of possible belief states the agent may be in after performing an action
 */
class Result {
    private ArrayList < BeliefState > beliefStates;

    /**
     * construct a new result
     * @param states the set of states corresponding to the new belief state
     */
    public Result(ArrayList < BeliefState > states) {
        this.beliefStates = states;
    }

    /**
     * returns the number of belief states
     * @return the number of belief states
     */
    public int size() {
        return this.beliefStates.size();
    }

    /**
     * return one of the belief state
     * @param index the index of the belief state to return
     * @return the belief state to return
     */
    public BeliefState getBeliefState(int index) {
        return this.beliefStates.get(index);
    }

    /**
     * return the list of belief-states
     * @return the list of belief-states
     */
    public ArrayList < BeliefState > getBeliefStates() {
        return this.beliefStates;
    }
}


/**
 * AI class that implements the algorithm to pick the next move of pacman
 */
public class AI {
    public static TreeMap < Position, Integer > alreadyBeenHere = new TreeMap < > (); // Positions on the map that our pacman has visited
    public static TreeSet < Pair > alreadyBeenHerePairs = new TreeSet < > (); // Positions on the map that our pacman has visited (only coordinates, without direction)
    public static TreeMap < BeliefState, HashMap < String, Integer >> visitedBeliefStates = new TreeMap < > (); // Already visited belief states and their corresponding possible actions' scores
    public static TreeMap < BeliefState, Integer > nbOfMeets = new TreeMap < > (); // Number of times we have meet a particluar belief state during the game
    public static int globalDepth = 3; // The depth of the AND-OR search tree (decremented in AND nodes corresponding to Belief states)
    public static Random rand = new Random(); // Will be used to avoid plateaux and add a little bit of randomness

    /**
     * Returns true if at least one of the ghosts is visible now
     * @param bs the current belief state of the agent
     * @return boolean indicating whether at least one ghost is visible
     */
    public static boolean hasVisibleGhosts(BeliefState bs) {
        Position pacmanPos = bs.getPacmanPos();
        int nGhosts = bs.getNbrOfGhost();

        for (int i = 0; i < nGhosts; i++) {
            for (Position ghostPos: bs.getGhostPositions(i)) {
                if (BeliefState.isVisible(ghostPos.x, ghostPos.y, pacmanPos.x, pacmanPos.y)) {
                    return true; // Found at least one visible ghost
                }
            }
        }
        return false;
    }

    /**
     * function that compute the next action to do (among UP, DOWN, LEFT, RIGHT)
     * @param beliefState the current belief-state of the agent
     * @return a string describing the next action (among PacManLauncher.UP/DOWN/LEFT/RIGHT)
     */
    public static String findNextMove(BeliefState beliefState) {
        // Possible actions from this belief state with their corresponding scores
        HashMap < String, Integer > actions;
        // Pacman's current position
        Position pacmanPos = beliefState.getPacmanPos();

        // We update the alreadyBeenHere dictionnaire to store the number of times this position was visisted
        if (alreadyBeenHere.containsKey(pacmanPos)) {
            alreadyBeenHere.put(pacmanPos, alreadyBeenHere.get(pacmanPos) + 1);
        } else {
            alreadyBeenHere.put(pacmanPos, 1);
        }

        // And we update the alreadyBeenHerePairs to store that this coordinate was visisted
        alreadyBeenHerePairs.add(new Pair(pacmanPos.x, pacmanPos.y));

        boolean ok = hasVisibleGhosts(beliefState);
        // If we are not in immediate danger, meaning that we don't see a ghost right now, we can risk a little bit
        if (!ok) {
            // If we have alreday visited this belief state, we can get immediatly the actions and not recalculate them
            if (visitedBeliefStates.containsKey(beliefState)) {
                // System.out.println("\t\tReusing previous computation for this belief state...");
                actions = visitedBeliefStates.get(beliefState);
            } else { // Otherwise we launch our AND-OR search to find the actions and their corresponding scores
                actions = OrAndTree.getNextAction(new NodeBeliefState(beliefState, globalDepth), globalDepth);
                // And store them to the dictionnary
                visitedBeliefStates.put(beliefState, actions);
            }

            // We update the number of times we have visited this belief state
            if (nbOfMeets.containsKey(beliefState)) {
                nbOfMeets.put(beliefState, nbOfMeets.get(beliefState) + 1);
                // System.out.println("\t\tNumber of times we've met this belief state: " + nbOfMeets.get(beliefState));
            } else {
                nbOfMeets.put(beliefState, 1);
                visitedBeliefStates.put(beliefState, actions);
            }
            // If we are in danger, then we should recalculate the actions and their corresponding scores
        } else {
            actions = OrAndTree.getNextAction(new NodeBeliefState(beliefState, globalDepth), globalDepth);
        }

        // If the algo hasn't been able to find any possible actions from here then well... I guess we have to apply a default action
        if (actions.size() == 0) {
            System.out.println("\t\tOhhh shit the algo couldn't find an action...");
            return PacManLauncher.DOWN;
        }

        // We sort the possible actions by their score
        LinkedHashMap < String, Integer > sorted = actions.entrySet().stream()
            .sorted((e1, e2) - > Integer.compare(e2.getValue(), e1.getValue()))
            .collect(
                LinkedHashMap::new,
                (m, e) - > m.put(e.getKey(), e.getValue()),
                Map::putAll
            );

        // And we retrieve the best one
        Iterator < String > it = sorted.keySet().iterator();
        String bestAction = it.next();
        // To avoid infinite loops, once we have visited a particular belief state
        // more than 5 times, we change the action applied from the best one to the second best action
        if (!ok) {
            if (nbOfMeets.get(beliefState) >= 5 && it.hasNext()) {
                // System.out.println("\t\tChoosing second best action because we've been here more than 5 times...");
                bestAction = it.next();
            }
        }

        // We convert an action in a form of string given by algorithm to the correct type
        switch (bestAction) {
            case "UP":
                return PacManLauncher.UP;
            case "LEFT":
                return PacManLauncher.LEFT;
            case "RIGHT":
                return PacManLauncher.RIGHT;
            case "DOWN":
                return PacManLauncher.DOWN;
            default:
                return PacManLauncher.DOWN;
        }
    }
}

/**
 * Pair is a class that stores coordinates (x, y) of a position and allows to compare one position with another
 * This class was implemented to be able to easier consider only the coordinates of the pacman and not its direction
 */
class Pair implements Comparable < Pair > {
    public int x;
    public int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Pair other) {
        if (this.x != other.x) {
            return Integer.compare(this.x, other.x);
        }
        return Integer.compare(this.y, other.y);
    }
}

/**
 * NodeBeliefState is a class that represents one AND node of an AND-OR search tree,
 * containing a belief state currently being studied
 */
class NodeBeliefState {
    // The current belief state represented by this node
    BeliefState data;
    // The score of this belief state
    // for leaf nodes, it is calculated using the heuristic function.
    // for intermediate nodes, the score is updated following the min-max strategy
    int value;
    // Possible actions from this belief state (at most 4: "UP", "DOWN", "RIGHT, "LEFT")
    ArrayList < NodeAction > children;
    // Indicates whether this node is a goal state (all gums in this level are eaten)
    boolean isGoal;

    NodeBeliefState(BeliefState data, int depth) {
        this.data = data; // current belief state
        this.value = this.evaluate(); // initial heuristic evaluation
        this.children = new ArrayList < > (); // The list of possible actions (will be expanded later)
        this.isGoal = data.getNbrOfGommes() == 0; // The goal state on the current level is reached when all gums are eaten
    }

    /**
     * Extends the current AND node by adding OR nodes for each possible action
     * and for each action adds the corresponding resulting belief states as AND nodes
     * @param depth the current node's depth, if 0, then the node won't be expanded
     */
    void expandNode(int depth) {
        // We do not expand if the maximum depth is achieved
        if (depth <= 0) {
            return;
        }
        // We get the list of possible actions and their corresponding resulting belief states
        Plans plans = this.data.extendsBeliefState();
        for (int i = 0; i < plans.size(); i++) {
            // We create the OR node correspong to this next possible action
            NodeAction nAction = new NodeAction(plans.getAction(i));
            // And add it to this node's children
            this.children.add(nAction);

            // For each resulting belief state by applying this action, we create a child AND node
            for (BeliefState bs: plans.getResult(i).getBeliefStates()) {
                nAction.children.add(new NodeBeliefState(bs, depth - 1));
            }
        }
    }

    /**
     * Heuristic function that evaluates how good the current belief state is
     * @return int the score of the current belief state
     */
    int evaluate() {
        // If pacman has won the current level - huge bonus
        if (this.isGoal) {
            return +1000000;
        }

        // Otherwise we calculate it
        int score = 0;
        // How far the pacman is from the nearest gum * 200, so the farthest he is, the more penalty he receives
        score -= 200 * this.data.distanceMinToGum();
        // We give pacman a little bonus if he discovers a new area on the map
        if (!AI.alreadyBeenHere.containsKey(this.data.getPacmanPos())) {
            score += 20000;
        }
        // We also take into account his current belief state's score (so for each eaten gum he receives + 200 * 10,
        // so that the reward is bigger than the act of just approaching a gum)
        score += 200 * this.data.getScore();
        // Finally to avoid plateaux, we add a little bit of randomness
        return (int) score + AI.rand.nextInt(1000);
    }
}

/**
 * NodeAction is a class that represents one OR node of an AND-OR search tree,
 * containing a possible action (or set of equivalent actions) from its parent belief state node
 */
class NodeAction {
    // As it is possible that more than one action leads to the same result, we store them all in one node
    ArrayList < String > actions;
    // The score of this action node (calculated once we have reached leaf nodes)
    int value;
    // Set of belief states resulting from applying this action on the current belief state
    ArrayList < NodeBeliefState > children;

    NodeAction(ArrayList < String > actions) {
        this.actions = actions;
        this.value = Integer.MIN_VALUE + 1; // We start with the smallest value, it will be updated later during the search
        this.children = new ArrayList < > ();
    }
}

/**
 * OrAndTree is a class that represents the AND-OR search tree which explores the current
 * belief state and whose main goal is to find the best action from a given belief state
 */
class OrAndTree {
    // Initial belief state from which we start the search
    NodeBeliefState root;
    // Maximum depth of the tree
    int depth;

    OrAndTree(NodeBeliefState root, int depth) {
        this.root = root;
        this.depth = depth;
    }

    /**
     * The function that implements the AND-OR algorithm to find the best action from the current initial belief state
     * @param initialState The initial belief state from which the Pacman starts
     * @param depth The maximum depth of the search tree (the depth is decremented only in AND nodes, meaning in belief states)
     * @return HashMap<String, Integer> a dictionary that contains the list of possible actions and their corresponding scores found by the algorithm
     */
    static HashMap < String, Integer > getNextAction(NodeBeliefState initialState, int depth) {
        // We are going to prune to make the algorithm faster
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // We expand the initial belief state
        initialState.expandNode(depth);

        // We initialize the possible actions from this belief state with their corresponding scores
        HashMap < String, Integer > actions = new HashMap < > ();
        // We store the best value found (for the prunning)
        int bestValue = Integer.MIN_VALUE;

        // For all possible actions from this belief state
        for (NodeAction action: initialState.children) {
            // We calculate the action's value using AND search
            int value = andSearch(action, depth - 1, alpha, beta);

            // To better evaluate the action, we update its value depending on the immediate next position it can lead to
            // (which we consider more important than future rewards deeper in the tree)

            // We consider one possible belief state resulting from this action
            // to get pacman's next possible position
            BeliefState nextBs = action.children.get(0).data;
            Position nextPos = nextBs.getPacmanPos();

            // If the action doesn't change pacman's position, it's useless
            if (initialState.data.getPacmanPos().x == nextPos.x && initialState.data.getPacmanPos().y == nextPos.y) {
                value = Integer.MIN_VALUE;
            } else {
                // If pacman gets closer to the nearest gum in this new position
                if ((initialState.data.distanceMinToGum() - nextBs.distanceMinToGum()) == 1) {
                    // And if pacman has never been in this position
                    if (AI.alreadyBeenHerePairs.contains(new Pair(nextPos.x, nextPos.y)) == false) {
                        // We give a big bonus
                        value += 50000 * (25 - nextBs.distanceMinToGum());
                    } else {
                        // If pacman has already visited this position we still give a bonus but smaller
                        value += 15000 * (25 - nextBs.distanceMinToGum());
                    }
                } else {
                    // If pacman became farther from the closed gum, because he ate it, we give him a big bonus
                    if (initialState.data.getNbrOfGommes() > nextBs.getNbrOfGommes() ||
                        initialState.data.getNbrOfSuperGommes() > nextBs.getNbrOfSuperGommes()) {
                        value += 20000;
                    }
                }
            }

            // We check whether pacman is going to certainly die after applying this action
            boolean isGoingToDieCertainly = true;
            for (NodeBeliefState bs: action.children) {
                if (bs.data.getLife() >= initialState.data.getLife()) {
                    isGoingToDieCertainly = false;
                    break;
                }
            }

            // If yes, then we assign the minimal value to this action
            if (isGoingToDieCertainly) {
                value = Integer.MIN_VALUE + 1;
            } else { // otherwise a bonus
                value += 5000;
            }

            // We store the value for all equivalent actions
            for (String a: action.actions) {
                actions.put(a, value);
            }

            // We update the values
            bestValue = Math.max(bestValue, value);
            alpha = Math.max(alpha, bestValue);
        }
        return actions;
    }

    /**
     * The function that implements the OR search from a given AND node (representing a belief state)
     * @param node The belief state from which the search starts
     * @param depth The maximum depth of the search tree (the depth is decremented only in AND nodes, meaning in belief states)
     * @param alpha the lower bound value to prune branches
     * @param beta the upper bound value to prune branches
     * @return int the score of the belief state given in the parameters
     */
    static int orSearch(NodeBeliefState node, int depth, int alpha, int beta) {
        // If the maximum depth is reached or the node is a goal node, we return the node's heuristic value
        if (depth == 0 || node.isGoal) {
            return node.value;
        }

        // Otherwise we expand the node
        node.expandNode(depth);

        // Checking whether we have possible actions after expanding the node
        if (node.children.isEmpty()) {
            return node.value;
        }

        // Current maximal value (for possible pruning and to return the final value of this belief state)
        int maxValue = Integer.MIN_VALUE;

        // We explore the possible actions from this belief state
        for (NodeAction action: node.children) {
            // We get the current action's value with AND search
            int childValue = andSearch(action, depth - 1, alpha, beta);

            // We update values if needed
            maxValue = Math.max(maxValue, childValue);
            alpha = Math.max(alpha, maxValue);

            // No need to continue exploring actions from this belief state because this branch
            // cannot lead to a better result than one of the previously explored, so we cut it
            if (alpha >= beta) {
                break;
            }
        }
        // We return the value of the given belief state
        return maxValue;
    }

    /**
     * The function that implements the AND search from a given OR node representing a possible action
     * @param action The action node from which the search starts
     * @param depth The maximum depth of the search tree (the depth is decremented only in AND nodes)
     * @param alpha the lower bound value used to prune branches
     * @param beta the upper bound value used to prune branches
     * @return int the score of the action node given in the parameters
     */
    static int andSearch(NodeAction action, int depth, int alpha, int beta) {
        // If the action doesn't lead to any belief states, that means game over,
        // so we immediately set to it the minimum value
        if (action.children.isEmpty()) {
            action.value = Integer.MIN_VALUE + 1;
            return action.value;
        }

        // Current minimal value (for possible pruning and to return the final value of this action)
        int minValue = Integer.MAX_VALUE;

        // We explore all possible belief states obtained by the action of this action node
        for (NodeBeliefState bs: action.children) {
            // We calculate the belief state's score with OR search
            int childValue = orSearch(bs, depth, alpha, beta);

            // We update values if needed
            minValue = Math.min(minValue, childValue);
            beta = Math.min(beta, minValue);

            // No need to continue exploring belief states from this possible action because this branch
            // can't lead to a better result than one of the previously explored, so we cut it
            if (beta <= alpha) {
                break;
            }
        }
        // We update the action node's value
        action.value = minValue;
        return action.value;
    }
}
