package fr.utln.jmonkey.tutorials.beginner;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

import static java.lang.Math.random;

/**
 * Example 12 - how to give objects physical properties so they bounce and fall.
 * @author base code by double1984, updated by zathras
 */
public class HelloPhysics extends SimpleApplication {

    public static void main(String args[]) {
        HelloPhysics app = new HelloPhysics();
        app.start();
    }

    /** Prepare the Physics Application State (jBullet) */
    private BulletAppState bulletAppState;


    /** Prepare Materials */
    private Material wall_mat;

    private Material stone_mat;
    private Material floor_mat;

    private Material strat;

    /** Prepare geometries for bricks and cannonballs. */
    private static final Box    box;
    private static final Sphere sphere;
    private static final Box    floor;
    private static final Box    stra;

    /** dimensions used for bricks and wall */
    private static final float brickLength = 0.3f;
    private static final float brickWidth  = 0.3f;
    private static final float brickHeight = 0.3f;
    private static int nombreAleatoire = 4 + (int)(Math.random() * 5);
    private static int nMur = nombreAleatoire*7;

    private Material[] wall_mats = new Material[nMur];

    static {
        /** Initialize the cannon ball geometry */
        sphere = new Sphere(32, 32, 0.4f, true, false);
        sphere.setTextureMode(TextureMode.Projected);
        /** Initialize the brick geometry */
        box = new Box(brickLength, brickHeight, brickWidth);
        box.scaleTextureCoordinates(new Vector2f(1f, 1f));
        /** Initialize the floor geometry */
        floor = new Box(10f, 0.1f, 5f);
        stra = new Box(5f, 0.05f, 4f);
        floor.scaleTextureCoordinates(new Vector2f(3, 6));
    }

    @Override
    public void simpleInitApp() {
        /** Set up Physics Game */
        bulletAppState = new BulletAppState();
        //bulletAppState.setDebugEnabled(true);
        stateManager.attach(bulletAppState);

        /** Configure cam to look at scene */
        cam.setLocation(new Vector3f(0, 4f, 6f));
        cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);
        /** Initialize the scene, materials, inputs, and physics space */
        initInputs();
        initMaterials();
        initWall();
        initFloor();
        initCrossHairs();
        BitmapText annance = new BitmapText(guiFont);
        annance.setSize(guiFont.getCharSet().getRenderedSize());

        annance.setText("essayer de trouver un mot de "+nombreAleatoire+" de lettres");
        annance.setLocalTranslation(700, 1000, 0);
        guiNode.attachChild(annance);
    }

    /** Add InputManager action: Left click triggers shooting. */
    private void initInputs() {
        inputManager.addMapping("shoot",
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "shoot");
    }

    /**
     * Every time the shoot action is triggered, a new cannon ball is produced.
     * The ball is set up to fly from the camera position in the camera direction.
     */
    final private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("shoot") && !keyPressed) {
                makeCannonBall();
            }
        }
    };
    /** Initialize the materials used in this scene. */
    public void initMaterials() {
        TextureKey key;
        Texture tex;
        for(int i=0; i<nMur; i++){
            wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            int lettreAlea =  (int)(Math.random() * 20);
            key = new TextureKey("Textures/Lettres/"+lettreAlea+".jpg");
            key.setGenerateMips(true);
            tex = assetManager.loadTexture(key);
            wall_mat.setTexture("ColorMap", tex);

            wall_mats[i] = wall_mat;
        }
        stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);

        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key3 = new TextureKey("Textures/bb.jpg");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(WrapMode.Repeat);
        floor_mat.setTexture("ColorMap", tex3);
    }

    /** Make a solid floor and add it to the scene. */
    public void initFloor() {
        Geometry floor_geo = new Geometry("Floor", floor);
        floor_geo.setMaterial(floor_mat);
        floor_geo.setLocalTranslation(0, -0.1f, 0);
        this.rootNode.attachChild(floor_geo);
        /* Make the floor physical with mass 0.0f! */
        RigidBodyControl floor_phy = new RigidBodyControl(0.0f);
        floor_geo.addControl(floor_phy);
        bulletAppState.getPhysicsSpace().add(floor_phy);
    }

    /** This loop builds a wall out of individual bricks. */
    public void initWall() {
        float startX = brickLength / 4;
        float height = 0;
        int tmp = 0 ;
        int n = nMur /6 ;
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < n; i++) {
                Vector3f vt = new Vector3f(i * brickLength * 2 + startX, brickHeight + height, 0);
                makeBrick(vt, wall_mats[tmp]);
                tmp ++ ;
            }
            startX = -startX;
            height += 2 * brickHeight;
        }
    }

    /** Creates one physical brick. */
    private void makeBrick(Vector3f loc, Material mat) {
        /** Create a brick geometry and attach to scene graph. */
        Geometry brick_geo = new Geometry("brick", box);
        brick_geo.setMaterial(mat);
        rootNode.attachChild(brick_geo);
        /** Position the brick geometry  */
        brick_geo.setLocalTranslation(loc);
        /* Make brick physical with a mass > 0. */
        //RigidBodyControl brick_phy = new RigidBodyControl(2f);
        /** Add physical brick to physics space. */
       // brick_geo.addControl(brick_phy);
        //bulletAppState.getPhysicsSpace().add(brick_phy);
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
        /* Make the ball physical with a mass > 0.0f */
        RigidBodyControl ball_phy = new RigidBodyControl(1f);
        /** Add physical ball to physics space. */
        ball_geo.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);
        /* Accelerate the physical ball to shoot it. */
        ball_phy.setLinearVelocity(cam.getDirection().mult(25));
    }

    /** A plus sign used as crosshairs to help the player with aiming.*/
    protected void initCrossHairs() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");        // fake crosshairs :)
        ch.setLocalTranslation( // center
                settings.getWidth() / 2,
                settings.getHeight() / 2, 0);
        guiNode.attachChild(ch);
    }
}