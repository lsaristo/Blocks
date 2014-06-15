/**
 *  FILE: Solver.java
 *  WRITTEN BY: John Wilkey FOR CS47B Blocks project.
 * 
 *	NOTE: That this program requires Java version 1.7 or later to run.  
 */
import java.io.*;
import java.util.*;

/**
 *	The Solver class.
 *	This is the driver class for the game. 
 *	Refer to Tray.java for Tray and Blocks implementation.
 */
public class Solver
{
	/* Debugging Constants, see -ooptions for usage */
    final static int DEBUG = 1;
    final static int INFO = 2;
    final static int WARN = 4;
    final static int ERROR = 5;
	final static int BENCH = 3;
    final static int SPECIAL = 0;

	/* Solver Algorithms, see -ooptions for usage */
	private static int SOLVER_ALG;

	/* Class members */
	private long startTime;
   	private static String startingConfig;
	private static String endingConfig;
	private Tray startingGame;
	private Tray desiredGame;

	/*
	 * Debug inner class used to control program-wide debugging. 
	 *	Refer to debugging options given in -ooptions
	 */
	private static class Debug {
		private static int SYSTEM_DEBUG_LEVEL = ERROR;
		private static boolean BENCH = false;
		private static boolean SILENT = false;
	}
		
	/* TaskTimer class to implement running time statistics. */ 
	private class Periodic extends TimerTask {
		private int trayCount;
		private int prevSize;
		private int dbSize;

		public Periodic() {
			trayCount 	= prevSize
						= dbSize
						= 0;
		}

		public void run() {
			dPrint(BENCH, "Report Card:\n\t\tElapsed time: " 
					+ ((System.currentTimeMillis() - startTime)/1000) + " seconds"
					+"\n\t\tTrays Tried: " + trayCount + "\n\t\tTable Size: " 
					+ prevSize + "\n\t\tMemory Size: " + dbSize);
		}	
	}

	/*
	 * Constructor for the Solver class.
	 * POSTCONDITION: Set up a new Solver class to solve a puzzle.  
	 */
	public Solver(String startingFile, String desiredFile) {
		startingGame = createGame(startingFile);
		desiredGame = createGame(startingGame.getRows(), startingGame.getCols(), desiredFile);
	}

	/* Enabled with program argument of -ooptions. Prints the help message then exits. */ 
	static void showHelp() {
		System.out.println(
			"  The Blocks Solver Program"
            +"\n\tUsage: Solver.java [-ooption] [initial config] [goal config]"
			+"\n\n\tGeneral options:"
			+"\n\t\toptions\t\tDisplays this help message\n\n\tControl Levels of Debugging Output:"
			+"\n\t\tdebug\t\tMost verbose output"
			+"\n\t\twarn\t\tLess verbose than info. More than error"
			+"\n\t\tinfo\t\tLess verbose than debug. More than warn (implies benchmark)"
			+"\n\t\tsilent\t\tSuppress default program output (i.e., the move list)"
			+"\n\t\tbenchmark\tInclude elapsed running time in output (implied in info)"
			+"\n\n\tAlgorithm options:"
			+"\n\t\talg #\t\tDefine nonstandard algorithm to solve the puzzle"
			+"\n\t\t\t\t\tValid algorithms:"
			+"\n\t\t\t\t\t\tStandard depth-first (per block), first try (default)"
            +"\n\n  Written by: John Wilkey. CS47B\n"
			);
	}

	/*
	 * Debug print routine. Prints the debugging message along with anything else that 
	 *	might be helpful at the time.
	 * Verbosity level range from 1 (DEBUG) to 4 (ERROR) 
	 */
    static void dPrint(int level, String message) {
            String LevelMeaning = "";
            switch (level) {
                case 1: LevelMeaning = "DEBUG";
                    	break;
                case 2: LevelMeaning = "INFO";
                    	break;
                case 3: LevelMeaning = "INFO";
                    	break;
                case 4: LevelMeaning = "WARN";
						break;
				case 5:	LevelMeaning = "ERROR";
						break;	
            }
		 	if( level >= Debug.SYSTEM_DEBUG_LEVEL
				|| (level == BENCH && Debug.BENCH) )
			    System.out.println("[ " + LevelMeaning + " ]: " +  message); 
            else if ( level == SPECIAL && !Debug.SILENT)
                System.out.println(message);
    }
    
	/*
	 *	Read the configuration file provided in program arguments
	 *	generate a new instance of a game Tray and populate that tray with Block(s) as 
	 *	stipulated in the configuration file.
	 *	
	 *	PRECONDITION: startingConfig contains valid Tray and Block descriptions. 
	 *	POSTCONDITION: Returns a new Tray populatd with Block(s). 
	 */
	static private Tray createGame(String config) { 
		Tray game = null;
		try 
			{ game = new Tray(new Scanner(new BufferedReader(new FileReader(config)))); }
		catch (IOException e) 
			{ dPrint(ERROR, "Reached unexpectd end of configuration file");	}
		dPrint(INFO, "Game has been created with the hash " + game.hashCode() + " and following configuration...\n" + game);
		return game;		
	}

