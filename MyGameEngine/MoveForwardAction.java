package MyGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import Networking.ProtocolClient;
import net.java.games.input.Event;

public class MoveForwardAction extends AbstractInputAction
{
	private Node dolphin;
	private ProtocolClient protClient;
	
	public MoveForwardAction(Node d, ProtocolClient p)
	{ 
		dolphin = d;
		protClient = p;
	}

	public void performAction(float time, Event e)
    {
		dolphin.moveForward(0.10f);
		protClient.sendMoveMessage(dolphin.getWorldPosition());
    }
}