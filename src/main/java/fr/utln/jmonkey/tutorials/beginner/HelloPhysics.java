package fr.utln.jmonkey.tutorials.beginner;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
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
    private BulletAppState bulletAppState;
    private Material wall_mat;
    private Material floor_mat;
    private static final Box    box;
    private static final Sphere sphere;
    private static final Box    floor;

    /** dimensions used for bricks and wall */
    private static final float brickLength = 0.28f;
    private static final float brickWidth  = 0.28f;
    private static final float brickHeight = 0.28f;

    private static int Max = 3 ;
    private static int Min = 3 ;
    private static int nombreAleatoire = Min + (int)(Math.random() * ((Max - Min) + 1));
    private static int nMur = nombreAleatoire*10;
    private static int frappe = 0 ;
    private Material[] wall_mats = new Material[nMur];
    private String mot = "";
    private String alph = "abcdefghijklmnopqrstuvwxyz";

    private Geometry[] gemotries = new Geometry[nMur];

    private Vector3f[] vects = new Vector3f[nMur];

    private int[] names = new int[nMur];
    static {
        /** Initialize the cannon ball geometry */
        sphere = new Sphere(32, 32, 0.4f, true, false);
        sphere.setTextureMode(TextureMode.Projected);
        /** Initialize the brick geometry */
        box = new Box(brickLength, brickHeight, brickWidth);
        box.scaleTextureCoordinates(new Vector2f(1f, 1f));
        /** Initialize the floor geometry */
        floor = new Box(10f, 0.1f, 5f);
        // floor.scaleTextureCoordinates(new Vector2f(6, 6));
    }
    private Geometry mark;
    private Node shootables;
    @Override
    public void simpleInitApp() {
        /** Set up Physics Game */
        bulletAppState = new BulletAppState();
        //bulletAppState.setDebugEnabled(true);
        stateManager.attach(bulletAppState);
        shootables = new Node("Shootables");
        rootNode.attachChild(shootables);


        /** Configure cam to look at scene */
        cam.setLocation(new Vector3f(0, 4f, 6f));
        cam.lookAt(new Vector3f(1, 2, 0), Vector3f.UNIT_Y);
        /** Initialize the scene, materials, inputs, and physics space */
        initMark();
        initMaterials();
        initWall();
        initFloor();
        initKeys();
        initCrossHairs();
        BitmapText annance = new BitmapText(guiFont);
        BitmapText motFormé = new BitmapText(guiFont);
        annance.setSize(guiFont.getCharSet().getRenderedSize());
        motFormé.setSize(guiFont.getCharSet().getRenderedSize());
        motFormé.setLocalTranslation(700, 1000, 0);
        annance.setText("essayer de trouver un mot de "+nombreAleatoire+" de lettres dans cette grille de "+nMur+" lettres");
        annance.setLocalTranslation(500, 1000, 0);
        guiNode.attachChild(annance);
    }
    private void initMark() {
        Sphere sphere = new Sphere(20, 20, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture texture = assetManager.loadTexture("Textures/dd.jpg");
        mark_mat.setTexture("ColorMap", texture);
        mark.setMaterial(mark_mat);
    }
    private void initKeys() {
        inputManager.addMapping("Shoot",
                new KeyTrigger(KeyInput.KEY_SPACE), // trigger 1: spacebar
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // trigger 2: left-button click
        inputManager.addListener(actionListener, "Shoot");
    }
    /** Defining the "Shoot" action: Determine what was hit and how to respond. */
    final private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Shoot") && !keyPressed) {
                frappe ++ ;
                // 1. Reset results list.
                CollisionResults results = new CollisionResults();
                // 2. Aim the ray from cam loc to cam direction.
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                // 3. Collect intersections between Ray and Shootables in results list.
                shootables.collideWith(ray, results);
                // 4. Print the results
                if (results.size() > 0) {
                    // For each hit, we know distance, impact point, name of geometry.
                    String hit = results.getCollision(0).getGeometry().getName();
                    int x = Integer.parseInt(hit);
                    mot = mot + alph.charAt(x);
                    if(frappe == nombreAleatoire){

                        if(VerifMot.verif(mot)){
                            ParticleEmitter fire =
                                    new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
                            Material mat_red = new Material(assetManager,
                                    "Common/MatDefs/Misc/Particle.j3md");
                            mat_red.setTexture("Texture", assetManager.loadTexture(
                                    "Effects/Explosion/flame.png"));
                            fire.setMaterial(mat_red);
                            fire.setImagesX(2);
                            fire.setImagesY(2); // 2x2 texture animation
                            fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
                            fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
                            fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
                            fire.setStartSize(1.5f);
                            fire.setEndSize(0.1f);
                            fire.setGravity(0, 0, 0);
                            fire.setLowLife(1f);
                            fire.setHighLife(3f);
                            fire.getParticleInfluencer().setVelocityVariation(0.3f);
                            rootNode.attachChild(fire);

                            ParticleEmitter debris =
                                    new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 10);
                            Material debris_mat = new Material(assetManager,
                                    "Common/MatDefs/Misc/Particle.j3md");
                            debris_mat.setTexture("Texture", assetManager.loadTexture(
                                    "Effects/Explosion/Debris.png"));
                            debris.setMaterial(debris_mat);
                            debris.setImagesX(3);
                            debris.setImagesY(3); // 3x3 texture animation
                            debris.setRotateSpeed(4);
                            debris.setSelectRandomImage(true);
                            debris.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 4, 0));
                            debris.setStartColor(ColorRGBA.White);
                            debris.setGravity(0, 6, 0);
                            debris.getParticleInfluencer().setVelocityVariation(.60f);
                            rootNode.attachChild(debris);
                            debris.emitAllParticles();
                            //mat_candy.setTexture("Texture", assetManager.loadTexture("Textures/vv.jpg")); // chargez la texture de bonbon
                            explose(vects);
                        }
                        else {
                            //mat_candy.setTexture("Texture", assetManager.loadTexture("Textures/ff.jpg")); // chargez la texture de bonbon
                        }

                    }
                }
                // 5. Use the results (we mark the hit object)
                if (results.size() > 0) {
                    // The closest collision point is what was truly hit:
                    CollisionResult closest = results.getClosestCollision();
                    // Let's interact - we mark the hit with a red dot.
                    mark.setLocalTranslation(closest.getContactPoint());
                    rootNode.attachChild(mark);
                } else {
                    // No hits? Then remove the red mark.
                    rootNode.detachChild(mark);
                }
            }
        }
    };
    /** Initialize the materials used in this scene. */
    public void initMaterials() {
        TextureKey key;
        Texture tex;
        for(int i=0; i<nMur; i++){
            int min = 0 ;
            int max = 24 ;
            int lettreAlea = min + (int)(Math.random() * ((max - min) + 1));
            wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            key = new TextureKey("Textures/Lettres/"+lettreAlea+".jpg");
            key.setGenerateMips(true);
            tex = assetManager.loadTexture(key);
            wall_mat.setTexture("ColorMap", tex);
            wall_mats[i] = wall_mat;
            names[i] = lettreAlea ;
        }
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
        float startX = brickLength / 4 - 2;
        float height = 0;
        int tmp = 0 ;
        int n = nMur /6 ;
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < n; i++) {
                Vector3f vt = new Vector3f(i * brickLength * 2 + startX, brickHeight + height, 0);
                makeBrick(vt, wall_mats[tmp], names[tmp], tmp);
                vects[tmp] = vt ;

                tmp ++ ;
            }
            height += 2 * brickHeight;
        }
    }
    /** Creates one physical brick.2 */
    private void makeBrick(Vector3f loc, Material mat, int name, int tmp) {
        Geometry brick_geo = new Geometry(Integer.toString(name), box);
        brick_geo.setMaterial(mat);
        shootables.attachChild(brick_geo);
        brick_geo.setLocalTranslation(loc);
        gemotries[tmp] = brick_geo ;

    }
    private void explose(Vector3f[] vects) {
        int tmp = 0 ;
        int n = nMur /6 ;
        float startX = brickLength / 4 - 2;
        float height = 0;

        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < n; i++) {
                Vector3f vt = new Vector3f(i * brickLength * 2 + startX, brickHeight + height , 1);
                gemotries[tmp].setLocalTranslation(vt);
                RigidBodyControl brick_phy = new RigidBodyControl(2f);
                /** Add physical brick to physics space. */
                gemotries[tmp].addControl(brick_phy);
                bulletAppState.getPhysicsSpace().add(brick_phy);
                /** Add physical brick to physics space. */
                tmp ++ ;
            }
            height += 1.5 * brickHeight;
        }


    }

    /** A plus sign used as crosshairs to help the player with aiming.*/
    private void initCrossHairs() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setColor("Color", ColorRGBA.Black); // changer la couleur en noir
        ch.setText("+"); // croix
        ch.setLocalTranslation( // centrer
                settings.getWidth() / 2 - ch.getLineWidth()/2, settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
        guiNode.attachChild(ch);
    }
}