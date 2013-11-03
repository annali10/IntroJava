/*
 * Name: Anna Li
 * Section Leader: Andy Aude
 * File: Chess.java
 * ------------------
 * This program plays the game Chess.
 */
 
import java.awt.Color;
import java.awt.event.*;
 
/** The main class responsible for managing the chess game */
public class Chess extends GraphicsProgram{
 
    /** Object responsible for handling the graphical display on the screen */
    ChessDisplay display;
 
    /** Object that keeps track of the locations of all pieces */
    ChessBoard board;
 
    /** Constants to signal the state of the game. The variables assigned are
     * arbitrary and never used. They are just important for keeping track of
     * the state of the game and whose turn it is and which state the game is in */
 
    public static final int BLACK_TURN = 0; // first click of black's turn
    public static final int BLACK_SELECTED = 1; // second click of black's turn
    public static final int WHITE_TURN = 2; // first click of white's turn
    public static final int WHITE_SELECTED = 3; // second click of white's turn
 
    /** Instance variable to keep track of the state of game - whose turn
     * and whether white or black is selected */
    private int state;
 
    /** Instance variable to store the piece highlighted to move */
    private ChessPiece storePiece;
 
    /** Method called before run responsible for initializing the ChessDisplay and 
     *  ChessBoard objects */
    public void init() {
        display = ChessDisplay.getInstance(this);           
        board = new ChessBoard();
 
        /* Use this method to change how the board is labeled on the screen. 
         * Passing in true will label the board like an official chessboard; 
         * passing in false will label the board like it is indexed in an array 
         * and in ChessDisplay. 
         */
        display.useRealChessLabels(false);  
        addMouseListeners();
        state = WHITE_TURN; 
    }
 
    /** The main method that runs the program */
    public void run() {
        display.draw(board);
    }
 
    public void mousePressed(MouseEvent e) {
        // returns an array of location row, column
        int [] mouseLocationOnBoard = display.getLocation(e.getX(), e.getY());
        // returns the piece at the specific location the mouse was clicked
        ChessPiece pieceJustClicked = board.pieceAt(mouseLocationOnBoard[0], mouseLocationOnBoard[1]);
        if (state == WHITE_TURN || state == BLACK_TURN) {
            // makes sure there's a piece
            mousePressWithNoPieceSelected(pieceJustClicked, mouseLocationOnBoard);
        } else if (state == WHITE_SELECTED || state == BLACK_SELECTED) {
            mousePressWithPieceSelected(pieceJustClicked, mouseLocationOnBoard);
        } 
    }
 
    /**
     * mousePressWithNoPieceSelected handles the first click of the mouse
     * It stores the piece that was clicked on in instance variable storePiece
     * Pre: No clicks but board is drawn. Whichever player's turn has been determined
     * Post: Stores first click and advances game to same player's next click
     * @param pieceJustClicked is the piece stored from mousePressed that's highlighted piece
     * @param mouseLocationOnBoard the array that stores the mouse click as a square on board
     */
    private void mousePressWithNoPieceSelected (ChessPiece pieceJustClicked, 
            int[] mouseLocationOnBoard) {
        if (pieceJustClicked != null) { // makes sure there's a piece on the spot where player clicked
            // makes sure the player is clicking the correct colored piece
            if (checksPlayerTurnAndColor(pieceJustClicked)) {
                display.selectSquare(mouseLocationOnBoard[0], mouseLocationOnBoard[1], Color.RED);
                storePiece = pieceJustClicked;
                display.draw(board);
                if (state == WHITE_TURN) {
                    state = WHITE_SELECTED;
                } else if (state == BLACK_TURN) {
                    state = BLACK_SELECTED;
                }
            }
        }   
    }
 
