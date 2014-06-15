import java.io.*;
import java.util.*;

/** 
 *  The Tray class. 
 *  This class implements the game's tray that will house Blocks
 *  (see Block class for further documentation)
 */
class Tray implements Iterable<Tray.Block>
{
    private int rowCount, colCount;
    private int[][] grid;
    private String changeFromPrevious;
    private ArrayList<Block> db;
    private Tray previousTray;
    private final static int EMPTY = 0;
  
    /** 
     * Tray constructor.
     * POSTCONDITION: Init a new Tray with a given Scanner file
     */
    public Tray(Scanner inScanner) {
        db = new ArrayList<Block>();
        rowCount = inScanner.nextInt();
        changeFromPrevious = new String();
        colCount = inScanner.nextInt();
        while(inScanner.hasNext()) {
            db.add(new Block(   inScanner.nextInt(),
                                inScanner.nextInt(),
                                inScanner.nextInt(),
                                inScanner.nextInt()));
        }
        Solver.dPrint(Solver.DEBUG, "Tray instantiated with the following database:\n"+db);
        if (!isOkay()) Solver.dPrint(Solver.ERROR, "** Tray Corrupted, exiting **");
    }
  
    /**
     * Tray constructor.
     * POSTCONDITION: Init a new Tray with the given row, col dimensions and a Scanner file. 
     */ 
    public Tray(int row, int col, Scanner inScanner) {
        colCount = col;
        rowCount = row;
        db = new ArrayList<Block>();
        changeFromPrevious = new String();
        while(inScanner.hasNext()) {
            db.add(new Block(   inScanner.nextInt(),
                                inScanner.nextInt(),
                                inScanner.nextInt(),
                                inScanner.nextInt()));
        }
        if (!isOkay()) Solver.dPrint(Solver.ERROR, "** Tray Corrupted, exiting **");
    }
 
    /**
     * Copy constructor
     */
    public Tray(Tray source) {
        rowCount = source.rowCount;
        previousTray = source.previousTray;
        changeFromPrevious = source.changeFromPrevious;
        colCount = source.colCount;
        db = new ArrayList<Block>();
        for(Block item : source.db)
            db.add(new Block(item));
        if (!isOkay()) Solver.dPrint(Solver.ERROR, "** Tray Corrupted, exiting **");
    }
    
    public int getRows() { return rowCount; }
    public int getCols() { return colCount; }
    public int numBlocks() { return db.size(); }

    /**
     * Checks the Tray for consistency. 
     * POSTCONDITION: Returns true if the current Tray is 
     *  in a valid state. That is, all Blocks are within 
     *  the Tray's boundaries. 
     */
    public boolean isOkay() throws IllegalStateException {
        try { reDraw(); }
        catch (ArrayIndexOutOfBoundsException e) { return false; }
        return true;
    }
    
    /**
     * Sets the pointer to the Tray from which this Tray was created.
     */
    public void setPreviousTray(Tray p) { previousTray = p ; }

    /**
     * Overriden hashCode() implementation. Must return the same
     *  value if no changes made and must agree with equals() as
     *  per symatics.
     */
    @Override
    public int hashCode() {
        String hashValue = "";
        for(int r=0; r<rowCount; r++)
            for(int c=0; c<colCount; c++) 
                if(grid[r][c] != EMPTY) 
                    hashValue += getBlock(grid[r][c]);
                else 
                    hashValue += "seed";
        return hashValue.hashCode();
    }
   
    /**
     * Overrites grid with the most current information stored in the database.
     *
     * @throws ArrayIndexOutOfBoundsException
     */
    private void reDraw() throws ArrayIndexOutOfBoundsException {
        grid = new int[rowCount][colCount];
        for(Block b : db)
            for(int r=0; r<b.getNumRows(); r++)
                for(int c=0; c<b.getNumCols(); c++)
                    grid[b.getr() + r][b.getc() + c] = b.getId();
    }

