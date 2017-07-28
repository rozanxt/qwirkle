package zan.util;

import java.util.Random;

public class GameUtility {
	
	private static Random rnd;
	
	public static void init() {
		rnd = new Random();
	}
	
	public static Random getRnd() {return rnd;}
	
	public static boolean isIntegerString(String str) {
		try {
			Integer.parseInt(str);
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
}
