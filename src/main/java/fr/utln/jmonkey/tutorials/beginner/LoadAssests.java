package fr.utln.jmonkey.tutorials.beginner;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.math.ColorRGBA;
import com.jme3.util.BufferUtils;

import javax.vecmath.Vector2f;

/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
public class LoadAssests extends SimpleApplication {


    public static void main(String[] args){
        LoadAssests app = new LoadAssests();
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {
        Mesh mesh = new Mesh();

        Vector3f [] vertices = new Vector3f[4];
        vertices[0] = new Vector3f(0,0,0);//bas gauche
        vertices[1] = new Vector3f(3,0,0);//bas dte
        vertices[2] = new Vector3f(0,3,0);//haut gauche
        vertices[3] = new Vector3f(3,3,0);//haut droite

        Vector2f[] texCoord = new Vector2f[4];
        texCoord[0] = new Vector2f(0,0);
        texCoord[1] = new Vector2f(1,0);
        texCoord[2] = new Vector2f(0,1);
        texCoord[3] = new Vector2f(1,1);

        int [] indexes = { 2,0,1, 1,3,2 };

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        //mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
        mesh.setBuffer(VertexBuffer.Type.Index,    3, BufferUtils.createIntBuffer(indexes));
        mesh.updateBound();
        Geometry geo = new Geometry ("ColoredMesh", mesh); // using the custom mesh
        Material matVC = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matVC.setBoolean("VertexColor", true);
        int colorIndex = 0;


        float[] colorArray = new float[4*4];


// note: the red and green values are arbitrary in this example
        for(int i = 0; i < 4; i++){
            // Red value (is increased by .2 on each next vertex here)
            colorArray[colorIndex++]= 0.1f+(.2f*i);
            // Green value (is reduced by .2 on each next vertex)
            colorArray[colorIndex++]= 0.9f-(0.2f*i);
            // Blue value (remains the same in our case)
            colorArray[colorIndex++]= 0.5f;
            // Alpha value (no transparency set here)
            colorArray[colorIndex++]= 1.0f;
        }
        mesh.setBuffer(VertexBuffer.Type.Color, 4, colorArray);
        geo.setMaterial(matVC);
        rootNode.attachChild(geo);
    }
}