    /**
     * Checks for equality of this Tray and rvalue. 
     *  Returns true iff their hashCode()'s are equal. 
     */
    @Override
    public boolean equals(Object rvalue) {
        Tray desiredTray = (Tray)rvalue;
        boolean isEqual = true;
        for(int r=0; r<rowCount; r++) 
            for(int c=0; c<colCount; c++) 
                if( desiredTray.grid[r][c] != EMPTY ) {
                    try {
                        isEqual = isEqual && 
                        grid[r][c] != EMPTY &&
                        getBlock(grid[r][c]).equals(desiredTray.getBlock(desiredTray.grid[r][c]));
                    }
                    catch (EmptyStackException e) { 
                        Solver.dPrint(Solver.ERROR, "WARNING: Tried to compare block from desiredTray"
                        +" at " + r + " " + c + " and we couldn't find this block in one of the 2 trays"
                        +". This shouldn't have happened.");
                        return false;
                    }
                }
        Solver.dPrint(Solver.DEBUG, "Comparing this tray with rvalue for equality: " + (isEqual ? "true" : "false"));
        return isEqual;
    }

    /**
     * POSTCONDITION: Visual representation of the Tray's configuration has been
     *  sent to std::out. 
     */
    @Override
    public String toString()
    {
        if (!isOkay()) Solver.dPrint(Solver.ERROR, "** Tray Corrupted, exiting **");
        String outString = "";
        for(int i=0; i<colCount; i++)
            outString += "--------";
        outString += "\n";
        for(int j=0; j<rowCount; j++)
        {
            for(int i=0; i<colCount; i++)
                outString += "| " + (grid[j][i] % 9000) + "\t";
            outString += "\b|\n";
        }
        for(int i=0; i<colCount; i++)
            outString += "--------";
        outString += "\n";
        return outString;
    }

    /**
     * POSTCONDITION: Returns true if we can put Block b at pos [row][col] in the Tray. False if not.
     *  If the position is not valid for the Tray configuration, throws ArrayIndexOutOfBoundsException.
     *
     * @throws ArrayIndexOutOfBoundsException.
     */
    private boolean canMoveHere(int row, int col, Block b) throws ArrayIndexOutOfBoundsException
    {
        boolean valid = true;
        for(int r=0; r<b.getNumRows(); r++)
            for(int c=0; c<b.getNumCols(); c++)
                if((grid[row + r][col + c] != EMPTY) && (grid[row + r][col + c] != b.getId() ))
                    valid = false;
        return valid;
    }

    /**
     * POSTCONDITION: If a block exists at Tray[row][col], return a reference to that Block. 
     *  Otherwise, throw EmptyStackException
     *
     * @throws EmptyStackException. 
     */ 
    public Block getBlock(int id) throws EmptyStackException {
        for(Block b : db) 
            if(b.getId() == id) 
                return b;
        Solver.dPrint(Solver.WARN, "Could not find Block id " + id + ". This is probably a bad thing");
        throw new EmptyStackException();
    }
    
    /**
     * Output the sequence of changes made to the current Tray since the original 
     *  Tray was created.
     */
    public void changeLog() {
        Stack<String> history = new Stack<String>();
        Tray dummy = new Tray(this);
        /* Enumerate moves, most recent first */
        while(dummy != null) {
            history.push(dummy.changeFromPrevious);
            dummy = dummy.previousTray;
        }
        /* Now print them out oldest move first. */ 
        while(history.size() > 0)
            Solver.dPrint(Solver.SPECIAL, history.pop());
    }