    static private Tray createGame(int row, int col, String config) {
        Tray game = null;
        try 
			{ game = new Tray(row, col, new Scanner(new BufferedReader(new FileReader(config)))); }
        catch (IOException e) 
			{ dPrint(ERROR, "Reached unexpectd end of configuration file"); }
        dPrint(INFO, "Game has been created with the hash " + game.hashCode() + " and following configuration...\n" + game);
        return game;
    }

	/*
	 * Process command-line arguments given to the program at runtime. 
	 * Refer to -ooptions for argument descriptions.
	 * 
	 * Throws NullPointerException
	 */
	static private int processArgs(String[] args) throws NullPointerException
	{
		try {
			int index = 0;
			while (args[index].charAt(0) == '-') {
				switch (args[index]) {
					case "-ooptions":	showHelp();
										return -2;
                    case "-odebug":     Debug.SYSTEM_DEBUG_LEVEL = DEBUG;
										dPrint(DEBUG, "** Debug level: DEBUG **");
                                        break;
					case "-oinfo":		Debug.SYSTEM_DEBUG_LEVEL = INFO;
										dPrint(INFO, "** Debug level: INFO **");
										break;
					case "-owarn":  	Debug.SYSTEM_DEBUG_LEVEL = WARN;
										dPrint(WARN, "**  Debug level: WARN **");
										break;
                    case "-osilent":    Debug.SILENT = true;
										dPrint(INFO, "** Supressing move list output **");
                                        break;
					case "-obenchmark":	Debug.BENCH = true;
										break;
					case "-oalg":		index++;
										SOLVER_ALG = new Integer(args[index]);
										break;
					default:			dPrint(ERROR, "Unrecognized option, bail");
										return -1;
				}
				++index;	
			}
			startingConfig = args[index];
			++index;
			endingConfig = args[index];
		}
		catch(ArrayIndexOutOfBoundsException e) {
			dPrint(ERROR, "Fatal Error: Malformed arguments.");
			return -2;
		}
			return 0;
	}


	/*
	 * (Attempt to) solve the current puzzle.
	 * Note that this is really just a wrapper method for calling the requested
	 *	solving algorithm defined in showHelp()
	 * POSTCONDITION: Returns true if a solution is found, false otherwise. 
	 */
	private boolean solvePuzzle(Tray currentTray)
	{
		switch (SOLVER_ALG)
		{
			default:	dPrint(INFO, "Using default solver algorithm"); 
						return algorithm_1(currentTray);
		}
	}

	/* Pseudo depth-first searching algorithm. First try. */ 
    private boolean algorithm_1(Tray currentTray)
    {
		/* Setup state-tracking */
		HashSet<Tray> memory = new HashSet<Tray>();
		Deque<Tray> previousTrays = new ArrayDeque<Tray>();
			previousTrays.add(currentTray);
		
		/* ArrayList of directions to iterate through */ 
		AbstractList<String> directions = new ArrayList<String>();
			directions.add("u");
			directions.add("d");
			directions.add("l");
			directions.add("r");

		/* Setup timing facilities. Echo progress every 7 seconds */
		Timer t = new Timer();
		Periodic task = new Periodic();
		t.scheduleAtFixedRate(task, 7000l , 7000l);

		/* Keep going until we run out of possible moves to make or find the solution */
		for(int i = 0 ; previousTrays.size() > 0; ) {
	        if( currentTray.equals(desiredGame) ) {
				dPrint(INFO, "*** SUCCESS *** We found a solution to the puzzle!\n"+currentTray);
                currentTray.changeLog();
                return true;
            }
	
			/*
			 * Generate (at most) four new trays for each piece that can be moved in
			 * currentTray. Each piece is moved in all possible directions by  one place.
			 */
            for(Tray.Block block : currentTray)
				for(String direction : directions) { 
					Tray newTray = currentTray.moveBlock(block, direction);
					if(newTray != null && !memory.contains(newTray)) {
						newTray.setPreviousTray(currentTray);
						previousTrays.push(newTray);
						memory.add(newTray);
					}

					/* Update statistics for progress output */
					task.trayCount = ++i;
					task.prevSize = previousTrays.size();
					task.dbSize = memory.size();
				}
            currentTray = previousTrays.pop();
		}
        return false;    
    }

	/* Program entry point */
    public static void main(String[] args)
    {
        Solver game = null;
		int keepGoing = 0;
		try 
			{ keepGoing = processArgs(args); }
		catch (NullPointerException e)
			{ dPrint(ERROR,"Not enough arguments"); }
		if(keepGoing < 0)
			System.exit(-1);

        /* Create the games and (attempt to) solve */
		try 
			{ game = new Solver(startingConfig, endingConfig); }
        catch (Exception e) {
            dPrint(ERROR, "Something went horribly wrong :(");
            System.exit(-1);
        }
		game.startTime = System.currentTimeMillis();
		if(!game.solvePuzzle(game.startingGame)) {
			dPrint(WARN, "***** SORRY ****** I could not find a solution to the puzzle :( ");
			dPrint(BENCH, "Elapsed time spent on puzzle: " + ((System.currentTimeMillis() - game.startTime)/1000) + " seconds");
            System.exit(-1);
		}
		dPrint(BENCH, "Solved the puzzle in " + ((System.currentTimeMillis() - game.startTime)/1000) + " seconds");
        System.exit(0);
	} // End of main
} // End of Solver class
