package a3;

import MyGameEngine.Camera3Pcontroller;
//import MyGameEngine.ChangeCameraModeAction;
import MyGameEngine.MoveBackwardsAction;
import MyGameEngine.MoveForwardAction;
import MyGameEngine.MoveLeftAction;
import MyGameEngine.MoveRightAction;
import MyGameEngine.MovementController;
import MyGameEngine.RotateLeftAction;
import MyGameEngine.RotateRightAction;
import MyGameEngine.RotateYawAxisAction;
import MyGameEngine.MoveOnXAxisAction;
import MyGameEngine.MoveOnYAxisAction;
import MyGameEngine.ScaleController;
import Networking.ProtocolClient;
import net.java.games.input.Event;
import MyGameEngine.QuitGameAction;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import Entities.GhostAvatar;

import javax.script.Invocable;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;
import java.lang.Math;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.geom.*;

import ray.input.*;
import ray.input.action.*;
import ray.networking.IGameConnection.ProtocolType;
import ray.rage.*;
import ray.rage.asset.texture.*;
import ray.rage.game.*;
import ray.rage.rendersystem.*;
import ray.rage.rendersystem.Renderable.*;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.states.*;
import ray.rage.rendersystem.shader.*;
import ray.rage.asset.material.*;
import ray.rage.scene.*;
import ray.rage.scene.Camera.Frustum.*;
import ray.rage.scene.controllers.*;
import ray.rml.*;
import ray.rage.util.BufferUtil;
import ray.rage.rendersystem.states.*;
import ray.rage.asset.texture.*;
import ray.rage.util.*;


public class MyGame extends VariableFrameRateGame
{
	GL4RenderSystem rs;
	
	private float elapsTime = 0.0f;
	private String elapsTimeStr, scoreStr1, scoreStr2, dispStr1, dispStr2;
	private int elapsTimeSec, score1 = 0;
	private int score2 = 0;
	private float randX, randZ;
	private Random randNum = new Random();
	private Camera camera1, camera2;
	private SceneNode dolphinN1, dolphinN2, cameraNodeN1, cameraNodeN2, orangeNG;
	private Light plight;
	private SceneManager sm;
	private Camera3Pcontroller orbitController1, orbitController2;
	private InputManager im = new GenericInputManager();
	private Viewport topViewport;
	private Action quitGameAction, MoveOnYAxisAction, MoveOnXAxisAction, MoveForwardAction, MoveBackwardsAction, 
				   MoveLeftAction, MoveRightAction, RotateLeftAction1, RotateRightAction1, RotateLeftAction2, RotateRightAction2, RotateYawAxisAction;
	private SceneNode[] orangeArray = new SceneNode[15];
	private MovementController[] moveArray = new MovementController[15];
	private static final String SKYBOX_NAME = "MySkyBox";
	private boolean skyBoxVisible = true;
	protected ScriptEngine jsEngine;
	protected ColorAction colorAction;
	protected File scriptFile3;
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected;
	private GhostAvatar ghost;
	private Vector<UUID> gameObjectsToRemove;
	

	public MyGame(String args, int i) {
		super();
		System.out.println("W,A,S,D to move forward, backwards, left and right");
	}

