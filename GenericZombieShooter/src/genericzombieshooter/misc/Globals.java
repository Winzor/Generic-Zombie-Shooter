package genericzombieshooter.misc;

import genericzombieshooter.structures.weapons.AssaultRifle;
import genericzombieshooter.structures.weapons.Flamethrower;
import genericzombieshooter.structures.weapons.Shotgun;
import java.awt.Point;
import java.util.Random;

/**
 * Contains publicly accessible variables for the entire project.
 * @author packetpirate
 */
public class Globals {
    // Final
    public static final int W_WIDTH = 800; // The width of the game window.
    public static final int W_HEIGHT = 640; // The height of the game window.
    public static final long SLEEP_TIME = 20; // The sleep time of the animation thread.
    public static final long SPAWN_TIME = 50; // The time it takes zombies to spawn. (SLEEP_TIME * SPAWN_TIME)
    public static final Random r = new Random();
    
    // Non-Final
    public static Runnable animation; // The primary animation thread.
    public static boolean running; // Whether or not the game is currently running.
    
    public static boolean [] keys; // The state of the game key controls.
    public static boolean [] buttons; // The state of the game mouse button controls.
    
    public static Point mousePos; // The current position of the mouse on the screen.
    
    public static AssaultRifle ASSAULT_RIFLE = new AssaultRifle();
    public static Shotgun SHOTGUN = new Shotgun();
    public static Flamethrower FLAMETHROWER = new Flamethrower();
}
