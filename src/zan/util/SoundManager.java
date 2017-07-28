package zan.util;

import java.io.File;
import java.net.URL;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemJPCT;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryJavaSound;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager {
	private static final String LOGNAME = "SoundManager :: ";
	
	private final static String SND_DIR = "res/snd/";
	
	private static SoundSystem sndSystem;
	
	public static void init() {
		try {
			SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
			SoundSystemConfig.addLibrary(LibraryJavaSound.class);
			SoundSystemConfig.setCodec("wav", CodecJOrbis.class);
			SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
		} catch(SoundSystemException e) {
		    System.err.println(LOGNAME + "Error linking with sound plug-ins");
		}
		sndSystem = new SoundSystemJPCT();
		
		//(!) Temporary Background Music
		try {
			URL url = new File(SND_DIR + "bgm/humoresky.ogg").toURI().toURL();
			sndSystem.newStreamingSource(true, "BGM", url, "humoresky.ogg", true, 0f, 0f, 0f, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
		} catch(Exception e) {
			System.err.println(LOGNAME + "Error loading background music: " + SND_DIR + "bgm/spacerock.ogg" + ":\n" + e);
		}
		sndSystem.setVolume("BGM", 0.05f);
		sndSystem.play("BGM");
	}
	public static void cleanup() {
		sndSystem.cleanup();
	}
	
	public static void loadSound(String fnm) {
		String name = getPrefix(fnm);
		try {
			URL url = new File(SND_DIR + fnm).toURI().toURL();
			sndSystem.newSource(false, name, url, fnm, false, 0f, 0f, 0f, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
		} catch(Exception e) {
			System.err.println(LOGNAME + "Error loading sound: " + SND_DIR + fnm + ":\n" + e);
		}
	}
	public static void playSFX(String name) {
		if (sndSystem.playing(name)) sndSystem.stop(name);
		sndSystem.play(name);
	}
	
	private static String getPrefix(String fnm) {
		int posn;
		if ((posn = fnm.lastIndexOf(".")) == -1) {
			System.out.println(LOGNAME + "No prefix found for filename: " + fnm);
			return fnm;
		} else return fnm.substring(0, posn);
	}
}
