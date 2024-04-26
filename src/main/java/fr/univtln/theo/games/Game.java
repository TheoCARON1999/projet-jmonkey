package fr.univtln.theo.games;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
import de.lessvoid.nifty.screen.DefaultScreenController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * This is the Main Class of your Game.
 */
public class Game extends SimpleApplication implements ActionListener, PhysicsCollisionListener {
    private Nifty nifty;
    private boolean isGameRunning = false;
    private boolean isPvE = false;

    private Spatial player;
    private Spatial player2;
    private Spatial toRestart;
    private Spatial lastPlayerTouched;
    private Node shootables;
    final private Vector3f walkDirection = new Vector3f();
    final private Vector3f walkDirection2 = new Vector3f();
    private Vector3f origin;

    private boolean left = false, right = false, up = false, down = false;
    private boolean left2 = false, right2 = false, up2 = false, down2 = false;

    private ScoreManager scoreManager;
    //private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    /** Prepare the Physics Application State (jBullet) */
    private BulletAppState bulletAppState;
    private AIPlayer aiplayer;

    /** Prepare Materials */
    private Material wall_mat;
    private Material stone_mat;
    private Material red_mat;
    private Material blue_mat;
    private Material black_mat;
    private Material yellow_mat;
    private Material orange_mat;
    private  Material white_mat;

    /** Prepare primitives geometries for the objects of the game. */
    private static final Box    box;
    private static final Box    wall;
    private static final Sphere sphere;
    private static final Sphere sphereMesh;
    private static final Cylinder cyl1;
    private static final Cylinder cyl2;
    private static final Cylinder palet;
    private static final Box    blackBox;
    private Geometry mark;

    /** dimensions used for bricks and wall */
    private static final float brickLength = 0.48f;
    private static final float brickWidth  = 0.24f;
    private static final float brickHeight = 0.12f;

    private final float respawnDelay = 0.5f;
    private float collisionDelay = 0.5f;
    private float time = 0;
    private float paletSpeed = 1f;
    private float playerSpeed = 10f;
    private float aiSpeed = 10f;
    private float paletMaxSpeed = 25f;

    private Geometry geoCyl1;
    private Geometry geoCyl2;
    private Geometry geoPalet;
    private Spatial airHockeyTable;

    private List<Bonus> activeBonuses = new ArrayList<>();
    private String[] bonusTypes = {"Speed", "Size", "Goal"};

    static {
        /** Initialize the cannon ball geometry */
        sphere = new Sphere(32, 32, 0.4f, true, false);
        sphere.setTextureMode(Sphere.TextureMode.Projected);
        box = new Box(brickLength, brickHeight, brickWidth);

        blackBox = new Box(8, 2, 4);
        wall = new Box(8f, 2f, 0.1f);wall.scaleTextureCoordinates(new Vector2f(1f, .5f));
        cyl1 = new Cylinder(2, 10, 3, 1, true);
        cyl2 = new Cylinder(2, 10, 3, 1, true);
        palet = new Cylinder(4, 14, 2.2f, 1, true);
        sphereMesh = new Sphere(20, 20, 0.5f, true, false);
    }