    /**
     * Move a Block in the current Tray.
     * POSTCONDITION: Returns a new Tray object as a copy of the current object except
     *  the desired Block has been moved in this new configuration by one space in the 
     *  desired direction. If the move is not allowed, return null.
     * Valid options for direction are: up, down, left, right. 
     */ 
    public Tray moveBlock(Block b, String direction)
    {
        int AMT = 1;
        int col_scale = 0;
        int row_scale = 0;
        int block_row  = b.getr();
        int block_col = b.getc();
        Tray newTray = new Tray(this);

        switch (direction)
        {
            case "u":       row_scale = -AMT;
                            break;
            case "d":       row_scale = AMT;
                            break;
            case "l":       col_scale = -AMT;
                            break;
            case "r":       col_scale = AMT;
        }
        try {
            if(!canMoveHere(block_row + row_scale, block_col + col_scale, b)) 
                return null;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
        newTray.changeFromPrevious = "" + b.getr() + " "    +  b.getc() + " " 
                                        + (b.getr() + row_scale) + " " + (b.getc() + col_scale);
        newTray.setPreviousTray(this);
        newTray.getBlock(b.getId()).setCoordinates(block_row+row_scale, block_col+col_scale);
        newTray.isOkay();
        return newTray;
    }

    /**
     * Implements the Iterable interface. 
     * Return a Tray::Iterator
     */
    @Override
    public Iterator<Block> iterator() {
        return new TrayIterator(this);
    }

    class TrayIterator implements Iterator<Block>
    {
        private Iterator databaseIter;
        
        TrayIterator(Tray inTray) {
            databaseIter = inTray.db.iterator(); 
        }   

        @Override
        public boolean hasNext() {
            return databaseIter.hasNext();
        }

        @Override
        public Block next() {
            return (Block)databaseIter.next();
        }

        @Override
        public void remove() {}
    }

    /**
     *  The Block class. Inner class used by the Tray class. 
     *  This class implements the Blocks that will populate the game tray. 
     *  (see Tray clss for further documentation). 
     */
    class Block
    {
        /* Class members */
        private int rowCount,
                    colCount,
                    col_pos,
                    row_pos;
        private final int id;
    
        /**
         * The Block class constructor. 
         * POSTCONDITION: A new l by w sized Block created at position col, row
         *  Note that the initial position determins the GUID for this Block and
         *  all Blocks copied from it. 
         */
        public Block(int rows, int cols, int row, int col) {
            rowCount = rows;
            colCount = cols;
            row_pos = row;
            col_pos = col;
            id = row + 257*col + rows % 31;
        }
        
        /**
         * Copy constructor.
         *  Note that the GUID will remain unchaned from the original block. This
         *  is by design. 
         */
        public Block(Block source) {
            rowCount = source.rowCount;
            colCount = source.colCount;
            id = source.id;
            row_pos = source.row_pos;
            col_pos = source.col_pos;
        }
        
        /**
         * The Block's hashCode() function. Note that the id serves as the 
         *  Block's GUID in the Tray and it's datastructures. As such, we 
         *  only need to compute the hashCode() once for each Block and never
         *  again. It should remain static throughout the program execution. 
         *  There is also a small performance benefit from not computing it 
         *  again unnecessarily. 
         */
        @Override
        public int hashCode() { return id; }

        /**
         * Test two Blocks for equality. Equality in this case means that they
         *  are the same dimensions and same spacial orientation (i.e., 
         *  a 2x1 is NOT equal to a 1x2. Note that the symantics of the equals()
         *  method does NOT agree with the hashCode() method and so this paradigm
         *  should not be relied upon here. 
         */
        @Override
        public boolean equals(Object other)    {
        Block compareBlock = (Block) other;
            Solver.dPrint(Solver.DEBUG, "Checking for equality of Block " + this + " and " 
                  + other + ": " + (this.toString().equals(other.toString()) ? "true" : "false"));
            return this.toString().equals(other.toString());
        }

        public int getNumCols() { return colCount; }
        public int getNumRows() { return rowCount; }
        public int getr() { return row_pos; }
        public int getc() { return col_pos; }
        public void setCoordinates(int r, int c) { row_pos = r; col_pos = c; }
        private int getId() { return id; }

        public String toString() { return "a " + rowCount + "x" + colCount + " block"; }
    
    } // End of the Block inner class. 
} // End of the Tray class
