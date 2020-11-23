package com.company;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Stack;
import javax.swing.*;



public class Main {

    public static JFrame mazeFrame;  // The main form of the program

    public static void main(String[] args) {
        int width  = 693;
        int height = 545;
        mazeFrame = new JFrame("Path Fining");
        mazeFrame.setContentPane(new MazePanel(width,height));
        mazeFrame.pack();
        mazeFrame.setResizable(false);
        // the form is located in the center of the screen
        mazeFrame.setLocationRelativeTo(null);
        mazeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mazeFrame.setVisible(true);
    } // end main()

    /**
     * This class defines the contents of the main form
     * and contains all the functionality of the program.
     */
    public static class MazePanel extends JPanel {
        private class MyMaze {
            private int dimensionX, dimensionY;         // dimension of maze
            private int gridDimensionX, gridDimensionY; // dimension of output grid
            private char[][] mazeGrid;                  // output grid
            private Cell[][] cells;                     // 2d array of Cells
            private Random random = new Random();       // The random object

            // initialize with x and y the same
            public MyMaze(int aDimension) {
                this(aDimension, aDimension);
            }
            // constructor
            public MyMaze(int xDimension, int yDimension) {
                dimensionX = xDimension;
                dimensionY = yDimension;
                gridDimensionX = xDimension * 2 + 1;
                gridDimensionY = yDimension * 2 + 1;
                mazeGrid = new char[gridDimensionX][gridDimensionY];
                init();
                generateMaze();
            }

            private void init() {
                // create cells
                cells = new Cell[dimensionX][dimensionY];
                for (int x = 0; x < dimensionX; x++)
                    for (int y = 0; y < dimensionY; y++)
                        cells[x][y] = new Cell(x, y, false); // create cell (see Cell constructor)
            }

            // inner class to represent a cell
            private class Cell {
                int x, y; // coordinates
                // cells this cell is connected to
                ArrayList<Cell> neighbors = new ArrayList<>();
                // impassable cell
                boolean wall = true;
                // if true, has yet to be used in generation
                boolean open = true;
                // construct Cell at x, y
                Cell(int x, int y) {
                    this(x, y, true);
                }
                // construct Cell at x, y and with whether it isWall
                Cell(int x, int y, boolean isWall) {
                    this.x = x;
                    this.y = y;
                    this.wall = isWall;
                }
                // add a neighbor to this cell, and this cell as a neighbor to the other
                void addNeighbor(Cell other) {
                    if (!this.neighbors.contains(other)) // avoid duplicates
                        this.neighbors.add(other);
                    if (!other.neighbors.contains(this)) // avoid duplicates
                        other.neighbors.add(this);
                }
                // used in updateGrid()
                boolean isCellBelowNeighbor() {
                    return this.neighbors.contains(new Cell(this.x, this.y + 1));
                }
                // used in updateGrid()
                boolean isCellRightNeighbor() {
                    return this.neighbors.contains(new Cell(this.x + 1, this.y));
                }
                // useful Cell equivalence
                @Override
                public boolean equals(Object other) {
                    if (!(other instanceof Cell)) return false;
                    Cell otherCell = (Cell) other;
                    return (this.x == otherCell.x && this.y == otherCell.y);
                }

                // should be overridden with equals
                @Override
                public int hashCode() {
                    // random hash code method designed to be usually unique
                    return this.x + this.y * 256;
                }

            }
            // generate from upper left (In computing the y increases down often)
            private void generateMaze() {
                generateMaze(0, 0);
            }
            // generate the maze from coordinates x, y
            private void generateMaze(int x, int y) {
                generateMaze(getCell(x, y)); // generate from Cell
            }
            private void generateMaze(Cell startAt) {
                // don't generate from cell not there
                if (startAt == null) return;
                startAt.open = false; // indicate cell closed for generation
                ArrayList<Cell> cellsList = new ArrayList<>();
                cellsList.add(startAt);

                while (!cellsList.isEmpty()) {
                    Cell cell;
                    // this is to reduce but not completely eliminate the number
                    // of long twisting halls with short easy to detect branches
                    // which results in easy mazes
                    if (random.nextInt(10)==0)
                        cell = cellsList.remove(random.nextInt(cellsList.size()));
                    else cell = cellsList.remove(cellsList.size() - 1);
                    // for collection
                    ArrayList<Cell> neighbors = new ArrayList<>();
                    // cells that could potentially be neighbors
                    Cell[] potentialNeighbors = new Cell[]{
                            getCell(cell.x + 1, cell.y),
                            getCell(cell.x, cell.y + 1),
                            getCell(cell.x - 1, cell.y),
                            getCell(cell.x, cell.y - 1)
                    };
                    for (Cell other : potentialNeighbors) {
                        // skip if outside, is a wall or is not opened
                        if (other==null || other.wall || !other.open)
                            continue;
                        neighbors.add(other);
                    }
                    if (neighbors.isEmpty()) continue;
                    // get random cell
                    Cell selected = neighbors.get(random.nextInt(neighbors.size()));
                    // add as neighbor
                    selected.open = false; // indicate cell closed for generation
                    cell.addNeighbor(selected);
                    cellsList.add(cell);
                    cellsList.add(selected);
                }
                updateGrid();
            }
            // used to get a Cell at x, y; returns null out of bounds
            public Cell getCell(int x, int y) {
                try {
                    return cells[x][y];
                } catch (ArrayIndexOutOfBoundsException e) { // catch out of bounds
                    return null;
                }
            }
            // draw the maze
            public void updateGrid() {
                char backChar = ' ', wallChar = 'X', cellChar = ' ';
                // fill background
                for (int x = 0; x < gridDimensionX; x ++)
                    for (int y = 0; y < gridDimensionY; y ++)
                        mazeGrid[x][y] = backChar;
                // build walls
                for (int x = 0; x < gridDimensionX; x ++)
                    for (int y = 0; y < gridDimensionY; y ++)
                        if (x % 2 == 0 || y % 2 == 0)
                            mazeGrid[x][y] = wallChar;
                // make meaningful representation
                for (int x = 0; x < dimensionX; x++)
                    for (int y = 0; y < dimensionY; y++) {
                        Cell current = getCell(x, y);
                        int gridX = x * 2 + 1, gridY = y * 2 + 1;
                        mazeGrid[gridX][gridY] = cellChar;
                        if (current.isCellBelowNeighbor())
                            mazeGrid[gridX][gridY + 1] = cellChar;
                        if (current.isCellRightNeighbor())
                            mazeGrid[gridX + 1][gridY] = cellChar;
                    }
            }
        } // end nested class MyMaze

