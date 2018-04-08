package Entities;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public class GhostAvatar  
{ 
    private UUID id;
    private SceneNode node;
    private Entity entity; 
    private Vector3 position;
    
    public GhostAvatar(UUID id, Vector3 position)  
    {  
        this.id = id;
        this.position = position;
    } 
    
    public void setNode(SceneNode n){
        node = n;
    }
    
    public void setEntity(Entity e){
        entity = e;
    }
    
    public UUID getID(){
        return id;
    }
    
}