package fr.univtln.theo.games;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class SpeedBonus extends Bonus {
    private float speedIncrease;

    public SpeedBonus(float duration, Geometry geometry, PhysicsSpace physicsSpace, Node rootNode, float speedIncrease) {
        super(duration, geometry, physicsSpace, rootNode);
        this.speedIncrease = speedIncrease;
    }

    @Override
    public void applyEffect(Spatial target) {
        this.target = target;
        RigidBodyControl control = target.getControl(RigidBodyControl.class);
        control.setLinearVelocity(control.getLinearVelocity().mult(speedIncrease));
    }

    @Override
    public void removeEffect() {
        RigidBodyControl control = target.getControl(RigidBodyControl.class);
        control.setLinearVelocity(control.getLinearVelocity().mult(1/speedIncrease));
    }
}