// for playing sound clips
import java.io.*;
import java.util.HashMap;
import javax.sound.sampled.*;

public class SoundManager {
    HashMap<String, Clip> clips;

	private static SoundManager instance = null;	// keeps track of Singleton instance

	private float volume;

    private SoundManager () {

        Clip clip;

        clips = new HashMap<String, Clip>();
        //forest-background
        clip = loadClip("sounds/forest-background.wav");
        clips.put("forest-background", clip);

        clip = loadClip("sounds/StartBgSound.wav");
        clips.put("BgStartGS", clip);

        clip = loadClip("sounds/mouse-click.wav");
        clips.put("btnclick", clip);

        clip = loadClip("sounds/pixel-jump.wav");
        clips.put("jump", clip);

        clip = loadClip("sounds/lego-walking-208360.wav");
        clips.put("walk", clip);

        //Clip clip = loadClip("sounds/background.wav");	// played from start of the game eg for how to populate the hashmap
		//clips.put("background", clip);

        volume = 1.0f;
    }

    public static SoundManager getInstance() {	// class method to retrieve instance of Singleton
		if (instance == null)
			instance = new SoundManager();
		
		return instance;
	}
    
    public Clip loadClip (String fileName) {	// gets clip from the specified file
        AudioInputStream audioIn;
        Clip clip = null;

        try {
                File file = new File(fileName);
                audioIn = AudioSystem.getAudioInputStream(file.toURI().toURL()); 
                clip = AudioSystem.getClip();
                clip.open(audioIn);
        }
        catch (Exception e) {
                System.out.println ("Error opening sound files: " + e);
        }
            return clip;
    }

    public Clip getClip (String title) {

		return clips.get(title);
	}

    public void playClip(String title, boolean looping) {
		Clip clip = getClip(title);
		if (clip != null) {
			clip.setFramePosition(0);
			if (looping)
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			else
				clip.start();
		}
    }


    public void stopClip(String title) {
        Clip clip = getClip(title);
        if (clip != null) {
            clip.stop();
        }
    }

    public void setVolume (String title, float volume) {
		Clip clip = getClip(title);

		FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
	
		float range = gainControl.getMaximum() - gainControl.getMinimum();
		float gain = (range * volume) + gainControl.getMinimum();

		gainControl.setValue(gain);
	}
}
