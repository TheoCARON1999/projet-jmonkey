package fr.univtln.theo.games;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class GoalBonus extends Bonus{
    private int scoreIncrease;

    public GoalBonus(float duration, Geometry geometry, PhysicsSpace physicsSpace, Node rootNode, int scoreIncrease) {
        super(duration, geometry, physicsSpace, rootNode);
        this.scoreIncrease = scoreIncrease;
    }

    @Override
    public void applyEffect(Spatial target) {

    }

    @Override
    public void removeEffect() {

    }
}
