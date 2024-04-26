package fr.univtln.theo.games;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

public class AIPlayer {
    private Geometry aiPaddle; // Le paddle contrôlé par l'IA
    private Geometry puck; // Le palet
    private float aiSpeed; // Vitesse de l'IA

    public AIPlayer(Geometry aiPaddle, Geometry puck, float aiSpeed) {
        this.aiPaddle = aiPaddle;
        this.puck = puck;
        this.aiSpeed = aiSpeed;
    }

    public void setAiSpeed(float aiSpeed) {
        this.aiSpeed = aiSpeed;
    }

    public void increaseAiSpeed(float increment) {
        aiSpeed += increment;
    }

    public void update(float tpf) {
        // Obtenez la position du palet
        Vector3f puckPosition = puck.getLocalTranslation();

        // Obtenez la position de l'IA
        Vector3f aiPosition = aiPaddle.getLocalTranslation();

        // Calculez la direction dans laquelle l'IA doit se déplacer pour atteindre le palet
        Vector3f direction = puckPosition.subtract(aiPosition);
        direction.y = 0; // Nous ne voulons pas que l'IA se déplace en hauteur
        direction.normalizeLocal();

        // Déplacez l'IA dans la direction du palet à la vitesse définie
        aiPaddle.getControl(RigidBodyControl.class).setLinearVelocity(direction.mult(aiSpeed));
    }

    public void updateWithAnticipation(float tpf) {
        // Obtenez la position du palet
        Vector3f puckPosition = puck.getLocalTranslation();

        // Obtenez la vitesse du palet
        Vector3f puckVelocity = puck.getControl(RigidBodyControl.class).getLinearVelocity();

        // Prédisez la position future du palet en utilisant sa vitesse
        Vector3f predictedPuckPosition = puckPosition.add(puckVelocity.mult(tpf));

        // Obtenez la position de l'IA
        Vector3f aiPosition = aiPaddle.getLocalTranslation();

        // Calculez la direction dans laquelle l'IA doit se déplacer pour atteindre la position prédite du palet
        Vector3f direction = predictedPuckPosition.subtract(aiPosition);
        direction.y = 0; // Nous ne voulons pas que l'IA se déplace en hauteur
        direction.normalizeLocal();

        // Déplacez l'IA dans la direction du palet à la vitesse définie
        aiPaddle.getControl(RigidBodyControl.class).setLinearVelocity(direction.mult(aiSpeed));
    }
}