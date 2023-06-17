package fr.utln.jmonkey.DeliciousWord;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
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
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.font.BitmapFont;

public class DeliciousWord extends SimpleApplication {

    public static void main(String args[]) {
        AppSettings settings = new AppSettings(true);
        settings.setTitle("DeliciousWord");
        DeliciousWord app = new DeliciousWord();
        app.setSettings(settings);
        app.start();
    }
    static float TOTAL_SECOND = 25;
    private AudioNode audio;
    float timeInSecond;
    final static String FORMAT = "Timer:   %.1fs.";
    BitmapText uiText;
    private BulletAppState bulletAppState;
    private Material wall_mat;
    private Material floor_mat;
    private static final Box    box;
    private static  Sphere sphere;
    private static final Box    floor;
    private static final float brickLength = 0.20f;
    private static final float brickWidth  = 0.20f;
    private static final float brickHeight = 0.20f;

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
    private BitmapText ch ;
    private double xm;
    private int ym;
    private int zm ;
    private Geometry mark;
    private Node shootables;
    private int[] names = new int[nMur];
    static {
        box = new Box(brickLength, brickHeight, brickWidth);
        box.scaleTextureCoordinates(new Vector2f(1f, 1f));
        floor = new Box(10f, 0.1f, 5f);
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        shootables = new Node("Shootables");
        rootNode.attachChild(shootables);
        cam.setLocation(new Vector3f(-1, 2f, 6f));
        cam.lookAt(new Vector3f(1, 2, 0), Vector3f.UNIT_Y);
        initMark();
        initMaterials();
        initWall();
        initFloor();
        initKeys();
        initCrossHairs();
        xm = -4 ;
        ym = 3 ;
        BitmapText annance = new BitmapText(guiFont);
        annance.setSize(guiFont.getCharSet().getRenderedSize());
        annance.setText("essayer de trouver un mot de "+nombreAleatoire+" de lettres dans cette grille de "+nMur+" lettres");
        annance.setLocalTranslation(800, 1000, 0);
        timeInSecond = 0;
        BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
        uiText = new BitmapText(fnt, false);
        uiText.setText(String.format(FORMAT, TOTAL_SECOND-timeInSecond));
        guiNode.attachChild(uiText);
        guiNode.attachChild(annance);
    }
    private void initMark() {
        sphere = new Sphere(20, 20, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture texture = assetManager.loadTexture("Textures/bon.jpg");
        mark_mat.setTexture("ColorMap", texture);
        mark.setMaterial(mark_mat);
    }
    private void initKeys() {
        inputManager.addMapping("Shoot",
                new KeyTrigger(KeyInput.KEY_SPACE),
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Shoot");
    }
    private void soundEffect(String s){
        audio= new AudioNode(assetManager, "Sound/Effects/"+s+".wav", AudioData.DataType.Buffer);
        audio.setPositional(false);
        audio.setLooping(false);
        audio.setVolume(2);
        rootNode.attachChild(audio);
        audio.playInstance();
    }
    final private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Shoot") && !keyPressed) {
                frappe ++ ;
                soundEffect("spash");
                CollisionResults results = new CollisionResults();
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                shootables.collideWith(ray, results);
                if (results.size() > 0) {
                    String hit = results.getCollision(0).getGeometry().getName();
                    int x = Integer.parseInt(hit);
                    mot = mot + alph.charAt(x);
                    Geometry brick_geo = new Geometry(hit, box);
                    TextureKey key;
                    Texture tex;
                    wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    key = new TextureKey("Textures/Lettres/"+hit+".jpg");
                    key.setGenerateMips(true);
                    tex = assetManager.loadTexture(key);
                    wall_mat.setTexture("ColorMap", tex);
                    brick_geo.setMaterial(wall_mat);
                    shootables.attachChild(brick_geo);
                    xm = xm + 0.4f ;
                    Vector3f vt = new Vector3f((float) xm,ym, 0);
                    brick_geo.setLocalTranslation(vt);
                    if(frappe == nombreAleatoire){
                        if(!VerifMot.verif(mot)){
                            ParticleEmitter fire =
                                    new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 25);
                            Material mat_red = new Material(assetManager,
                                    "Common/MatDefs/Misc/Particle.j3md");
                            mat_red.setTexture("Texture", assetManager.loadTexture(
                                    "Effects/Explosion/flame.png"));
                            fire.setMaterial(mat_red);
                            fire.setImagesX(2);
                            fire.setImagesY(2);
                            fire.setEndColor(ColorRGBA.randomColor());
                            fire.setStartColor(ColorRGBA.randomColor());
                            fire.getParticleInfluencer().setInitialVelocity(new Vector3f(-1, 2, 0));
                            fire.setStartSize(2f);
                            fire.setEndSize(0.5f);
                            fire.setGravity(0, 0, 0);
                            fire.setLowLife(1f);
                            fire.setHighLife(4f);
                            fire.getParticleInfluencer().setVelocityVariation(0.3f);
                            rootNode.attachChild(fire);
                            soundEffect("perd");
                            ch.setColor(ColorRGBA.randomColor());
                            ch.setText("Perdu !! tu n'as pas reussi à trouver  un mot de "+frappe+" lettres !");
                            guiNode.detachChild(uiText);
                            TOTAL_SECOND += 1000000 ;
                            disparait(vects);
                        }
                        else {
                            soundEffect("vic");
                            ParticleEmitter debris ;
                            debris = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 700 );
                            debris.setSelectRandomImage(true);
                            debris.setRandomAngle(true);
                            debris.setRotateSpeed(FastMath.TWO_PI * 4);
                            debris.setStartColor(new ColorRGBA(1f, 0.59f, 0.28f, 1.0f / 1f));
                            debris.setEndColor(new ColorRGBA(.5f, 0.5f, 0.5f, 0f));
                            debris.setStartSize(.2f);
                            debris.setEndSize(.2f);
                            debris.setParticlesPerSec(0);
                            debris.setGravity(0, 12f, 0);
                            debris.setLowLife(1.4f);
                            debris.setHighLife(1.5f);
                            debris.getParticleInfluencer()
                                    .setInitialVelocity(new Vector3f(0, 15, 0));
                            debris.getParticleInfluencer().setVelocityVariation(.60f);
                            debris.setImagesX(3);
                            debris.setImagesY(3);
                            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
                            mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
                            debris.setMaterial(mat);
                            debris.emitAllParticles();
                            rootNode.attachChild(debris);
                            TOTAL_SECOND += 1000000 ;
                            guiNode.detachChild(uiText);
                            ch.setColor(ColorRGBA.randomColor());
                            ch.setText("Bravo !! tu as trouvé le mot "+mot+" composé de "+frappe+ " lettres avant la fin");
                            explose(vects);
                        }
                    }
                }
                if (results.size() > 0) {
                    CollisionResult closest = results.getClosestCollision();
                    mark.setLocalTranslation(closest.getContactPoint());
                    rootNode.attachChild(mark);
                } else {
                    rootNode.detachChild(mark);
                }
            }
        }
    };
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
                gemotries[tmp].addControl(brick_phy);
                bulletAppState.getPhysicsSpace().add(brick_phy);
                tmp ++ ;
            }
            height += 1.5 * brickHeight;
        }
    }
    private void disparait(Vector3f[] vects) {
        int tmp = 0 ;
        int n = nMur /6 ;
        float startX = brickLength / 4 - 2;
        float height = 0;
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < n; i++) {
                Vector3f vt = new Vector3f(i * brickLength * 100 + startX, brickHeight + height , 100);
                gemotries[tmp].setLocalTranslation(vt);
                RigidBodyControl brick_phy = new RigidBodyControl(2f);
                gemotries[tmp].addControl(brick_phy);
                bulletAppState.getPhysicsSpace().add(brick_phy);
                tmp ++ ;
            }
            height += 100 * brickHeight;
        }
    }
    private void initCrossHairs() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        ch = new BitmapText(guiFont);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");
        ch.setLocalTranslation(
                settings.getWidth() / 2 - ch.getLineWidth()/2, settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
        guiNode.attachChild(ch);
    }
    @Override
    public void simpleUpdate(float tpf) {
        timeInSecond += tpf;
        uiText.setText(String.format(FORMAT, TOTAL_SECOND-timeInSecond));
        uiText.setLocalTranslation(500, 1000, 0);
        if (timeInSecond >= TOTAL_SECOND) {
            stop();
        }
    }
}