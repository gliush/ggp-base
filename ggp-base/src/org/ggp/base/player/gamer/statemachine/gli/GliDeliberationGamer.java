package org.ggp.base.player.gamer.statemachine.gli;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.util.func.Pair;




	
/**
 * Deliberation strategy gamer. It checks on every step all possible ways and follows the best one
 * 
 * @author Ivan Glushkov
 */
public final class GliDeliberationGamer extends StateMachineGamer
{
	/**
	 * Does nothing
	 */
	@Override
	public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		// Do nothing.
	}
	
	public Pair<Integer,List<Move>> 
	findBest(Pair<Integer, List<Move>> bestRes, List<Move> prevMoves, MachineState state, long finishBy, int depth) 
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{

		StateMachine theMachine = getStateMachine();

		//System.out.println(System.currentTimeMillis() + " " + depth + " state: " + state.toString());

		if(theMachine.isTerminal(state)) {
			Pair<Integer, List<Move>> res;
	        int goal = theMachine.getGoal(state, getRole());
	        if (goal > bestRes.x) {
	        	res = new Pair <Integer, List<Move>>(goal, new ArrayList<Move>(prevMoves));
		        System.out.println(System.currentTimeMillis() + " final bestRes:" + res.toString());
	        } else {
	        	res = bestRes;
	        }
	        return res;
	    }

		if(System.currentTimeMillis() > finishBy) {
		    System.out.println(System.currentTimeMillis() + " timeout bestRes:" + bestRes.toString());
			return bestRes;
		}
		
		List<Move> moves = theMachine.getLegalMoves(state, getRole());
		
		for (Move curMove : moves) { 
			// the enemy has to choose 'noop', so we can use getRandomJointMove here
			List<Move> jointMoves = theMachine.getRandomJointMove(state, getRole(), curMove);
			System.out.println(System.currentTimeMillis() + " " + depth + " curMove:" + curMove.toString());
			//System.out.println(System.currentTimeMillis() + " " + depth + " joint:" + jointMoves.toString());
			MachineState nextState = theMachine.getNextState(state, jointMoves);
			//System.out.println(System.currentTimeMillis() + " " + depth + " nextState:" + nextState.toString());
			
			List<Move> nextMoves = new ArrayList<Move>(prevMoves);
			nextMoves.add(curMove);
			
		    bestRes = findBest(bestRes, nextMoves, nextState, finishBy, depth+1);
		}
		
		return bestRes;
	}

	
	/**
	 */
	@Override
	public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException
	{
		long finishBy = timeout - 1000;
		//System.out.println("rules:\n"+ getMatch().getGame().getRules().toString());
		System.out.println("state: " + getCurrentState().toString());
		List<Move> moves = new ArrayList<Move>();
		moves.add(getStateMachine().getLegalMoves(getCurrentState(), getRole()).get(0));
		
		Pair <Integer, List<Move>> acc0 = new Pair<Integer, List<Move>>(-1, moves);
		Pair <Integer, List<Move>> best = findBest(acc0, new ArrayList<Move>(), getCurrentState(), finishBy, 0);
		Move res = best.y.get(0);
		System.out.println("return " + res.toString());
		return res;
	}
	@Override
	public void stateMachineStop() {
		// Do nothing.
	}
	/**
	 * Uses a CachedProverStateMachine
	 */
	@Override
	public StateMachine getInitialStateMachine() {
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public String getName() {
		return "GliDeliberate";
	}

	@Override
	public DetailPanel getDetailPanel() {
		return new SimpleDetailPanel();
	}
	
	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// Do nothing.
	}
	
	@Override
	public void stateMachineAbort() {
		// Do nothing.
	}	
}