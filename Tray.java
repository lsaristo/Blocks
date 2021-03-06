import java.io.*;
import java.util.*;

/* 
 * The Tray class. 
 * This class implements the game's tray that will house Blocks
 *
 * @see Block class for further documentation.
 */
class Tray implements Iterable<Tray.Block>
{
    private int rowCount, colCount;
    private int[][] grid;
    private String changeFromPrevious;
    private ArrayList<Block> db;
    private Tray previousTray;
    private final static int EMPTY = 0;
  
    /* 
     * Tray constructor.
     *
     * @param inScanner Scanner to read Tray in from. 
     */
    public Tray(Scanner inScanner)
	{
        this(inScanner.nextInt(), inScanner.nextInt(), inScanner);
    }
  
    /*
     * Tray constructor.
     *
     * @param row Number of rows in this Tray.
     * @param col Number of columns in this Tray.
     * @param inScanner Scanner to read config of tray from. 
     */ 
    public Tray(int row, int col, Scanner inScanner)
	{
        colCount = col;
        rowCount = row;
        db = new ArrayList<Block>();
        changeFromPrevious = new String();

        while(inScanner.hasNext()) {
            db.add(
                new Block(
                    inScanner.nextInt(),
                    inScanner.nextInt(),
                    inScanner.nextInt(),
                    inScanner.nextInt()
                )
            );
        }

        if(!isOkay()) { 
            Solver.dPrint(Solver.ERROR, "** Tray Corrupted, exiting **");
        }
    }
 
    /*
     * Copy constructor
     *
     * @param source Tray to copy from.
     */
    public Tray(Tray source)
	{
        rowCount = source.rowCount;
        previousTray = source.previousTray;
        changeFromPrevious = source.changeFromPrevious;
        colCount = source.colCount;
        db = new ArrayList<Block>();
        
        for(Block item : source.db) {
            db.add(new Block(item));
        }
        
        if(!isOkay()) { 
            Solver.dPrint(Solver.ERROR, "** Tray Corrupted, exiting **");
        }
    }
   
    /*
     * Return the number of rows of this Tray.
     */ 
    public int getRows()
	{ 
        return rowCount; 
    }

    /*
     * Return the number of columns of this Tray.
     */
    public int getCols() 
    { 
        return colCount; 
    }

    /*
     * Return the number of Blocks in the Tray.
     */
    public int numBlocks()
	{ 
        return db.size(); 
    }

    /*
     * Checks the Tray for consistency. 
     * Returns true if the current Tray is 
     * in a valid state. That is, all Blocks are within 
     * the Tray's boundaries. 
     *
     * @throws IllegalStateException
     */
    public boolean isOkay() throws IllegalStateException 
    {
        try {
            reDraw();
        } catch (ArrayIndexOutOfBoundsException e) { 
            return false; 
        }
        return true;
    }
    
    /*
     * Sets the pointer to the Tray from which this Tray was created.
     */
    public void setPreviousTray(Tray p)
	{ 
        previousTray = p; 
    }

    /*
     * Overriden hashCode() implementation. Must return the same
     * value if no changes made and must agree with equals() as
     * per symatics.
     */
    @Override
    public int hashCode()
	{
        String hashValue = "";
        
        for(int r=0; r<rowCount; r++) {
            for(int c=0; c<colCount; c++) {
                if(grid[r][c] != EMPTY) {
                    hashValue += getBlock(grid[r][c]);
                } else { 
                    hashValue += "seed";
                }
            }
        }
        return hashValue.hashCode();
    }
   
    /*
     * Overrites grid with the most current information stored in the database.
     *
     * @throws ArrayIndexOutOfBoundsException
     */
    private void reDraw() throws ArrayIndexOutOfBoundsException 
    {
        grid = new int[rowCount][colCount];
        
        for(Block b : db) {
            for(int r=0; r<b.getNumRows(); r++) {
                for(int c=0; c<b.getNumCols(); c++) {
                    grid[b.getr() + r][b.getc() + c] = b.getId();
                }
            }
        }
    }

