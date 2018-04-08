package MyGameEngine;

import ray.input.action.AbstractInputAction;
import net.java.games.input.Event;
import ray.rage.scene.Node;

public class MoveOnXAxisAction extends AbstractInputAction
{
	private Node dolphin;
	
	public MoveOnXAxisAction(Node d) {
		dolphin = d;
	}

	public void performAction(float time, Event e)
	{
		if (e.getValue() < -0.5){
			dolphin.moveLeft(-0.10f);
		}
		else if (e.getValue() > 0.5)
			dolphin.moveRight(-0.10f);
	}
}
