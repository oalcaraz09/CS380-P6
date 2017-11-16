

/**
 * Oscar Alcaraz
 * CS 380 Project 6
 * Tic Tac Toe
 */

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;


public class TicTacToeClient {

    private static String previousMove = "";
    private static byte[][] previousBoard;
    private static BoardMessage MESSAGE;
    private static HashMap<String, Integer> commandList;
    private static final String NEW_GAME = "New Game";

    public static void main(String[] args) {
    	
        try {
        	
            Socket socket = new Socket("18.221.102.182", 38006);
            ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
            
            generateCommandList();
            connectWithUserName(objectOut);
            
            System.out.println("\n Tic-Tac-Toe\n");
            System.out.println("Type 'help' for a list of commands available.");
            
            while(true) {
            	
                byte[][] board = (previousMove.equals("help")) ? previousBoard: receiveBoard(objectIn);
                printBoard(board);
                
                if(MESSAGE.getStatus() != BoardMessage.Status.IN_PROGRESS) {
                	
                    System.out.println(MESSAGE.getStatus());
                    System.out.println("Starting New Game");
                    performCommand(NEW_GAME, objectOut);
                    board = (previousMove.equals("help")) ? previousBoard: receiveBoard(objectIn);
                    printBoard(board);
                    
                }
                do {
                    previousMove = performCommand(enterCommand(), objectOut);
                    
                } while (previousMove.equals("INVALID"));
            }
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void generateCommandList() {
    	
        commandList = new HashMap<>();
        commandList.put(NEW_GAME, 0);
        commandList.put("exit", 1);
        commandList.put("help", 4);
        commandList.put("move", 5);
    }

    public static void connectWithUserName(ObjectOutputStream objectOut) throws Exception {
    	
            Scanner inputKB = new Scanner(System.in);
            System.out.print("Enter a Username: ");
            
            ConnectMessage connect = new ConnectMessage(inputKB.nextLine());
            objectOut.writeObject(connect);
            
            performCommand(NEW_GAME, objectOut);
    }


    public static String enterCommand() {
    	
        Scanner inputKB = new Scanner(System.in);
        System.out.print("Command: ");
        
        return inputKB.nextLine();
    }

    public static String performCommand(String command, ObjectOutputStream objectOut) throws Exception {

        String[] move = null;
        
        if(command.charAt(0) == 'm') {
            move = command.split(" ");
            command = move[0];
        }

        if(!commandList.containsKey(command) || (command.charAt(0) == 'm' && move.length < 3))
        	
            return "INVALID";

        CommandMessage commandMsg = null;
        MoveMessage moveMsg = null;
        
        switch (commandList.get(command) ) {
        
            case 0:
            	
                commandMsg = new CommandMessage(CommandMessage.Command.NEW_GAME);
                break;
                
            case 1:
            	
                commandMsg = new CommandMessage(CommandMessage.Command.EXIT);
                System.out.println("Game Stopped");
                System.exit(0);
                break;
                
            case 4:
            	
                System.out.println("move X Y | exit | help");
                return "help";
                
            case 5:
            	
                moveMsg = new MoveMessage(Byte.parseByte(move[1]), Byte.parseByte(move[2]));
                objectOut.writeObject(moveMsg);
                break;
                
        }
        
        objectOut.writeObject(commandMsg);
        
        return "";
    }

    public static byte[][] receiveBoard(ObjectInputStream objectIn) throws Exception{
    	
        Object reveive = objectIn.readObject();
        
        if(reveive instanceof BoardMessage) {
        	
            BoardMessage boardMsg = ((BoardMessage) reveive);
            MESSAGE = boardMsg;
            checkGameStatus(boardMsg);
            
            byte[][] gameBoard = boardMsg.getBoard();
            
            previousBoard = gameBoard;
            
            return gameBoard;
            
       } else if(reveive instanceof ErrorMessage) {
    	   
            System.out.println(((ErrorMessage) reveive).getError());
       }
        
        return previousBoard;
    }

    public static byte[][] checkGameStatus(BoardMessage boardMsg) {
    	
        byte[][] gameBoard;
        
        if(boardMsg.getStatus() == BoardMessage.Status.IN_PROGRESS) {
        	
            gameBoard = boardMsg.getBoard();
            previousBoard = gameBoard;

        } else {
        	
            gameBoard = previousBoard;
        }
        
        return gameBoard;
    }

    public static void printBoard(byte[][] gameBoard) {
    	
        System.out.println("____________");
        
        for(int i = 0; i < gameBoard.length; ++i) {
        	
            System.out.print("|");
            
            for(int k = 0; k < gameBoard.length; ++k) {
            	
                switch (gameBoard[i][k]) {
                
                    case 0:
                    	
                        System.out.print(" _ |");
                        break;
                        
                    case 1:
                    	
                        System.out.print(" X |");
                        break;
                        
                    case 2:
                    	
                        System.out.print(" O |");
                        break;
                        
                }
            }
            
            System.out.println();
        }
        
        System.out.println("_____________");
    }
}