        /**
         * Helper class that represents the cell of the grid
         */
        private class Cell {
            int row;     // the row number of the cell(row 0 is the top)
            int col;     // the column number of the cell (Column 0 is the left)
            double g;    // the value of the function g of A* and Greedy algorithms
            double h;    // the value of the function h of A* and Greedy algorithms
            double f;    // the value of the function h of A* and Greedy algorithms
            double dist; // the distance of the cell from the initial position of the robot
            // Ie the label that updates the Dijkstra's algorithm
            Cell prev;   // Each state corresponds to a cell
            // and each state has a predecessor which
            // is stored in this variable

            public Cell(int row, int col){
                this.row = row;
                this.col = col;
            }

        } // end nested class Cell

        /**
         * Auxiliary class that specifies that the cells will be sorted
         * according their 'f' field
         */
        private class CellComparatorByF implements Comparator<Cell>{
            @Override
            public int compare(Cell cell1, Cell cell2){
                return Double.compare(cell1.f,cell2.f);
            }
        } // end nested class CellComparatorByF

        /**
         * Auxiliary class that specifies that the cells will be sorted
         * according their 'dist' field
         */
        private class CellComparatorByDist implements Comparator<Cell>{
            @Override
            public int compare(Cell cell1, Cell cell2){
                return Double.compare(cell1.dist,cell2.dist);
            }
        } // end nested class CellComparatorByDist

        /**
         * Class that handles mouse movements as we "paint"
         * obstacles or move the robot and/or target.
         */
        private class MouseHandler implements MouseListener, MouseMotionListener {
            private int cur_row, cur_col, cur_val;
            @Override
            public void mousePressed(MouseEvent evt) {
                int row = (evt.getY() - 10) / squareSize;
                int col = (evt.getX() - 10) / squareSize;
                if (row >= 0 && row < rows && col >= 0 && col < columns) {
                    if (realTime ? true : !found && !searching){
                        if (realTime)
                            fillGrid();
                        cur_row = row;
                        cur_col = col;
                        cur_val = grid[row][col];
                        if (cur_val == EMPTY)
                            grid[row][col] = OBST;
                        if (cur_val == OBST)
                            grid[row][col] = EMPTY;
                        if (realTime && dijkstra.isSelected())
                            initializeDijkstra();
                    }
                    if (realTime)
                        realTimeAction();
                    else
                        repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent evt) {
                int row = (evt.getY() - 10) / squareSize;
                int col = (evt.getX() - 10) / squareSize;
                if (row >= 0 && row < rows && col >= 0 && col < columns){
                    if (realTime ? true : !found && !searching){
                        if (realTime)
                            fillGrid();
                        if (!(row == cur_row && col == cur_col) && (cur_val == ROBOT || cur_val == TARGET)){
                            int new_val = grid[row][col];
                            if (new_val == EMPTY){
                                grid[row][col] = cur_val;
                                if (cur_val == ROBOT) {
                                    robotStart.row = row;
                                    robotStart.col = col;
                                } else {
                                    targetPos.row = row;
                                    targetPos.col = col;
                                }
                                grid[cur_row][cur_col] = new_val;
                                cur_row = row;
                                cur_col = col;
                                cur_val = grid[row][col];
                            }
                        } else if (grid[row][col] != ROBOT && grid[row][col] != TARGET)
                            grid[row][col] = OBST;
                        if (realTime && dijkstra.isSelected())
                            initializeDijkstra();
                    }
                    if (realTime)
                        realTimeAction();
                    else
                        repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent evt) { }
            @Override
            public void mouseEntered(MouseEvent evt) { }
            @Override
            public void mouseExited(MouseEvent evt) { }
            @Override
            public void mouseMoved(MouseEvent evt) { }
            @Override
            public void mouseClicked(MouseEvent evt) { }

        } // end nested class MouseHandler

        /**
         * The class that is responsible for the animation
         */
        private class RepaintAction implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent evt) {
                checkTermination();
                repaint();
                if (endOfSearch)
                {
                    animation = false;
                    timer.stop();
                }
            }
        } // end nested class RepaintAction

        /*
         **********************************************************
         *          Constants of MazePanel class
         **********************************************************
         */

        private final static int
                INFINITY = Integer.MAX_VALUE, // The representation of the infinite
                EMPTY    = 0,  // empty cell
                OBST     = 1,  // cell with obstacle
                ROBOT    = 2,  // the position of the robot
                TARGET   = 3,  // the position of the target
                FRONTIER = 4,  // cells that form the frontier (OPEN SET)
                CLOSED   = 5,  // cells that form the CLOSED SET
                ROUTE    = 6;  // cells that form the robot-to-target path

        // Messages to the user
        private final static String
                MSG_DRAW_AND_SELECT =
                "\"Paint\" obstacles, then click 'Real-Time' or 'Step-by-Step' or 'Animation'",
                MSG_SELECT_STEP_BY_STEP_ETC =
                        "Click 'Step-by-Step' or 'Animation' or 'Clear'",
                MSG_NO_SOLUTION =
                        "There is no path to the target !!!";

        /*
         **********************************************************
         *          Variables of MazePanel class
         **********************************************************
         */

        JSpinner rowsSpinner, columnsSpinner; // Spinners for entering # of rows and columns

        int rows    = 41,           // the number of rows of the grid
                columns = 41,           // the number of columns of the grid
                squareSize = 500/rows;  // the cell size in pixels


        int arrowSize = squareSize/2; // the size of the tip of the arrow
        // pointing the predecessor cell
        ArrayList<Cell> openSet   = new ArrayList();// the OPEN SET
        ArrayList<Cell> closedSet = new ArrayList();// the CLOSED SET
        ArrayList<Cell> graph     = new ArrayList();// the set of vertices of the graph
        // to be explored by Dijkstra's algorithm

        Cell robotStart; // the initial position of the robot
        Cell targetPos;  // the position of the target

        JLabel message;  // message to the user

        // basic buttons
        JButton resetButton, mazeButton, clearButton, realTimeButton, stepButton, animationButton, aboutButton;

        // buttons for selecting the algorithm
        JRadioButton dfs, bfs, aStar, greedy, dijkstra;

        // the slider for adjusting the speed of the animation
        JSlider slider;

        // Diagonal movements allowed?
        JCheckBox diagonal;
        // Draw arrows to predecessors
        JCheckBox drawArrows;

        int[][] grid;        // the grid
        boolean realTime;    // Solution is displayed instantly
        boolean found;       // flag that the goal was found
        boolean searching;   // flag that the search is in progress
        boolean endOfSearch; // flag that the search came to an end
        boolean animation;   // flag that the animation is running
        int delay;           // time delay of animation (in msec)
        int expanded;        // the number of nodes that have been expanded

