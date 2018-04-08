package Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import Entities.GhostAvatar;
import a3.MyGame;
import ray.networking.IGameConnection.ProtocolType;
import ray.networking.client.GameConnectionClient;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class ProtocolClient extends GameConnectionClient
{ 
	private MyGame game;
	private GameServerUDP thisUDPServer;
	private UUID id;
	private Vector<GhostAvatar> ghostAvatars;
	public ProtocolClient(InetAddress remAddr, int remPort, ProtocolType pType, MyGame game) throws IOException
	{ 
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
	}
	
	protected void processPacket(Object msg)
	{ 
		String strMessage = (String) msg;
		String[] messageTokens = strMessage.split(",");
		if(messageTokens.length > 0)
		{
			if(messageTokens[0].compareTo("join") == 0) // receive “join”
			{ // format: join, success or join, failure
				if(messageTokens[1].compareTo("success") == 0)	
				{ 
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
				}
				if(messageTokens[1].compareTo("failure") == 0)
				{ 
					game.setIsConnected(false);
				} 
			}
			if(messageTokens[0].compareTo("bye") == 0) // receive “bye”
			{ // format: bye, remoteId
				UUID ghostID = UUID.fromString(messageTokens[1]);
				removeGhostAvatar(ghostID);
			}
			if ((messageTokens[0].compareTo("dsfr") == 0 ) // receive “dsfr”
					|| (messageTokens[0].compareTo("create")==0))
			{ // format: create, remoteId, x,y,z or dsfr, remoteId, x,y,z
				UUID ghostID = UUID.fromString(messageTokens[1]);
				Vector3 ghostPosition = Vector3f.createFrom(Float.parseFloat(messageTokens[2]),Float.parseFloat(messageTokens[3]),Float.parseFloat(messageTokens[4]));
				createGhostAvatar(ghostID, ghostPosition); 
			}
			if (messageTokens[0].compareTo("create") == 0)
            {
                Vector3 position = Vector3f.createFrom(Float.parseFloat(messageTokens[1]), 
                        Float.parseFloat(messageTokens[2]), Float.parseFloat(messageTokens[3]));
                createGhostAvatar(id, position);
            }
            if (messageTokens[0].compareTo("wsds") == 0)
            {
                Vector3 position = Vector3f.createFrom(Float.parseFloat(messageTokens[1]), 
                        Float.parseFloat(messageTokens[2]), Float.parseFloat(messageTokens[3]));
            }
            
			if(messageTokens[0].compareTo("move") == 0) // rec. “move...”
			{ // etc….. 
				Vector3 position = Vector3f.createFrom(Float.parseFloat(messageTokens[1]),Float.parseFloat(messageTokens[2]),Float.parseFloat(messageTokens[3]));
				moveGhostAvatar(id, position); 
			}
		}
	}


	 public void createGhostAvatar(UUID gID, Vector3 gPosition)
	    {
	        GhostAvatar newGhost = new GhostAvatar(gID, gPosition);
	        ghostAvatars.add(newGhost);
	    }
	 
	 public void removeGhostAvatar(UUID gID)
	    {
	        GhostAvatar currentGhost = ghostAvatars.firstElement();
	        Iterator<GhostAvatar> gIterator = ghostAvatars.iterator();
	        
	        while(gIterator.hasNext() == true)
	        {
	            if(currentGhost.getID() == gID)
	            {
	                gIterator.remove();
	                break;
	            }
	            else
	                currentGhost = gIterator.next();
	        }
	    }

	private void moveGhostAvatar(UUID id2, Vector3 position) {
		// TODO Auto-generated method stub
		
	}

	public void sendJoinMessage() // format: join, localId
	{ 
		try
		{
			sendPacket(new String("join," + id.toString()));
		} catch (IOException e) { 
			e.printStackTrace();
		}
	} 
	
	public void sendCreateMessage(Vector3 pos)
	{ // format: (create, localId, x,y,z)
		try
		{ 
			String message = new String("create," + id.toString());
			message += "," + ((Tuple3d) pos).getX()+"," + ((Tuple3d) pos).getY() + "," + ((Tuple3d) pos).getZ();
			sendPacket(message);
		}
		catch (IOException e) { 
			e.printStackTrace();
		}
	} 
	
	public void sendByeMessage()
	{ // etc….. 
		try
		{
			String message = new String("bye," + id.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void sendDetailsForMessage(UUID remId, Vector3 pos)
	{ // etc….. 
		try
		{ 
			String message = new String("Details for Message," + remId.toString());
			message += "," + ((Tuple3d) pos).getX()+"," + ((Tuple3d) pos).getY() + "," + ((Tuple3d) pos).getZ();
			sendPacket(message);
		}
		catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
	public void sendWantsDetailsMessages()
	{
		try
		{	
			String message = new String("details," + id.toString());
			sendPacket(message);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMoveMessage(Vector3 pos)
	{ // etc….. 
		try
		{ 
			String message = new String("move," + id.toString());
			message += "," + ((Tuple3d) pos).getX()+"," + ((Tuple3d) pos).getY() + "," + ((Tuple3d) pos).getZ();
			sendPacket(message);
		}
		catch (IOException e) { 
			e.printStackTrace();
		}
	}
}
