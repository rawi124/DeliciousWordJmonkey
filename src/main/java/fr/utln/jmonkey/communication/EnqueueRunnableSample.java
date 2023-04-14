package fr.utln.jmonkey.communication;

import java.util.concurrent.Callable;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class EnqueueRunnableSample extends SimpleApplication {

	public EnqueueRunnableSample(){

	}

	@Override
	public void simpleInitApp() {
	}

	public Geometry createRandomShape() {
		double r = Math.random();

		ColorRGBA color = null;

		Mesh mesh = null;

		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); 
		
		Geometry geom = null;

		if (r < 1.0d/5.0d) {
			mesh = new Sphere(32, 32, 1.0f, false, true);
			geom = new Geometry("Sphere", mesh); 
			color =  ColorRGBA.Green;
		} else if (r < 2.0d/5.0d) {
			mesh = new Dome(Vector3f.ZERO, 2, 4, 1f,false); // Pyramid
			geom = new Geometry("Pyramid", mesh); 
			color =  ColorRGBA.Yellow;
		} else if (r < 3.0d/5.0d) {
			mesh = new Dome(Vector3f.ZERO, 2, 32, 1f,false); // Cone
			geom = new Geometry("Cone", mesh); 
			color =  ColorRGBA.Brown;
		} else if (r < 4.0d/5.0d) {
			mesh = new Dome(Vector3f.ZERO, 32, 32, 1f,false); // Small hemisphere
			geom = new Geometry("Dome", mesh); 
			color =  ColorRGBA.LightGray;
		} else {
			mesh = new Torus(12,12, 0.5f, 1.0f); // Spiral torus
			geom = new Geometry("Torus", mesh); 
			color =  ColorRGBA.Magenta;
		}

		mat.setColor("Color", color);
		geom.setMaterial(mat);
		
		return geom;
	}

	public static void main(String[] args){
		EnqueueRunnableSample app = new EnqueueRunnableSample();

		System.out.println("Starting application");
		app.start(true); // start the application
		System.out.println("Started application [OK]");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
		
		while (app.getContext().isCreated()) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}

			app.enqueue(new Callable<Object>() {

				@Override
				public Object call() throws Exception {
					app.getRootNode().detachAllChildren();

					app.getRootNode().attachChild(app.createRandomShape());
					return null;
				}});
			
		}
		System.out.println("Context destroyed");
	}
}