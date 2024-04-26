package fr.univtln.theo.games;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.io.IOException;

public abstract class Bonus implements Savable {
    private static float size = 1.0f;

    protected float duration;
    protected boolean isActive;
    protected Geometry geometry;
    protected PhysicsSpace physicsSpace;
    protected Node rootNode;

    protected Spatial target;

    public Bonus(float duration, Geometry geometry, PhysicsSpace physicsSpace, Node rootNode) {
        this.duration = duration;
        this.isActive = false;
        this.geometry = geometry;
        this.physicsSpace = physicsSpace;
        this.rootNode = rootNode;
    }

    public abstract void applyEffect(Spatial target);
    public abstract void removeEffect();

    public void update(float tpf, Spatial target) {
        if (isActive) {
            duration -= tpf;
            if (duration <= 0) {
                removeEffect();
                isActive = false;
            }
        }
    }

    public void activate(Spatial target) {
        applyEffect(target);
        isActive = true;
    }

    public boolean isActive() {
        return isActive;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void removeFromScene() {
        rootNode.detachChild(geometry);
        physicsSpace.remove(geometry.getControl(RigidBodyControl.class));
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(size, "size", 1.0f);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule capsule = im.getCapsule(this);
        size = capsule.readFloat("size", 1.0f);
    }
}
