package org.jorigin.jme3.input;

import java.io.IOException;

import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Torus;
import com.jme3.scene.control.Control;

/**
 * A {@link com.jme3.scene.control.Control JMonkey engine control} dedicated to camera behavior. 
 * This control enable to rotate and zoom a camera around a target but also to make pan and walk.<br/>
 * <ul>
 * <li>Rotating around target: done by pressing <i>right mouse button</i> and by moving mouse horizontally and vertically.</li>
 * <li>Panning: done by pressing <i>left mouse button</i> and by moving mouse horizontally and vertically. </li>
 * <li>Walking: done by pressing <i>middle mouse button</i> or by pressing simultaneously <i>right and left mouse buttons</i> and by moving mouse vertically.</li>
 * </ul>
 * Be aware that this control will modify underlying camera parameters (when rotating, panning and walking) 
 * but also underlying target position (when panning, walking), 
 * so do not attach this control to camera or target that you don't want to change. 
 * @author Julien SEINTURIER - <a href="http://www.univ-tln.fr">Universit&eacute; de Toulon</a> / <a href="http://www.lis-lab.fr">CNRS LIS umr 7020</a> - <a href="http://www.seinturier.fr">http://www.seinturier.fr</a> (<a href="mailto:julien.seinturier@univ-tln.fr">julien.seinturier@univ-tln.fr</a>)
 */
public class ExamineCamera implements Control {

	private boolean enabled  = false;

	private boolean attached = false;

	private Camera camera = null;
	
	private CameraControl cameraControl = null;

	private Spatial root                = null;

	private Node target                = null;

	private Geometry trackballXAxisDisplayGeometry = null;
	private Geometry trackballXAxisPhysicGeometry  = null;

	private Geometry trackballYAxisDisplayGeometry = null;
	private Geometry trackballYAxisPhysicGeometry  = null;

	private Geometry trackballZAxisDisplayGeometry = null;
	private Geometry trackballZAxisPhysicGeometry  = null;

	private Node trackball             = null;

	private float trackballAlpha       = 0.1f;

	private Node guiNode = null;

	private CameraNode  cameraNode     = null;

	private InputManager inputManager  = null;

	private AssetManager assetManager  = null;

	private float rotationSensitivity  = 2.0f;

	private float rotationMaxSensitivity = 10000.0f;

	private float rotationMinSensitivity = 0.001f;

	private float moveSensitivity      = 0.5f;

	private float moveMinSensitivity   = 0.0001f;

	private float moveMaxSensitivity   = 100000;

	private float zoomSensitivity      = 2.0f;

	private float zoomMinSensitivity   = 0.05f;

	private float zoomMaxSensitivity   = 100000.0f;

	private float distance             = 10.0f;

	private boolean useRotationRatio   = true;

	private float lowRotationRatio     = 0.2f;

	private boolean useMoveRatio       = true;

	private float moveRatio            = 0.2f;

	private float maxDistance          = 1000000.0f;

	private float minDistance          = 0.001f;

	private float rotH                 = 0.0f;

	private float rotV                 = 0.0f;

	private float transH               = 0.0f;

	private float transV               = 0.0f;

	private float transD               = 0.0f;

	private Vector2f cursorMoveRatio     = new Vector2f(0.0f, 0.0f);

	private Vector3f translation       = null;

	private Vector3f cameraTranslation = null;

	private boolean rotate             = false;

	private boolean translate          = false;

	private boolean walk               = false;

	private final Vector3f direction   = new Vector3f();

	private boolean trackballXAxisFocused = false;
	private boolean trackballYAxisFocused = false;
	private boolean trackballZAxisFocused = false;

	// Original camera parameters
	// These parameter are used for replacing camera within its initial state after
	// examine camera is detached.
	private float originalFrustumNear   = 0.0f;
	private float originalFrustumFar    = 0.0f;
	private float originalFrustumLeft   = 0.0f;
	private float originalFrustumRight  = 0.0f;
	private float originalFrustumTop    = 0.0f;
	private float originalFrustumBottom = 0.0f;

	/**
	 * Create a new examine camera control. This control is attached to the given {@link com.jme3.renderer.Camera JMonkey camera} 
	 * and to the given {@link com.jme3.scene.Node target node}. The provided {@link com.jme3.input.InputManager input manager} enables to grab 
	 * events from control devices. 
	 * After being created, an examine camera has to be attached to a camera and to the scene graph using {@link #attach(Camera, Node)}. 
	 * Without this attachment, the examine camera will not update.
	 * @param manager the {@link com.jme3.input.InputManager input manager} that enables to grab events from control devices.
	 * @param manager the {@link com.jme3.asset.AssetManager asset manager} that enables to create trackball visual.
	 */
	public ExamineCamera(InputManager manager, AssetManager assetManager){
		this.inputManager = manager;
		this.assetManager = assetManager;

		this.translation = new Vector3f();

		this.cameraTranslation = new Vector3f();

		// Creating the camera control
		this.cameraControl = new CameraControl();
		this.cameraControl.setEnabled(true);

		//creating the camera Node
		//Setting the direction to Spatial to camera, this means the camera will copy the movements of the Node
		this.cameraNode = new CameraNode("EXAMINE_CAMERA_NODE", cameraControl);
		this.cameraNode.setControlDir(ControlDirection.SpatialToCamera);
		this.cameraNode.setEnabled(true);

		// The target is a "virtual node" that represents the examinated point
		this.target = new Node();
		this.target.attachChild(cameraNode);

		setDistance(10.0f);

		registerInput();
	}

