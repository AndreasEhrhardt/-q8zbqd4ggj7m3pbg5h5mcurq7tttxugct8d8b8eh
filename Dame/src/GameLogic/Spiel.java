package GameLogic;
//###########################################################
//## Imports

import java.io.*;
import java.util.*;

import Enumerations.FarbEnum;
import Interfaces.iBediener;
import KI.KI_Dame;

import java.awt.*;

//###########################################################
//## Class

public class Spiel implements iBediener {
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//++ Exceptions

	static class eNoDiagonalMoveException extends Exception{}
	static class eInvalidPointException extends Exception{}
	static class eSamePositionException extends Exception{}
	static class eOutOfGameboardException extends Exception{}
	static class eDestinationPointIsBlockedException extends Exception{}
	static class eNoFigureFoundOnFieldException extends Exception{}
	static class eSomeOtherMoveErrors extends Exception{}
	static class eEnemyFigureSelectedException extends Exception{}
	static class eDistanceToFarException extends Exception{}
	static class eNoBackJumpExcpetion extends Exception{}
	static class eWayIsBlocked extends Exception{}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ++ Properties

	private String saveGameName = "./savegame.data";
	private Spielbrett gameboard;
	private Spieler gamer[];
	private Spieler currentGamer;
	static int maxLoopCount = 10;

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ++ Constructor

