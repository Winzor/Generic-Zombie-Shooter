package genericzombieshooter;

import genericzombieshooter.actors.Player;
import genericzombieshooter.actors.Zombie;
import genericzombieshooter.misc.Globals;
import genericzombieshooter.misc.Sounds;
import genericzombieshooter.structures.Particle;
import genericzombieshooter.structures.Vector2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains many of the methods used to update game objects and handles the game
 * thread.
 *
 * @author packetpirate
 */
public class GZSFramework {
    // Member variables.
    public GZSCanvas canvas;
    
    private long zSpawn;
    public long getSpawnTime() { return zSpawn; }
    
    // Game objects.
    private Player player; // The player character.
    public Player getPlayer() { return player; }
    private List<Zombie> zombies;
    public List<Zombie> getZombies() { return zombies; }
    private List<Particle> projectiles;
    public List<Particle> getProjectiles() { return projectiles; }
    
    private int score; // The player's current score.
    public int getScore() { return score; }

    public GZSFramework() {
        canvas = new GZSCanvas(this);

        { // Begin initializing member variables.
            Globals.keys = new boolean[4];
            for (boolean b : Globals.keys) b = false;
            Globals.buttons = new boolean[2];
            for (boolean b : Globals.buttons) b = false;

            Globals.mousePos = new Point(0, 0);
        } // End member variable initialization.
        
        zSpawn = Globals.SPAWN_TIME;

        { // Begin initializing game objects.
            player = new Player(((Globals.W_WIDTH / 2) - 20), ((Globals.W_HEIGHT / 2) - 20), 40, 40);
            zombies = new ArrayList<Zombie>();
            projectiles = new ArrayList<Particle>();
        } // End game object initialization.

        { // Begin adding key and mouse listeners to canvas.
            canvas.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent k) {
                    if (k.getKeyCode() == KeyEvent.VK_W) Globals.keys[0] = true;
                    if (k.getKeyCode() == KeyEvent.VK_A) Globals.keys[1] = true;
                    if (k.getKeyCode() == KeyEvent.VK_S) Globals.keys[2] = true;
                    if (k.getKeyCode() == KeyEvent.VK_D) Globals.keys[3] = true;
                }

                @Override
                public void keyReleased(KeyEvent k) {
                    if (k.getKeyCode() == KeyEvent.VK_W) Globals.keys[0] = false;
                    if (k.getKeyCode() == KeyEvent.VK_A) Globals.keys[1] = false;
                    if (k.getKeyCode() == KeyEvent.VK_S) Globals.keys[2] = false;
                    if (k.getKeyCode() == KeyEvent.VK_D) Globals.keys[3] = false;
                    if (k.getKeyCode() == Globals.ASSAULT_RIFLE.getKey()) player.setWeapon(1);
                    if (k.getKeyCode() == Globals.SHOTGUN.getKey()) player.setWeapon(2);
                    if (k.getKeyCode() == Globals.FLAMETHROWER.getKey()) player.setWeapon(3);
                }
            });

            canvas.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent m) {
                    if (m.getButton() == MouseEvent.BUTTON1) Globals.buttons[0] = true;
                    if (m.getButton() == MouseEvent.BUTTON3) Globals.buttons[1] = true;
                }

                @Override
                public void mouseReleased(MouseEvent m) {
                    if (m.getButton() == MouseEvent.BUTTON1) Globals.buttons[0] = false;
                    if (m.getButton() == MouseEvent.BUTTON3) Globals.buttons[1] = false;
                }
            });

            canvas.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent m) {
                    Globals.mousePos.x = m.getX();
                    Globals.mousePos.y = m.getY();
                }

                @Override
                public void mouseDragged(MouseEvent m) {
                    Globals.mousePos.x = m.getX();
                    Globals.mousePos.y = m.getY();
                }
            });
        } // End adding key and mouse listeners to canvas.

        Sounds.init();
        initializeThread();
        startThread();
    }

    /**
     * Updates the game objects in the animation loop.
     **/
    public void update() {
        // Calculate the player's angle based on the mouse position.
        double cX = player.getCenterX();
        double cY = player.getCenterY();
        double pAngle = Math.atan2((cY - Globals.mousePos.y), (cX - Globals.mousePos.x)) - Math.PI / 2;
        player.rotate(pAngle);
        
        // Move the player according to which keys are being held down.
        for(int i = 0; i < Globals.keys.length; i++)  {
            if(Globals.keys[i]) player.move(i);
        }
        
        // If the left mouse button is held down, create a new projectile.
        if(Globals.buttons[0]) {
            Point target = new Point(Globals.mousePos);
            Point2D.Double pos = new Point2D.Double((player.x + 28), (player.y - 8));
            double theta = Math.atan2((target.x - pos.x), (target.y - pos.y));
            AffineTransform.getRotateInstance(pAngle, player.getCenterX(), player.getCenterY()).transform(pos, pos);
            player.getWeapon().fire(theta, pos);
        }
        player.decCooldown();
        
        // Update zombie vectors and positions.
        if(!zombies.isEmpty()) {
            for(Zombie z : zombies) {
                // Update the zombie animation.
                z.getImage().update();
                
                // Update the zombie's movement vector.
                Vector2D v_ = new Vector2D((player.getCenterX() - z.getCenterX()), 
                                           (player.getCenterY() - z.getCenterY()));
                Vector2D n_ = v_.normalize();
                if(v_.getLength() >= 5) {
                    // Update the zombie's position on the screen.
                    z.x += n_.x;
                    z.y += n_.y;
                    z.getImage().move((int)z.x, (int)z.y);
                    
                    // Update the zombie's rotation toward the player.
                    double angle = Math.atan2((z.getCenterY() - player.getCenterY()), 
                                              (z.getCenterX() - player.getCenterX())) - Math.PI / 2;
                    z.rotate(angle);
                }
            }
        }
        
        // If the zombie spawn counter has reached 0, spawn a new zombie.
        if(zSpawn == 0) {
            createZombie();
            zSpawn = Globals.SPAWN_TIME;
        } else zSpawn -= 1;
        
        // If the player is touching a zombie, damage him according to the zombie's damage.
        if(!zombies.isEmpty()) {
            for(Zombie z : zombies) {
                if(player.intersects(z)) player.takeDamage(z.getDamage());
            }
        }
        // Update the player's weapon, including its ammo.
        player.getWeapon().updateWeapon();
        // Check for collisions between zombies and ammo.
        Iterator<Zombie> it = this.zombies.iterator();
        while(it.hasNext()) {
            Zombie z = it.next();
            int damage = player.getWeapon().checkForDamage(z);
            if(damage > 0) {
                z.takeDamage(damage);
                if(z.isDead()) {
                    score += z.getScore();
                    it.remove();
                }
            }
        }
    }
    
    /**
     * Creates a new zombie on the screen.
     **/
    private void createZombie() {
        // Decide which side of the screen to spawn the zombie on.
        int spawnSide = Globals.r.nextInt(4) + 1;
        
        double x_ = 0;
        double y_ = 0;
        double w_ = 40;
        double h_ = 40;
        
        switch(spawnSide) {
            case 1:
                x_ = Globals.r.nextInt((Globals.W_WIDTH - 40) + 1);
                break;
            case 2:
                x_ = Globals.W_WIDTH - 40;
                y_ = Globals.r.nextInt((Globals.W_HEIGHT - 40) + 1);
                break;
            case 3:
                x_ = Globals.r.nextInt((Globals.W_WIDTH - 40) + 1);
                y_ = Globals.W_HEIGHT - 40;
                break;
            case 4:
                y_ = Globals.r.nextInt((Globals.W_HEIGHT - 40) + 1);
                break;
        }
        
        // Create the zombie.
        String fileName_ = "/resources/images/GZS_Zombie_2.png";
        Rectangle2D.Double rect_ = new Rectangle2D.Double(x_, y_, w_, h_);
        Zombie z_ = new Zombie(rect_, 250, 1, 100, fileName_);
        zombies.add(z_);
    }

    /**
     * Initializes the main animation thread.
     **/
    private void initializeThread() {
        Globals.animation = new Runnable() {
            @Override
            public void run() {
                Globals.running = true;
                while (Globals.running) {
                    update();
                    canvas.repaint();

                    try {
                        Thread.sleep(Globals.SLEEP_TIME);
                    } catch (InterruptedException ie) {
                        System.out.println("ERROR: Problem occured in main thread.");
                    }
                }
                System.exit(0);
            }
        };
    }

    /**
     * Creates a new thread from the animation Runnable and then starts it.
     **/
    private void startThread() {
        new Thread(Globals.animation).start();
    }

    /**
     * Stops the animation thread.
     **/
    private void stopThread() {
        Globals.animation = null;
    }
}
