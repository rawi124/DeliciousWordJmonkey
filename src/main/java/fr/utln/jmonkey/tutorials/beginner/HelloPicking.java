package fr.utln.jmonkey.tutorials.beginner;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/** Sample 8 - how to let the user pick (select) objects in the scene
 * using the mouse or key presses. Can be used for shooting, opening doors, etc. */
public class HelloPicking extends SimpleApplication {

  public static void main(String[] args) {
    HelloPicking app = new HelloPicking();
    app.start();
  }

  @Override
  public void simpleInitApp() {
    
  }

  /** Declaring the "Shoot" action and mapping to its triggers. */
  private void initKeys() {
    
  }
  /** Defining the "Shoot" action: Determine what was hit and how to respond. */
  private ActionListener actionListener = new ActionListener() {

    public void onAction(String name, boolean keyPressed, float tpf) {
      
    }
  };

  /** A cube object for target practice */
  protected Geometry makeCube(String name, float x, float y, float z) {
    return null;
  }

  /** A floor to show that the "shot" can go through several objects. */
  protected Geometry makeFloor() {
    return null;
  }

  /** A red ball that marks the last spot that was "hit" by the "shot". */
  protected void initMark() {
    
  }

  /** A centred plus sign to help the player aim. */
  protected void initCrossHairs() {
    
  }

  protected Spatial makeCharacter() {
    
    return null;
  }
}