	@Override
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule capsule = ex.getCapsule(this);
		capsule.write(maxDistance, "maxDistance", 40);
		capsule.write(minDistance, "minDistance", 1);
	}

	@Override
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		maxDistance = ic.readFloat("maxDistance", 40);
		minDistance = ic.readFloat("minDistance", 1);
	}

	@Override
	public Control cloneForSpatial(Spatial spatial) {
		ExamineCamera cc = new ExamineCamera(inputManager, assetManager);
		cc.setDistanceMax(getDistanceMax());
		cc.setDistanceMin(getDistanceMin());
		return cc;
	}

	@Override
	public void setSpatial(Spatial spatial) {

		// Attach only if the given spatial differs from the actual root
		if (spatial == root) {
			return;
		}

		if (root != null) {
			root.removeControl(this);

			if (root instanceof Node) {
				((Node)root).detachChild(target);
			}
		}

		root = spatial;

		if (root != null) {   	   	
			root.addControl(this);

			if (root instanceof Node) {
				((Node)root).attachChild(target);
			}
		}
	}

	@Override
	public void update(float tpf) {

		if (enabled){

			// Updating trackball display
			if (trackballXAxisFocused) {
				trackballXAxisDisplayGeometry.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Color);
			} else {
				trackballXAxisDisplayGeometry.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			}

			if (trackballYAxisFocused) {
				trackballYAxisDisplayGeometry.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Color);
			} else {
				trackballYAxisDisplayGeometry.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			}

			if (trackballZAxisFocused) {
				trackballZAxisDisplayGeometry.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Color);
			} else {
				trackballZAxisDisplayGeometry.getMaterial().getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
			}

			// Filtering by rotation ratio
			if (isUseRotationRatio()){
				if (Math.abs(rotV) <= Math.abs(rotH*getRotationRatio())){
					rotV = 0.0f;
				} else if (Math.abs(rotH) <= Math.abs(rotV*getRotationRatio())){
					rotH = 0.0f;
				}
			}
			
			// Applying rotation around scene X axis only
			if (trackballXAxisFocused) {
				
				if (Math.abs(rotH) > Math.abs(rotV)) {

					// Instantiate quaternion from scene axis and given angle
					Quaternion rotation = new Quaternion().fromAngleNormalAxis(rotH, target.getWorldTransform().invert().transformVector(Vector3f.UNIT_X, new Vector3f()).normalize());

					// Rotate target and trackball
					target.rotate(rotation);						
					trackball.rotate(rotH, 0.0f, 0.0f);
				} else {
					// Instantiate quaternion from scene axis and given angle
					Quaternion rotation = new Quaternion().fromAngleNormalAxis(rotV, target.getWorldTransform().invert().transformVector(Vector3f.UNIT_X, new Vector3f()).normalize());

					// Rotate target and trackball
					target.rotate(rotation);						
					trackball.rotate(rotV, 0.0f, 0.0f);
				}

			} else if (trackballYAxisFocused) {
				if (Math.abs(rotH) > Math.abs(rotV)) {

					// Instantiate quaternion from scene axis and given angle
					Quaternion rotation = new Quaternion().fromAngleNormalAxis(rotH, target.getWorldTransform().invert().transformVector(Vector3f.UNIT_Y, new Vector3f()).normalize());

					// Rotate target and trackball
					target.rotate(rotation);						
					trackball.rotate(0.0f, rotH, 0.0f);
				} else {
					// Instantiate quaternion from scene axis and given angle
					Quaternion rotation = new Quaternion().fromAngleNormalAxis(rotV, target.getWorldTransform().invert().transformVector(Vector3f.UNIT_Y, new Vector3f()).normalize());

					// Rotate target and trackball
					target.rotate(rotation);						
					trackball.rotate(0.0f, rotV, 0.0f);
				}
			} else if (trackballZAxisFocused) {
				
				if (Math.abs(rotH) > Math.abs(rotV)) {

					// Instantiate quaternion from scene axis and given angle
					Quaternion rotation = new Quaternion().fromAngleNormalAxis(rotH, target.getWorldTransform().invert().transformVector(Vector3f.UNIT_Z, new Vector3f()).normalize());

					// Rotate target and trackball
					target.rotate(rotation);						
					trackball.rotate(0.0f, 0.0f, rotH);
				} else {
					// Instantiate quaternion from scene axis and given angle
					Quaternion rotation = new Quaternion().fromAngleNormalAxis(rotV, target.getWorldTransform().invert().transformVector(Vector3f.UNIT_Z, new Vector3f()).normalize());

					// Rotate target and trackball
					target.rotate(rotation);						
					trackball.rotate(0.0f, 0.0f, rotV);
				}
				
			// Generic rotation
			} else {
				float rotX = rotV*cursorMoveRatio.y;
				
				if (cursorMoveRatio.y < getRotationRatio()) {
					rotX = 0.0f;
				}

				float rotY = rotH*cursorMoveRatio.x;
				if (cursorMoveRatio.x < getRotationRatio()) {
					rotY = 0.0f;
				}

				float rotZ = 0.0f;
				if (cursorMoveRatio.x >= getRotationRatio()) {
					rotZ +=rotV*cursorMoveRatio.x;
				}

				if (cursorMoveRatio.y >= getRotationRatio()) {
					rotZ +=rotH*cursorMoveRatio.y;
				}    

				target.rotate(rotX, rotY, rotZ);

				// Update trackball rotation
				trackball.rotate(rotX, rotY, rotZ);
			}
			
			rotH = 0.0f;
			rotV = 0.0f;
			cursorMoveRatio.x = 0.0f;
			cursorMoveRatio.y = 0.0f;
			
			// Processing translation / zoom
			if (isUseMoveRatio()){
				if (Math.abs(transV) <= Math.abs(transH*getRotationRatio())){
					transV = 0.0f;
				} else if (Math.abs(transH) <= Math.abs(transV*getRotationRatio())){
					transH = 0.0f;
				}
			}

			cameraTranslation.setX(transH);
			cameraTranslation.setY(transV);
			cameraTranslation.setZ(transD);
			cameraNode.getWorldTransform().transformInverseVector(cameraTranslation, translation);

			cameraNode.getWorldRotation().mult(cameraTranslation, translation);

			target.move(translation);

			cameraNode.getLocalTranslation().setZ(-distance);

			// If the camera is in parallel projection mode, the frustrum has to be changed in order to 
			// produce zoom factor
			if (camera.isParallelProjection()) {
				float aspect = (float) camera.getWidth() / camera.getHeight();
				cameraControl.getCamera().setFrustum(-1000, 1000, -aspect * distance, aspect * distance, distance, -distance);
			}

			transV = 0.0f;
			transH = 0.0f;
		}
	}

	@Override
	public void render(RenderManager rm, ViewPort vp) {
	}

	private void onActionImpl(String name, boolean isPressed, float tpf) {

		if (enabled){

			if (name.equals("EXAMINE_toggleWalk")){
				//toggling walk on or off
				if (isPressed) {
					walk      = true;
					rotate    = false;
					translate = false;          
				} else {
					walk = false;
				}
			} else if (name.equals("EXAMINE_toggleRotate")){
				//toggling rotation on or off

				if (isPressed) {
					if (!translate){
						rotate = true;
					} else {
						walk      = true;
						rotate    = false;
						translate = false;
					}

				} else {
					rotate = false;
					walk   = false;
				}
			} else if (name.equals("EXAMINE_togglePan")){
				//toggling translation on or off
				if (isPressed) {

					if (!rotate){
						translate = true;
					} else {
						walk      = true;
						rotate    = false;
						translate = false;
					}
				} else {
					translate = false;
					walk      = false;
				}
			} 
		}
	}

	private void onAnalogImpl(String name, float value, float tpf) {


		if (enabled){	

			// Get the screen coordinate of the trackball center
			Vector3f targetPositionScreen   = camera.getScreenCoordinates(target.getWorldTranslation());

			Vector2f cursorPositionScreen = inputManager.getCursorPosition();

			Vector2f targetToCursorScreen = new Vector2f(cursorPositionScreen.x - targetPositionScreen.x, cursorPositionScreen.y - targetPositionScreen.y);

			float targetToCursorNorm = FastMath.abs(targetToCursorScreen.x) + FastMath.abs(targetToCursorScreen.y);

			// Check if an axis of the trackball is picked
			if (!walk && !translate && !rotate) {
				CollisionResults results = new CollisionResults();

				Vector3f pickDirection = new Vector3f(0.0f, 0.0f, -1.0f);
				Vector3f pickOrigin = new Vector3f(cursorPositionScreen.x, cursorPositionScreen.y, 0.0f);

				Ray ray = new Ray(pickOrigin, pickDirection);

				trackball.collideWith(ray, results);

				trackballXAxisFocused = false;
				trackballYAxisFocused = false;
				trackballZAxisFocused = false;

				if (results.size() > 0) {

					Geometry collided = results.getClosestCollision().getGeometry();

					if ((collided == trackballXAxisDisplayGeometry) || ((collided == trackballXAxisPhysicGeometry))){
						trackballXAxisFocused = true;
					} else if ((collided == trackballYAxisDisplayGeometry) || ((collided == trackballYAxisPhysicGeometry))) {
						trackballYAxisFocused = true;
					} else if ((collided == trackballZAxisDisplayGeometry) || ((collided == trackballZAxisPhysicGeometry))) {
						trackballZAxisFocused = true;
					}        
				}
			}
			
			cursorMoveRatio.x = FastMath.abs(targetToCursorScreen.x) / targetToCursorNorm;
			cursorMoveRatio.y = FastMath.abs(targetToCursorScreen.y / targetToCursorNorm);


			//computing the normalized direction of the cam to move the target node
			direction.set(cameraControl.getCamera().getDirection()).normalizeLocal();

			if (name.equals("EXAMINE_moveForward")) {
				direction.multLocal(5 * tpf);
				target.move(direction);
			}

			if (name.equals("EXAMINE_moveBackward")) {
				direction.multLocal(-5 * tpf);
				target.move(direction);
			}

			if (name.equals("EXAMINE_moveUp") && translate) {
				transV += -1.0f*moveSensitivity*value;
			}

			if (name.equals("EXAMINE_moveDown") && translate) {
				transV += 1.0f*moveSensitivity*value;
			}

			if (name.equals("EXAMINE_moveRight") && translate) {
				transH += 1.0f*moveSensitivity*value;
			}

			if (name.equals("EXAMINE_moveLeft") && translate) {
				transH += -1.0f*moveSensitivity*value;
			}
			if (name.equals("EXAMINE_rotateRight") && rotate) {
				rotH += rotationSensitivity*value;
				//target.rotate(0, rotationSensitivity*value, 0);
			}

			if (name.equals("EXAMINE_rotateLeft") && rotate) {
				rotH += -1.0f*rotationSensitivity*value;
			}

			if (name.equals("EXAMINE_rotateUp") && rotate) {
				rotV += -1.0f*rotationSensitivity*value;
			}

			if (name.equals("EXAMINE_rotateDown") && rotate) {
				rotV += rotationSensitivity*value;
			}

			if (name.equals("EXAMINE_moveUp") && walk) {
				transD += -1.0f*moveSensitivity*value;
			}

			if (name.equals("EXAMINE_moveDown") && walk) {
				transD += 1.0f*moveSensitivity*value;
			}

			if (name.equals("EXAMINE_zoomIn")) {
				distance -= zoomSensitivity*value;

				if (distance < getDistanceMin()){
					distance = getDistanceMin();
				}

			}

			if (name.equals("EXAMINE_zoomOut")) {
				distance += zoomSensitivity*value;

				if (distance > getDistanceMax()){
					distance = getDistanceMax();
				}

			}
		}
	}

	/**
	 * Attach the examine camera to a camera and a spatial. 
	 * The camera is the one which has to be controlled by the examine camera. 
	 * The spatial is a node within the scene graph from which this examine camera update method is called. By default the scene root node can be used.
	 * @param camera the camera to control
	 * @param spatial a spatial within the scene graph from which this examine camera update method is called
	 * @see #detach()
	 */
	public void attach(Camera camera, Node sceneNode, Node guiNode) {

		// The node is already attached to the given camera and the given node
		if ((camera == this.camera) && (sceneNode == this.root) && (guiNode == this.guiNode)) {
			return;
		}

		// If the examine camera is already attached to other configuration, it is first detached.
		if (attached) {
			detach();
		}

		// Save original camera parameters (restored by detach method)
		originalFrustumNear   = camera.getFrustumNear();
		originalFrustumFar    = camera.getFrustumFar();
		originalFrustumLeft   = camera.getFrustumLeft();
		originalFrustumRight  = camera.getFrustumRight();
		originalFrustumTop    = camera.getFrustumTop();
		originalFrustumBottom = camera.getFrustumBottom();

		this.camera = camera;
		
		// Attach the underlying camera control to the given camera
		cameraNode.setCamera(this.camera);

		// attach the control to the given spatial within the scene graph.
		// This spatial is only used for propagating update to underlying control
		setSpatial(sceneNode);

		trackball = createTrackballGUI(this.camera.getWidth(), this.camera.getHeight());

		this.guiNode = guiNode;
		if (this.guiNode != null) {
			this.guiNode.attachChild(trackball);
		}
	}

	/**
	 * Detach the examine camera from attached camera and spatial.
	 */
	public void detach() {

		setSpatial(null);

		// Restore original camera parameters (saved within attach method)
		cameraControl.getCamera().setFrustumNear(originalFrustumNear);
		cameraControl.getCamera().setFrustumFar(originalFrustumFar);
		cameraControl.getCamera().setFrustumLeft(originalFrustumLeft);
		cameraControl.getCamera().setFrustumRight(originalFrustumRight);
		cameraControl.getCamera().setFrustumTop(originalFrustumTop);
		cameraControl.getCamera().setFrustumBottom(originalFrustumBottom);

		camera = null;
		
		// Detach the camera from the camera control
		cameraControl.setCamera(null);

		if ((guiNode != null) && (trackball != null)){
			guiNode.detachChild(trackball);
		}
	}

	/**
	 * Return the enabled/disabled state of the control.
	 * @return true if the control is enabled
	 * @see #setEnabled(boolean)
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Enable or disable the control
	 * @param enabled true to enable the control.
	 * @see #isEnabled()
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		cameraNode.setEnabled(enabled);

		if (!enabled) {
			rotate    = false; // reset this flag in-case it was on before
			translate = false;
		}
	}

	/**
	 * Get the sensitivity of the rotation movement. 
	 * A high value makes the controlled camera to rotate faster, a low value makes the camera to rotate slower.
	 * @return the sensitivity of the rotation movement. 
	 * @see #setRotationSensitivity(float)
	 * @see #getMoveSensitivity()
	 * @see #getZoomSensitivity()
	 */
	public float getRotationSensitivity() {
		return rotationSensitivity;
	}

	/**
	 * Set the sensitivity of the rotation movement. 
	 * A high value makes the controlled camera to rotate faster, a low value makes the camera to rotate slower.<br/>
	 * The given <code>rotationSensitivity</code> has to be lower than the {@link #getRotationMaxSensitivity()} and higher than {@link #getRotationMinSensitivity()}. 
	 * If it is not the case, the rotation sensitivity value is set either to {@link #getRotationMaxSensitivity()} or {@link #getRotationMinSensitivity()}
	 * @param rotationSensitivity the sensitivity of the rotation movement. 
	 * @see #getRotationSensitivity()
	 * @see #setMoveSensitivity(float)
	 * @see #setZoomSensitivity(float)
	 */
	public void setRotationSensitivity(float rotationSensitivity) {

		if (rotationSensitivity < getRotationMinSensitivity()){
			this.rotationSensitivity = getRotationMinSensitivity();
		} else if (rotationSensitivity > getRotationMaxSensitivity()){
			this.rotationSensitivity = getRotationMaxSensitivity();
		} else {
			this.rotationSensitivity = rotationSensitivity;
		}
	}

	/**
	 * Get the maximal sensitivity that is allowed for rotation. When using {@link #setRotationSensitivity(float)}, 
	 * if the given value is higher than this maximum, then the active value is set to {@link #getRotationMaxSensitivity()}.
	 * @see #setRotationMaxSensitivity(float)
	 * @see #getRotationSensitivity()
	 * @return the maximum sensitivity that is allowed for rotation.
	 */
	public float getRotationMaxSensitivity() {
		return rotationMaxSensitivity;
	}

	/**
	 * Set the maximal sensitivity that is allowed for rotation. When using {@link #setRotationSensitivity(float)}, 
	 * if the given value is higher than this maximum, then the active value is set to {@link #getRotationMaxSensitivity()}.
	 * @param rotationMaxSensitivity the maximum sensitivity that is allowed for rotation.
	 * @see #getRotationMaxSensitivity()
	 * @see #getRotationSensitivity()
	 */
	public void setRotationMaxSensitivity(float rotationMaxSensitivity) {
		this.rotationMaxSensitivity = rotationMaxSensitivity;
	}

	/**
	 * Get the the minimal sensitivity that is allowed for rotation. When using {@link #setRotationSensitivity(float)}, 
	 * if the given value is lower than this maximum, then the active value is set to {@link #getRotationMinSensitivity()}.
	 * @return the the minimal sensitivity that is allowed for rotation.
	 * @see #setRotationMinSensitivity(float)
	 * @see #getRotationSensitivity()
	 */
	public float getRotationMinSensitivity() {
		return rotationMinSensitivity;
	}

	/**
	 * Set the the minimal sensitivity that is allowed for rotation. When using {@link #setRotationSensitivity(float)}, 
	 * if the given value is lower than this maximum, then the active value is set to {@link #getRotationMinSensitivity()}.
	 * @param rotationMinSensitivity the the minimal sensitivity that is allowed for rotation.
	 * @see #getRotationMinSensitivity()
	 * @see #getRotationSensitivity()
	 */
	public void setRotationMinSensitivity(float rotationMinSensitivity) {
		this.rotationMinSensitivity = rotationMinSensitivity;
	}

	/**
	 * Get the sensitivity of the pan and walk movements.
	 * A high value makes the controlled camera to pan/walk faster, a low value makes the camera to pan/walk slower.
	 * @return the sensitivity of the pan and walk movements.
	 * @see #setMoveSensitivity(float)
	 * @see #getRotationSensitivity()
	 * @see #getZoomSensitivity()
	 */
	public float getMoveSensitivity() {
		return moveSensitivity;
	}

	/**
	 * Set the sensitivity of the pan and walk movements.
	 * A high value makes the controlled camera to pan/walk faster, a low value makes the camera to pan/walk slower.<br/>
	 * The given <code>moveSensitivity</code> has to be lower than the {@link #getMoveMaxSensitivity()} and higher than {@link #getMoveMinSensitivity()}. 
	 * If it is not the case, the move sensitivity value is set either to {@link #getMoveMaxSensitivity()} or {@link #getMoveMinSensitivity()}
	 * @param moveSensitivity the sensitivity of the pan and walk movements.
	 * @see #getMoveSensitivity()
	 * @see #getMoveMaxSensitivity()
	 * @see #getMoveMinSensitivity()
	 * @see #setRotationSensitivity(float)
	 * @see #setZoomSensitivity(float)
	 */
	public void setMoveSensitivity(float moveSensitivity) {

		if (moveSensitivity < getMoveMinSensitivity()){
			this.moveSensitivity = getMoveMinSensitivity();
		} else if (moveSensitivity > getMoveMaxSensitivity()){
			this.moveSensitivity = getMoveMaxSensitivity();
		} else {
			this.moveSensitivity = moveSensitivity;
		}
	}

	/**
	 * Get the the minimal sensitivity that is allowed for pan / walk. When using {@link #setMoveSensitivity(float)}, 
	 * if the given value is lower than this minimum, then the active value is set to {@link #getMoveMinSensitivity()}.
	 * @return the the minimal sensitivity that is allowed for pan / walk.
	 * @see #setMoveMaxSensitivity(float)
	 * @see #getMoveSensitivity()
	 */
	public float getMoveMinSensitivity() {
		return moveMinSensitivity;
	}

	/**
	 * Set the the minimal sensitivity that is allowed for pan / walk. When using {@link #setMoveSensitivity(float)}, 
	 * if the given value is lower than this minimum, then the active value is set to {@link #getMoveMinSensitivity()}.
	 * @param moveMinSensitivity the the minimal sensitivity that is allowed for pan / walk.
	 * @see #getMoveMaxSensitivity()
	 * @see #getMoveSensitivity()
	 */
	public void setMoveMinSensitivity(float moveMinSensitivity) {
		this.moveMinSensitivity = moveMinSensitivity;
	}

	/**
	 * Get the the maximal sensitivity that is allowed for pan / walk. When using {@link #setMoveSensitivity(float)}, 
	 * if the given value is higher than this maximum, then the active value is set to {@link #getMoveMaxSensitivity()}.
	 * @return the the maximal sensitivity that is allowed for pan / walk.
	 * @see #setMoveMaxSensitivity(float)
	 * @see #getMoveSensitivity()
	 */
	public float getMoveMaxSensitivity() {
		return moveMaxSensitivity;
	}

	/**
	 * Set the the maximal sensitivity that is allowed for pan / walk. When using {@link #setMoveSensitivity(float)}, 
	 * if the given value is higher than this maximum, then the active value is set to {@link #getMoveMaxSensitivity()}.
	 * @param moveMaxSensitivity the the maximal sensitivity that is allowed for pan / walk.
	 * @see #getMoveMaxSensitivity()
	 * @see #getMoveSensitivity()
	 */
	public void setMoveMaxSensitivity(float moveMaxSensitivity) {
		this.moveMaxSensitivity = moveMaxSensitivity;
	}

	/**
	 * Get the sensitivity of the camera zoom.
	 * A high value makes the controlled camera to zoom faster, a low value makes the camera to zoom slower.
	 * @return the sensitivity of the camera zoom.
	 * @see #setZoomSensitivity(float)
	 * @see #getRotationSensitivity()
	 * @see #getMoveSensitivity()
	 */
	public float getZoomSensitivity() {
		return zoomSensitivity;
	}

	/**
	 * Set the sensitivity of the camera zoom.
	 * A high value makes the controlled camera to zoom faster, a low value makes the camera to zoom slower.
	 * @param zoomSensitivity the sensitivity of the camera zoom.
	 * @see #getZoomSensitivity()
	 * @see #setRotationSensitivity(float)
	 * @see #setMoveSensitivity(float)
	 */
	public void setZoomSensitivity(float zoomSensitivity) {

		if (zoomSensitivity < getZoomMinSensitivity()){
			this.zoomSensitivity = getZoomMinSensitivity();
		} else if (zoomSensitivity > getZoomMaxSensitivity()){
			this.zoomSensitivity = getZoomMaxSensitivity();
		} else {
			this.zoomSensitivity = zoomSensitivity;
		}
	}

	/**
	 * Get the the minimal sensitivity that is allowed for zoom. When using {@link #setZoomSensitivity(float)}, 
	 * if the given value is lower than this minimum, then the active value is set to {@link #getZoomMinSensitivity()}.
	 * @return the minimal sensitivity that is allowed for zoom.
	 * @see #setZoomMinSensitivity(float)
	 * @see #getZoomMaxSensitivity()
	 * @see #getZoomSensitivity()
	 */
	public float getZoomMinSensitivity() {
		return zoomMinSensitivity;
	}

	/**
	 * Set the minimal sensitivity that is allowed for zoom. When using {@link #setZoomSensitivity(float)}, 
	 * if the given value is lower than this minimum, then the active value is set to {@link #getZoomMinSensitivity()}.
	 * @param zoomMinSensitivity the minimal sensitivity that is allowed for zoom.
	 * @see #getZoomMinSensitivity()
	 * @see #getZoomMaxSensitivity()
	 * @see #getZoomSensitivity()
	 */
	public void setZoomMinSensitivity(float zoomMinSensitivity) {
		this.zoomMinSensitivity = zoomMinSensitivity;
	}

	/**
	 * Get the maximal sensitivity that is allowed for zoom. When using {@link #setZoomSensitivity(float)}, 
	 * if the given value is lower than this max, then the active value is set to {@link #getZoomMaxSensitivity()}.
	 * @return the maximal sensitivity that is allowed for zoom.
	 * @see #setZoomMaxSensitivity(float)
	 * @see #getZoomMinSensitivity()
	 * @see #getZoomSensitivity()
	 */
	public float getZoomMaxSensitivity() {
		return zoomMaxSensitivity;
	}

	/**
	 * Set the  the maximal sensitivity that is allowed for zoom. When using {@link #setZoomSensitivity(float)}, 
	 * if the given value is lower than this max, then the active value is set to {@link #getZoomMaxSensitivity()}.
	 * @param zoomMaxSensitivity the maximal sensitivity that is allowed for zoom.
	 * @see #getZoomMaxSensitivity()
	 * @see #getZoomMinSensitivity()
	 * @see #getZoomSensitivity()
	 */
	public void setZoomMaxSensitivity(float zoomMaxSensitivity) {
		this.zoomMaxSensitivity = zoomMaxSensitivity;
	}

	/**
	 * Get the distance from the camera to the target. This value is assumed to be the zoom value and is positive or equals to 0.
	 * @return the distance from the camera to the target. This value is assumed to be the zoom value and is positive or equals to 0.
	 * @see #setDistance(float)
	 */
	public float getDistance() {
		return distance;
	}

	/**
	 * Set the distance from the camera to the target. This value is assumed to be the zoom value and has to be positive or equals to 0.<br/>
	 * if the given distance is lower than {@link #getDistanceMin()} or higher than {@link #getDistanceMax()}, 
	 * the distance value is respectively set to {@link #getDistanceMin()} or {@link #getDistanceMax()}.
	 * @param distance distance from the camera to the target. This value is assumed to be the zoom value.
	 * @see #getDistance()
	 * @see #getDistanceMin()
	 * @see #getDistanceMax()
	 */
	public void setDistance(float distance) {

		if (distance < 0.0f) {
			throw new IllegalArgumentException("Distance "+distance+" is invalid, expected a positive number.");
		}

		if ((distance <= maxDistance) && (distance >= minDistance)) {
			this.distance = distance;
		}
	}

	/**
	 * Get the maximal distance from the camera to the target. This value is assumed to be the maximal zoom value.
	 * @return the maximal distance from the camera to the target. This value is assumed to be the maximal zoom value.
	 * @see #getDistance()
	 * @see #getDistanceMin()
	 */
	public float getDistanceMax() {
		return maxDistance;
	}

	/**
	 * Set the maximal distance from the camera to the target. This value is assumed to be the maximal zoom value and has to be positive or equals to 0.
	 * @param distance the maximal distance from the camera to the target. This value is assumed to be the maximal zoom value and has to be positive or equals to 0.
	 * @see #getDistanceMax()
	 * @see #setDistance(float)
	 * @see #setDistanceMin(float)
	 * @throws IllegalArgumentException if the given distance is not a positive number
	 */
	public void setDistanceMax(float distance) {

		if (distance < 0.0f) {
			throw new IllegalArgumentException("Distance "+distance+" is invalid, expected a positive number.");
		}

		this.maxDistance = distance;
	}

	/**
	 * Get the minimal distance from the camera to the target. This value is assumed to be the minimal zoom value.
	 * @return the minimal distance from the camera to the target. This value is assumed to be the minimal zoom value.
	 * @see #setDistanceMin(float)
	 * @see #getDistance()
	 * @see #getDistanceMax()
	 */
	public float getDistanceMin() {
		return minDistance;
	}

	/**
	 * Set the minimal distance from the camera to the target. This value is assumed to be the minimal zoom value and has to be positive or equals to 0.
	 * @param distance the minimal distance from the camera to the target. This value is assumed to be the minimal zoom value and has to be positive or equals to 0.
	 * @see #getDistanceMin()
	 * @see #setDistance(float)
	 * @see #setDistanceMax(float)
	 * @throws IllegalArgumentException if the given distance is not a positive number
	 */
	public void setDistanceMin(float distance) {

		if (distance < 0.0f) {
			throw new IllegalArgumentException("Distance "+distance+" is invalid, expected a positive number.");
		}

		this.minDistance = distance;
	}

	/**
	 * Get if a ratio between rotation has to be used. <br/>
	 * Such a ratio enables to consider only one rotation when moving mouse for ignoring noise.
	 * @return <code>true</code> if the ratio between rotation has to be used and <code>false</code> otherwise.
	 * @see #setUseRotationRatio(boolean)
	 * @see #getRotationRatio()
	 * @see #isUseMoveRatio()
	 */
	public boolean isUseRotationRatio() {
		return useRotationRatio;
	}

	/**
	 * Set if a ratio between rotation has to be used. <br/>
	 * Such a ratio enables to consider only one rotation when moving mouse for ignoring noise.
	 * @param useRotationRatio <code>true</code> if the ratio between rotation has to be used and <code>false</code> otherwise.
	 * @see #isUseRotationRatio()
	 * @see #getRotationRatio()
	 * @see #isUseMoveRatio()
	 */
	public void setUseRotationRatio(boolean useRotationRatio) {
		this.useRotationRatio = useRotationRatio;
	}

	/**
	 * Get the ratio between two rotations (horizontal and vertical) that the lower value has to respect. 
	 * If the lower value of the rotation is lower than the higher value multiplied by the ratio, 
	 * the lower rotation is ignored. This is useful for eliminating mouse move noise.
	 * @return the ratio between two rotations.
	 * @see #setRotationRatio(float)
	 * @see #isUseRotationRatio()
	 * @see #getMoveRatio()
	 */
	public float getRotationRatio() {
		return lowRotationRatio;
	}

	/**
	 * Set the ratio between two rotations (horizontal and vertical) that the lower value has to respect. 
	 * If the lower value of the rotation is lower than the higher value multiplied by the ratio, 
	 * the lower rotation is ignored. This is useful for eliminating mouse move noise.
	 * @param lowRotationRatio the ratio between two rotations.
	 * @see #getRotationRatio()
	 * @see #isUseRotationRatio()
	 * @see #getMoveRatio()
	 */
	public void setRotationRatio(float lowRotationRatio) {
		this.lowRotationRatio = lowRotationRatio;
	}

	/**
	 * Get if a ratio between pan values has to be used. <br/>
	 * Such a ratio enables to consider only one pan axis when moving mouse for ignoring noise.
	 * @return <code>true</code> if the ratio between pan values has to be used and <code>false</code> otherwise.
	 * @see #setUseMoveRatio(boolean)
	 * @see #getMoveRatio()
	 * @see #isUseRotationRatio()
	 */
	public boolean isUseMoveRatio() {
		return useMoveRatio;
	}

	/**
	 * Set if a ratio between pan values has to be used. <br/>
	 * Such a ratio enables to consider only one pan axis when moving mouse for ignoring noise.
	 * @param useMoveRatio <code>true</code> if the ratio between pan values has to be used and <code>false</code> otherwise.
	 * @see #isUseMoveRatio()
	 * @see #getMoveRatio()
	 * @see #isUseRotationRatio()
	 */
	public void setUseMoveRatio(boolean useMoveRatio) {
		this.useMoveRatio = useMoveRatio;
	}

	/**
	 * Get the the ratio between two pan values (horizontal and vertical) that the lower value has to respect. 
	 * If the lower value is lower than the higher value multiplied by the ratio, 
	 * the lower value is ignored. This is useful for eliminating mouse move noise.
	 * @return the ratio between two pan values.
	 * @see #setMoveRatio(float)
	 * @see #isUseMoveRatio()
	 * @see #getRotationRatio()
	 */
	public float getMoveRatio() {
		return moveRatio;
	}

	/**
	 * Set the the ratio between two pan values (horizontal and vertical) that the lower value has to respect. 
	 * If the lower value is lower than the higher value multiplied by the ratio, 
	 * the lower value is ignored. This is useful for eliminating mouse move noise.
	 * @param moveRatio the ratio between two pan values.
	 * @see #getMoveRatio()
	 * @see #isUseMoveRatio()
	 * @see #getRotationRatio()
	 */
	public void setMoveRatio(float moveRatio) {
		this.moveRatio = moveRatio;
	}

	/**
	 * Return the target of the camera.
	 * @return the target of the camera.
	 */
	public Node getSpatial(){
		return this.target;
	}

	protected void registerInput() {
		inputManager.addMapping("EXAMINE_kb_moveForward"  , new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("EXAMINE_kb_moveBackward" , new KeyTrigger(KeyInput.KEY_DOWN), new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("EXAMINE_kb_moveRight"    , new KeyTrigger(KeyInput.KEY_RIGHT), new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("EXAMINE_kb_moveLeft"     , new KeyTrigger(KeyInput.KEY_LEFT), new KeyTrigger(KeyInput.KEY_A));

		inputManager.addMapping("EXAMINE_toggleRotate"    , new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)); 
		inputManager.addMapping("EXAMINE_togglePan"       , new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("EXAMINE_toggleWalk"      , new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));

		inputManager.addMapping("EXAMINE_moveUp"          , new MouseAxisTrigger(MouseInput.AXIS_Y, false));
		inputManager.addMapping("EXAMINE_moveDown"        , new MouseAxisTrigger(MouseInput.AXIS_Y, true));
		inputManager.addMapping("EXAMINE_moveRight"       , new MouseAxisTrigger(MouseInput.AXIS_X, false));
		inputManager.addMapping("EXAMINE_moveLeft"        , new MouseAxisTrigger(MouseInput.AXIS_X, true));
		inputManager.addMapping("EXAMINE_rotateRight"     , new MouseAxisTrigger(MouseInput.AXIS_X, true));
		inputManager.addMapping("EXAMINE_rotateLeft"      , new MouseAxisTrigger(MouseInput.AXIS_X, false));
		inputManager.addMapping("EXAMINE_rotateUp"        , new MouseAxisTrigger(MouseInput.AXIS_Y, false));
		inputManager.addMapping("EXAMINE_rotateDown"      , new MouseAxisTrigger(MouseInput.AXIS_Y, true));

		inputManager.addMapping("EXAMINE_zoomIn"          , new MouseAxisTrigger(MouseInput.AXIS_WHEEL,false));
		inputManager.addMapping("EXAMINE_zoomOut"         , new MouseAxisTrigger(MouseInput.AXIS_WHEEL,true));

		inputManager.addListener(new AnalogListener() {

			@Override
			public void onAnalog(String name, float value, float tpf) {
				onAnalogImpl(name, value, tpf);

			}}, "EXAMINE_rotateRight", "EXAMINE_rotateLeft", "EXAMINE_rotateUp", "EXAMINE_rotateDown", 
				"EXAMINE_moveUp", "EXAMINE_moveDown", "EXAMINE_moveRight", "EXAMINE_moveLeft",
				"EXAMINE_zoomIn", "EXAMINE_zoomOut");

		inputManager.addListener(new ActionListener() {

			@Override
			public void onAction(String name, boolean isPressed, float tpf) {
				onActionImpl(name, isPressed, tpf);
			}}, "EXAMINE_toggleRotate", "EXAMINE_togglePan", "EXAMINE_toggleWalk");

		//manager.addListener(this, "EXAMINE_moveForward", "EXAMINE_moveBackward", "EXAMINE_moveRight", "EXAMINE_moveLeft");
		//manager.addListener(this, "EXAMINE_toggleRotate", "EXAMINE_togglePan", "EXAMINE_toggleWalk");

		/*
    manager.addListener(this, "EXAMINE_rotateRight", "EXAMINE_rotateLeft", 
                              "EXAMINE_rotateUp", "EXAMINE_rotateDown", 
                              "EXAMINE_moveUp", "EXAMINE_moveDown",
                              "EXAMINE_moveRight", "EXAMINE_moveLeft",
                              "EXAMINE_zoomIn", "EXAMINE_zoomOut",
                              "EXAMINE_toggleRotate", "EXAMINE_togglePan", "EXAMINE_toggleWalk");
		 */
	}

	private Node createTrackballGUI(int screenWidth, int screenHeight) {

		int circleSamples = 100;
		int radialSamples = 100;


		// The thickness in pixel of the trackball edges
		int trackballThickness = 1;

		// The ratio of the screen that is occuped by the trackball
		float trackballRatio = 0.20f;

		// The thickness in pixel of the trackball edges
		int trackballSensitivity = 5;

		// Trackball is centered within the screen and has a size of 20% of the screen.
		int screenMinSize = Math.min(screenWidth, screenHeight);

		float outerRadius = screenMinSize*trackballRatio;
		float innerRadius = trackballThickness;

		Node node = new Node("Trackball");

		// The X circle (Red)
		Torus trackballXAxisDisplayMesh = new Torus(circleSamples, radialSamples, innerRadius, outerRadius);

		Material trackballXAxisDisplayMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		trackballXAxisDisplayMaterial.setColor("Color", new ColorRGBA(1.0f, 0.0f, 0.0f, trackballAlpha));
		trackballXAxisDisplayMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		trackballXAxisDisplayGeometry = new Geometry("TrackballXAxis", trackballXAxisDisplayMesh);
		trackballXAxisDisplayGeometry.setMaterial(trackballXAxisDisplayMaterial);

		Material trackballXAxisPhysicsMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		trackballXAxisPhysicsMaterial.setColor("Color", new ColorRGBA(1.0f, 0.0f, 0.0f, trackballAlpha));

		Torus trackballXAxisPhysicsMesh = new Torus(circleSamples, radialSamples, trackballSensitivity, outerRadius);

		trackballXAxisPhysicGeometry = new Geometry("TrackballXAxisPhysics", trackballXAxisPhysicsMesh);
		trackballXAxisPhysicGeometry.setMaterial(trackballXAxisPhysicsMaterial);
		trackballXAxisPhysicGeometry.setCullHint(CullHint.Always);

		Quaternion xQuat = new Quaternion();
		xQuat.fromAngleAxis(FastMath.HALF_PI , new Vector3f(0,1,0));

		trackballXAxisDisplayGeometry.setLocalRotation(xQuat);
		trackballXAxisPhysicGeometry.setLocalRotation(xQuat);

		// The Y Circle (Green)
		Torus trackballYAxisDisplayMesh = new Torus(circleSamples, radialSamples, innerRadius, outerRadius);

		Material trackballYAxisDisplayMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		trackballYAxisDisplayMaterial.setColor("Color", ColorRGBA.Green);
		trackballYAxisDisplayMaterial.setColor("Color", new ColorRGBA(0.0f, 1.0f, 0.0f, trackballAlpha));
		trackballYAxisDisplayMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		trackballYAxisDisplayGeometry = new Geometry("TrackballYAxis", trackballYAxisDisplayMesh);
		trackballYAxisDisplayGeometry.setMaterial(trackballYAxisDisplayMaterial);

		Material trackballYAxisPhysicsMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		trackballYAxisPhysicsMaterial.setColor("Color", new ColorRGBA(0.0f, 1.0f, 0.0f, trackballAlpha));

		Torus trackballYAxisPhysicsMesh = new Torus(circleSamples, radialSamples, trackballSensitivity, outerRadius);

		trackballYAxisPhysicGeometry = new Geometry("TrackballYAxisPhysics", trackballYAxisPhysicsMesh);
		trackballYAxisPhysicGeometry.setMaterial(trackballYAxisPhysicsMaterial);
		trackballYAxisPhysicGeometry.setCullHint(CullHint.Always);

		Quaternion yQuat = new Quaternion();
		yQuat.fromAngleAxis( FastMath.HALF_PI , new Vector3f(1,0,0) );
		trackballYAxisDisplayGeometry.setLocalRotation(yQuat);
		trackballYAxisPhysicGeometry.setLocalRotation(yQuat);

		// The Z Circle (Blue)
		Torus trackballZAxisDisplayMesh = new Torus(circleSamples, radialSamples, innerRadius, outerRadius);

		Material trackballZAxisDisplayMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		trackballZAxisDisplayMaterial.setColor("Color", ColorRGBA.Blue);
		trackballZAxisDisplayMaterial.setColor("Color", new ColorRGBA(0.0f, 0.0f, 1.0f, trackballAlpha));
		trackballZAxisDisplayMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		trackballZAxisDisplayGeometry = new Geometry("TrackballZAxis", trackballZAxisDisplayMesh);
		trackballZAxisDisplayGeometry.setMaterial(trackballZAxisDisplayMaterial);

		Material trackballZAxisPhysicsMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		trackballZAxisPhysicsMaterial.setColor("Color", new ColorRGBA(0.0f, 0.0f, 1.0f, trackballAlpha));

		Torus trackballZAxisPhysicsMesh = new Torus(circleSamples, radialSamples, trackballSensitivity, outerRadius);

		trackballZAxisPhysicGeometry = new Geometry("TrackballZAxisPhysics", trackballZAxisPhysicsMesh);
		trackballZAxisPhysicGeometry.setMaterial(trackballZAxisPhysicsMaterial);
		trackballZAxisPhysicGeometry.setCullHint(CullHint.Always);

		node.attachChild(trackballXAxisDisplayGeometry);
		node.attachChild(trackballXAxisPhysicGeometry);

		node.attachChild(trackballYAxisDisplayGeometry);
		node.attachChild(trackballYAxisPhysicGeometry);

		node.attachChild(trackballZAxisDisplayGeometry);
		node.attachChild(trackballZAxisPhysicGeometry);

		// Place the trackball at the center of the screen and place it at a distance in Z in order to allow picking
		// from GUI node
		node.setLocalTranslation(new Vector3f(screenWidth/2.0f, screenHeight/2.0f, -2.0f*outerRadius));

		return node;	
	}
}


