package genericzombieshooter.structures.weapons;

import genericzombieshooter.misc.Globals;
import genericzombieshooter.structures.Particle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Used to represent the Assault Rifle weapon.
 * @author Darin Beaudreau
 */
public class AssaultRifle extends Weapon {
    // Final Variables
    private static final int DEFAULT_AMMO = 60;
    private static final int MAX_AMMO = 300;
    private static final int AMMO_PER_USE = 0;
    private static final int DAMAGE_PER_PARTICLE = 150;
    private static final double PARTICLE_SPREAD = 10.0;
    private static final int PARTICLE_LIFE = 2000;
    
    // Member Variables
    private List<Particle> particles;
    public List<Particle> getParticles() { return this.particles; }
    
    public AssaultRifle() {
        super("RTPS", KeyEvent.VK_1, DEFAULT_AMMO, MAX_AMMO, AMMO_PER_USE, 10);
        this.particles = new ArrayList<Particle>();
    }
    
    @Override
    public void updateWeapon() {
        // Update all particles and remove them if their life has expired or they are out of bounds.
        Iterator<Particle> it = this.particles.iterator();
        while(it.hasNext()) {
            Particle p = it.next();
            p.update();
            if(!p.isAlive()) {
                it.remove();
                continue;
            }
            if(p.outOfBounds()) {
                it.remove();
                continue;
            }
        }
        this.cool();
    }
    
    @Override
    public void drawAmmo(Graphics2D g2d) {
        // Draw all particles whose life has not yet expired.
        if(this.particles.size() > 0) {
            g2d.setColor(Color.ORANGE);
            for(Particle p : this.particles) {
                if(p.isAlive()) p.draw(g2d);
            }
        }
    }
    
    @Override
    public void fire(double theta, Point2D.Double pos) {
        // If there is enough ammo left...
        if(this.canFire()) {
            // Create a new bullet and add it to the list.
            int width = 4;
            int height = 10;
            this.particles.add(new Particle(theta, AssaultRifle.PARTICLE_SPREAD, 8.0,
                                            (AssaultRifle.PARTICLE_LIFE / (int)Globals.SLEEP_TIME), pos,
                                            new Dimension(width, height)));
            // Use up ammo.
            this.consumeAmmo();
            this.resetCooldown();
        }
    }
    
    @Override
    public int checkForDamage(Rectangle2D.Double rect) {
        int damage = 0;
        // Check all particles for collisions with the target rectangle.
        Iterator<Particle> it = this.particles.iterator();
        while(it.hasNext()) {
            Particle p = it.next();
            // If the particle is still alive and has collided with the target.
            if(p.isAlive() && rect.contains(p.getPos())) {
                // Add the damage of the particle and remove it from the list.
                damage += DAMAGE_PER_PARTICLE;
                it.remove();
            }
        }
        return damage;
    }
}