	/**
	 * Constructor
	 */
	public Spiel() {
		// Initialize game
		this.initialize();

		// Start game-loop
		this.gameLoop();
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ++ Methods

	/**
	 * Initializes new game and creates a new gameboard and two new players.
	 */
	private void initialize(){
		gamer = new Spieler[2];

		// Create gameboard
		this.gameboard = this.createGameBoard();

		if(askNewGame()){
			// Create gamer 1
			gamer[0] = getPlayer(1);

			// Create gamer 2
			gamer[1] = getPlayer(2);
		}
		else{
			this.loadGame();
		}

		// Set start player
		this.currentGamer = gamer[0];
	}

	/**
	 * The game-loop is the main loop of the application.
	 * The loop checks for finished
	 */
	private void gameLoop(){
		while(!gameFinished()){
			// Output current gameboard
			this.outputGameboardCSV();

			// Current player have to move
			this.currentGamer.move(this);

			// Set next player
			if(this.currentGamer == this.gamer[0])
				this.currentGamer = this.gamer[1];
			else
				this.currentGamer = this.gamer[0];
		}
	}

	/**
	 * Checks if move is valid else it will throw exceptions
	 * 
	 * @param fromPoint
	 * @param toPoint
	 * @return True if move is valid
	 * @throws Spiel.eSamePositionException
	 * @throws Spiel.eNoDiagonalMoveException
	 * @throws Spiel.eOutOfGameboardException
	 * @throws Spiel.eNoFigureFoundOnFieldException
	 * @throws Spiel.eDestinationPointIsBlockedException
	 */
	public boolean moveIsValid(Point fromPoint, Point toPoint) 
			throws Spiel.eSamePositionException, Spiel.eNoDiagonalMoveException, Spiel.eOutOfGameboardException,
			Spiel.eNoFigureFoundOnFieldException, Spiel.eDestinationPointIsBlockedException,
			Spiel.eDistanceToFarException, Spiel.eEnemyFigureSelectedException
	{
		int diffX = (int)(toPoint.getX() - fromPoint.getX());
		int diffY = (int)(toPoint.getY() - fromPoint.getY());

		Spielfeld fromField = this.gameboard.getField((int)fromPoint.getX(), (int)fromPoint.getY());
		Spielfeld toField = this.gameboard.getField((int)toPoint.getX(), (int)toPoint.getY());

		// Check if both fields are the same
		if(fromPoint.equals(toPoint)) throw new Spiel.eSamePositionException();

		// Check if move is diagonal
		if(!(diffX == diffY || (diffX * (-1) == diffY))) throw new Spiel.eNoDiagonalMoveException();

		// Check if toPoint and fromPoint are valid fields
		if(!this.isValidField(fromPoint, toPoint)) throw new Spiel.eOutOfGameboardException();
		
		// Check if field have figure
		Spielfigur gameFigure = fromField.getFigure();
		if(gameFigure == null) throw new Spiel.eNoFigureFoundOnFieldException();

		// Check if destination have already a destination
		Spielfigur destinationfigure = toField.getFigure();
		if(destinationfigure != null) throw new Spiel.eDestinationPointIsBlockedException();

		// Check if figure is jumping to far
		if(!gameFigure.isDame()){
			if(diffX > 1 || (diffX * (-1)) > 1){
				if(!((diffX == 2 || (diffX * (-1)) == 2))){
					throw new Spiel.eDistanceToFarException();
				}
				else{
					System.out.println(((diffX / 2)));
					Spielfigur midfigure = this.gameboard.getField((int)fromPoint.getX() + (diffX / 2),(int)fromPoint.getY() + (diffY / 2)).getFigure();
					System.out.println("TEST2");
					if(midfigure == null || midfigure.getColor() == this.currentGamer.getColor()){
						throw new Spiel.eDistanceToFarException();
					}
				}
			}
		}

		// Check if figure is from enemy team
		if(gameFigure.getColor() != this.currentGamer.getColor()) throw new Spiel.eEnemyFigureSelectedException();

		return true;
	}

	/**
	 * @param fromPoint
	 * @param toPoint
	 * @return
	 */
	private boolean isValidField(Point fromPoint, Point toPoint){
		int boardSize = this.gameboard.getFields().length;

		if(fromPoint.getX() < 0 || fromPoint.getX() >= boardSize ||
				fromPoint.getY() < 0 || fromPoint.getY() >= boardSize ||
				toPoint.getX() < 0 || toPoint.getX() >= boardSize ||
				toPoint.getY() < 0 || toPoint.getY() >= boardSize)
		{
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Method for moving on the board, but before it actually moves it calls the moveIsValid 
	 * function to check if the move is valid.
	 * If you ate a token from the opponent, the token will be removed from the board.
	 * 
	 * @param fromPoint
	 * @param toPoint
	 * @throws Spiel.eSamePositionException
	 * @throws Spiel.eNoDiagonalMoveException
	 * @throws Spiel.eOutOfGameboardException
	 * @throws Spiel.eNoFigureFoundOnFieldException
	 * @throws Spiel.eDestinationPointIsBlockedException
	 * @throws Spiel.eSomeOtherMoveErrors
	 */
	public void move(Point fromPoint, Point toPoint)
			throws Spiel.eSamePositionException, Spiel.eNoDiagonalMoveException, Spiel.eOutOfGameboardException,
			Spiel.eNoFigureFoundOnFieldException, Spiel.eDestinationPointIsBlockedException, Spiel.eSomeOtherMoveErrors,
			Spiel.eDistanceToFarException, Spiel.eEnemyFigureSelectedException
	{		
		if(this.moveIsValid(fromPoint, toPoint)){
			// Get fields
			Spielfeld fromField = this.gameboard.getField((int)fromPoint.getX(), (int)fromPoint.getY());
			Spielfeld toField = this.gameboard.getField((int)toPoint.getX(), (int)toPoint.getY());

			// Get moveable figure
			Spielfigur gameFigure = fromField.getFigure();

			// Remove every figure on the move-line
			boolean removed = this.removeFigures(fromPoint, toPoint);

			// Check for the "blowing"-rule
			if(!removed) this.checkForBlowing();

			// Check if figure is after blowing already exist
			if(fromField.getFigure() == gameFigure){
				// Set new coordinations to figure
				gameFigure.setPoint(toPoint);

				// Remove figure from old field and set to new field
				toField.setFigure(gameFigure);
				fromField.removeFigure();


				if(this.canDestroyOtherFigures(toPoint)){

				}

			}
		}
		else{
			throw new eSomeOtherMoveErrors();
		}
	}

	private boolean canDestroyOtherFigures(Point point){
		int xCurrent = (int)point.getX(), yCurrent = (int)point.getY();

		Spielfeld felder[][] = this.gameboard.getFields();
		for(int i = 0; i < felder.length; i++){
			for(int j = 0; j < felder[i].length; j++){
				Spielfigur figure = felder[i][j].getFigure();
				if(figure != null){
					if(figure.getColor() != this.currentGamer.getColor()){
						Point movePoint = new Point(i,j);
						if(xCurrent < (int)movePoint.getX()) 
							movePoint.setLocation(movePoint.getX() + 1, movePoint.getY());
						else 
							movePoint.setLocation(movePoint.getX() - 1, movePoint.getY());

						if(yCurrent < (int)movePoint.getY()) 
							movePoint.setLocation(movePoint.getX(), movePoint.getY() + 1);
						else 
							movePoint.setLocation(movePoint.getX(), movePoint.getY() - 1);

						try{
							if(moveIsValid(point,movePoint)) return true;
						}
						catch(Exception e){}
					}
				}
			}
		}
		return false;
	}

	private void checkForBlowing(){

	}

	private boolean removeFigures(Point fromPoint, Point toPoint){
		int moveX, moveY, currentX = (int)fromPoint.getX(), currentY = (int)fromPoint.getY();
		boolean removed = false;

		if(fromPoint.getX() < toPoint.getX()) moveX = 1;
		else moveX = -1;

		if(fromPoint.getY() < toPoint.getY()) moveY = 1;
		else moveY = -1;

		do{
			currentX += moveX;
			currentY += moveY;

			Spielfeld currentField = this.gameboard.getField(currentX, currentY);
			Spielfigur currentFigure = currentField.getFigure();

			if(currentFigure != null){
				if(currentFigure.getColor() == this.currentGamer.getColor()) throw new RuntimeException();

				currentField.removeFigure();

				removed = true;
			}
		}while(currentX != toPoint.getX() & currentY != toPoint.getY());

		return removed;
	}

	/**
	 * Method for creating variable sized board.
	 * Gets the size and then creates the gameboard.
	 */
	private Spielbrett createGameBoard() {
		// Get gameboard size
		int fieldCount = getGameboardSize();

		// Create gameboard
		return new Spielbrett(fieldCount);
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ++ Methods ( Getter)

	/**
	 * @return
	 */
	public Spielbrett getGameboard(){
		return this.gameboard;
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ++ Methods ( Setter)

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// ++ Methods (Override)

	/**
	 * Forces the user to enter game board size and checks if number is even.
	 * If user fails to enter valid size multiple times, the size is set to 8x8
	 */
	@Override
	public int getGameboardSize() {
		// Create scanner
		Scanner sc = new Scanner(System.in);

		// Get field size
		int maxField = 20, minField = 8, fieldCount = 8;
		for (int i = 0; i <= maxLoopCount; i++) {
			try{
				// Output information
				System.out.print("Bitte Spielfeldgr��e angeben (" + minField + "-" + maxField + "):");
				// Read next field size
				fieldCount = sc.nextInt();
				// If size is valid, leave loop
				if (fieldCount >= minField && fieldCount <= maxField){
					if(fieldCount % 2 == 0) break;
					else System.out.println("Nur gerade Spielfeldgr��en sind erlaubt!");
				}
			}
			catch(NoSuchElementException | IllegalStateException e ){
				// Clear input buffer
				sc.nextLine();

				// Continue loop
				continue;
			}
			finally{
				// Check if endless loop
				if (i == this.maxLoopCount) {
					System.out.println("No valid number detected, we will choose the value 8");
					fieldCount = 8;
				}
			}
		}
		return fieldCount;
	}

	/**
	 * This method is used to check if players are human or KIs and asks for the name of human players.
	 * If all entry is valid, it starts the game with the given data.
	 * If user fails to properly decide if player is human or KI multiple times, it is set to KI automatically
	 * If user fails to enter a proper name multiple times, it is set to Peter automatically
	 * 
	 * @param playerNumber
	 * @return
	 * 
	 */
	@Override
	public Spieler getPlayer(int playerNumber) {
		// create Scanner
		Scanner sc = new Scanner(System.in);

		// create Gamer
		int gamerID = 0;
		for (int i = 0; i <= maxLoopCount; i++) {
			try{
				// Get current color name
				String colorName;
				if(playerNumber == 1) colorName = "white";
				else colorName = "black";

				// Get gamer type
				System.out.println("Spieler " + playerNumber + " (" + colorName + "): Spieler (1) oder KI(2)?");
				System.out.print("Ihre Eingabe: ");
				gamerID = sc.nextInt();
				System.out.println("");

				// Check if type is valid
				if (gamerID == 1 || gamerID == 2)
					break;
			}catch(NoSuchElementException | IllegalStateException e ){
				// Clear input buffer
				sc.nextLine();
			} finally{
				// Check if endless loop
				if (i == maxLoopCount) {
					System.out.println("No valid number detected, we will choose KI for you");
					gamerID = 2;
				}
			}

		}

		// Create temp-reference
		Spieler newGamer;

		// Set player color
		FarbEnum color;
		if(playerNumber == 1) color = FarbEnum.wei�;
		else color = FarbEnum.schwarz;

		// Create player
		if (gamerID == 1) {
			String gamerName = "";
			for (int i = 0; i <= maxLoopCount; i++) {
				System.out.print("Bitte Spielername eingeben:");
				gamerName = sc.next();
				System.out.println("");

				if (!gamerName.isEmpty())
					break;

				if (i == maxLoopCount) {
					System.out.println("No name insert, we will call you Peter");
					gamerName = "Peter";
				}
			}

			// Create new normal player
			newGamer =  new Spieler(gamerName, color);
		} else if (gamerID == 2) {
			// Create new KI-Player
			newGamer = new Spieler(new KI_Dame(), color);
		} else {
			// Create a default player
			newGamer = new Spieler();
		}

		// Return new gamer
		return newGamer;
	}


	@Override
	public void nextMove()	{

	}

	/**
	 * This method is used to save the game to a CSV file
	 */
	@Override
	public void outputGameboardCSV(){
		// Get gameboard fields
		Spielfeld felder[][] = this.gameboard.getFields();

		// Define start variable
		char currentRow = (char)(65 + felder.length - 1);
		int currentColumn = 1;

		// Create empty line
		System.out.println("");

		// For every row - DESC
		for(int i = felder.length - 1; i >= 0; i--){
			currentColumn = 1;

			// Write colum name
			System.out.print(currentRow);

			// For every column
			for(int j = 0; j < felder.length; j++){
				// Get figure of field
				Spielfigur currentFigure = felder[j][i].getFigure();

				// Write seperator
				System.out.print(";");

				// Check if field have figure or not
				if(currentFigure == null) System.out.print("  ");
				else{ 
					if(currentFigure.getColor() == FarbEnum.wei�) {
						if(currentFigure.isDame())
							System.out.print("W+");
						else
							System.out.print("W ");
					}
					else
						if(currentFigure.isDame())
							System.out.print("S+");
						else
							System.out.print("S ");
				}

				// Increase column value
				currentColumn++;
			}

			// End of line
			System.out.println("");

			// Increase row value
			int ascii = currentRow;
			currentRow = (char)--ascii;
		}

		// Draw column names
		for(int i = 0; i <= felder.length; i++){
			if(i == 0) System.out.print(" ");
			else{
				System.out.print(";");
				System.out.print(String.format("%02d",i));
			}
		}
		System.out.println("");

		// Create empty line
		System.out.println("");
	}

	@Override
	public void loadingScreen() {
		// TODO Auto-generated method stub

	}

	/**
	 * This method is used to read a CSV to continue playing where left off
	 */
	@Override
	public void loadGame() {
		try{
			// Open file stream
			FileInputStream f_in = new FileInputStream(saveGameName);
			ObjectInputStream obj_in = new ObjectInputStream (f_in);

			// Read object
			Object obj = obj_in.readObject();

			// Check if object is from same class
			if(obj.getClass() == Spiel.class){
				// Parse object
				Spiel lastGame = (Spiel)obj;

				// Get game-data
				this.gamer[0] = lastGame.gamer[0];
				this.gamer[1] = lastGame.gamer[1];
				this.gameboard = lastGame.gameboard;
				this.currentGamer = lastGame.currentGamer;
			}
		}
		catch(IOException | ClassNotFoundException e){
			// Output error message
			System.out.println("Savegame is corrupt");

			// Exit game
			System.exit(-1);
		}
	}

	/**
	 * This saves the the game
	 */
	@Override
	public void saveGame() {
		try{
			// Save game state
			FileOutputStream game = new FileOutputStream(saveGameName);
			ObjectOutputStream gameObjStream = new ObjectOutputStream (game);
			gameObjStream.writeObject(this);

			// Close file handle
			gameObjStream.close();
		}
		catch(IOException e){
			// File save error
			System.out.println("Cant save game - state");
		}
	}

	/**
	 * This checks if game is finished
	 */
	@Override
	public boolean gameFinished() {
		int whiteFigures = 0, blackFigures = 0;

		Spielfeld felder[][] = this.gameboard.getFields();
		for(int i = 0; i < felder.length; i++){
			for(int j = 0; j < felder[i].length; j++){
				Spielfigur figure = felder[i][j].getFigure();
				if(figure != null){
					if(figure.getColor() == FarbEnum.schwarz) blackFigures++;
					else if(figure.getColor() == FarbEnum.wei�) whiteFigures++;
				}
			}
		}

		if(whiteFigures == 0 || blackFigures == 0){
			String winName;
			if(whiteFigures == 0) winName = FarbEnum.getColorName(FarbEnum.schwarz);
			else winName = FarbEnum.getColorName(FarbEnum.wei�);

			System.out.println("Herzlichen Gl�ckwunsch " + winName + "! Sie haben gewonnen");

			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * This method is used to ask if user wants to start a new game or continue playing from a saved game
	 */
	@Override
	public boolean askNewGame() {
		// Create help variables
		int gameType = 0;
		Scanner sc = new Scanner(System.in);

		// Check if savegame avaiable
		File f = new File(saveGameName);
		if(!f.exists() || f.isDirectory()) { 
			return true;
		}

		// Get gametype
		for (int i = 0; i <= maxLoopCount; i++) {
			try{
				// Ask for new game / load game
				System.out.print("Create new game (1) or load game (2): ");

				// Get result
				gameType = sc.nextInt();

				// Go to next line
				System.out.println("");

				// Check if result is valid
				if(gameType  == 1 || gameType == 2) break;

			}catch(NoSuchElementException | IllegalStateException e ){
				// Clear input buffer
				sc.nextLine();
			} finally{
				// Check if endless loop
				if (i == maxLoopCount) {
					System.out.println("No valid number detected, we will choose 'new game'");
					gameType = 1;
				}
			}
		}

		// Return result
		if(gameType == 1) return true;
		else return false;
	}
}