        // the object that controls the animation
        RepaintAction action = new RepaintAction();

        // the Timer which governs the execution speed of the animation
        Timer timer;

        public MazePanel(int width, int height) {

            super.setLayout(null);

            MouseHandler listener = new MouseHandler();
            super.addMouseListener(listener);
            super.addMouseMotionListener(listener);

            super.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.blue));
            super.setPreferredSize( new Dimension(width,height) );

            grid = new int[rows][columns];

            // We create the contents of the panel

            message = new JLabel(MSG_DRAW_AND_SELECT, JLabel.CENTER);
            message.setForeground(Color.blue);
            message.setFont(new Font("Helvetica",Font.PLAIN,16));

            JLabel rowsLbl = new JLabel("# of rows (5-83):", JLabel.RIGHT);
            rowsLbl.setFont(new Font("Helvetica",Font.PLAIN,13));

            SpinnerModel rowModel = new SpinnerNumberModel(41, //initial value
                    5,  //min
                    83, //max
                    1); //step
            rowsSpinner = new JSpinner(rowModel);

            JLabel columnsLbl = new JLabel("# of columns (5-83):", JLabel.RIGHT);
            columnsLbl.setFont(new Font("Helvetica",Font.PLAIN,13));

            SpinnerModel colModel = new SpinnerNumberModel(41, //initial value
                    5,  //min
                    83, //max
                    1); //step
            columnsSpinner = new JSpinner(colModel);

            resetButton = new JButton("New grid");
            resetButton.setBackground(Color.lightGray);
            resetButton.setToolTipText
                    ("Clears and redraws the grid according to the given rows and columns");
            resetButton.addActionListener(this::resetButtonActionPerformed);

            mazeButton = new JButton("Maze");
            mazeButton.setBackground(Color.lightGray);
            mazeButton.setToolTipText
                    ("Creates a random maze");
            mazeButton.addActionListener(this::mazeButtonActionPerformed);

            clearButton = new JButton("Clear");
            clearButton.setBackground(Color.lightGray);
            clearButton.setToolTipText
                    ("First click: clears search, Second click: clears obstacles");
            clearButton.addActionListener(this::clearButtonActionPerformed);

            realTimeButton = new JButton("Real-Time");
            //realTimeButton.addActionListener(new ActionHandler());
            realTimeButton.setBackground(Color.lightGray);
            realTimeButton.setToolTipText
                    ("Position of obstacles, robot and target can be changed when search is underway");
            realTimeButton.addActionListener(this::realTimeButtonActionPerformed);

            stepButton = new JButton("Step-by-Step");
            stepButton.setBackground(Color.lightGray);
            stepButton.setToolTipText
                    ("The search is performed step-by-step for every click");
            stepButton.addActionListener(this::stepButtonActionPerformed);

            animationButton = new JButton("Animation");
            animationButton.setBackground(Color.lightGray);
            animationButton.setToolTipText
                    ("The search is performed automatically");
            animationButton.addActionListener(this::animationButtonActionPerformed);

            JLabel delayLbl = new JLabel("Delay (0-1000 msec)", JLabel.CENTER);
            delayLbl.setFont(new Font("Helvetica",Font.PLAIN,10));

            slider = new JSlider(0,1000,500); // initial value of delay 500 msec
            slider.setToolTipText
                    ("Regulates the delay for each step (0 to 1000 msec)");

            delay = slider.getValue();

            // ButtonGroup that synchronizes the five RadioButtons
            // choosing the algorithm, so that only one
            // can be selected anytime
            ButtonGroup algoGroup = new ButtonGroup();

            dfs = new JRadioButton("DFS");
            dfs.setToolTipText("Depth First Search algorithm");
            algoGroup.add(dfs);

            bfs = new JRadioButton("BFS");
            bfs.setToolTipText("Breadth First Search algorithm");
            algoGroup.add(bfs);

            aStar = new JRadioButton("A*");
            aStar.setToolTipText("A* algorithm");
            algoGroup.add(aStar);

            greedy = new JRadioButton("Greedy");
            greedy.setToolTipText("Greedy search algorithm");
            algoGroup.add(greedy);

            dijkstra = new JRadioButton("Dijkstra");
            dijkstra.setToolTipText("Dijkstra's algorithm");
            algoGroup.add(dijkstra);