	public static void main(String[] args)
	{ 
		Game game = new MyGame(args[0], Integer.parseInt(args[1]));

		try
		{ 
			game.startup();
			game.run();
		}

		catch (Exception e){ 
			e.printStackTrace(System.err);
		}

		finally
		{ 
			game.shutdown();
			game.exit();
		} 
	}

	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}

	protected void setupWindowViewports(RenderWindow rw)
	{ 
		rw.addKeyListener(this);
		topViewport = rw.getViewport(0);
	}
	
    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) 
    {
        camera1 = sm.createCamera("MainCamera1", Projection.PERSPECTIVE);
        camera1.setMode('r');
        rw.getViewport(0).setCamera(camera1);
        
    }
    
    protected void setupNetworking()
    {
    	gameObjectsToRemove = new Vector<UUID>();
    	isClientConnected = false;
    	try {
    		protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
    	} catch (UnknownHostException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	if(protClient == null){
    		System.out.println("missing protocol host");
    	} else {
    		protClient.sendJoinMessage();
    	}
    }

    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException 
    {
    	TextureManager tm = eng.getTextureManager();
    	Configuration conf = eng.getConfiguration();
    	ScriptEngineManager factory = new ScriptEngineManager();
    	java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
    	jsEngine = factory.getEngineByName("js");
    	
    	scriptFile3 = new File("UpdateLightColor.js");
    	this.runScript(scriptFile3);
    	
		ManualObject axesO = makeAxes(eng, sm);
		SceneNode axesN = sm.getRootSceneNode().createChildSceneNode("axesNode");
		axesN.attachObject(axesO);
		axesN.moveBackward(2.0f);

        Entity dolphinE1 = sm.createEntity("myDolphin1", "dolphinHighPoly.obj");
        dolphinE1.setPrimitive(Primitive.TRIANGLES);
        dolphinN1 = sm.getRootSceneNode().createChildSceneNode(dolphinE1.getName() + "Node");
        dolphinN1.moveBackward(2.0f);
        dolphinN1.attachObject(dolphinE1);
        cameraNodeN1 = sm.getRootSceneNode().createChildSceneNode(camera1.getName() + "Node");
        cameraNodeN1.attachObject(camera1);
         
        ScaleController sc = new ScaleController(this, sm);
		
		Tessellation tessE = sm.createTessellation("tessE", 8);
		tessE.setSubdivisions(36f);
		SceneNode tessN = sm.getRootSceneNode().createChildSceneNode("tessN");
		tessN.attachObject(tessE);
		tessN.scale(10, 20, 10);
		tessE.setHeightMap(this.getEngine(), "heightmap.png");
		tessE.setTexture(this.getEngine(), "snowygrass.jpg");
//		tessE.setNormalMap(this.getEngine(), "heightmap2.png");
		
        sm.getAmbientLight().setIntensity(new Color(.50f, .50f, .50f));
			
		plight = sm.createLight("testLamp1", Light.Type.POINT);
		plight.setAmbient(new Color(0.3f, 0.3f, 0.3f));
        plight.setDiffuse(new Color(.7f, .7f, .7f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(100.0f);
		
		SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
        plightNode.moveUp(2.5f);
        
        tm.setBaseDirectoryPath(conf.valueOf("assets.skyboxes.path"));
		Texture front = tm.getAssetByPath("TropicalSunnyDayFront.png");
		Texture back = tm.getAssetByPath("TropicalSunnyDayBack.png");
		Texture left = tm.getAssetByPath("TropicalSunnyDayLeft.png");
		Texture right = tm.getAssetByPath("TropicalSunnyDayRight.png");
		Texture top = tm.getAssetByPath("TropicalSunnyDayTop.png");
		Texture bottom = tm.getAssetByPath("TropicalSunnyDayBottom.png");
		
		AffineTransform xform = new AffineTransform();
		xform.translate(0, front.getImage().getHeight());
		xform.scale(1d, -1d);
		front.transform(xform);
		back.transform(xform);
		left.transform(xform);
		right.transform(xform);
		top.transform(xform);
		bottom.transform(xform);
		
		SkyBox sb = sm.createSkyBox(SKYBOX_NAME);
		sb.setTexture(front, SkyBox.Face.FRONT);
		sb.setTexture(back, SkyBox.Face.BACK);
		sb.setTexture(left, SkyBox.Face.LEFT);
		sb.setTexture(right, SkyBox.Face.RIGHT);
		sb.setTexture(top, SkyBox.Face.TOP);
		sb.setTexture(bottom, SkyBox.Face.BOTTOM);
		sm.setActiveSkyBox(sb);
		
        //sceneManagerN = sm;
        
    	setupInputs();
    	
    	setupOrbitCamera(eng, sm);
    	  	 
    	sm.addController(sc);
    }

    protected ManualObject makeAxes(Engine eng, SceneManager sm) throws IOException
    {
        ManualObject axes = sm.createManualObject("Axes");
        ManualObjectSection axesSec = axes.createManualSection("AxesSection");
        axesSec.setPrimitive(Primitive.LINES);
        axes.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

        float [] vertices = new float[]
        {
             0.0f, 0.0f, 0.0f, 30.0f, 0.0f, 0.0f, 0.0f,
             0.0f, 0.0f, 0.0f, 0.0f, 30.0f, 0.0f, 0.0f,
             0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 30.0f, 0.0f
        };
        
        int[] indices = new int[] { 0,1,2,3,4,5 };

        FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
        IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);
        axesSec.setVertexBuffer(vertBuf);
        axesSec.setIndexBuffer(indexBuf);

        Material mat = sm.getMaterialManager().getAssetByPath("default.mtl");
        mat.setEmissive(Color.BLUE);
        Texture tex = sm.getTextureManager().getAssetByPath(mat.getTextureFilename());
        TextureState tstate = (TextureState) sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
        tstate.setTexture(tex);
        axesSec.setRenderState(tstate);
        axesSec.setMaterial(mat);
        
        return axes;
    }

    
    protected ManualObject makeOcean(Engine eng, SceneManager sm) throws IOException
	{ 
		ManualObject ocean = sm.createManualObject("ocean");
		ManualObjectSection oceanSec = ocean.createManualSection("OceanSection");
		ocean.setGpuShaderProgram(sm.getRenderSystem().
		getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));

		float[] vertices = new float[]
		{ 
			-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 
			1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f 
		};

		float[] texcoords = new float[]
		{
			0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,
			1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
		};

		float[] normals = new float[]
		{ 
			0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
			0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
		};

		int[] indices = new int[] {  0,1,2,3,4,5 };

		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf = BufferUtil.directIntBuffer(indices);

		oceanSec.setVertexBuffer(vertBuf);
		oceanSec.setTextureCoordsBuffer(texBuf);
		oceanSec.setNormalsBuffer(normBuf);
		oceanSec.setIndexBuffer(indexBuf);

		Texture tex = eng.getTextureManager().getAssetByPath("water.jpg");
		TextureState texState = (TextureState)sm.getRenderSystem().
		createRenderState(RenderState.Type.TEXTURE);
		texState.setTexture(tex);
		FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().
		createRenderState(RenderState.Type.FRONT_FACE);
		faceState.setVertexWinding(FrontFaceState.VertexWinding.CLOCKWISE);

		ocean.setDataSource(DataSource.INDEX_BUFFER);
		ocean.setRenderState(texState);
		ocean.setRenderState(faceState);

		return ocean;
	}

    protected void setupInputs()
	{	 
    	im = new GenericInputManager();
		String kbName = im.getKeyboardName();
		String gpName = im.getFirstGamepadName();
		quitGameAction = new QuitGameAction(this);
		MoveForwardAction = new MoveForwardAction(dolphinN1, protClient);
		MoveBackwardsAction = new MoveBackwardsAction(dolphinN1, protClient);
		MoveOnYAxisAction = new MoveOnYAxisAction(dolphinN1);
		MoveLeftAction = new MoveLeftAction(dolphinN1);
		MoveRightAction = new MoveRightAction(dolphinN1);
		RotateLeftAction1 = new RotateLeftAction(dolphinN1);
		RotateRightAction1 = new RotateRightAction(dolphinN1);
		RotateLeftAction2 = new RotateLeftAction(dolphinN1);
		RotateRightAction2 = new RotateRightAction(dolphinN1);
		RotateYawAxisAction = new RotateYawAxisAction(RotateLeftAction2, RotateRightAction2);
		MoveOnXAxisAction = new MoveOnXAxisAction(dolphinN1);
		colorAction = new ColorAction(sm);

		// attach the action objects to keyboard and gamepad components
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, MoveForwardAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, MoveBackwardsAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, RotateRightAction1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, RotateLeftAction1, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.SPACE, colorAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		//im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, MoveLeftAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);		
		//im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, MoveRightAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.Y, MoveOnYAxisAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);		
		im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.X, RotateYawAxisAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(gpName, net.java.games.input.Component.Identifier.Axis.RX, RotateYawAxisAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._6, RotateRightAction2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._5, RotateLeftAction2, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(gpName, net.java.games.input.Component.Identifier.Button._8, quitGameAction, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.ESCAPE,quitGameAction ,InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	}


	protected void setupOrbitCamera(Engine eng, SceneManager sm)
	{
		SceneNode dolphinN1 = sm.getSceneNode("myDolphin1Node");
		SceneNode cameraNodeN1 = sm.getSceneNode("MainCamera1Node");
		String kbName = im.getKeyboardName();
		String gpName = im.getFirstGamepadName();
		Camera camera1 = sm.getCamera("MainCamera1");			
		orbitController1 = new Camera3Pcontroller(camera1, cameraNodeN1, dolphinN1, kbName, im);
		orbitController2 = new Camera3Pcontroller(camera1, cameraNodeN1, dolphinN1, gpName, im);
	}
	
	protected void updateVerticalPosition()
    {
        SceneNode dolphinN = this.getEngine().getSceneManager().getSceneNode("myDolphin1Node");
        
        SceneNode tessN = this.getEngine().getSceneManager().getSceneNode("tessN");
        
        Tessellation tessE = ((Tessellation) tessN.getAttachedObject("tessE"));
        
        Vector3 worldAvatarPosition = dolphinN.getWorldPosition();
        Vector3 localAvatarPosition = dolphinN.getLocalPosition();
        Vector3 newAvatarPosition = Vector3f.createFrom(localAvatarPosition.x(), 
                                                        tessE.getWorldHeight(worldAvatarPosition.x(), worldAvatarPosition.z()),
                                                        localAvatarPosition.z());
        
        dolphinN.setLocalPosition(newAvatarPosition);
            
    }
	
	private void runScript(File scriptFile)
	 { try
	 { FileReader fileReader = new FileReader(scriptFile);
	 jsEngine.eval(fileReader);
	 fileReader.close();
	 }
	 catch (FileNotFoundException e1)
	 { System.out.println(scriptFile + " not found " + e1); }
	 catch (IOException e2)
	 { System.out.println("IO problem with " + scriptFile + e2); }
	 catch (ScriptException e3)
	 { System.out.println("Script Exception in " + scriptFile + e3); }
	 catch (NullPointerException e4)
	 { System.out.println ("Null ptr exception reading " + scriptFile + e4); }
	 }
	
	public class ColorAction extends AbstractInputAction
	{ 
	    private SceneManager sm;

	    public ColorAction(SceneManager s) { 
	    	sm = s; 
	    } // constructor

	    public void performAction(float time, Event e)

	    { //cast the engine so it supports invoking functions
	    	System.out.println("Hello");
	    	Invocable invocableEngine = (Invocable) jsEngine ;
	        //get the light to be updated
	    	
	        //Light lgt = sm.getLight("testLamp1");
	        Light lgt = getEngine().getSceneManager().getLight("testLamp1");

	        // invoke the script function
	        try
	        { 
	            invocableEngine.invokeFunction("updateAmbientColor", lgt); 
	        } catch (ScriptException e1) { 
	            System.out.println("ScriptException in " + scriptFile3 + e1); 
	        } catch (NoSuchMethodException e2) { 
	            System.out.println("No such method in " + scriptFile3 + e2); 
	        } catch (NullPointerException e3) { 
	            System.out.println ("Null ptr exception reading " + scriptFile3 + e3); }
	    } 
	}
	
	public void setIsConnected(boolean b){
		isClientConnected = b;
	}
	
	public Vector3 getPlayerPosition()
	{ 
		SceneNode dolphinN = sm.getSceneNode("myDolphin1	Node");
		return dolphinN.getWorldPosition();
	}
	
	public void addGhostAvatarToGameWorld(GhostAvatar avatar) throws IOException
	{ 
		if (avatar != null)
		{ 
			Entity ghostE = sm.createEntity("ghost", "bread.obj");
			ghostE.setPrimitive(Primitive.TRIANGLES);
			SceneNode ghostN = sm.getRootSceneNode().
			createChildSceneNode(avatar.getID().toString());
			ghostN.attachObject(ghostE);
			ghostN.setLocalPosition(0.0f, 0.0f, 2.0f);
			avatar.setNode(ghostN);
			avatar.setEntity(ghostE);
			//avatar.setPosition(node’s position... maybe redundant);
		} 
	}
	
	public void removeGhostAvatarFromGameWorld(GhostAvatar avatar)
	{ 
		if(avatar != null) gameObjectsToRemove.add(avatar.getID());
	}
	
	protected void processNetworking(float elapsTime)
	{ // Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
		//remove ghost avatars for players who have left the game
		Iterator<UUID> it = gameObjectsToRemove.iterator();
		while(it.hasNext())
		{ 
			sm.destroySceneNode(it.next().toString());
		}
		gameObjectsToRemove.clear();
	}
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{ // for leaving the game... need to attach to an input device
		@Override
		public void performAction(float time, Event evt)
		{ 
			if(protClient != null && isClientConnected == true)
			{ 
				protClient.sendByeMessage();
			} 
		} 
	}
	
	@Override
	protected void update(Engine engine)
	{
		rs = (GL4RenderSystem) engine.getRenderSystem();
		int height = topViewport.getActualHeight();
		
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		scoreStr1 = Integer.toString(score1);

		dispStr1 = "PLAYER 1: Current time: " + elapsTimeStr + " Score: " + scoreStr1;
		rs.setHUD(dispStr1, 15, (height +20));

		orbitController1.updateCameraPosition();
		orbitController2.updateCameraPosition();
		im.update(elapsTime);
		updateVerticalPosition();
		//processNetworking(elapsTime);
	}
}