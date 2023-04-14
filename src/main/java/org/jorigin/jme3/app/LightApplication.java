package org.jorigin.jme3.app;

import com.jme3.app.LegacyApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.audio.AudioListenerState;
import com.jme3.profile.AppStep;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;

/**
 * A light jME3 Applications. This application only contains a root node for the 3D scene and a GUI node. 
 */
public abstract class LightApplication extends LegacyApplication {


    protected Node rootNode = new Node("Root Node");
    protected Node guiNode = new Node("Gui Node");
    protected boolean showSettings = true;

    public LightApplication() {
        this(new AudioListenerState(),  new ConstantVerifierState());
    }

    public LightApplication(AppState... initialStates) {
        super(initialStates);
    }

    @Override
    public void start() {
        // set some default settings in-case
        // settings dialog is not shown
        boolean loadSettings = false;
        if (settings == null) {
            setSettings(new AppSettings(true));
            loadSettings = true;
        }

        // show settings dialog
        if (showSettings) {
            if (!JmeSystem.showSettingsDialog(settings, loadSettings)) {
                return;
            }
        }
        //re-setting settings they can have been merged from the registry.
        setSettings(settings);
        super.start();
    }

    /**
     * Retrieves guiNode
     * @return guiNode Node object
     *
     */
    public Node getGuiNode() {
        return guiNode;
    }

    /**
     * Retrieves rootNode
     * @return rootNode Node object
     *
     */
    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    /**
     * Toggles settings window to display at start-up
     * @param showSettings Sets true/false
     *
     */
    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }


    @Override
    public void initialize() {
        super.initialize();

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        // call user code
        simpleInitApp();
    }

    @Override
    public void update() {
        if (prof != null)
            prof.appStep(AppStep.BeginFrame);

        super.update(); // makes sure to execute AppTasks
        if (speed == 0 || paused) {
            return;
        }

        float tpf = timer.getTimePerFrame() * speed;

        // update states
        if (prof != null)
            prof.appStep(AppStep.StateManagerUpdate);
        stateManager.update(tpf);

        // simple update and root node
        simpleUpdate(tpf);

        if (prof != null)
            prof.appStep(AppStep.SpatialUpdate);
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);

        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        // render states
        if (prof != null)
            prof.appStep(AppStep.StateManagerRender);
        stateManager.render(renderManager);

        if (prof != null)
            prof.appStep(AppStep.RenderFrame);
        renderManager.render(tpf, context.isRenderable());
        simpleRender(renderManager);
        stateManager.postRender();

        if (prof != null)
            prof.appStep(AppStep.EndFrame);
    }

    public abstract void simpleInitApp();

    public void simpleUpdate(float tpf) {
    }

    public void simpleRender(RenderManager rm) {
    }
}