            JPanel algoPanel = new JPanel();
            algoPanel.setBorder(javax.swing.BorderFactory.
                    createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(),
                            "Algorithms", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.TOP, new java.awt.Font("Helvetica", 0, 14)));

            dfs.setSelected(true);  // DFS is initially selected

            diagonal = new
                    JCheckBox("Diagonal movements");
            diagonal.setToolTipText("Diagonal movements are also allowed");

            drawArrows = new
                    JCheckBox("Arrows to predecessors");
            drawArrows.setToolTipText("Draw arrows to predecessors");

            JLabel robot = new JLabel("Robot", JLabel.CENTER);
            robot.setForeground(Color.red);
            robot.setFont(new Font("Helvetica",Font.PLAIN,14));

            JLabel target = new JLabel("Target", JLabel.CENTER);
            target.setForeground(Color.GREEN);
            target.setFont(new Font("Helvetica",Font.PLAIN,14));

            JLabel frontier = new JLabel("Frontier", JLabel.CENTER);
            frontier.setForeground(Color.blue);
            frontier.setFont(new Font("Helvetica",Font.PLAIN,14));

            JLabel closed = new JLabel("Closed set", JLabel.CENTER);
            closed.setForeground(Color.CYAN);
            closed.setFont(new Font("Helvetica",Font.PLAIN,14));

            // we add the contents of the panel
            super.add(message);
            super.add(rowsLbl);
            super.add(rowsSpinner);
            super.add(columnsLbl);
            super.add(columnsSpinner);
            super.add(resetButton);
            super.add(mazeButton);
            super.add(clearButton);
            super.add(realTimeButton);
            super.add(stepButton);
            super.add(animationButton);
            super.add(delayLbl);
            super.add(slider);
            super.add(dfs);
            super.add(bfs);
            super.add(aStar);
            super.add(greedy);
            super.add(dijkstra);
            super.add(algoPanel);
            super.add(diagonal);
            super.add(drawArrows);
            super.add(robot);
            super.add(target);
            super.add(frontier);
            super.add(closed);

            // we regulate the sizes and positions
            message.setBounds(0, 515, 500, 23);
            rowsLbl.setBounds(520, 5, 130, 25);
            rowsSpinner.setBounds(655, 5, 35, 25);
            columnsLbl.setBounds(520, 35, 130, 25);
            columnsSpinner.setBounds(655, 35, 35, 25);
            resetButton.setBounds(520, 65, 170, 25);
            mazeButton.setBounds(520, 95, 170, 25);
            clearButton.setBounds(520, 125, 170, 25);
            realTimeButton.setBounds(520, 155, 170, 25);
            stepButton.setBounds(520, 185, 170, 25);
            animationButton.setBounds(520, 215, 170, 25);
            delayLbl.setBounds(520, 245, 170, 10);
            slider.setBounds(520, 255, 170, 25);
            dfs.setBounds(530, 300, 70, 25);
            bfs.setBounds(600, 300, 70, 25);
            aStar.setBounds(530, 325, 70, 25);
            greedy.setBounds(600, 325, 85, 25);
            dijkstra.setBounds(530, 350, 85, 25);
            algoPanel.setLocation(520,280);
            algoPanel.setSize(170, 100);
            diagonal.setBounds(520, 385, 170, 25);
            drawArrows.setBounds(520, 410, 170, 25);
            robot.setBounds(520, 465, 80, 25);
            target.setBounds(605, 465, 80, 25);
            frontier.setBounds(520, 485, 80, 25);
            closed.setBounds(605, 485, 80, 25);


            // we create the timer
            timer = new Timer(delay, action);

            // We attach to cells in the grid initial values.
            // Here is the first step of the algorithms
            initializeGrid(false);

        } // end constructor

        /**
         * Creates a new clean grid or a new maze
         */
        private void initializeGrid(Boolean makeMaze) {
            rows    = (int)(rowsSpinner.getValue());
            columns = (int)(columnsSpinner.getValue());
            // the maze must have an odd number of rows and columns
            if (makeMaze && rows % 2 == 0)
                rows -= 1;
            if (makeMaze && columns % 2 == 0)
                columns -= 1;
            squareSize = 500/(rows > columns ? rows : columns);
            arrowSize = squareSize/2;
            grid = new int[rows][columns];
            robotStart = new Cell(rows-2,1);
            targetPos = new Cell(1,columns-2);
            fillGrid();
            if (makeMaze) {
                MyMaze maze = new MyMaze(rows/2,columns/2);
                for (int x = 0; x < maze.gridDimensionX; x++)
                    for (int y = 0; y < maze.gridDimensionY; y++)
                        if (maze.mazeGrid[x][y] == 'X')  // wallChar
                            grid[x][y] = OBST;
            }
        } // end initializeGrid()

        /**
         * Gives initial values ​​for the cells in the grid.
         */
        private void fillGrid() {
            /**
             * With the first click on button 'Clear' clears the data
             * of any search was performed (Frontier, Closed Set, Route)
             * and leaves intact the obstacles and the robot and target positions
             * in order to be able to run another algorithm
             * with the same data.
             * With the second click removes any obstacles also.
             */
            if (searching || endOfSearch){
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < columns; c++) {
                        if (grid[r][c] == FRONTIER || grid[r][c] == CLOSED || grid[r][c] == ROUTE)
                            grid[r][c] = EMPTY;
                        if (grid[r][c] == ROBOT)
                            robotStart = new Cell(r,c);
                        if (grid[r][c] == TARGET)
                            targetPos = new Cell(r,c);
                    }
                searching = false;
            } else {
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < columns; c++)
                        grid[r][c] = EMPTY;
                robotStart = new Cell(rows-2,1);
                targetPos = new Cell(1,columns-2);
            }
            if (aStar.isSelected() || greedy.isSelected()){
                robotStart.g = 0;
                robotStart.h = 0;
                robotStart.f = 0;
            }
            expanded = 0;
            found = false;
            searching = false;
            endOfSearch = false;

            // The first step of the other four algorithms is here
            // 1. OPEN SET: = [So], CLOSED SET: = []
            openSet.removeAll(openSet);
            openSet.add(robotStart);
            closedSet.removeAll(closedSet);

            grid[targetPos.row][targetPos.col] = TARGET;
            grid[robotStart.row][robotStart.col] = ROBOT;
            message.setText(MSG_DRAW_AND_SELECT);
            timer.stop();
            repaint();

        } // end fillGrid()

        /**
         * Enables radio buttons and checkboxes
         */
        private void enableRadiosAndChecks() {
            slider.setEnabled(true);
            dfs.setEnabled(true);
            bfs.setEnabled(true);
            aStar.setEnabled(true);
            greedy.setEnabled(true);
            dijkstra.setEnabled(true);
            diagonal.setEnabled(true);
            drawArrows.setEnabled(true);
        } // end enableRadiosAndChecks()

        /**
         * Disables radio buttons and checkboxes
         */
        private void disableRadiosAndChecks() {
            slider.setEnabled(false);
            dfs.setEnabled(false);
            bfs.setEnabled(false);
            aStar.setEnabled(false);
            greedy.setEnabled(false);
            dijkstra.setEnabled(false);
            diagonal.setEnabled(false);
            drawArrows.setEnabled(false);
        } // end disableRadiosAndChecks()

        /**
         * Executes when the user presses the button "New Grid"
         */
        private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {
            animation = false;
            realTime = false;
            realTimeButton.setEnabled(true);
            realTimeButton.setForeground(Color.black);
            stepButton.setEnabled(true);
            animationButton.setEnabled(true);
            enableRadiosAndChecks();
            initializeGrid(false);
        } // end resetButtonActionPerformed()

        /**
         * Executes when the user presses the button "Maze"
         */
        private void mazeButtonActionPerformed(java.awt.event.ActionEvent evt) {
            animation = false;
            realTime = false;
            realTimeButton.setEnabled(true);
            realTimeButton.setForeground(Color.black);
            stepButton.setEnabled(true);
            animationButton.setEnabled(true);
            enableRadiosAndChecks();
            initializeGrid(true);
        } // end mazeButtonActionPerformed()

        /**
         * Executes when the user presses the button "Clear"
         */
        private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
            animation = false;
            realTime = false;
            realTimeButton.setEnabled(true);
            realTimeButton.setForeground(Color.black);
            stepButton.setEnabled(true);
            animationButton.setEnabled(true);
            enableRadiosAndChecks();
            fillGrid();
        } // end clearButtonActionPerformed()

        /**
         * Executes when the user presses the button "Real-Time"
         */
        private void realTimeButtonActionPerformed(java.awt.event.ActionEvent evt) {
            if (realTime)
                return;
            realTime = true;
            searching = true;
            // The Dijkstra's initialization should be done just before the
            // start of search, because obstacles must be in place.
            if (dijkstra.isSelected())
                initializeDijkstra();
            realTimeButton.setForeground(Color.red);
            disableRadiosAndChecks();
            realTimeAction();
        } // end realTimeButtonActionPerformed()

        /**
         * Action performed during real-time search
         */
        public void realTimeAction() {
            do
                checkTermination();
            while (!endOfSearch);
        } // end of realTimeAction()

        /**
         * Executes when the user presses the button "Step-by-Step"
         */
        private void stepButtonActionPerformed(java.awt.event.ActionEvent evt) {
            animation = false;
            timer.stop();
            if (found || endOfSearch)
                return;
            if (!searching && dijkstra.isSelected())
                initializeDijkstra();
            searching = true;
            message.setText(MSG_SELECT_STEP_BY_STEP_ETC);
            realTimeButton.setEnabled(false);
            disableRadiosAndChecks();
            slider.setEnabled(true);
            checkTermination();
            repaint();
        } // end stepButtonActionPerformed()

        /**
         * Executes when the user presses the button "Animation"
         */
        private void animationButtonActionPerformed(java.awt.event.ActionEvent evt) {
            animation = true;
            if (!searching && dijkstra.isSelected())
                initializeDijkstra();
            searching = true;
            message.setText(MSG_SELECT_STEP_BY_STEP_ETC);
            realTimeButton.setEnabled(false);
            disableRadiosAndChecks();
            slider.setEnabled(true);
            delay = slider.getValue();
            timer.setDelay(delay);
            timer.start();
        } // end animationButtonActionPerformed()

        /**
         * Executes when the user presses the button "About Maze"
         */

        /**
         * Checks if we have reached the end of search
         */
        public void checkTermination() {
            // Here we decide whether we can continue the search or not.

            // In the case of Dijkstra's algorithm
            // here we check condition of step 11:
            // 11. while Q is not empty.

            // In the case of DFS, BFS, A* and Greedy algorithms
            // here we have the second step:
            // 2. If OPEN SET = [], then terminate. There is no solution.
            if ((dijkstra.isSelected() && graph.isEmpty()) ||
                    (!dijkstra.isSelected() && openSet.isEmpty()) ) {
                endOfSearch = true;
                grid[robotStart.row][robotStart.col]=ROBOT;
                message.setText(MSG_NO_SOLUTION);
                stepButton.setEnabled(false);
                animationButton.setEnabled(false);
                repaint();
            } else {
                expandNode();
                if (found) {
                    endOfSearch = true;
                    plotRoute();
                    stepButton.setEnabled(false);
                    animationButton.setEnabled(false);
                    slider.setEnabled(false);
                    repaint();
                }
            }
        }

        /**
         * Expands a node and creates his successors
         */
        private void expandNode(){
            // Dijkstra's algorithm to handle separately
            if (dijkstra.isSelected()){
                Cell u;
                // 11: while Q is not empty:
                if (graph.isEmpty())
                    return;
                // 12:  u := vertex in Q (graph) with smallest distance in dist[] ;
                // 13:  remove u from Q (graph);
                u = graph.remove(0);
                // Add vertex u in closed set
                closedSet.add(u);
                // If target has been found ...
                if (u.row == targetPos.row && u.col == targetPos.col){
                    found = true;
                    return;
                }
                // Counts nodes that have expanded.
                expanded++;
                // Update the color of the cell
                grid[u.row][u.col] = CLOSED;
                // 14: if dist[u] = infinity:
                if (u.dist == INFINITY){
                    // ... then there is no solution.
                    // 15: break;
                    return;
                } // 16: end if
                // Create the neighbors of u
                ArrayList<Cell> neighbors = createSuccesors(u, false);
                // 18: for each neighbor v of u:
                for (Cell v: neighbors) {
                    // 20: alt := dist[u] + dist_between(u, v) ;
                    double alt = u.dist + distBetween(u,v);
                    // 21: if alt < dist[v]:
                    if (alt < v.dist) {
                        // 22: dist[v] := alt ;
                        v.dist = alt;
                        // 23: previous[v] := u ;
                        v.prev = u;
                        // Update the color of the cell
                        grid[v.row][v.col] = FRONTIER;
                        // 24: decrease-key v in Q;
                        // (sort list of nodes with respect to dist)
                        Collections.sort(graph, new CellComparatorByDist());
                    }
                }
            } else { // The handling of the other four algorithms
                Cell current;
                if (dfs.isSelected() || bfs.isSelected()) {
                    // Here is the 3rd step of the algorithms DFS and BFS
                    // 3. Remove the first state, Si, from OPEN SET ...
                    current = openSet.remove(0);
                } else {
                    // Here is the 3rd step of the algorithms A* and Greedy
                    // 3. Remove the first state, Si, from OPEN SET,
                    // for which f(Si) ≤ f(Sj) for all other
                    // open states Sj  ...
                    // (sort first OPEN SET list with respect to 'f')
                    Collections.sort(openSet, new CellComparatorByF());
                    current = openSet.remove(0);
                }
                // ... and add it to CLOSED SET.
                closedSet.add(0,current);
                // Update the color of the cell
                grid[current.row][current.col] = CLOSED;
                // If the selected node is the target ...
                if (current.row == targetPos.row && current.col == targetPos.col) {
                    // ... then terminate etc
                    Cell last = targetPos;
                    last.prev = current.prev;
                    closedSet.add(last);
                    found = true;
                    return;
                }
                // Count nodes that have been expanded.
                expanded++;
                // Here is the 4rd step of the algorithms
                // 4. Create the successors of Si, based on actions
                //    that can be implemented on Si.
                //    Each successor has a pointer to the Si, as its predecessor.
                //    In the case of DFS and BFS algorithms, successors should not
                //    belong neither to the OPEN SET nor the CLOSED SET.
                ArrayList<Cell> succesors;
                succesors = createSuccesors(current, false);
                // Here is the 5th step of the algorithms
                // 5. For each successor of Si, ...
                for (Cell cell: succesors){
                    // ... if we are running DFS ...
                    if (dfs.isSelected()) {
                        // ... add the successor at the beginning of the list OPEN SET
                        openSet.add(0, cell);
                        // Update the color of the cell
                        grid[cell.row][cell.col] = FRONTIER;
                        // ... if we are runnig BFS ...
                    } else if (bfs.isSelected()){
                        // ... add the successor at the end of the list OPEN SET
                        openSet.add(cell);
                        // Update the color of the cell
                        grid[cell.row][cell.col] = FRONTIER;
                        // ... if we are running A* or Greedy algorithms (step 5 of A* algorithm) ...
                    } else if (aStar.isSelected() || greedy.isSelected()){
                        // ... calculate the value f(Sj) ...
                        int dxg = current.col-cell.col;
                        int dyg = current.row-cell.row;
                        int dxh = targetPos.col-cell.col;
                        int dyh = targetPos.row-cell.row;
                        if (diagonal.isSelected()){
                            // with diagonal movements
                            // calculate the Euclidean distance
                            if (greedy.isSelected()) {
                                // especially for the Greedy ...
                                cell.g = 0;
                            } else {
                                cell.g = current.g + Math.sqrt(dxg*dxg + dyg*dyg);
                            }
                            cell.h = Math.sqrt(dxh*dxh + dyh*dyh);
                        } else {
                            // without diagonal movements
                            // calculate the Manhattan distance
                            if (greedy.isSelected()) {
                                // especially for the Greedy ...
                                cell.g = 0;
                            } else {
                                cell.g = current.g + Math.abs(dxg)+Math.abs(dyg);
                            }
                            cell.h = Math.abs(dxh)+Math.abs(dyh);
                        }
                        cell.f = cell.g+cell.h;
                        // ... If Sj is neither in the OPEN SET nor in the CLOSED SET states ...
                        int openIndex   = isInList(openSet,cell);
                        int closedIndex = isInList(closedSet,cell);
                        if (openIndex == -1 && closedIndex == -1) {
                            // ... then add Sj in the OPEN SET ...
                            // ... evaluated as f(Sj)
                            openSet.add(cell);
                            // Update the color of the cell
                            grid[cell.row][cell.col] = FRONTIER;
                            // Else ...
                        } else {
                            // ... if already belongs to the OPEN SET, then ...
                            if (openIndex > -1){
                                // ... compare the new value assessment with the old one.
                                // If old <= new ...
                                if (openSet.get(openIndex).f <= cell.f) {
                                    // ... then eject the new node with state Sj.
                                    // (ie do nothing for this node).
                                    // Else, ...
                                } else {
                                    // ... remove the element (Sj, old) from the list
                                    // to which it belongs ...
                                    openSet.remove(openIndex);
                                    // ... and add the item (Sj, new) to the OPEN SET.
                                    openSet.add(cell);
                                    // Update the color of the cell
                                    grid[cell.row][cell.col] = FRONTIER;
                                }
                                // ... if already belongs to the CLOSED SET, then ...
                            } else {
                                // ... compare the new value assessment with the old one.
                                // If old <= new ...
                                if (closedSet.get(closedIndex).f <= cell.f) {
                                    // ... then eject the new node with state Sj.
                                    // (ie do nothing for this node).
                                    // Else, ...
                                } else {
                                    // ... remove the element (Sj, old) from the list
                                    // to which it belongs ...
                                    closedSet.remove(closedIndex);
                                    // ... and add the item (Sj, new) to the OPEN SET.
                                    openSet.add(cell);
                                    // Update the color of the cell
                                    grid[cell.row][cell.col] = FRONTIER;
                                }
                            }
                        }
                    }
                }
            }
        } //end expandNode()
        private ArrayList<Cell> createSuccesors(Cell current, boolean makeConnected){
            int r = current.row;
            int c = current.col;
            // We create an empty list for the successors of the current cell.
            ArrayList<Cell> temp = new ArrayList<>();

            if (r > 0 && grid[r-1][c] != OBST &&
                    // ... and (only in the case are not running the A* or Greedy)
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                            isInList(openSet,new Cell(r-1,c)) == -1 &&
                                    isInList(closedSet,new Cell(r-1,c)) == -1)) {
                Cell cell = new Cell(r-1,c);

                if (dijkstra.isSelected()){
                    if (makeConnected)
                        temp.add(cell);
                    else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1)
                            temp.add(graph.get(graphIndex));
                    }
                } else {
                    // ... update the pointer of the up-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the up-side cell to the successors of the current one.
                    temp.add(cell);
                }
            }
            if (diagonal.isSelected()){
                // If we are not even at the topmost nor at the rightmost border of the grid
                // and the up-right-side cell is not an obstacle ...
                if (r > 0 && c < columns-1 && grid[r-1][c+1] != OBST &&
                        // ... and one of the upper side or right side cells are not obstacles ...
                        // (because it is not reasonable to allow
                        // the robot to pass through a "slot")
                        (grid[r-1][c] != OBST || grid[r][c+1] != OBST) &&
                        // ... and (only in the case are not running the A* or Greedy)
                        // not already belongs neither to the OPEN SET nor CLOSED SET ...
                        ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                                isInList(openSet,new Cell(r-1,c+1)) == -1 &&
                                        isInList(closedSet,new Cell(r-1,c+1)) == -1)) {
                    Cell cell = new Cell(r-1,c+1);
                    if (dijkstra.isSelected()){
                        if (makeConnected)
                            temp.add(cell);
                        else {
                            int graphIndex = isInList(graph,cell);
                            if (graphIndex > -1)
                                temp.add(graph.get(graphIndex));
                        }
                    } else {
                        // ... update the pointer of the up-right-side cell so it points the current one ...
                        cell.prev = current;
                        // ... and add the up-right-side cell to the successors of the current one.
                        temp.add(cell);
                    }
                }
            }
            // If not at the rightmost limit of the grid
            // and the right-side cell is not an obstacle ...
            if (c < columns-1 && grid[r][c+1] != OBST &&
                    // ... and (only in the case are not running the A* or Greedy)
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected())? true :
                            isInList(openSet,new Cell(r,c+1)) == -1 &&
                                    isInList(closedSet,new Cell(r,c+1)) == -1)) {
                Cell cell = new Cell(r,c+1);
                if (dijkstra.isSelected()){
                    if (makeConnected)
                        temp.add(cell);
                    else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1)
                            temp.add(graph.get(graphIndex));
                    }
                } else {
                    // ... update the pointer of the right-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the right-side cell to the successors of the current one.
                    temp.add(cell);
                }
            }
            if (diagonal.isSelected()){
                // If we are not even at the lowermost nor at the rightmost border of the grid
                // and the down-right-side cell is not an obstacle ...
                if (r < rows-1 && c < columns-1 && grid[r+1][c+1] != OBST &&
                        // ... and one of the down-side or right-side cells are not obstacles ...
                        (grid[r+1][c] != OBST || grid[r][c+1] != OBST) &&
                        // ... and (only in the case are not running the A* or Greedy)
                        // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                        ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                                isInList(openSet,new Cell(r+1,c+1)) == -1 &&
                                        isInList(closedSet,new Cell(r+1,c+1)) == -1)) {
                    Cell cell = new Cell(r+1,c+1);
                    if (dijkstra.isSelected()){
                        if (makeConnected)
                            temp.add(cell);
                        else {
                            int graphIndex = isInList(graph,cell);
                            if (graphIndex > -1)
                                temp.add(graph.get(graphIndex));
                        }
                    } else {
                        // ... update the pointer of the downr-right-side cell so it points the current one ...
                        cell.prev = current;
                        // ... and add the down-right-side cell to the successors of the current one.
                        temp.add(cell);
                    }
                }
            }
            // If not at the lowermost limit of the grid
            // and the down-side cell is not an obstacle ...
            if (r < rows-1 && grid[r+1][c] != OBST &&
                    // ... and (only in the case are not running the A* or Greedy)
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                            isInList(openSet,new Cell(r+1,c)) == -1 &&
                                    isInList(closedSet,new Cell(r+1,c)) == -1)) {
                Cell cell = new Cell(r+1,c);
                if (dijkstra.isSelected()){
                    if (makeConnected)
                        temp.add(cell);
                    else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1)
                            temp.add(graph.get(graphIndex));
                    }
                } else {
                    // ... update the pointer of the down-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the down-side cell to the successors of the current one.
                    temp.add(cell);
                }
            }
            if (diagonal.isSelected()){
                // If we are not even at the lowermost nor at the leftmost border of the grid
                // and the down-left-side cell is not an obstacle ...
                if (r < rows-1 && c > 0 && grid[r+1][c-1] != OBST &&
                        // ... and one of the down-side or left-side cells are not obstacles ...
                        (grid[r+1][c] != OBST || grid[r][c-1] != OBST) &&
                        // ... and (only in the case are not running the A* or Greedy)
                        // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                        ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                                isInList(openSet,new Cell(r+1,c-1)) == -1 &&
                                        isInList(closedSet,new Cell(r+1,c-1)) == -1)) {
                    Cell cell = new Cell(r+1,c-1);
                    if (dijkstra.isSelected()){
                        if (makeConnected)
                            temp.add(cell);
                        else {
                            int graphIndex = isInList(graph,cell);
                            if (graphIndex > -1)
                                temp.add(graph.get(graphIndex));
                        }
                    } else {
                        // ... update the pointer of the down-left-side cell so it points the current one ...
                        cell.prev = current;
                        // ... and add the down-left-side cell to the successors of the current one.
                        temp.add(cell);
                    }
                }
            }
            // If not at the leftmost limit of the grid
            // and the left-side cell is not an obstacle ...
            if (c > 0 && grid[r][c-1] != OBST &&
                    // ... and (only in the case are not running the A* or Greedy)
                    // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                    ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                            isInList(openSet,new Cell(r,c-1)) == -1 &&
                                    isInList(closedSet,new Cell(r,c-1)) == -1)) {
                Cell cell = new Cell(r,c-1);
                if (dijkstra.isSelected()){
                    if (makeConnected)
                        temp.add(cell);
                    else {
                        int graphIndex = isInList(graph,cell);
                        if (graphIndex > -1)
                            temp.add(graph.get(graphIndex));
                    }
                } else {
                    // ... update the pointer of the left-side cell so it points the current one ...
                    cell.prev = current;
                    // ... and add the left-side cell to the successors of the current one.
                    temp.add(cell);
                }
            }
            if (diagonal.isSelected()){
                // If we are not even at the topmost nor at the leftmost border of the grid
                // and the up-left-side cell is not an obstacle ...
                if (r > 0 && c > 0 && grid[r-1][c-1] != OBST &&
                        // ... and one of the up-side or left-side cells are not obstacles ...
                        (grid[r-1][c] != OBST || grid[r][c-1] != OBST) &&
                        // ... and (only in the case are not running the A* or Greedy)
                        // not already belongs neither to the OPEN SET nor to the CLOSED SET ...
                        ((aStar.isSelected() || greedy.isSelected() || dijkstra.isSelected()) ? true :
                                isInList(openSet,new Cell(r-1,c-1)) == -1 &&
                                        isInList(closedSet,new Cell(r-1,c-1)) == -1)) {
                    Cell cell = new Cell(r-1,c-1);
                    if (dijkstra.isSelected()){
                        if (makeConnected)
                            temp.add(cell);
                        else {
                            int graphIndex = isInList(graph,cell);
                            if (graphIndex > -1)
                                temp.add(graph.get(graphIndex));
                        }
                    } else {
                        // ... update the pointer of the up-left-side cell so it points the current one ...
                        cell.prev = current;
                        // ... and add the up-left-side cell to the successors of the current one.
                        temp.add(cell);
                    }
                }
            }
            // When DFS algorithm is in use, cells are added one by one at the beginning of the
            // OPEN SET list. Because of this, we must reverse the order of successors formed,
            // so the successor corresponding to the highest priority, to be placed
            // the first in the list.
            // For the Greedy, A* and Dijkstra's no issue, because the list is sorted
            // according to 'f' or 'dist' before extracting the first element of.
            if (dfs.isSelected())
                Collections.reverse(temp);

            return temp;
        } // end createSuccesors()

        /**
         * Returns the distance between two cells
         *
         * @param u the first cell
         * @param v the other cell
         * @return  the distance between the cells u and v
         */
        private double distBetween(Cell u, Cell v){
            double dist;
            int dx = u.col-v.col;
            int dy = u.row-v.row;
            if (diagonal.isSelected()){
                // with diagonal movements
                // calculate the Euclidean distance
                dist = Math.sqrt(dx*dx + dy*dy);
            } else {
                // without diagonal movements
                // calculate the Manhattan distance
                dist = Math.abs(dx)+Math.abs(dy);
            }
            return dist;
        } // end distBetween()

        /**
         * Returns the index of the cell 'current' in the list 'list'
         *
         * @param list    the list in which we seek
         * @param current the cell we are looking for
         * @return        the index of the cell in the list
         *                if the cell is not found returns -1
         */
        private int isInList(ArrayList<Cell> list, Cell current){
            int index = -1;
            for (int i = 0 ; i < list.size(); i++) {
                Cell listItem = list.get(i);
                if (current.row == listItem.row && current.col == listItem.col) {
                    index = i;
                    break;
                }
            }
            return index;
        } // end isInList()

        /**
         * Returns the predecessor of cell 'current' in list 'list'
         *
         * @param list      the list in which we seek
         * @param current   the cell we are looking for
         * @return          the predecessor of cell 'current'
         */
        private Cell findPrev(ArrayList<Cell> list, Cell current){
            int index = isInList(list, current);
            Cell listItem = list.get(index);
            return listItem.prev;
        } // end findPrev()

        /**
         * Calculates the path from the target to the initial position
         * of the robot, counts the corresponding steps
         * and measures the distance traveled.
         */
        private void plotRoute(){
            int steps = 0;
            double distance = 0;
            int index = isInList(closedSet,targetPos);
            Cell cur = closedSet.get(index);
            grid[cur.row][cur.col]= TARGET;
            do {
                steps++;
                if (diagonal.isSelected()) {
                    int dx = cur.col-cur.prev.col;
                    int dy = cur.row-cur.prev.row;
                    distance += Math.sqrt(dx*dx + dy*dy);
                } else
                    distance++;
                cur = cur.prev;
                grid[cur.row][cur.col] = ROUTE;
            } while (!(cur.row == robotStart.row && cur.col == robotStart.col));
            grid[robotStart.row][robotStart.col]=ROBOT;
            String msg;
            msg = String.format("Nodes expanded: %d, Steps: %d, Distance: %.3f",
                    expanded,steps,distance);
            message.setText(msg);

        } // end plotRoute()

        /**
         * Appends to the list containing the nodes of the graph only
         * the cells belonging to the same connected component with node v.
         * This is a Breadth First Search of the graph starting from node v.
         *
         * @param v    the starting node
         */
        private void findConnectedComponent(Cell v){
            Stack<Cell> stack;
            stack = new Stack();
            ArrayList<Cell> succesors;
            stack.push(v);
            graph.add(v);
            while(!stack.isEmpty()){
                v = stack.pop();
                succesors = createSuccesors(v, true);
                for (Cell c: succesors) {
                    if (isInList(graph, c) == -1){
                        stack.push(c);
                        graph.add(c);
                    }
                }
            }
        } // end findConnectedComponent()

        /**
         * Initialization of Dijkstra's algorithm
         */
        private void initializeDijkstra() {

            // First create the connected component
            // to which the initial position of the robot belongs.
            graph.removeAll(graph);
            findConnectedComponent(robotStart);
            // Here is the initialization of Dijkstra's algorithm
            // 2: for each vertex v in Graph;
            for (Cell v: graph) {
                // 3: dist[v] := infinity ;
                v.dist = INFINITY;
                // 5: previous[v] := undefined ;
                v.prev = null;
            }
            // 8: dist[source] := 0;
            graph.get(isInList(graph,robotStart)).dist = 0;
            // 9: Q := the set of all nodes in Graph;
            // Instead of the variable Q we will use the list
            // 'graph' itself, which has already been initialised.

            // Sorts the list of nodes with respect to 'dist'.
            Collections.sort(graph, new CellComparatorByDist());
            // Initializes the list of closed nodes
            closedSet.removeAll(closedSet);
        } // end initializeDijkstra()

        /**
         * Repaints the grid
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.DARK_GRAY);
            // Fills the background color.
            g.fillRect(10, 10, columns*squareSize+1, rows*squareSize+1);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (grid[r][c] == EMPTY) {
                        g.setColor(Color.WHITE);
                    } else if (grid[r][c] == ROBOT) {
                        g.setColor(Color.RED);
                    } else if (grid[r][c] == TARGET) {
                        g.setColor(Color.GREEN);
                    } else if (grid[r][c] == OBST) {
                        g.setColor(Color.BLACK);
                    } else if (grid[r][c] == FRONTIER) {
                        g.setColor(Color.BLUE);
                    } else if (grid[r][c] == CLOSED) {
                        g.setColor(Color.CYAN);
                    } else if (grid[r][c] == ROUTE) {
                        g.setColor(Color.YELLOW);
                    }
                    g.fillRect(11 + c*squareSize, 11 + r*squareSize, squareSize - 1, squareSize - 1);
                }
            }


            if (drawArrows.isSelected()) {
                // We draw all arrows from each open or closed state
                // to its predecessor.
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < columns; c++)
                        // If the current cell is the goal and the solution has been found,
                        // or belongs in the route to the target,
                        // or is an open state,
                        // or is a closed state but not the initial position of the robot
                        if ((grid[r][c] == TARGET && found)  || grid[r][c] == ROUTE  ||
                                grid[r][c] == FRONTIER || (grid[r][c] == CLOSED &&
                                !(r == robotStart.row && c == robotStart.col))){
                            // The tail of the arrow is the current cell, while
                            // the arrowhead is the predecessor cell.
                            Cell head;
                            if (grid[r][c] == FRONTIER)
                                if (dijkstra.isSelected())
                                    head = findPrev(graph,new Cell(r,c));
                                else
                                    head = findPrev(openSet,new Cell(r,c));
                            else
                                head = findPrev(closedSet,new Cell(r,c));

                            // The coordinates of the center of the current cell
                            int tailX = 11 + c * squareSize + squareSize / 2;
                            int tailY = 11 + r * squareSize + squareSize / 2;
                            // The coordinates of the center of the predecessor cell
                            int headX = 11 + head.col * squareSize + squareSize / 2;
                            int headY = 11 + head.row * squareSize + squareSize / 2;
                            int thickness = squareSize > 25 ? 2 : 1;

                            // If the current cell is the target
                            // or belongs to the path to the target ...
                            if (grid[r][c] == TARGET  || grid[r][c] == ROUTE){
                                // ... draw a red arrow directing to the target.
                                g.setColor(Color.RED);
                                drawArrow(g,thickness,tailX,tailY,headX,headY);
                                // Else ...
                            } else {
                                // ... draw a black arrow to the predecessor cell.
                                g.setColor(Color.BLACK);
                                drawArrow(g,thickness,headX,headY,tailX,tailY);
                            }
                        }
            }
        } // end paintComponent()

        /**
         * Draws an arrow from point (x2,y2) to point (x1,y1)
         */
        private void drawArrow(Graphics g1, int thickness, int x1, int y1, int x2, int y2) {
            Graphics2D g = (Graphics2D) g1.create();

            double dx = x2 - x1, dy = y2 - y1;
            double angle = Math.atan2(dy, dx);
            int len = (int) Math.sqrt(dx * dx + dy * dy);
            AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
            at.concatenate(AffineTransform.getRotateInstance(angle));
            g.transform(at);

            // We draw an horizontal arrow 'len' in length
            // that ends at the point (0,0) with two tips 'arrowSize' in length
            // which form 20 degrees angles with the axis of the arrow ...
            g.setStroke(new BasicStroke(thickness));
            g.drawLine(0, 0, len, 0);
            g.drawLine(0, 0, (int)(arrowSize * Math.sin(70 * Math.PI / 180)) , (int)(arrowSize  *Math.cos(70 * Math.PI / 180)));
            g.drawLine(0, 0, (int)(arrowSize * Math.sin(70 * Math.PI / 180)) , -(int)(arrowSize * Math.cos(70 * Math.PI / 180)));
            // ... and class AffineTransform handles the rest !!!!!!
        } // end drawArrow()

    } // end nested classs MazePanel

} // end class Maze
