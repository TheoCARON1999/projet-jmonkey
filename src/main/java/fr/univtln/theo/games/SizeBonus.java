package fr.univtln.theo.games;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class SizeBonus extends Bonus {
    private float sizeIncrease;

    public SizeBonus(float duration, Geometry geometry, PhysicsSpace physicsSpace, Node rootNode, float sizeIncrease) {
        super(duration, geometry, physicsSpace, rootNode);
        this.sizeIncrease = sizeIncrease;
    }

    @Override
    public void applyEffect(Spatial target) {
        target.scale(sizeIncrease);
        this.target = target;

        // Update the collision shape of the target's RigidBodyControl
        RigidBodyControl rigidBodyControl = target.getControl(RigidBodyControl.class);
        if (rigidBodyControl != null) {
            CollisionShape newCollisionShape = CollisionShapeFactory.createDynamicMeshShape(target);
            rigidBodyControl.setCollisionShape(newCollisionShape);
        }
    }

    @Override
    public void removeEffect() {
        target.scale(1/sizeIncrease);

        // Update the collision shape of the target's RigidBodyControl
        RigidBodyControl rigidBodyControl = target.getControl(RigidBodyControl.class);
        if (rigidBodyControl != null) {
            CollisionShape newCollisionShape = CollisionShapeFactory.createDynamicMeshShape(target);
            rigidBodyControl.setCollisionShape(newCollisionShape);
        }
    }
}
