package org.jorigin.jme3.sample;

import org.jorigin.jme3.app.LightApplication;
import org.jorigin.jme3.input.ExamineCamera;

import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Sphere;

/**
 * An example illustrating the use of an {@link ExamineCamera}.
 * @author Julien SEINTURIER - <a href="http://www.univ-tln.fr">Universit&eacute; de Toulon</a> / <a href="http://www.lis-lab.fr">CNRS LIS umr 7020</a> - <a href="http://www.seinturier.fr">http://www.seinturier.fr</a> (<a href="mailto:julien.seinturier@univ-tln.fr">julien.seinturier@univ-tln.fr</a>)
 */
public class ExamineCameraSample extends LightApplication {

	Node scene = null;
	
	Vector3f lookAt = new Vector3f(0,0,1);

	Vector3f up = new Vector3f(0,1,0);
	
	boolean pressed = false;

	/**
	 * Instantiate the example.
	 */
	public ExamineCameraSample(){
		super();
	}
	
	/**
	 * Create the scene to display.
	 * @return the root {@link Node node} of the scene to display.
	 */
	private Node createScene() {
		Node node = null;
		
		float enpointsSize = 0.5f;
		float referentialUnit = 4.0f;
		
		int shapeSamples = 20;
		
		// A red box located along X Axis unit (positive)
		Mesh axisXPositiveMesh = new Dome(Vector3f.ZERO, 2, shapeSamples, enpointsSize,false);
		
		Material axisXPositiveMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		axisXPositiveMaterial.setColor("Color", ColorRGBA.Red);
		axisXPositiveMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		
		Geometry axisXPositiveGeometry = new Geometry("RED_BOX", axisXPositiveMesh);
		axisXPositiveGeometry.setMaterial(axisXPositiveMaterial);
		
		axisXPositiveGeometry.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI , new Vector3f(0,0,1)));
		axisXPositiveGeometry.setLocalTranslation(new Vector3f(referentialUnit, 0.0f, 0.0f));

		// A red box located along X Axis unit (negative)
		Sphere axisXNegativeMesh = new Sphere(shapeSamples, shapeSamples, enpointsSize);
		
		Material axisXNegativeMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		axisXNegativeMaterial.setColor("Color", ColorRGBA.Red);
				
		Geometry axisXNegativeGeometry = new Geometry("RED_BOX", axisXNegativeMesh);
		axisXNegativeGeometry.setMaterial(axisXNegativeMaterial);
				
		axisXNegativeGeometry.setLocalTranslation(new Vector3f(-referentialUnit, 0.0f, 0.0f));
		
		// A green box located along Y Axis unit (positive)
		Mesh axisYPositiveMesh = new Dome(Vector3f.ZERO, 2, shapeSamples, enpointsSize,false);
				
		Material axisYPositiveMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		axisYPositiveMaterial.setColor("Color", ColorRGBA.Green);	
		axisYPositiveMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		
		Geometry axisYPositiveGeometry = new Geometry("GREEN_BOX", axisYPositiveMesh);
		axisYPositiveGeometry.setMaterial(axisYPositiveMaterial);
				
		axisYPositiveGeometry.setLocalTranslation(new Vector3f(0.0f, referentialUnit, 0.0f));
		
		// A green box located along Y Axis unit (negative)
		Sphere axisYNegativeMesh = new Sphere(shapeSamples, shapeSamples, enpointsSize);
						
		Material axisYNegativeMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		axisYNegativeMaterial.setColor("Color", ColorRGBA.Green);
						
		Geometry axisYNegativeGeometry = new Geometry("GREEN_BOX", axisYNegativeMesh);
		axisYNegativeGeometry.setMaterial(axisYNegativeMaterial);
						
		axisYNegativeGeometry.setLocalTranslation(new Vector3f(0.0f, -referentialUnit, 0.0f));
		
		// A blue box located along Z Axis unit (positive)
		Mesh axisZPositiveEndMesh = new Dome(Vector3f.ZERO, 2, shapeSamples, enpointsSize,false);
		
		Material axisZPositiveEndMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		axisZPositiveEndMaterial.setColor("Color", ColorRGBA.Blue);
		axisZPositiveEndMaterial.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		
		Geometry axisZPositiveEndGeometry = new Geometry("BLUE_BOX", axisZPositiveEndMesh);
		axisZPositiveEndGeometry.setMaterial(axisZPositiveEndMaterial);
		
		axisZPositiveEndGeometry.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI , new Vector3f(1,0,0)));
		axisZPositiveEndGeometry.setLocalTranslation(new Vector3f(0.0f, 0.0f, referentialUnit));
		
		// A blue box located along Z Axis unit (negative)
		Mesh axisZNegativeEndMesh = new Sphere(shapeSamples, shapeSamples, enpointsSize);
				
		Material axisZNegativeEndMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		axisZNegativeEndMaterial.setColor("Color", ColorRGBA.Blue);
				
		Geometry axisZNegativeEndGeometry = new Geometry("BLUE_BOX", axisZNegativeEndMesh);
		axisZNegativeEndGeometry.setMaterial(axisZNegativeEndMaterial);
				
		axisZNegativeEndGeometry.setLocalTranslation(new Vector3f(0.0f, 0.0f, -referentialUnit));

		node = new Node("Scene");
		node.attachChild(axisXPositiveGeometry);
		node.attachChild(axisXNegativeGeometry);
		node.attachChild(axisYPositiveGeometry);
		node.attachChild(axisYNegativeGeometry);
		node.attachChild(axisZPositiveEndGeometry);
		node.attachChild(axisZNegativeEndGeometry);

		return node;
	}
	
	@Override
	public void simpleInitApp() {

		scene = createScene();

		rootNode.attachChild(scene);

		// Create a new examine camera
		ExamineCamera examCamera = new ExamineCamera(inputManager, assetManager);
		examCamera.setEnabled(true);
		
		// Attach the examineCamera to the camera and the scene.
		examCamera.attach(cam, rootNode, guiNode);
		
		// Set the mouse cursor always visible
		inputManager.setCursorVisible(true);
	}

	
	@Override
	public void simpleUpdate(float tpf) {
	}
	
	/**
	 * The main method.
	 * @param args the main method arguments.
	 */
	public static void main(String[] args) {
		ExamineCameraSample app = new ExamineCameraSample();
		app.start();
	}
}