    public static void main(String[] args) {
        Game app = new Game();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        // Désactive le mouvement de la caméra par défaut
        flyCam.setEnabled(false);
        flyCam.setDragToRotate(true);

        // --- add a main menu ---
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();

        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");

        nifty.addScreen("start", new ScreenBuilder("start") {{
            controller(new MyScreenController(Game.this));
            layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
                panel(new PanelBuilder("panel_mid") {{
                    childLayoutVertical();
                    alignCenter();
                    height("50%");
                    width("75%");

                    // Ajout du titre
                    control(new LabelBuilder("Title", "Air Hockey Table") {{
                        alignCenter();
                        valignCenter();
                        height("40%");
                        width("100%");
                        font("Interface/Fonts/Default.fnt");
                    }});
                    // Ajout d'un espace
                    panel(new PanelBuilder() {{
                        height("5%");
                    }});

                    control(new ButtonBuilder("PvPButton", "PvP") {{
                        alignCenter();
                        valignCenter();
                        height("15%");
                        width("50%");
                        interactOnClick("startPvPGame()");
                    }});
                    control(new ButtonBuilder("PvEButton", "PvE") {{
                        alignCenter();
                        valignCenter();
                        height("15%");
                        width("50%");
                        interactOnClick("startPvEGame()");
                    }});
                    control(new ButtonBuilder("QuitButton", "Quit") {{
                        alignCenter();
                        valignCenter();
                        height("15%");
                        width("50%");
                        interactOnClick("quitGame()");
                    }});
                }});
            }});
        }}.build(nifty));

        nifty.gotoScreen("start");

        viewPort.addProcessor(niftyDisplay);

        nifty.addScreen("empty", new ScreenBuilder("empty") {{
            controller(new DefaultScreenController());
            layer(new LayerBuilder("background") {{
                childLayoutVertical();
            }});
        }}.build(nifty));



        //--- add a score manager ---
        scoreManager = new ScoreManager(guiNode, guiFont, ((float) settings.getWidth() /2)-300, 100, assetManager);

        /** Set up Physics */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.setDebugEnabled(true);
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
        System.out.println("Physics Space accuracy: " + bulletAppState.getPhysicsSpace().getAccuracy());
        // Tune physics space accuracy
        bulletAppState.getPhysicsSpace().setAccuracy(0.005f);


        shootables = new Node("Shootables");


        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(50);

    }

    public void InitGame() {
        setUpLight();
        initMaterials();
        initMark();

        Spatial sceneModel = assetManager.loadModel("Scenes/town/main.scene");
        sceneModel.setLocalScale(2f);
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
        RigidBodyControl landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(landscape);


        Geometry geoRedBox = new Geometry("RedBox", box);
        geoRedBox.setMaterial(red_mat);
        Geometry geoBlueBox = new Geometry("BlueBox", box);
        geoBlueBox.setMaterial(blue_mat);
        // Table de Air Hockey
        Geometry geoBlackBox = new Geometry("BlackBox", blackBox);
        geoBlackBox.setMaterial(black_mat);

        // pushers et palet
        geoCyl1 = new Geometry("Cylinder1", cyl1);
        geoCyl1.scale(0.1f);
        geoCyl1.rotate(1.62f, 0, 0); // (float) Math.PI/2, 0, 0
        geoCyl1.setMaterial(red_mat);
        geoCyl2 = new Geometry("Cylinder2", cyl2);
        geoCyl2.scale(0.1f);
        geoCyl2.rotate(1.62f, 0, 0); // (float) Math.PI/2, 0, 0
        geoCyl2.setMaterial(blue_mat);
        geoPalet = new Geometry("palet", palet);
        geoPalet.scale(0.1f);
        geoPalet.rotate(1.62f, 0, 0); // (float) Math.PI/2, 0, 0
        geoPalet.setMaterial(yellow_mat);
        geoCyl1.setUserData("player", "1");geoCyl1.setUserData("group", "player");
        geoCyl2.setUserData("player", "2");geoCyl2.setUserData("group", "player");

        // Localisation visuelle
        geoBlueBox.setLocalTranslation(10, 2, -2);
        geoRedBox.setLocalTranslation(10, 4, 1);
        geoBlackBox.setLocalTranslation(10, 3, 7);
        geoCyl1.setLocalTranslation(geoBlackBox.getLocalTranslation().add(-blackBox.xExtent*0.8f, blackBox.yExtent, 0));
        geoCyl2.setLocalTranslation(geoBlackBox.getLocalTranslation().add(blackBox.xExtent*0.8f, blackBox.yExtent, 0));
        geoPalet.setLocalTranslation(geoBlackBox.getLocalTranslation().add(0, blackBox.yExtent, 0));

        // --- Boites de collisions des buts ---
        Node collisionNode1 = new Node("CollisionNode1");
        BoxCollisionShape boxCollisionShape1 = new BoxCollisionShape(new Vector3f(0.1f, 1f, blackBox.zExtent));
        collisionNode1.setLocalTranslation(geoBlackBox.getLocalTranslation().add(-blackBox.xExtent, blackBox.yExtent*1.5f, 0));
        RigidBodyControl collisionNodeRigidBody1 = new RigidBodyControl(boxCollisionShape1, 0);
        collisionNode1.addControl(collisionNodeRigidBody1);
        bulletAppState.getPhysicsSpace().add(collisionNodeRigidBody1);
        shootables.attachChild(collisionNode1);
        Node collisionNode2 = new Node("CollisionNode2");
        BoxCollisionShape boxCollisionShape2 = new BoxCollisionShape(new Vector3f(0.1f, 1f, blackBox.zExtent));
        collisionNode2.setLocalTranslation(geoBlackBox.getLocalTranslation().add(blackBox.xExtent, blackBox.yExtent*1.5f, 0));
        RigidBodyControl collisionNodeRigidBody2 = new RigidBodyControl(boxCollisionShape2, 0);
        collisionNode2.addControl(collisionNodeRigidBody2);
        bulletAppState.getPhysicsSpace().add(collisionNodeRigidBody2);
        shootables.attachChild(collisionNode2);
        System.out.println("taille BlackBox : " + blackBox.xExtent + " " + blackBox.yExtent + " " + blackBox.zExtent);
        System.out.println("taille getBlackBox : " + blackBox.getXExtent() + " " + blackBox.getYExtent() + " " + blackBox.getZExtent());
        System.out.println("taille geoBlackBox : " + geoBlackBox.getLocalScale());
        System.out.println("taille boxCollisionShape1 : " + boxCollisionShape1.getScale());
        System.out.println("taille boxCollisionShape2 : " + boxCollisionShape2.getScale());

        // --- Ajout du plafond invisible ---
        Node ceiling = new Node("Ceiling");
        BoxCollisionShape ceilingCollisionShape = new BoxCollisionShape(new Vector3f(blackBox.xExtent, 0.1f, blackBox.zExtent));
        ceiling.setLocalTranslation(geoBlackBox.getLocalTranslation().add(0, blackBox.yExtent*2, 0));
        RigidBodyControl ceilingRigidBody = new RigidBodyControl(ceilingCollisionShape, 0);
        ceiling.addControl(ceilingRigidBody);
        bulletAppState.getPhysicsSpace().add(ceilingRigidBody);
        shootables.attachChild(ceiling);

        // --- Ajout des murs ---
        makeWall(geoBlackBox.getLocalTranslation().add(0, blackBox.yExtent, -blackBox.zExtent));
        makeWall(geoBlackBox.getLocalTranslation().add(0, blackBox.yExtent, blackBox.zExtent));

        // --- Ajout d'une zone neutre ---
        Node neutralZone = new Node("NeutralZone");
        BoxCollisionShape neutralZoneCollisionShape = new BoxCollisionShape(new Vector3f(0.1f, 1f, blackBox.zExtent));
        neutralZone.setLocalTranslation(geoBlackBox.getLocalTranslation().add(0, blackBox.yExtent, 0));
        RigidBodyControl neutralZoneRigidBody = new RigidBodyControl(neutralZoneCollisionShape, 0);
        neutralZoneRigidBody.setCollisionGroup(2);
        neutralZoneRigidBody.setCollideWithGroups(2);
        neutralZone.addControl(neutralZoneRigidBody);
        bulletAppState.getPhysicsSpace().add(neutralZoneRigidBody);
        shootables.attachChild(neutralZone);
        // --- Ajout d'une blanche de départ pour s'imboliser la position de la zone neutre ---
        Geometry geoNeutralZone = new Geometry("NeutralZone", new Box(0.1f, 0.1f, blackBox.zExtent));
        geoNeutralZone.setMaterial(white_mat);
        geoNeutralZone.setLocalTranslation(neutralZone.getLocalTranslation());
        shootables.attachChild(geoNeutralZone);


        // Corps rigides (pour la physique/contrôle des collisions
        RigidBodyControl redBoxRigidBody = new RigidBodyControl(0.5f);
        RigidBodyControl blueBoxRigidBody = new RigidBodyControl(0.5f);
        RigidBodyControl blackBoxRigidBody = new RigidBodyControl(0f);
        RigidBodyControl cyl1RigidBody = new RigidBodyControl(0.4f);
        RigidBodyControl cyl2RigidBody = new RigidBodyControl(0.4f);
        RigidBodyControl paletRigidBody = new RigidBodyControl(0.2f);

        // Configuration des corps rigides des pushers
        cyl1RigidBody.setCollisionGroup(2);
        cyl1RigidBody.addCollideWithGroup(1);
        cyl2RigidBody.setCollisionGroup(2);
        cyl2RigidBody.addCollideWithGroup(1);

        // bind the geometry to the physical object
        geoRedBox.addControl(redBoxRigidBody);
        geoBlueBox.addControl(blueBoxRigidBody);
        geoBlackBox.addControl(blackBoxRigidBody);
        geoCyl1.addControl(cyl1RigidBody);
        geoCyl2.addControl(cyl2RigidBody);
        geoPalet.addControl(paletRigidBody);

        cyl1RigidBody.setAngularFactor(0);
        cyl2RigidBody.setAngularFactor(0);

        // Add the geometry to the scene
        rootNode.attachChild(shootables);
        shootables.attachChild(geoRedBox);
        shootables.attachChild(geoBlueBox);
        shootables.attachChild(geoBlackBox);
        shootables.attachChild(geoCyl1);
        shootables.attachChild(geoCyl2);
        shootables.attachChild(geoPalet);


        // Add the geometry to the physics space
        bulletAppState.getPhysicsSpace().add(player);
        bulletAppState.getPhysicsSpace().add(redBoxRigidBody);
        bulletAppState.getPhysicsSpace().add(blueBoxRigidBody);
        bulletAppState.getPhysicsSpace().add(blackBoxRigidBody);
        bulletAppState.getPhysicsSpace().add(cyl1RigidBody);
        bulletAppState.getPhysicsSpace().add(cyl2RigidBody);
        bulletAppState.getPhysicsSpace().add(paletRigidBody);

        airHockeyTable = geoBlackBox;

        player = geoCyl1;
        origin = geoBlackBox.getLocalTranslation().add(0, blackBox.yExtent, 0);

        System.out.println("getName : " + player.getName());
        System.out.println("getControl : " + player.getControl(RigidBodyControl.class));
        System.out.println("getCollisionGroup : " + player.getControl(RigidBodyControl.class).getCollisionGroup());
        System.out.println("getCollideWithGroups : " + player.getControl(RigidBodyControl.class).getCollideWithGroups());
        System.out.println("getLocalScale : " + player.getLocalScale());
        System.out.println("getLocalRotation : " + player.getLocalRotation());
        System.out.println("getLocalTranslation : " + player.getLocalTranslation());
        System.out.println("getWorldScale : " + player.getWorldScale());
        System.out.println("getWorldRotation : " + player.getWorldRotation());
        System.out.println("getWorldTranslation : " + player.getWorldTranslation());
        System.out.println("getWorldBound : " + player.getWorldBound());
        System.out.println("getWorldTransform : " + player.getWorldTransform());

        System.out.println("\ngetWorldBound : " + geoBlackBox.getWorldBound());
        System.out.println("getWorldBound : " + geoCyl1.getWorldBound());
        System.out.println("getWorldBound : " + geoCyl2.getWorldBound());
        System.out.println("getWorldBound : " + geoPalet.getWorldBound());
        System.out.println("getWorldBound : " + collisionNode1.getWorldBound());
        System.out.println("getWorldBound : " + collisionNode2.getWorldBound());
        System.out.println("getWorldBound : " + neutralZone.getWorldBound());
        System.out.println("getWorldBound : " + sceneModel.getWorldBound());

        System.out.println("\ngetName : " + geoPalet.getName());
        System.out.println("getControl : " + geoPalet.getControl(RigidBodyControl.class));
        System.out.println("getCollisionGroup : " + geoPalet.getControl(RigidBodyControl.class).getCollisionGroup());
        System.out.println("getCollideWithGroups : " + geoPalet.getControl(RigidBodyControl.class).getCollideWithGroups());
        System.out.println("\ngetName : " + neutralZone.getName());
        System.out.println("getControl : " + neutralZone.getControl(RigidBodyControl.class));
        System.out.println("getCollisionGroup : " + neutralZone.getControl(RigidBodyControl.class).getCollisionGroup());
        System.out.println("getCollideWithGroups : " + neutralZone.getControl(RigidBodyControl.class).getCollideWithGroups());

        // --- glowing effect ---
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        BloomFilter bloom = new BloomFilter(BloomFilter.GlowMode.Objects);
        fpp.addFilter(bloom);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.setColor("Color", ColorRGBA.Green); // Set the color of the material
        mat.setColor("GlowColor", ColorRGBA.Magenta); // Set the glow color of the material
        //mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.FrontAndBack);
        // Création de la géométrie
        Box box = new Box(0.1f, 1f, blackBox.zExtent); // Utilisez les dimensions souhaitées
        Geometry boxGeometry = new Geometry("Box", box);
        // Set the material to wireframe
        boxGeometry.setMaterial(mat);
        // Ajout de la géométrie au noeud de collision
        collisionNode1.attachChild(boxGeometry);
        collisionNode2.attachChild(boxGeometry.clone());

        isGameRunning = true;
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

    /** We over-write some navigational key mappings here, so we can
     * add physics-controlled walking and jumping: */
    private void setUpKeysPvE() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE));


        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Shoot");
        inputManager.addListener(this, "Pause");

        inputManager.addMapping("Left2", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right2", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Up2", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Down2", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addListener(this, "Left2");
        inputManager.addListener(this, "Right2");
        inputManager.addListener(this, "Up2");
        inputManager.addListener(this, "Down2");
    }

    public void setUpKeysPvP() {
        inputManager.addMapping("Left2", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Right2", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("Up2", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Down2", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addListener(this, "Left2");
        inputManager.addListener(this, "Right2");
        inputManager.addListener(this, "Up2");
        inputManager.addListener(this, "Down2");
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_G));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        inputManager.addListener(this, "Shoot");
        inputManager.addListener(this, "Pause");

        //touche de secour
        inputManager.addMapping("Left2", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("Right2", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Up2", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Down2", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addListener(this, "Left2");
        inputManager.addListener(this, "Right2");
        inputManager.addListener(this, "Up2");
        inputManager.addListener(this, "Down2");
    }

    /** These are our custom actions triggered by key presses.
     * We do not walk yet, we just keep track of the direction the user pressed. */
    @Override
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
            if (value) { left = true; } else { left = false; }
        } else if (binding.equals("Right")) {
            if (value) { right = true; } else { right = false; }
        } else if (binding.equals("Up")) {
            if (value) { up = true; } else { up = false; }
        } else if (binding.equals("Down")) {
            if (value) {
                down = true;
            } else {
                down = false;
            }
        } else if (binding.equals("Left2")) {
            if (value) { left2 = true; } else { left2 = false; }
        } else if (binding.equals("Right2")) {
            if (value) { right2 = true; } else { right2 = false; }
        } else if (binding.equals("Up2")) {
            if (value) { up2 = true; } else { up2 = false; }
        } else if (binding.equals("Down2")) {
            if (value) {
                down2 = true;
            } else {
                down2 = false;
            }
        //} else if (binding.equals("Jump")) {
        //    player.jump();
        } else if (binding.equals("Pause") && !value) {
            if (isGameRunning) {
                isGameRunning = false;
                nifty.gotoScreen("start");
            } else {
                isGameRunning = true;
                nifty.gotoScreen("empty");
            }
        } else if (binding.equals("Shoot") && !value) {
            CollisionResults results = new CollisionResults();
            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            shootables.collideWith(ray, results);
            if (results.size() > 0) {
                // The closest collision point is what was truly hit:
                CollisionResult closest = results.getClosestCollision();
                // Let's interact - we mark the hit with a red dot.
                mark.setLocalTranslation(closest.getContactPoint());
                rootNode.attachChild(mark);
                // And we add a new bullet.
                makeCannonBall();
            }
        }
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        if (collisionDelay > 0) {
            return;
        }
        Spatial nodeA = event.getNodeA();
        Spatial nodeB = event.getNodeB();
        /*
        System.out.println("nodeA User Data : " + nodeA.getUserDataKeys() + " test : " +
                !nodeA.getUserDataKeys().isEmpty() + " Velocity : " + nodeA.getControl(RigidBodyControl.class).getLinearVelocity());
        System.out.println("nodeB User Data : " + nodeB.getUserDataKeys() + " test : " +
                !nodeB.getUserDataKeys().isEmpty() + " Velocity : " + nodeB.getControl(RigidBodyControl.class).getLinearVelocity());
        */
        if(nodeA != null && nodeB != null) {
            if((nodeA.getName().equals("CollisionNode1") || nodeB.getName().equals("CollisionNode1")) &&
                    (nodeA.getName().equals("palet") || nodeB.getName().equals("palet"))) {
                System.out.println("Collision between " + nodeA.getName() + " and " + nodeB.getName());
                if (scoreManager.incrementScore2() > ScoreManager.MAX_SCORE) {
                    // On réinitialise les scores
                    scoreManager.resetScores();
                    if (scoreManager.incrementLevel() > ScoreManager.MAX_LEVEL) {
                        // On a un gagnant
                        System.out.println("Player 2 wins !");
                        scoreManager.setWinnerText("Player 2 wins !");

                    }
                }
                if (nodeA.getName().equals("palet")) {
                    toRestart = event.getNodeA();
                } else {
                    toRestart = event.getNodeB();
                }
                bulletAppState.getPhysicsSpace().remove(toRestart.getControl(RigidBodyControl.class));
                //executorService.schedule(() -> enqueue(toRestart::removeFromParent), 1, TimeUnit.SECONDS);
                time = 0; // Le délai avant la suppression du noeud

                // Réinitialisez le délai après une collision
                collisionDelay = 0.5f; // Ignorez les collisions pendant 0.5 secondes

            } else if((nodeA.getName().equals("CollisionNode2") || nodeB.getName().equals("CollisionNode2")) &&
                    (nodeA.getName().equals("palet") || nodeB.getName().equals("palet"))) {
                System.out.println("Collision between " + nodeA.getName() + " and " + nodeB.getName());
                if (scoreManager.incrementScore1() > ScoreManager.MAX_SCORE) {
                    // On réinitialise les scores
                    scoreManager.resetScores();
                    if (scoreManager.incrementLevel() > ScoreManager.MAX_LEVEL) {
                        // On a un gagnant
                        System.out.println("Player 1 wins !");
                        scoreManager.setWinnerText("Player 1 wins !");

                    }
                    if (isPvE) {
                        aiplayer.increaseAiSpeed(2f);
                    }
                }

                if (nodeA.getName().equals("palet")) {
                    toRestart = event.getNodeA();
                } else {
                    toRestart = event.getNodeB();
                }
                bulletAppState.getPhysicsSpace().remove(toRestart.getControl(RigidBodyControl.class));
                //executorService.schedule(() -> enqueue(toRestart::removeFromParent), 1, TimeUnit.SECONDS);
                time = 0; // Le délai avant la suppression du noeud

                // Réinitialisez le délai après une collision
                collisionDelay = 0.5f; // Ignorez les collisions pendant 0.5 secondes



            } else if ((nodeA.getName().equals("palet") || nodeB.getName().equals("palet")) &&
                    (nodeA.getName().equals("Cylinder1") || nodeB.getName().equals("Cylinder1") ||
                            nodeA.getName().equals("Cylinder2") || nodeB.getName().equals("Cylinder2"))) {
                // Augmentation de la vitesse du palet dans la direction de la collision
                if (nodeA.getName().equals("palet")) {
                    lastPlayerTouched = nodeB;
                    nodeA.getControl(RigidBodyControl.class).setLinearVelocity(
                            nodeA.getControl(RigidBodyControl.class).getLinearVelocity().add(
                                    nodeB.getControl(RigidBodyControl.class).getLinearVelocity().mult(paletSpeed)));
                    if (nodeA.getControl(RigidBodyControl.class).getLinearVelocity().length() > paletMaxSpeed) {
                        nodeA.getControl(RigidBodyControl.class).setLinearVelocity(
                                nodeA.getControl(RigidBodyControl.class).getLinearVelocity().normalize().mult(paletMaxSpeed));
                    }
                } else {
                    lastPlayerTouched = nodeA;
                    nodeB.getControl(RigidBodyControl.class).setLinearVelocity(
                            nodeB.getControl(RigidBodyControl.class).getLinearVelocity().add(
                                    nodeA.getControl(RigidBodyControl.class).getLinearVelocity().mult(paletSpeed)));
                    if (nodeB.getControl(RigidBodyControl.class).getLinearVelocity().length() > paletMaxSpeed) {
                        nodeB.getControl(RigidBodyControl.class).setLinearVelocity(
                                nodeB.getControl(RigidBodyControl.class).getLinearVelocity().normalize().mult(paletMaxSpeed));
                    }
                }
                //System.out.println("!!!\nVelocity : " + nodeA.getControl(RigidBodyControl.class).getLinearVelocity());
                //System.out.println("Velocity : " + nodeB.getControl(RigidBodyControl.class).getLinearVelocity() + "\n!!!");

            } else if (nodeA.getName().equals("palet") && nodeB.getName().equals("Bonus")) {
                System.out.println("Collision between " + nodeA.getName() + " and " + nodeB.getName());
                Bonus bonus = nodeB.getUserData("bonus");
                if (bonus.getClass().equals(GoalBonus.class)) {
                    Vector3f goalPosition; // Position du but adverse
                    if (lastPlayerTouched.getName().equals("Cylinder1")) {
                        goalPosition = new Vector3f(airHockeyTable.getLocalTranslation().x + blackBox.xExtent, airHockeyTable.getLocalTranslation().y, airHockeyTable.getLocalTranslation().z);
                    } else {
                        goalPosition = new Vector3f(airHockeyTable.getLocalTranslation().x - blackBox.xExtent, airHockeyTable.getLocalTranslation().y, airHockeyTable.getLocalTranslation().z);
                    }

                    Vector3f directionToGoal = goalPosition.subtract(geoPalet.getLocalTranslation()).normalizeLocal();
                    geoPalet.getControl(RigidBodyControl.class).setLinearVelocity(directionToGoal.mult(paletSpeed*10f));
                } else {
                    bonus.activate(lastPlayerTouched);
                    activeBonuses.add(bonus);
                }
                bonus.removeFromScene();
            } else if (nodeB.getName().equals("palet") && nodeA.getName().equals("Bonus")) {
                System.out.println("Collision between " + nodeA.getName() + " and " + nodeB.getName());
                Bonus bonus = nodeA.getUserData("bonus");
                if (bonus.getClass().equals(GoalBonus.class)) {
                    Vector3f goalPosition; // Position du but adverse
                    if (lastPlayerTouched.getName().equals("Cylinder1")) {
                        goalPosition = new Vector3f(airHockeyTable.getLocalTranslation().x + blackBox.xExtent, airHockeyTable.getLocalTranslation().y, airHockeyTable.getLocalTranslation().z);
                    } else {
                        goalPosition = new Vector3f(airHockeyTable.getLocalTranslation().x - blackBox.xExtent, airHockeyTable.getLocalTranslation().y, airHockeyTable.getLocalTranslation().z);
                    }

                    Vector3f directionToGoal = goalPosition.subtract(geoPalet.getLocalTranslation()).normalizeLocal();
                    geoPalet.getControl(RigidBodyControl.class).setLinearVelocity(directionToGoal.mult(paletSpeed * 10f));
                } else {
                    bonus.activate(lastPlayerTouched);
                    activeBonuses.add(bonus);
                }
                bonus.removeFromScene();
            }
        }
    }

    /**
     * This is the main event loop--walking happens here.
     * We check in which direction the player is walking by interpreting
     * the camera direction forward (camDir) and to the side (camLeft).
     * The setWalkDirection() command is what lets a physics-controlled player walk.
     * We also make sure here that the camera moves with player.
     */
    @Override
    public void simpleUpdate(float tpf) {
        if (!isGameRunning) {
            return;
        }

        walkDirection.set(0, 0, 0);
        walkDirection2.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(0, 0, -1);
        }
        if (right) {
            walkDirection.addLocal(0, 0, 1);
        }
        if (up) {
            walkDirection.addLocal(1, 0, 0);
        }
        if (down) {
            walkDirection.addLocal(-1, 0, 0);
        }
        //player.move(walkDirection);
        player.getControl(RigidBodyControl.class).setLinearVelocity(walkDirection.mult(playerSpeed));

        if (left2) {
            walkDirection2.addLocal(0, 0, 1);
        }
        if (right2) {
            walkDirection2.addLocal(0, 0, -1);
        }
        if (up2) {
            walkDirection2.addLocal(1, 0, 0);
        }
        if (down2) {
            walkDirection2.addLocal(-1, 0, 0);
        }
        if (!isPvE)
            player2.getControl(RigidBodyControl.class).setLinearVelocity(walkDirection2.mult(playerSpeed));

        // Add the elapsed time to time
        time += tpf;
        if (time > respawnDelay) {
            // Remove the spatial and the physics
            if (toRestart != null) {
                //toRestart.setLocalTranslation(origin); marche pas à cause de son contrôlleur
                toRestart.getControl(RigidBodyControl.class).setPhysicsLocation(origin);
                toRestart.getControl(RigidBodyControl.class).setLinearVelocity(Vector3f.ZERO);
                bulletAppState.getPhysicsSpace().add(toRestart.getControl(RigidBodyControl.class));

                toRestart = null;
            }
            time = 0;
        }

        // Diminuez le délai au fil du temps
        if (collisionDelay > 0) {
            collisionDelay -= tpf;
        }

        // Mise à jour de l'IA
        if (isPvE)
            aiplayer.updateWithAnticipation(tpf);

        // Update bonuses
        Iterator<Bonus> it = activeBonuses.iterator();
        while (it.hasNext()) {
            Bonus bonus = it.next();
            bonus.update(tpf, player); // Assuming 'player' is the Spatial object of the player
            if (!bonus.isActive()) {
                it.remove();
            }
        }

        // Générer aléatoirement des bonus
        if (Math.random() < 0.005 && rootNode.getQuantity() < 10) { // 1% de chance par frame

            Geometry bonusGeometry = new Geometry("Bonus", sphereMesh);
            //bonusGeometry.setMaterial(orange_mat);
            bonusGeometry.setMaterial(assetManager.loadMaterial("Materials/CustomMaterialBonus.j3m"));
            sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
            TangentBinormalGenerator.generate(sphereMesh);           // for lighting effect
            bonusGeometry.setLocalTranslation(randomPositionOnTable());
            // ajout d'un rigidBody
            RigidBodyControl bonusRigidBody = new RigidBodyControl(0);
            bonusRigidBody.setCollisionGroup(4);
            bonusRigidBody.addCollideWithGroup(1);
            bonusGeometry.addControl(bonusRigidBody);

            // Sélectionner un type de bonus aléatoire
            String bonusType = bonusTypes[new Random().nextInt(bonusTypes.length)];

            Bonus bonus;
            switch (bonusType) {
                case "Speed":
                    bonus = new SpeedBonus(10, bonusGeometry, bulletAppState.getPhysicsSpace(), rootNode, 1.5f);
                    bonusGeometry.setUserData("bonus", bonus);
                    break;
                case "Size":
                    bonus = new SizeBonus(20, bonusGeometry, bulletAppState.getPhysicsSpace(), rootNode, 1.2f);
                    bonusGeometry.setUserData("bonus", bonus);
                    break;
                case "Goal":
                    bonus = new GoalBonus(3, bonusGeometry, bulletAppState.getPhysicsSpace(), rootNode, 1);
                    bonusGeometry.setUserData("bonus", bonus);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + bonusType);
            }

            bulletAppState.getPhysicsSpace().add(bonusRigidBody);
            rootNode.attachChild(bonus.getGeometry());
            activeBonuses.add(bonus);
        }
    }
    private Vector3f randomPositionOnTable() {
        float xExtent = blackBox.xExtent;
        float zExtent = blackBox.zExtent;
        float x = (float) (Math.random() * 2 * xExtent - xExtent);
        float z = (float) (Math.random() * 2 * zExtent - zExtent);
        return airHockeyTable.getLocalTranslation().add(new Vector3f(x, blackBox.yExtent*1.2f, z));
    }

    // --- Main Menu methods ---
    public void startPvPGame() {
        setUpKeysPvP();

        System.out.println("PvP Game started");
        nifty.gotoScreen("empty");
        InitGame();
        player2 = geoCyl2;

        /** Configure cam to look at scene */
        cam.setLocation(airHockeyTable.getLocalTranslation().add(0, blackBox.yExtent*10f, -blackBox.zExtent*10f));
        cam.lookAt(airHockeyTable.getLocalTranslation(), Vector3f.UNIT_Y);
        cam.setLocation(cam.getLocation().add(0, 0, blackBox.zExtent*10f));
        cam.lookAt(airHockeyTable.getLocalTranslation(), Vector3f.UNIT_Y);
    }

    public void startPvEGame() {
        setUpKeysPvE();

        System.out.println("PvE Game started");
        nifty.gotoScreen("empty");
        // réactive le dépalcement de la caméra
        flyCam.setEnabled(true);
        // Réactive le mouvement de la caméra
        flyCam.setDragToRotate(false);
        InitGame();
        /** Configure cam to look at scene */
        cam.setLocation(airHockeyTable.getLocalTranslation().add(-blackBox.xExtent*1.6f, 15f, 0));
        cam.lookAt(airHockeyTable.getLocalTranslation(), Vector3f.UNIT_Y);

        aiplayer = new AIPlayer(geoCyl2, geoPalet, aiSpeed);
        isPvE = true;
    }

    public void quitGame() {
        // Code to quit the game goes here
    }
    // --- End of Main Menu methods ---

    /** A red ball that marks the last spot that was "hit" by the "shot". */
    private void initMark() {
        Sphere RedSphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", RedSphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }

    /** Creates one physical cannonball.
     * By default, the ball is accelerated and flies
     * from the camera position in the camera direction.*/
    public void makeCannonBall() {
        /** Create a cannon ball geometry and attach to scene graph. */
        Geometry ball_geo = new Geometry("cannon ball", sphere);
        ball_geo.setMaterial(stone_mat);
        rootNode.attachChild(ball_geo);
        /** Position the cannon ball  */
        ball_geo.setLocalTranslation(cam.getLocation());
        /** Make the ball physical with a mass > 0.0f */
        RigidBodyControl ball_phy = new RigidBodyControl(1f);
        /** Add physical ball to physics space. */
        ball_geo.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);
        /** Accelerate the physical ball to shoot it. */
        ball_phy.setLinearVelocity(cam.getDirection().mult(25));
    }

    /** Initialize the materials used in this scene. */
    public void initMaterials() {
        red_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        red_mat.setColor("Color", ColorRGBA.Red);
        blue_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blue_mat.setColor("Color", ColorRGBA.Blue);
        black_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        black_mat.setColor("Color", ColorRGBA.Black);
        yellow_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        yellow_mat.setColor("Color", ColorRGBA.Yellow);
        //orange_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //orange_mat.setColor("Color", ColorRGBA.Orange);
        white_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        white_mat.setColor("Color", ColorRGBA.White);

        wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        wall_mat.setTexture("ColorMap", tex);

        stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);

    }

    /** Creates one physical wall. */
    private void makeWall(Vector3f loc) {
        /** Create a wall geometry and attach to scene graph. */
        Geometry geoWall = new Geometry("wall", wall);
        geoWall.setMaterial(wall_mat);
        shootables.attachChild(geoWall);
        /** Position the wall geometry  */
        geoWall.setLocalTranslation(loc);
        /** Make wall physical with a mass = 0. */
        RigidBodyControl brick_phy = new RigidBodyControl(0f);
        /** Add physical wall to physics space. */
        geoWall.addControl(brick_phy);
        bulletAppState.getPhysicsSpace().add(brick_phy);
    }
}