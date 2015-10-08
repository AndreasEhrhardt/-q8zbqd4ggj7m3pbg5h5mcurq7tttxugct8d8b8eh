//###########################################################
//## Imports

import java.util.*;

//###########################################################
//## Class

public class Spiel {

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++ Properties

	private Spielbrett gameboard;
	private Spieler gamer [];
	private Spieler currentGamer;

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++ Constructor

	public Spiel(){
		gamer = new Spieler[2];

		try{
			// Create keyboard reader
			Scanner sc = new Scanner(System.in);
			
			//+++++++++++++++++++++++++++++++++++++++++++
			//++ Get field size and create new gameboard
			
			// Define min field size and max field size
			int maxField = 1001, minField = 4;
			System.out.println("Bitte Spielfeldgr��e angeben (" + minField + "-" + maxField + "):");
			int fieldCount = 8;
			for(int i = 0; i < 1001; i++){
				// Read next field size
				fieldCount = sc.nextInt();
				// If size is valid, leave loop
				if(fieldCount >= minField && fieldCount <= maxField) break;
				// Check if endless loop
				if(i == 1000){ 
					System.out.println("No valid number detected, we will choose the value 8");
					fieldCount = 8;
				}
			}
			this.gameboard = new Spielbrett(fieldCount);
			
			// Get player 1 informations
			
		}
		catch(Exception e){
			System.out.println("Some errors appear");
		}
	}

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++ Methods


	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++ Methods ( Getter)


	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++ Methods ( Setter)


	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++ Methods (Override)

}