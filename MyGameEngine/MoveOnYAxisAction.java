package MyGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import net.java.games.input.Event;

public class MoveOnYAxisAction extends AbstractInputAction
{
	private Node dolphin;
	public MoveOnYAxisAction(Node d) {
		dolphin = d;
	}

	public void performAction(float time, Event e)
	{
		if (e.getValue() < -0.5){
			dolphin.moveForward(0.10f);
		}
		else if (e.getValue() > 0.5)
			dolphin.moveBackward(0.10f);
	}
}