    /*
     * Checks for equality of this Tray and rvalue. 
     * Returns true iff their hashCode()'s are equal. 
     *
     * @param rvalue object testing equality with. 
     */
    @Override
    public boolean equals(Object rvalue)
	{
        Tray desiredTray = (Tray)rvalue;
        boolean isEqual = true;
        
        for(int r=0; r<rowCount; r++) {
            for(int c=0; c<colCount; c++) { 
                if(desiredTray.grid[r][c] != EMPTY ) {
                    try {
                        boolean isEmpty = grid[r][c] != EMPTY;
                        Block sourceBlock = getBlock(grid[r][c]);
                        Block desiredBlock = 
                            desiredTray.getBlock(desiredTray.grid[r][c]);
                        
                        isEqual = 
                            isEqual 
                            && isEmpty
                            && sourceBlock.equals(desiredBlock);

                    } catch (EmptyStackException e) { 
                        String errorMessage = 
                            "WARNING: Tried to compare block from desiredTray"
                            + r 
                            + " " 
                            + c 
                            + ". Couldn't find this block in one of the trays";

                        Solver.dPrint(Solver.ERROR, errorMessage);
                        return false;
                    }
                }
            }
        }
        return isEqual;
    }

    /*
     * Dump string representation of this Tray.
     */
    @Override
    public String toString()
    {
        String outString = "";
        
        if(!isOkay()) {
            Solver.dPrint(Solver.ERROR, "** Tray Corrupted, exiting **");
        }
        
        for(int i=0; i<colCount; i++) {
            outString += "--------";
        }
        outString += "\n";

        for(int j=0; j<rowCount; j++) {
            for(int i=0; i<colCount; i++) {
                outString += "| " + (grid[j][i] % 9000) + "\t";
            }
            outString += "\b|\n";
        }
        
        for(int i=0; i<colCount; i++) {
            outString += "--------";
        }
        outString += "\n";
        
        return outString;
    }

    /*
     * Returns true if we can put Block b at pos [row][col] in the Tray, False 
     * if not. 
     *
     * @param row number. Origin is top left.
     * @param col number. Origin is top left.
     * @param b Block to check. 
     * @throws ArrayIndexOutOfBoundsException if the position is not valid. 
     * @see Block class. 
     */
    private boolean canMoveHere(int row, int col, Block b) 
            throws ArrayIndexOutOfBoundsException 
    {
        boolean valid = true;
        
        for(int r=0; r<b.getNumRows(); r++) {
            for(int c=0; c<b.getNumCols(); c++) {
                boolean cond1 = grid[row + r][col + c] != EMPTY;
                boolean cond2 = grid[row + r][col + c] != b.getId();
                valid = cond1 && cond2 ? false : valid;
            }
        }
        return valid;
    }

    /*
     * If a block exists at Tray[row][col], return a reference to that Block. 
     *
     * @param id of the Block we're looking up.
     * @throws EmptyStackException. 
     */ 
    public Block getBlock(int id) throws EmptyStackException 
    {
        for(Block b : db) {
            if(b.getId() == id) {
                return b;
            }
        }
        String errorMessage = 
            "Could not find Block id " 
            + id 
            + ". This is probably a bad thing";

        Solver.dPrint(Solver.WARN, errorMessage);
        throw new EmptyStackException();
    }
    
    /*
     * Output the sequence of changes made to the current Tray since the
     * original Tray was created.
     */
    public void changeLog()
	{
        Stack<String> history = new Stack<String>();
        Tray dummy = new Tray(this);
        
        while(dummy != null) {
            history.push(dummy.changeFromPrevious);
            dummy = dummy.previousTray;
        }
        while(history.size() > 0) {
            Solver.dPrint(Solver.SPECIAL, history.pop());
        }
    }

    /*
     * Move a Block in the current Tray.
     * 
     * @param b Block to be moved.
     * @param direction. Valid directions "u", "d", "l", "r".
     * @returns a new Tray object as a copy of the current object except the
     * desired Block has been moved in this new configuration by one space in
     * the desired direction. If the move is not allowed, return null.
     */ 
    public Tray moveBlock(Block b, String direction)
	{
        int AMT = 1;
        int col_scale = 0;
        int row_scale = 0;
        int block_row  = b.getr();
        int block_col = b.getc();
        Tray newTray = new Tray(this);

        switch (direction) {
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
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }

        newTray.changeFromPrevious = "" + b.getr() + " " + b.getc() + " " 
            + (b.getr() + row_scale) + " " + (b.getc() + col_scale);

        newTray.setPreviousTray(this);
        newTray.getBlock(b.getId()).setCoordinates(block_row+row_scale, 
            block_col+col_scale);
        newTray.isOkay();
        return newTray;
    }

