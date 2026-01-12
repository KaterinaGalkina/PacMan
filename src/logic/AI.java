package logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;


/**
 * class used to represent plan. It will provide for a given set of results an action to perform in each result
 */
class Plans{
	ArrayList<Result> results;
	ArrayList<ArrayList<String>> actions;
	
	/**
	 * construct an empty plan
	 */
	public Plans() {
		this.results = new ArrayList<Result>();
		this.actions = new ArrayList<ArrayList<String>>();
	}
	
	/**
	 * add a new pair of belief-state and corresponding (equivalent) actions 
	 * @param beliefBeliefState the belief state to add
	 * @param action a list of alternative actions to perform. Only one of them is chosen but their results should be similar
	 */
	public void addPlan(Result beliefBeliefState, ArrayList<String> action) {
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
	public ArrayList<String> getAction(int index){
		return this.actions.get(index);
	}
}

/**
 * class used to represent a transition function i.e., a set of possible belief states the agent may be in after performing an action
 */
class Result{
	private ArrayList<BeliefState> beliefStates;

	/**
	 * construct a new result
	 * @param states the set of states corresponding to the new belief state
	 */
	public Result(ArrayList<BeliefState> states) {
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
	public ArrayList<BeliefState> getBeliefStates(){
		return this.beliefStates;
	}
}


/**
 * class implement the AI to choose the next move of the Pacman
 */
public class AI{/**
	 * function that compute the next action to do (among UP, DOWN, LEFT, RIGHT)
	 * @param beliefState the current belief-state of the agent
	 * @param deepth the deepth of the search (size of the largest sequence of action checked)
	 * @return a string describing the next action (among PacManLauncher.UP/DOWN/LEFT/RIGHT)
	 */
	public static String findNextMove(BeliefState beliefState) {
		String bestAction = OrAndTree.getNextAction(new NodeBeliefState(beliefState, globalDepth), globalDepth);
		alreadyBeenHere.add(beliefState.getPacmanPos());
		// lastAction = bestAction;
		// Iterator<Position> it = alreadyBeenHere.iterator();
		
		// System.out.println("Current state: " + it.next().toString());
		// scan.nextLine();

		if (bestAction == null){
			System.out.println("\t\tOhhh shit the algo couldn't find an action...");
			return PacManLauncher.DOWN;
		}

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
	public static HashSet<Position> alreadyBeenHere = new HashSet<>();
	public static int globalDepth = 2;
	public static Random rand = new Random();
	public static Scanner scan = new Scanner(System.in);
	// public static String lastAction = null;
}

class NodeBeliefState {
	BeliefState data;
	int value;
	ArrayList<NodeAction> children;
	boolean isGoal;

	NodeBeliefState(BeliefState data, int depth){
		this.data = data;
		this.value = this.evaluate();
		this.children = new ArrayList<>();
		this.isGoal = data.getNbrOfGommes() == 0 && data.getNbrOfSuperGommes() == 0;
	}

	void expandNode(int depth) {
		Plans plans = this.data.extendsBeliefState();

		for(int ind = 0; ind < plans.size(); ind++){
			NodeAction nAction = new NodeAction(plans.getAction(ind).getFirst());  // We don't care which action we choose if it leads to the same result
			this.children.add(nAction);
			for(BeliefState bs: plans.getResult(ind).getBeliefStates()){ // We are adding all belief states of this action
				nAction.children.add(new NodeBeliefState(bs, depth - 1));
			}
		}
	}