    /**
     * boolean to check if it's white's turn and white is clicking on a white piece or 
     * if it's black's turn, black is clicking on a black piece
     * @param piece the chess piece the player just clicked on
     * @return true if it meets the conditions, otherwise returns if wrong piece clicked
     */
    private boolean checksPlayerTurnAndColor (ChessPiece piece) {
        return (state == WHITE_TURN && piece.getColor() == ChessPiece.WHITE) 
        || (state == BLACK_TURN && piece.getColor() == ChessPiece.BLACK); 
    }
 
    /**
     * mousePressedWithPieceSelected method handles the second click of the mouse
     * It checks if a move is legal, moves the piece and checks for check and stalemate.
     * Pre: After the player's first click and the game has checked it's the player's
     * second click
     * Post: The game has moved the piece to the second clicked spot and advanced to
     * next player's turn
     * @param pieceJustClicked is the piece stored from mousePressed that's highlighted piece
     * @param mouseLocationOnBoard the array that stores the mouse click as a square on board
     */
    private void mousePressWithPieceSelected (ChessPiece pieceJustClicked, 
            int[] mouseLocationOnBoard) {
        // checks location of the second mouse click
        boolean checkMove = storePiece.canMoveTo(mouseLocationOnBoard[0], 
                mouseLocationOnBoard[1], board);
        // makes sure the move is legal and selected spot is spot that's currently occupied
        if (checkMove == true && storePiece != pieceJustClicked) {
            // moves piece and updates board
            movePieceToSelectedSpotUpdate(mouseLocationOnBoard);
            if (state == WHITE_SELECTED) {
                checkForCheckAndStalemateWhenWhite();
                state = BLACK_TURN; // advances to Black player's turn
            } else { // if state == BLACK_SELECTED
                checkForCheckAndStalemateWhenBLACK();
                state = WHITE_TURN; // advances to White player's turn
            }
        } else {
            display.unselectAll();
            display.draw(board);
            if (state == WHITE_SELECTED) {
                state = WHITE_TURN;
            } else { // if (state == BLACK_SELECTED)
                state = BLACK_TURN;
            }
        }
    }
 
    /**
     * On second click of mouse, the method removes the piece using 
     * removePiece from ChessBoard class, moves the piece and adds it back
     * to the board. Finally it unselects all highlighted squares and redraws
     * the board
     * Precondition: checked the move is legal and the selected spot isn't
     * the spot the piece is currently on
     * Postcondition: new board drawn reflects move made by player
     * @param mouseLocationOnBoard takes in the array where the piece's location is
     * stored
     */
 
    private void movePieceToSelectedSpotUpdate(int[] mouseLocationOnBoard) {
        board.removePiece(storePiece.getRow(), storePiece.getCol());
        storePiece.moveTo(mouseLocationOnBoard[0], mouseLocationOnBoard[1]);
        board.addPiece(storePiece);
        display.unselectAll();
        display.draw(board);
    }
 
 
    /**
     * Checks if player white's move has resulted in check or a 
     * stalemate. Precondition: white just moved a chess piece
     * Postcondition: if game is in check or stalemate, program prints 
     * out message to alert player.
     */
    private void checkForCheckAndStalemateWhenWhite() {
        if (isInCheckMate (board, ChessPiece.BLACK)) {
            println("Black is in checkmate. Game over");
        } else if (isInCheck(board, ChessPiece.BLACK)) {
            println("White checks Black");
        } else if (isInStalemate(board, ChessPiece.BLACK)) {
            println("Stalemate. Game Over");
        }
    }
 
    /**
     * Checks if player black's move has resulted in check or a 
     * stalemate. Precondition: black just moved a chess piece
     * Postcondition: if game is in check or stalemate, program prints 
     * out message to alert player.
     */
 
    private void checkForCheckAndStalemateWhenBLACK() {
        if (isInCheckMate (board, ChessPiece.WHITE)) {
            println("White is in checkmate. Game over");
        } else if (isInCheck(board, ChessPiece.WHITE)) {
            println("Black checks White");
        } else if (isInStalemate(board, ChessPiece.WHITE)) {
            println("Stalemate. Game Over");
        }
    }
}