    /*
     * Implements the Iterable interface. 
     * Return a Tray.Iterator
     */
    @Override
    public Iterator<Block> iterator()
	{
        return new TrayIterator(this);
    }

    /*
     * Tray iterator class.
     */
    class TrayIterator implements Iterator<Block>
    {
        private Iterator databaseIter;
        
        TrayIterator(Tray inTray) {
            databaseIter = inTray.db.iterator(); 
        }   

        @Override
        public boolean hasNext()
	    {
            return databaseIter.hasNext();
        }

        @Override
        public Block next()
	    {
            return (Block)databaseIter.next();
        }

        @Override
        public void remove()
	    {
        }
    }

    /*
     * The Block class. Inner class used by the Tray class. 
     * This class implements the Blocks that will populate the game tray. 
     * @see Tray for further documentation.
     */
    class Block
    {
        private final int id;
        private int rowCount, colCount, col_pos, row_pos;
    
        /*
         * The Block class constructor. 
         * POSTCONDITION: A new l by w sized Block created at position col, row
         *  Note that the initial position determins the GUID for this Block and
         *  all Blocks copied from it. 
         *
         * @param rows number of rows this Block occupies on the Tray.
         * @param cols number of columns this Block occupies on the Tray.
         * @param row location.
         * @param col location. 
         */
        public Block(int rows, int cols, int row, int col)
	    {
            rowCount = rows;
            colCount = cols;
            row_pos = row;
            col_pos = col;
            id = row + 257*col + rows % 31;
        }
        
        /*
         * Copy constructor.
         * Note that the GUID will remain unchaned from the original block. This
         * is by design. 
         *
         * @param source Block to copy. 
         */
        public Block(Block source)
	    {
            rowCount = source.rowCount;
            colCount = source.colCount;
            id = source.id;
            row_pos = source.row_pos;
            col_pos = source.col_pos;
        }
        
        /*
         * The Block's hashCode() function. Note that the id serves as the 
         *  Block's GUID in the Tray and it's datastructures. As such, we 
         *  only need to compute the hashCode() once for each Block and never
         *  again. It should remain static throughout the program execution. 
         *  There is also a small performance benefit from not computing it 
         *  again unnecessarily. 
         */
        @Override
        public int hashCode()
	    {
            return id; 
        }

        /*
         * Test two Blocks for equality. Equality in this case means that they
         *  are the same dimensions and same spacial orientation (i.e., 
         *  a 2x1 is NOT equal to a 1x2. Note that the symantics of the equals
         *  method does NOT agree with the hashCode() method and so this should 
         *  not be relied upon here. 
         *
         * @param other Object to check for equality against. 
         */
        @Override
        public boolean equals(Object other)    
        {
            Block compareBlock = (Block) other;
            Solver.dPrint(
                Solver.DEBUG, 
                "Checking for equality of Block " 
                + this 
                + " and " 
                + other 
                + ": " 
                + (this.toString().equals(other.toString()) ? "true" : "false")
            );
            return this.toString().equals(other.toString());
        }

        /*
         * Return the number of columns this Block spans.
         */
        public int getNumCols()
	    { 
            return colCount; 
        }

        /*
         * Return the number of rows this Block spans.
         */
        public int getNumRows()
	    { 
            return rowCount; 
        }

        /*
         * Return the row position of this Block.
         */
        public int getr()
	    { 
            return row_pos; 
        }

        /*
         * Return the column position of this Block.
         */
        public int getc()
	    { 
            return col_pos; 
        }

        /*
         * Move this Block to the position given.
         *
         * @param r row position.
         * @param c column position.
         */
        public void setCoordinates(int r, int c)
	    { 
            row_pos = r; col_pos = c; 
        }

        /*
         * Return this Block's GUID.
         */
        private int getId()
	    { 
            return id; 
        }

        /*
         * String representation of this Block.
         */
        @Override
        public String toString()
	    { 
            return "a " + rowCount + "x" + colCount + " block"; 
        }
    } // End of the Block inner class. 
} // End of the ray class