	int evaluate() { // Heuristic to evaluate how good the current belief state is
		// TODO : replace this by somethign else, it scares the pacman to actually advance
		// if (this.data.getLife() == 0) { // If we are dead huge penalisation
		// 	return -1_000_000;
		// }

		if (this.isGoal){ // If we won the game
			return +1_000_000;
		}

		int score = 0;

		score -= 100 * this.data.distanceMinToGum();

		// score -= 150 * this.data.getNbrOfGommes();
		// score -= 300 * this.data.getNbrOfSuperGommes();

		// // We want to penalize if the pacman is close to the ghost (for each ghost too close), or award if we just had a super gum to eat a ghost
		// Position pacmanPos = this.data.getPacmanPosition();
		
		// for(int i = 0; i < this.data.getNbrOfGhost(); i++){
		// 	TreeSet<Position> ghPos = this.data.getGhostPositions(i);
		// 	if (ghPos.size() == 1){ // We are sure where the ghost is, because we can see him
		// 		double x = Math.abs(ghPos.getFirst().getRow() - pacmanPos.getRow());
		// 		double y = Math.abs(ghPos.getFirst().getColumn() - pacmanPos.getColumn());
		// 		double distance = x + y;
		// 		if (this.data.getCompteurPeur(i) == 0) {
		// 			return (int)(-100_000 / (distance + 1));
		// 		} else {
		// 			return (int) (30_000 / (distance + 1));
		// 		}
		// 	}
		// }

		if (!AI.alreadyBeenHere.contains(this.data.getPacmanPos())) { // Bonus if the position is new
			score += 20_000;
		}

		// What we already grabbed, strong (would be times 10 with each eaten gum), because we want to eat gums as fast as possible
		score += 50 * this.data.getScore();

		// return score;
		// We add a little bit of randomness to prevent plateaux
		return (int) (AI.rand.nextInt(100) + score);
	}
}

class NodeAction {
	String action;
	int value;
	ArrayList<NodeBeliefState> children;

	NodeAction(String action){
		this.action = action;
		this.value = -1_000_000; // The minimum (the case when pacman is dead), it is going to be updated later
		this.children = new ArrayList<>();
	}
}

class OrAndTree {
	NodeBeliefState root;
	int depth;

	OrAndTree(NodeBeliefState root, int depth){
		this.root = root;
		this.depth = depth;
	}

	static String getNextAction(NodeBeliefState initialState, int depth) {

		OrAndTree tree = new OrAndTree(initialState, depth);

		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;

		initialState.expandNode(depth);

		NodeAction bestAction = null;
		int bestValue = Integer.MIN_VALUE;

		for (NodeAction action : initialState.children) {
			int value = andSearch(action, tree.depth - 1, alpha, beta);

			Position nextPos = initialState.data.extendsBeliefState(action.action).getBeliefState(0).getPacmanPos();

			if (initialState.data.getPacmanPos().x == nextPos.x && initialState.data.getPacmanPos().x == nextPos.y) {
				value -= 50_000;
			}

			// if (AI.lastAction != null && isReverse(AI.lastAction, action.action)) { // If it help to escape maybe it is better
			// 	value -= 5_000;
			// }

			if (value > bestValue) {
				bestValue = value;
				bestAction = action;
			}

			alpha = Math.max(alpha, bestValue);
		}
		if (bestAction == null){
			return null;
		}
		return bestAction.action;
	}

	static boolean isReverse(String a, String b) {
		return (a.equals("LEFT") && b.equals("RIGHT")) ||
			(a.equals("RIGHT") && b.equals("LEFT")) ||
			(a.equals("UP") && b.equals("DOWN")) ||
			(a.equals("DOWN") && b.equals("UP"));
	}

	static int orSearch(NodeBeliefState node, int depth, int alpha, int beta) {
		if (depth == 0 || node.isGoal) {
			node.value = node.evaluate();
			return node.value;
		}

		node.expandNode(depth);

		int value = Integer.MIN_VALUE;

		for (NodeAction action : node.children) {
			int childValue = andSearch(action, depth - 1, alpha, beta);
			value = Math.max(value, childValue);
			alpha = Math.max(alpha, value);

			if (alpha >= beta) {
				break; // beta cut
			}
		}

		node.value = value;
		return value;
	}

	static int andSearch(NodeAction action, int depth, int alpha, int beta) {

		int value = Integer.MAX_VALUE;
		// We avoid considering all belief states, just some randomly chosen between 5 and 10 at most
		int maxNbBeliefStates = 5 + AI.rand.nextInt(6);

		for (NodeBeliefState bs : action.children) {
			int childValue = orSearch(bs, depth, alpha, beta);
			value = Math.min(value, childValue);
			beta = Math.min(beta, value);

			if (beta <= alpha) {
				break; // alpha cut
			}

			if (maxNbBeliefStates == 0){
				break; // Max number of belief states for this action is acheieved
			}
			maxNbBeliefStates--;
		}

		action.value = value;
		return value;
	}
}