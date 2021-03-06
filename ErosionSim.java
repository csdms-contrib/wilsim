/***********************************************************************************************
CLASS:	      ErosionSim

FUNCTION:     ErosionSim is a class where the cellular automata algorithm is implemented. 
	      The class contains:
	      -Constructor: 
    		*ErosionSim(SharedParameters s, ErosionCanvas e, ErosionCanvas e25p, ErosionCanvas e50p, ErosionCanvas e75p, ErosionCanvas e100p,
    				ErosionIntervals i, ErosionIntervals is, 
    				ErosionIntervals c, ErosionIntervals cs, ErosionIntervals r, ErosionIntervals rs, 
    				ErosionHypsometric h)
    		 it initilializes needed variables to be able to communicate with other parts of the applet.    		   
	      -Helping functions:
    		*public void start()
    		 it sets up the thread that will run the algorithm
		*public void run()
		 what the thread will do while it is true. It will first
		 reset the parameters and conditions, then it will check for ending
		 conditions and if everything is alright, it will proceed to execute
		 the algorithm.
		 Algorithm inside run() = if the precipiton has just fallen, it starts
		 the counter, and it gets a random cell in the grid. If it is first
		 time, it will calculate the erosion percentages for the rest of the 
		 simulation.		
		 It will also check if tectonics is enabled and will proceed to apply
		 it.
		 Once those functions are executed, it calls a function to get the 8
		 cells surrounding the randomly chosen one and calls a function to 
		 apply diffusion.
		 After checking that a wall was not hit, we continue in the while loop
		 by calling the functions to search the lowest cell in the group, to
		 calculate the corresponding erosion, to check the carrying capacity and 
		 to draw the grid with the changes.
		 The target coordinates move from the randomly chosen cell to the lowest
		 cell and the loop continues.
		 It takes care of interval saving and snapshot calculations when required
	    	*private boolean bored()
	    	 to check the status of the thread
	    	*public synchronized void resume()
	    	 to get the thread going
    		*public void suspend()
    		 to supend the thread
    		*private void reset()
    		 to do the first set up of parameters and values
	    	*private void setColors()
	    	 to check for colors in the rainbow
	    	*private void resetSlope()
	    	 to check for slope in graph
    		*private void resetCanvasSlope(int j, int i, float height)
    		 to reset the canvas
    		*private void getstartingCell()
    		 this functions gets an x,y random location in the topographic grid.
   		*private void getSurroundingCells() 
   		 this function gets the cells surrounding the randomly selected one.
		*public void geterosionValue()
		 get erosion according to parameters from applet.
		*public void applyDiffusion()
		 apply diffusion when first precipiton falls.
		*public void searchlowestCell()
		 search for lowest cell in 3x3 grid
		*public void calculateErosion() 
		 apply the erosion according to erodibility parameters
		*public void checkcarryingCapacity() 
		 check the carrying capacity
		*public void applyTectonics() 
		 apply tectonics if selected
	 	*public void saveInterval() 
	 	 to save intervals
    		*private void resetIntervalArrays()
		 to reset arrays for interval values
    		*private void resetIntervals(ErosionIntervals eicopy, boolean rowflag)
		 to reset the canvas
		*public void cleanup() 
		 set variables before each iteration
		*public void printMessages(int messageNumber) 
		 function called to print messages
	        *void resetHypsometric()
	         to reset hypsometric values and image
		*void calculateHypsometric()
		 to calculate and get the curve drawn
		
INPUT:	      User will select parameters.

DATE CREATED: August 2002
***********************************************************************************************/
import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class ErosionSim implements Runnable
    	{
    	SharedParameters gv;
    	ErosionCanvas ecanv, ecanv25p, ecanv50p, ecanv75p, ecanv100p;

	Thread thisthread;  // The simulation thread
	// variables for the surface object creation
    	static int BARMINHEIGHT = 20;
    	static int BARMINWIDTH = 2;
    	static int EDGES = 2;
    	double incrvertical = 0;
    	double incrhorizontal = 0;
    	double x, y, x1, y1 = 0;
	int newcolor[] = new int[3];
	int changecolor[] = new int[3];
    	double slope;
	//to hold the grid objects
	SurfaceBar surfaceArray[][];
	//to save interval information
	double sumColumns[];
	double sumColumnsBedrock[];
	double sumRows[];
	double sumIntervalsSediment[];
	double averageIntervals[];
	double averageIntervalsBedrock[];
	double columnIntervals[];
	double columnIntervalsBedrock[];
	double columnSediment[];
	double rowIntervals[];
	double rowIntervalsBedrock[];
	double rowSediment[];
	ErosionIntervals ei, eis, er, ers, ec, ecs;
	boolean rowflag = true;
	//flags needed troughout the simulation
    	boolean keepgoing, firstTime, uiSetup, running;
	static boolean startNeeded = true;
	//flag for any bar found
	boolean anybars = false;
	//variables for diffusion, tectonics, erosion and capacity
    	static int minHeight, nextHeight, current;
	int xcoordArray[] = new int[9];
	int ycoordArray[] = new int[9];
	int xcoordDiffusionArray[] = new int[4];
	int ycoordDiffusionArray[] = new int[4];
	int diffusesedimentx = 0;
	int diffusesedimenty = 0;
	int getsedimentx = 0;
	int getsedimenty = 0;
	int irand, jrand, pirand, pjrand;
	double currentDiffusion = 0;
	double possibleDiffusion = 0;
	double diffusionPower = 0;
	int steps = 0;
	double heightDifference = 0;
	double heightDiff = 0;
	//coord variables to be used later
	int newx, newy;
	double targetBar = 0;
	double lowestBar = 0;
    	//for messages
    	static Label values2 = new Label("");
    	static Label values4 = new Label("");
	// Erosion variables
	double erosion, rand, randxleft, randxright, randybottom, randytop, randerosion;
	double erosionPower, possibleErosion;
	double currentErosion = 0;
	double basicErosion = 0;
	ErosionColors colors = new ErosionColors();
	int resetColor = 0;
	int incrIndex = 0;
	int incrDiffusionIndex = 0;
	static int intervalStep = 1;
	static int intervalCounter = 0;
	static int nIntervals = 10;
	static double min = 20;
	static double max = 21;
	//for the hypsometric curve graph
        ErosionHypsometric eh;
	static double relativeHeight[];
	static double relativeArea[];
	static int HYPSOINTERVAL = 11;
	static boolean initialize = true;

    	// For debugging purposes
    	Frame errs;
    	TextArea msg;
		
/***********************************************************************************************
    Constructor	
***********************************************************************************************/
    ErosionSim(SharedParameters s, ErosionCanvas e, 
    		ErosionCanvas e25p, ErosionCanvas e50p, ErosionCanvas e75p, ErosionCanvas e100p,
    		ErosionIntervals i, ErosionIntervals is, 
    		ErosionIntervals c, ErosionIntervals cs, 
    		ErosionIntervals r, ErosionIntervals rs, 
    		ErosionHypsometric h)
    	{
	gv = s;
	ecanv = e;
	ecanv25p = e25p;
	ecanv50p = e50p;
	ecanv75p = e75p;
	ecanv100p = e100p;
	ei = i;
	eis = is;
	er = r;
	ers = rs;
	ec = c;
	ecs = cs;
	eh = h;
	uiSetup = false;
	thisthread = new Thread(this);
	// Set up the debugging frame
	errs = new Frame("Debugging info");
	errs.setSize(500, 500);
	msg = new TextArea("");
	errs.add(msg);
//	errs.show();
    	}

/***********************************************************************************************
    to get the simulation started
***********************************************************************************************/
    public void start()
    	{
	// Reduce priority of renderer so that UI takes precedence
	Thread current = thisthread.currentThread();
	thisthread.setPriority(current.getPriority() - 1);
	thisthread.start();
    	}

/***********************************************************************************************
    this function drives the simulation
***********************************************************************************************/
    public void run()
    	{
	// Initialize all the data values
	reset();  
	while(true)
	    	{
		if(bored())
		    	{
			synchronized(this)
				{
			    	try 
			    		{
					wait();
				    	}
			    	catch (InterruptedException ie){}
				}
				continue;  // Recheck conditions
		    	}

		// Let's do something!
		if(gv.ITERATIONCOUNTER >= gv.TOTALYEARS)
		    	{
			// Time to stop -- all done
			running = false;
			gv.ROUTINESTARTED = false;
			continue;
		    	}

		// To get it started
		if(firstTime)
		    	{
			gv.ITERATIONCOUNTER += 1;
			intervalStep++;
			// Call getstartingCell to get the first random bar
			getstartingCell();
		    	}
		// To re-calculate erosion values every time a change occurs
		if(gv.EROSIONNEEDED)
		    	{
			geterosionValue();
			gv.EROSIONNEEDED = false;
		    	}
				
		// To apply tectonics if selected
		if(gv.APPLYTECTONICS && firstTime)
		    	{
   		    	applyTectonics();
   			}

		// To look for the eight target surrounding cells every time it 
		// goes through this loop
		getSurroundingCells();
		
		//get erosion value according to applet parameters and apply diffusion
		if(firstTime && keepgoing)
		    {
			applyDiffusion();
			firstTime = false;
		    }
		
		//only apply these if getSurroundingCells was successful       
		if(keepgoing)
		    {
			//search for the lowest cell in the 3x3 grid
			searchlowestCell();
			
			//apply erosion according to erodibility parameters
			calculateErosion();	
			
			//check capacity
			checkcarryingCapacity();
												
			//always calculate for intervals - it doesn't hurt
			if(intervalStep == (gv.ENDTIME / nIntervals) || initialize)
				{
				saveInterval();
				intervalStep = 1;	
				initialize = false;
				calculateHypsometric();
				intervalCounter++;
				}		
			
			//set lowest bar as target
			jrand = newx; 
			irand = newy; 
		    }
		// to make sure colors are set right for the snapshots as well as for the animation
		if((gv.BARSPROCESSED % 7500) == 0 || gv.ITERATIONCOUNTER == gv.ENDTIME || gv.ITERATIONCOUNTER == 0 
		    || gv.ITERATIONCOUNTER == gv.ENDTIME / 4
		    || gv.ITERATIONCOUNTER == gv.ENDTIME / 4 * 2
		    || gv.ITERATIONCOUNTER == gv.ENDTIME / 4 * 3
		    || gv.ITERATIONCOUNTER == gv.ENDTIME - 5)
			{
			setColors();
			ecanv.setWallColor(200, 180, 100);
			}
		ecanv.redraw();

		//work with the snapshots at their corresponding times
		if (gv.ITERATIONCOUNTER == gv.ENDTIME / 4)
			{
			setSnapshot(ecanv25p);
			}
		if (gv.ITERATIONCOUNTER == gv.ENDTIME / 4 * 2)
			{
			setSnapshot(ecanv50p);
			}
		if (gv.ITERATIONCOUNTER == gv.ENDTIME / 4 * 3)
			{
			setSnapshot(ecanv75p);
			}
		if (gv.ITERATIONCOUNTER == (gv.ENDTIME - 5))
			{
			setSnapshot(ecanv100p);
        		gv.SHOWSURFACEBUTTON.setLabel("Run");
			}
		if (gv.ITERATIONCOUNTER == gv.ENDTIME)
			{
			gv.STARTALLOVER = true;
			}
		thisthread.yield();  // Play nice -- let someone else have some time
	    	}
    	}//end of run()

   	
/***********************************************************************************************
    if thread gets lazy
***********************************************************************************************/
    private boolean bored()
    	{
	// When this returns true, there is nothing constructive to do.
	if(running)
	    return false;

	return true;
    	}// end of bored

/***********************************************************************************************
    to get thread going from where it was paused there is checking if stuff needs to be reset
***********************************************************************************************/
    public synchronized void resume()
    	{
	// This is called to start the simulation up (again)
	if(gv.OLDCOLUMNS != gv.COLUMNS || gv.OLDROWS != gv.ROWS)
		{
		gv.OLDCOLUMNS = gv.COLUMNS;
		gv.OLDROWS = gv.ROWS;
		gv.STARTALLOVER = true;	
		}
	if((int)(gv.ENDTIME/gv.TIMESTEP) != gv.TOTALYEARS)
		{
		//make sure the ending time is updated
		values2.setText("" + gv.ENDTIME);
		gv.STARTALLOVER = true;	
		}
	if(gv.EROSIONNEEDED)
		{
		geterosionValue();	
		gv.EROSIONNEEDED = false;
		}	
	if(gv.STARTALLOVER)
		{
		reset(); // Maybe break this out later to separate button?
		gv.STARTALLOVER = false;
		}
	if(gv.OLDSLOPE != gv.SLOPE)
		{
		resetSlope();	
		}
	if(gv.RESETINTERVALSLEGEND)
		{
		resetIntervals(ei, rowflag);
		resetIntervals(eis, rowflag);
		resetIntervals(ec, rowflag);
		resetIntervals(ecs, rowflag);
		rowflag = false;
		resetIntervals(er, rowflag);
		resetIntervals(ers, rowflag);
		rowflag = true;
		resetIntervalArrays();
		gv.RESETINTERVALSLEGEND = false;		
		}
	gv.ROUTINESTARTED = true;
	keepgoing = true;
	running = true;
	notify();
    	}// end of resume

/***********************************************************************************************
    hold on
***********************************************************************************************/
    public void suspend()
    	{
	gv.ROUTINESTARTED = false;
	running = false;
    	}

/***********************************************************************************************
    reset values
***********************************************************************************************/
    private void reset()
    	{
	// When this truly becomes a reset function, the next few lines
	// should go somewhere more appropriate	
	gv.EROSIONNEEDED = true;
	gv.ROUTINESTARTED = false;
        gv.TOTALYEARS = (int) (gv.ENDTIME / gv.TIMESTEP);
	gv.OLDCOLUMNS = gv.COLUMNS;
	gv.OLDROWS = gv.ROWS;
	if(!uiSetup)
	    	{
		values2 = gv.values2;
		values4 = gv.values4;
	    	}
	values4.setText("           ");
	//temporary change the cell size to width = 1 impact on erosion value
	gv.BARWIDTH = 1;
	surfaceArray = new SurfaceBar[gv.ROWS][gv.COLUMNS];
	ecanv.setGridSize(gv.ROWS, gv.COLUMNS);
	ecanv.setViewHeight(BARMINHEIGHT);
	resetSnapshots(ecanv25p);
	resetSnapshots(ecanv50p);
	resetSnapshots(ecanv75p);
	resetSnapshots(ecanv100p);

	x = y = x1 = y1 = 0;
	incrvertical = incrhorizontal = 0;
	newcolor[0] = 220;
	newcolor[1] = 180;
	newcolor[2] = 0;

	firstTime = true;
	startNeeded = true;
	gv.CARRYINGCAPACITY = 0;
	gv.STEPCOUNTER= 0;
	steps = 0;
	intervalStep = 1;
	intervalCounter = 0;
	initialize = true;
	running = false;

	//set iteration counter to zero to start
	gv.ITERATIONCOUNTER = 0;
	gv.BARSPROCESSED = 0;
	gv.COLORCHANGE = 0;
	
	// create the surface
	for(int j = 0; j < gv.ROWS; j++)
	    	{
		for(int i = 0; i < gv.COLUMNS; i++)
		    	{			
			incrhorizontal = (BARMINWIDTH) * i;
			x1 = BARMINWIDTH;
			y1 = BARMINHEIGHT;
			slope = BARMINHEIGHT * gv.SLOPE * j / gv.ROWS;
			//first row only
			if(j == 0)
			 	{
				surfaceArray[j][i] = new SurfaceBar( x, y, 1, y1 - 1, slope, newcolor[0], newcolor[1], newcolor[2]);
				}
			else
				{
				surfaceArray[j][i] = new SurfaceBar( x, y, x1, y1, slope, newcolor[0], newcolor[1], newcolor[2]);
				}
			surfaceArray[j][i].setfinalHeight();
		        resetCanvasSlope(j, i, (float) surfaceArray[j][i].getsurfacefinalHeight());
		    	}//end for i
			incrvertical = incrvertical - 1;
	    }//end for j
	setColors();
	resetIntervals(ei, rowflag);
	resetIntervals(eis, rowflag);
	resetIntervals(ec, rowflag);
	resetIntervals(ecs, rowflag);
	rowflag = false;
	resetIntervals(er, rowflag);
	resetIntervals(ers, rowflag);
	rowflag = true;
	resetIntervalArrays();
	resetHypsometric();
  	}//end reset

/***********************************************************************************************
    when slope is resetted from applet
***********************************************************************************************/
    private void resetSlope()
    	{
	gv.OLDSLOPE = gv.SLOPE;
	for(int j = 0; j < gv.ROWS; j++)
	    	{
		double newSlope = BARMINHEIGHT * gv.SLOPE * j / gv.ROWS;
		for(int i = 0; i < gv.COLUMNS; i++)
		    {			
		    surfaceArray[j][i].setSlope(newSlope);
		    surfaceArray[j][i].setfinalHeight();
		    resetCanvasSlope(j, i, (float) surfaceArray[j][i].getsurfacefinalHeight());		
		    }
		}			
    	}//end of resetSlope()

/***********************************************************************************************
    to reset slope in canvas
***********************************************************************************************/
    private void resetCanvasSlope(int j, int i, float height)
    	{
    	ecanv.setDataHeight(j, i, height);		    
	ecanv25p.setDataHeight(j, i, height);		    
	ecanv50p.setDataHeight(j, i, height);		    
	ecanv75p.setDataHeight(j, i, height);		    
	ecanv100p.setDataHeight(j, i, height);		    
	}//end of reset canvas slope

/***********************************************************************************************
    this function gets an x,y random location in the array
***********************************************************************************************/
    private void getstartingCell()
    	{
	gv.ROUTINESTARTED = true;
	//get random values for first target at the beginnig of an iteration	
	//set index to 1+ off because of the walls
	jrand = (int) (0 + Math.random() * (gv.ROWS - 1));
	irand = (int) (0 + Math.random() * (gv.COLUMNS - 1));
	gv.STEPCOUNTER += 1;
        }//end getstarting Cell

/***********************************************************************************************
    get erosion according to parameters from applet
***********************************************************************************************/
    public void geterosionValue()
	{
	rand = 0;
	basicErosion = 0;
	//to get erosion value if no break point has been selected
	if(gv.XPOINT < 0 && gv.YPOINT < 0)
		{
		//if random erosion over basic is selected
		if(gv.RANDEROSION)
			{	
			//get a the random value
			basicErosion = gv.RANDVALUE;
			}
		else
			{
			basicErosion = gv.EROSION;	
			}
		randxleft = randxright = randybottom = randytop = -1;
		}
	else
		{
		gv.EROSION = 0;
		randxleft = gv.XRANDLEFT;
		randxright = gv.XRANDRIGHT;
		randybottom = gv.YRANDBOTTOM;
		randytop = gv.YRANDTOP;					
		}//end of erosion checking	
	}//end of geterosionValue()

/***********************************************************************************************
    to apply tectonics
***********************************************************************************************/   
    public void applyTectonics()
	{
	double tectbottom, tecttop, tectleft, tectright;
	tectbottom = tecttop = tectleft = tectright = 0;
	//calculate erosion of bar based on basic erosion (uniform or random)
	//check y break point
	if(gv.TECTONICSYPOINT > -1 && gv.TECTONICSYPOINT <= gv.ROWS)
		{
		for(int trows = 0; trows < gv.ROWS; trows++)
			{
			for(int tcolumns = 0; tcolumns < gv.COLUMNS; tcolumns++)
				{
				if(trows < gv.TECTONICSYPOINT)
					{
					tectbottom = gv.TECTONICSYBOTTOM;
					surfaceArray[trows][tcolumns].setTectonics(tectbottom);	
					}							
				if(trows >= gv.TECTONICSYPOINT)
					{
					tecttop = gv.TECTONICSYTOP;
					surfaceArray[trows][tcolumns].setTectonics(tecttop);	
					}							
				surfaceArray[trows][tcolumns].setfinalHeight();
				}
			}
		}//end for break point at y
	//check x break point
	if(gv.TECTONICSXPOINT > -1 && gv.TECTONICSXPOINT < gv.COLUMNS)
		{
		for(int trows = 0; trows < gv.ROWS; trows++)
			{
			for(int tcolumns = 0; tcolumns < gv.COLUMNS; tcolumns++)
				{
				if(tcolumns < gv.TECTONICSXPOINT)
					{
					tectleft = gv.TECTONICSXLEFT;
					surfaceArray[trows][tcolumns].setTectonics(tectleft);	
					}							
				if(tcolumns >= gv.TECTONICSXPOINT)
					{
					tectright = gv.TECTONICSXRIGHT;
					surfaceArray[trows][tcolumns].setTectonics(tectright);	
					}
				surfaceArray[trows][tcolumns].setfinalHeight();
				}
			}
		}//end for break point at y
	}//end of applyTectonics

/***********************************************************************************************
    this function gets the cells surrounding the randomly selected one
***********************************************************************************************/   
    private void getSurroundingCells()
	{		  
	int randx1 = jrand;
	int randy1 = irand;
	gv.BARSPROCESSED += 1;
	keepgoing = true;
	cleanup();
	
	if(gv.ITERATIONCOUNTER > 0)
		{	
		if((gv.BARSPROCESSED % 7500) == 0 || gv.ITERATIONCOUNTER == gv.ENDTIME)
			{
			values4.setText("" + gv.ITERATIONCOUNTER);
			steps = 0;
			}
		newx = newy = 0;	
		incrIndex = 0;
		incrDiffusionIndex = 0;
		//get 3x3 grid and closest neighbors
		//get 3x3 grid in vectors for later analysis
	     try
		{
		if(randx1 - 1 >= 0 && randx1 - 1 < gv.ROWS && randy1 - 1 >= 0 && randy1 - 1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1 - 1;
			ycoordArray[incrIndex] = randy1 - 1;			
			incrIndex++;
			}
		if(randx1 - 1 >= 0 && randx1 - 1 < gv.ROWS && randy1 >= 0 && randy1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1 - 1;
			ycoordArray[incrIndex] = randy1;			
			xcoordDiffusionArray[incrDiffusionIndex] = randx1 - 1;
			ycoordDiffusionArray[incrDiffusionIndex] = randy1;
			incrIndex++;
			incrDiffusionIndex++;
			}
		if(randx1 - 1 >= 0 && randx1 - 1 < gv.ROWS && randy1 + 1 >= 0 && randy1 + 1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1 - 1;
			ycoordArray[incrIndex] = randy1 + 1;			
			incrIndex++;
			}
		if(randx1 >= 0 && randx1 < gv.ROWS && randy1 - 1 >= 0 && randy1 - 1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1;
			ycoordArray[incrIndex] = randy1 - 1;			
			xcoordDiffusionArray[incrDiffusionIndex] = randx1;
			ycoordDiffusionArray[incrDiffusionIndex] = randy1 - 1;
			incrIndex++;
			incrDiffusionIndex++;
			}
		if(randx1 >= 0 && randx1 < gv.ROWS && randy1 >= 0 && randy1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1;
			ycoordArray[incrIndex] = randy1;			
			incrIndex++;
			}
		if(randx1 >= 0 && randx1 < gv.ROWS && randy1 + 1 >= 0 && randy1 + 1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1;
			ycoordArray[incrIndex] = randy1 + 1;			
			xcoordDiffusionArray[incrDiffusionIndex] = randx1;
			ycoordDiffusionArray[incrDiffusionIndex] = randy1 + 1;
			incrIndex++;
			incrDiffusionIndex++;
			}
		if(randx1 + 1 >= 0 && randx1 + 1 < gv.ROWS && randy1 - 1 >= 0 && randy1 - 1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1 + 1;
			ycoordArray[incrIndex] = randy1 - 1;			
			incrIndex++;
			}
		if(randx1 + 1 >= 0 && randx1 + 1 < gv.ROWS && randy1 >= 0 && randy1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1 + 1;
			ycoordArray[incrIndex] = randy1;			
			xcoordDiffusionArray[incrDiffusionIndex] = randx1 + 1;
			ycoordDiffusionArray[incrDiffusionIndex] = randy1;
			incrIndex++;
			incrDiffusionIndex++;
			}
		if(randx1 + 1 >= 0 && randx1 + 1 < gv.ROWS && randy1 + 1 >= 0 && randy1 + 1 < gv.COLUMNS)
			{
			xcoordArray[incrIndex] = randx1 + 1;
			ycoordArray[incrIndex] = randy1 + 1;			
			incrIndex++;
			}//end of getting 3x3 grid in vectors
		   }
	     catch (ArrayIndexOutOfBoundsException aioobe){}	
	     }//end of iteration and thread checking
	}//end of getsurroundingCells

/***********************************************************************************************
    apply diffusion when first precipiton falls
***********************************************************************************************/   
    public void applyDiffusion()
	{
	//get target coordinates in local variables
	int randrow1 = jrand;
	int randcolumn1 = irand;
	getsedimentx = -1;
	getsedimenty = -1;
	double sedimentTaken = 0;
				
	for(int t = 0; t < incrDiffusionIndex; t++)
		{
		//extract each one of the closest neighbors from vector (4 bars)
		int tempx = xcoordDiffusionArray[t];
		int tempy = ycoordDiffusionArray[t];
		diffusionPower = 0;
		possibleDiffusion = 1;
		currentDiffusion = 0;
		if(diffusesedimentx > -1 && diffusesedimenty > -1)
			{
			//compare it to with target cell - decide which cells dump and get sediment
			if(surfaceArray[tempx][tempy].getsurfacefinalHeight() > surfaceArray[randrow1][randcolumn1].getsurfacefinalHeight())
				{
				diffusesedimentx = tempx;
				diffusesedimenty = tempy;
				getsedimentx = randrow1;
				getsedimenty = randcolumn1;
				}
			else
				{
				diffusesedimentx = randrow1;
				diffusesedimenty = randcolumn1;
				getsedimentx = tempx;
				getsedimenty = tempy;
				}
			//determine the height difference between the two cells
			heightDifference = surfaceArray[diffusesedimentx][diffusesedimenty].getsurfacefinalHeight() - surfaceArray[getsedimentx][getsedimenty].getsurfacefinalHeight();
			//if neighbor has sediment
			if (surfaceArray[diffusesedimentx][diffusesedimenty].getSediment() > 0)
				{
				diffusionPower = heightDifference / (gv.BARWIDTH * gv.BARWIDTH);
				//check for basic erosion rate
				if(gv.XPOINT < 0 && gv.YPOINT < 0)
					{								
					possibleDiffusion = basicErosion * 2 * diffusionPower;
					}
				//check for erosion rate with break at x
				if(gv.XPOINT >= 0 && randxleft >= 0 && randcolumn1 <= gv.XPOINT)
					{
					possibleDiffusion = randxleft * 2 * diffusionPower;
					}
				if(gv.XPOINT >= 0 && randxright >= 0 && randcolumn1 > gv.XPOINT)
					{
					possibleDiffusion = randxright * 2 * diffusionPower;							
					}
				//check for erosion rate with break at y
				if(gv.YPOINT >= 0 && randybottom >= 0 && randrow1 <= gv.YPOINT)
					{
					possibleDiffusion = randybottom * 2 * diffusionPower;							
					}
				if(gv.YPOINT >= 0 && randytop >= 0 && randrow1 > gv.YPOINT)
					{
					possibleDiffusion = randytop * 2 * diffusionPower;
					}
				//if sediment is not enough
				if (possibleDiffusion > surfaceArray[diffusesedimentx][diffusesedimenty].getSediment())
					{
					currentDiffusion = surfaceArray[diffusesedimentx][diffusesedimenty].getSediment();
					possibleDiffusion -= currentDiffusion;
					sedimentTaken = surfaceArray[diffusesedimentx][diffusesedimenty].getSediment();	
					surfaceArray[diffusesedimentx][diffusesedimenty].setSediment(-sedimentTaken);
					}
				else
					{
					currentDiffusion = possibleDiffusion;
					}
				}
			//if neighbor does not have sediment
			if ((surfaceArray[diffusesedimentx][diffusesedimenty].getSediment() == 0) && (currentDiffusion != possibleDiffusion))
				{
				diffusionPower = (heightDifference - currentDiffusion) / (gv.BARWIDTH * gv.BARWIDTH);
				//check for basic erosion rate
				if(gv.XPOINT < 0 && gv.YPOINT < 0)
					{								
					possibleDiffusion = basicErosion * diffusionPower;
					}
				//check for erosion rate with break at x
				if(gv.XPOINT >= 0 && randxleft >= 0 && randcolumn1 <= gv.XPOINT)
					{
					possibleDiffusion = randxleft * diffusionPower;
					}
				if(gv.XPOINT >= 0 && randxright >= 0 && randcolumn1 > gv.XPOINT)
					{
					possibleDiffusion = randxright * diffusionPower;
					}
				//check for erosion rate with break at y
				if(gv.YPOINT >= 0 && randybottom >= 0 && randrow1 <= gv.YPOINT)
					{
					possibleDiffusion = randybottom * diffusionPower;						
					}
				if(gv.YPOINT >= 0 && randytop >= 0 && randrow1 > gv.YPOINT)
					{
					possibleDiffusion = randytop * diffusionPower;
					}
				currentDiffusion = currentDiffusion + possibleDiffusion;
				}
			//only for front row - apply only ten percent
			surfaceArray[diffusesedimentx][diffusesedimenty].setErosion(currentDiffusion - sedimentTaken);
			if(randrow1 == 0)
				{
				currentDiffusion = currentDiffusion * 0.10;
				}
			surfaceArray[getsedimentx][getsedimenty].setSediment(currentDiffusion);
			}
		}//end of for loop			
	for(int t = 0; t < incrIndex; t++)
		{
		//fix height after diffusion
		surfaceArray[xcoordArray[t]][ycoordArray[t]].setfinalHeight();
		}
	}//end of applyDiffusion


/***********************************************************************************************
    search for lowest cell in 3x3 grid
***********************************************************************************************/   
    public void searchlowestCell()
	{
	//to do certain number of iterations 
	int randx1 = jrand;
	int randy1 = irand;
	current = 0;
	minHeight = current;
	int xcoord1 = 0;
	int ycoord1 = 0;
	int xcoord2 = 0;
	int ycoord2 = 0;
		
	//find lowest height in array
	for(nextHeight = current + 1; nextHeight < incrIndex; nextHeight++)
		{
		double bar2height = -1;
		double bar1height = -1;						
		xcoord2 = xcoordArray[nextHeight];
		ycoord2 = ycoordArray[nextHeight];
		xcoord1 = xcoordArray[minHeight];
		ycoord1 = ycoordArray[minHeight];
		bar2height = surfaceArray[xcoord2][ycoord2].getsurfacefinalHeight();				
		bar1height = surfaceArray[xcoord1][ycoord1].getsurfacefinalHeight();
			
		//if next bar is lower than current, change value of index
		if(bar2height < bar1height) 								
			{
			minHeight = nextHeight;
			}						
		}//end of for loop
		 
	//this is to check if there is any lowest cell
	if(xcoordArray[minHeight] >= 0 && ycoordArray[minHeight] >= 0)
		{	
		newx = xcoordArray[minHeight];
		newy = ycoordArray[minHeight];
 		//this is to check if the lowest now was the lowest before in order to avoid an endless loop
 		if (newx == gv.OLDX && newy == gv.OLDY)
 			{
			cleanup();
			firstTime = true;
			keepgoing = false;
       			return;
			}
		else
			{
			gv.OLDX = randx1;
			gv.OLDY = randy1;						
			}
		//this is to check if the lowest now and lowest before are the same height
		if (surfaceArray[newx][newy].getsurfacefinalHeight() == surfaceArray[randx1][randy1].getsurfacefinalHeight())
       			{
			firstTime = true;
			keepgoing = false;
       			return;
       			}
		pjrand = randx1;
		pirand = randy1;				
		}
	else
		{		
		firstTime = true;
		keepgoing = false;
       		return;
		}//end if anybars
	}//end of searchlowestCell()

/***********************************************************************************************
    apply the erosion according to erodibility parameters
***********************************************************************************************/   
    public void calculateErosion()
	{
	//to do certain number of iterations 
	if(keepgoing)
		{
		int randx2 = jrand;
		int randy2 = irand;	
		//get the heights of both bars and calculate height difference
		gv.HEIGHTDIFFERENCE = heightDiff = surfaceArray[randx2][randy2].getsurfacefinalHeight() - surfaceArray[newx][newy].getsurfacefinalHeight();
		erosionPower = 0;
		possibleErosion = 1;
		currentErosion = 0;
		gv.SEDIMENT = 0;
		double sedimentTaken = 0;
		if(surfaceArray[randx2][randy2].getSediment() > 0)
			{
			erosionPower = heightDiff / (gv.BARWIDTH * gv.BARWIDTH);
			//calculate erosion of bar based on basic erosion (uniform or random)
			if(gv.XPOINT < 0 && gv.YPOINT < 0)
				{
				possibleErosion = (basicErosion * 2) * erosionPower;
				}
			//check y break point
			if(gv.YPOINT > -1 && gv.YPOINT <= gv.ROWS && randx2 > gv.YPOINT)
				{				
				possibleErosion = (randytop * 2) * erosionPower;
				}
			if(gv.YPOINT > -1 && gv.YPOINT <= gv.ROWS && randx2 <= gv.YPOINT)
				{
				possibleErosion = (randybottom * 2) * erosionPower;
				}
			//check x break point
			if(gv.XPOINT > -1 && gv.XPOINT <= gv.COLUMNS && randy2 > gv.XPOINT)
				{
				possibleErosion = (randxright * 2) * erosionPower;
				}
			if(gv.XPOINT > -1 && gv.XPOINT <= gv.COLUMNS && randy2 <= gv.XPOINT)
				{
				possibleErosion = (randxleft * 2) * erosionPower;
				}
			if(possibleErosion > surfaceArray[randx2][randy2].getSediment())
				{
				currentErosion = surfaceArray[randx2][randy2].getSediment();
				possibleErosion -= currentErosion;
				sedimentTaken = surfaceArray[randx2][randy2].getSediment();
				surfaceArray[randx2][randy2].setSediment(- sedimentTaken);
				}
			else
				{
				currentErosion = possibleErosion;
				}
			}
		if((surfaceArray[randx2][randy2].getSediment() == 0) && (currentErosion != possibleErosion))
			{
			erosionPower = (heightDiff - currentErosion) / (gv.BARWIDTH * gv.BARWIDTH);
			//calculate erosion of bar based on basic erosion (uniform or random)
			if(gv.XPOINT < 0 && gv.YPOINT < 0)
				{
				possibleErosion = basicErosion * erosionPower;
				}
			//check y break point
			if(gv.YPOINT > -1 && gv.YPOINT <= gv.ROWS && randx2 > gv.YPOINT)
				{				
				possibleErosion = randytop * erosionPower;
				}
			if(gv.YPOINT > -1 && gv.YPOINT <= gv.ROWS && randx2 <= gv.YPOINT)
				{
				possibleErosion = randybottom * erosionPower;
				}
			//check x break point
			if(gv.XPOINT > -1 && gv.XPOINT <= gv.COLUMNS && randy2 > gv.XPOINT)
				{
				possibleErosion = randxright * erosionPower;
				}
			if(gv.XPOINT > -1 && gv.XPOINT <= gv.COLUMNS && randy2 <= gv.XPOINT)
				{
				possibleErosion = randxleft * erosionPower;
				}
			currentErosion = currentErosion + possibleErosion;
			}				
		gv.SEDIMENT = currentErosion;
		//only for front row
		surfaceArray[randx2][randy2].setErosion(currentErosion  - sedimentTaken);
		if(newx == 0)
			{
			currentErosion = currentErosion * 0.10;
			}
		surfaceArray[newx][newy].setSediment(currentErosion);
		surfaceArray[randx2][randy2].setfinalHeight();
		surfaceArray[newx][newy].setfinalHeight();
		}
	}//end of applyErosion

/***********************************************************************************************
    to see if erosion continues
***********************************************************************************************/   
    public void checkcarryingCapacity()
	{
	if(keepgoing)
		{
		double changeperStep = 0;
		//check default without changes in climate		
		if(gv.RAINFALLRATEDEFAULT > 0)
			{
			gv.CARRYINGCAPACITY = gv.RAINFALLRATEDEFAULT * gv.HEIGHTDIFFERENCE;			
			if(gv.SEDIMENT > gv.CARRYINGCAPACITY)
				{
				firstTime = true;
				keepgoing = false;
		        	return;
				}
			}					
		//check when climate is increasing
		if(gv.RAININCREASELOW != 0 && gv.RAININCREASEHIGH != 0)
			{
			 double highlowdifference = gv.RAININCREASEHIGH - gv.RAININCREASELOW;
		 	 changeperStep = highlowdifference / (gv.ENDTIME / gv.TIMESTEP);

			 //this is just to reset the carrying capacity
			 if(startNeeded)
			 	{
			 	gv.CARRYINGCAPACITY = gv.RAININCREASELOW;
			 	startNeeded = false;
				}

		 	if(gv.STEPCOUNTER >= (int) gv.TIMESTEP)
		 		{
				gv.CARRYINGCAPACITY += changeperStep;
			 	gv.STEPCOUNTER = 1;	
		 		}
			if(gv.SEDIMENT > (gv.CARRYINGCAPACITY * gv.HEIGHTDIFFERENCE))
				{
				firstTime = true;
				keepgoing = false;
		        	return;
				}
			}//end increase 
		//check when climate is decreasing
		if(gv.RAINDECREASELOW != 0 && gv.RAINDECREASEHIGH != 0)
			{
			 double highlowdifference = gv.RAINDECREASEHIGH - gv.RAINDECREASELOW;
			 if(startNeeded)
			 	{
			 	gv.CARRYINGCAPACITY = gv.RAINDECREASEHIGH;
			 	startNeeded = false;
				}
		 	changeperStep = highlowdifference / (gv.ENDTIME / gv.TIMESTEP);

		 	if(gv.STEPCOUNTER > (int) gv.TIMESTEP)
		 		{
		 		gv.CARRYINGCAPACITY -= changeperStep;
		 		gv.STEPCOUNTER = 1;	
		 		}

			if(gv.SEDIMENT > (gv.CARRYINGCAPACITY * gv.HEIGHTDIFFERENCE))
				{
				firstTime = true;
				keepgoing = false;
		        	return;
				}
			}
		}//end of keepgoing
	}//end of check carrying Capacity

/***********************************************************************************************
    to reset interval arrays
***********************************************************************************************/   
    private void resetIntervalArrays()
    	{
	sumIntervalsSediment = new double[gv.ROWS];
	sumColumns = new double[gv.ROWS];
	sumColumnsBedrock = new double[gv.ROWS];
	sumRows = new double[gv.ROWS];
	averageIntervals = new double[gv.ROWS];
	averageIntervalsBedrock = new double[gv.ROWS];
	columnIntervals = new double[gv.ROWS];
	columnIntervalsBedrock = new double[gv.ROWS];
	columnSediment = new double[gv.ROWS];
	for(int i = 0; i < gv.ROWS; i++)
		{
		sumRows[i] = 0;	
		sumIntervalsSediment[i] = 0;
		sumColumns[i] = 0;
		sumColumnsBedrock[i]=0;
		averageIntervals[i] = 0;
		averageIntervalsBedrock[i] = 0;
		columnIntervals[i] = 0;
		columnIntervalsBedrock[i] = 0;
		columnSediment[i] = 0;
		}
	rowIntervals = new double[gv.COLUMNS];
	rowIntervalsBedrock = new double[gv.COLUMNS];
	rowSediment = new double[gv.COLUMNS];
	for(int i = 0; i < gv.COLUMNS; i++)
		{
		rowIntervals[i] = 0;	
		rowIntervalsBedrock[i] = 0;
		rowSediment[i] = 0;
		}
	}// end of reset interval arrays

/***********************************************************************************************
    to reset intervals
***********************************************************************************************/   
    private void resetIntervals(ErosionIntervals eicopy, boolean rowflag)
    	{
	eicopy.clearIntervals();	
	if (rowflag)
		{
		eicopy.settings(gv.ROWS);
		}
	else
		{
		eicopy.settings(gv.COLUMNS);
		}
	resetIntervalsParms(eicopy);
	gv.FIRSTINTERVAL = true;
    	}// end of reset intervals

/***********************************************************************************************
    to reset intervals parameters
***********************************************************************************************/   
    private void resetIntervalsParms(ErosionIntervals eicopy)
    	{
	min = surfaceArray[0][0].getsurfacefinalHeight();
	max = surfaceArray[gv.ROWS-1][gv.COLUMNS-1].getsurfacefinalHeight() 
		+ (gv.ENDTIME * gv.TECTONICSPERCENTAGE);
	eicopy.passParms(min, max);	   		
	eicopy.passMsg(msg);
    	}// to reset graph parameters

/***********************************************************************************************
    to save intervals
***********************************************************************************************/   
    public void saveInterval()
	{
	int ndx = 0;
	for(int i = 0; i < gv.ROWS; i++)
		{
		for (int j = 0; j < gv.COLUMNS; j++)
			{
			sumColumns[ndx] += surfaceArray[i][j].getsurfacefinalHeight();
			sumColumnsBedrock[ndx] += surfaceArray[i][j].getBedrock();
			sumIntervalsSediment[ndx] += (surfaceArray[i][j].getsurfacefinalHeight() - surfaceArray[i][j].getBedrock());
			}
		sumColumns[ndx] = sumColumns[ndx] / gv.COLUMNS;
		sumColumnsBedrock[ndx] = sumColumnsBedrock[ndx] / gv.COLUMNS;
		sumIntervalsSediment[ndx] = sumIntervalsSediment[ndx] / gv.COLUMNS;
		ndx += 1;
		}	
	if (intervalCounter > 0)
		{
		if(gv.AVGVISIBLE)
			{
			for (int i = 0; i < gv.ROWS; i++)
				{
				averageIntervals[i] = sumColumns[i];
				averageIntervalsBedrock[i] = sumColumnsBedrock[i];
				}
			drawIntervals(ei, averageIntervals, averageIntervalsBedrock);
			drawIntervals(eis, sumIntervalsSediment);
			}
		if(gv.COLVISIBLE)
			{
			for (int i = 0; i < gv.ROWS; i++)
				{
				columnIntervals[i] = surfaceArray[i][gv.COLINTERVALS - 1].getsurfacefinalHeight();
				columnIntervalsBedrock[i] = surfaceArray[i][gv.COLINTERVALS - 1].getBedrock();
				columnSediment[i] = (surfaceArray[i][gv.COLINTERVALS - 1].getsurfacefinalHeight() - surfaceArray[i][gv.COLINTERVALS - 1].getBedrock());
				}				
			drawIntervals(ec, columnIntervals, columnIntervalsBedrock);
			drawIntervals(ecs, columnSediment);
			}
		if(gv.ROWVISIBLE)
			{
			for (int i = 0; i < gv.COLUMNS; i++)
				{
				rowIntervals[i] = surfaceArray[gv.ROWINTERVALS - 1][i].getsurfacefinalHeight();
				rowIntervalsBedrock[i] = surfaceArray[gv.ROWINTERVALS - 1][i].getBedrock();
				rowSediment[i] = (surfaceArray[gv.ROWINTERVALS - 1][i].getsurfacefinalHeight() - surfaceArray[gv.ROWINTERVALS - 1][i].getBedrock());
				}					
			drawIntervals(er, rowIntervals, rowIntervalsBedrock);
			drawIntervals(ers, rowSediment);
			}
		}
	thisthread.yield(); 
	}//end of save intervals

/***********************************************************************************************
    to draw intervals with two values
***********************************************************************************************/   
    public void drawIntervals(ErosionIntervals eicopy, double arrayValues1[], double arrayValues2[])
	{
	eicopy.startIntervals(arrayValues1, arrayValues2);
	eicopy.refreshGraph();
	}//end of draw intervals


/***********************************************************************************************
    to draw intervals with one value
***********************************************************************************************/   
    public void drawIntervals(ErosionIntervals eicopy, double arrayValues1[])
	{
	eicopy.startIntervals(arrayValues1);
	eicopy.refreshGraph();
	}//end of draw intervals

/***********************************************************************************************
    to reset hypsometric curve graph
***********************************************************************************************/   
    void resetHypsometric()
	{
	relativeHeight = new double[HYPSOINTERVAL];
	relativeArea = new double[HYPSOINTERVAL];
	for (int i = 0; i < HYPSOINTERVAL; i++)
		{
		relativeHeight[i] = 1;
		relativeArea[i] = 1;
		}
	eh.clearHypsometric();
	eh.refreshHypsometric();	
	}

/***********************************************************************************************
    to calculate the hypsometric curve
***********************************************************************************************/   
    void calculateHypsometric()
	{
	if (gv.HYPSOMETRIC && intervalCounter > 0)
		{
		double h = surfaceArray[0][0].getsurfacefinalHeight();
		double H = 0;
		double a = 0;
		double A = 0;
		int aCounter = 0;
		
		//calculate little h
		for(int i = 0; i < gv.ROWS; i++)
			{
			for (int j = 0; j < gv.COLUMNS; j++)
				{
				double htemp = surfaceArray[i][j].getsurfacefinalHeight();				
				if(htemp < h) 								
					{
					h = htemp;
					}
				}
			}//end of for loop
		//calculate big H
		for(int i = 0; i < gv.ROWS; i++)
			{
			for (int j = 0; j < gv.COLUMNS; j++)
				{
				double Htemp = surfaceArray[i][j].getsurfacefinalHeight();				
				if(Htemp > H) 								
					{
					H = Htemp;
					}
				}//end of for loop
			}
		//calculate little a
		int ndx1 = 0;
		for (double ndx = 0.0; ndx <= 1.0; ndx+= 0.1)
			{
			relativeHeight[ndx1] = ndx;	
			for(int i = 0; i < gv.ROWS; i++)
				{
				for (int j = 0; j < gv.COLUMNS; j++)
					{
					double atemp = surfaceArray[i][j].getsurfacefinalHeight();				
					if(atemp > (ndx * ( H - h)) + h) 								
						{
						aCounter++;
						}
					}
				}
			relativeArea[ndx1] =  (double) aCounter / (gv.COLUMNS * gv.ROWS);
			ndx1++;
			aCounter = 0;
			}//end of for loop	
		eh.startHypsometric(relativeHeight, relativeArea);	
		thisthread.yield();
		}//end of calculation conditions
	eh.refreshHypsometric();	
	}//end of calculate Hypsometric


/***********************************************************************************************
    if thread gets lazy
***********************************************************************************************/
    private void setColors()
    	{
	//scale of colors ranging from blue to orange
	//blue meaning the lowest and orange meaning the highest
	//implementation of 4 color areas
	double lowestBar = BARMINHEIGHT;
	double highestBar = 0;
	for(int i = 0; i < gv.ROWS; i++)
		{
		for (int j = 0; j < gv.COLUMNS; j++)
			{
			double bar1 = surfaceArray[i][j].getsurfacefinalHeight();				
			if(bar1 > highestBar) 								
				{
				highestBar = bar1;
				}
			}//end of for loop
		}
	double difference = highestBar - lowestBar;
	double interpolation = difference / 1023;
	int colorIndex = 0;
	for(int x = 0; x < gv.ROWS; x++)
		{
		for(int y = 0; y < gv.COLUMNS; y++)
			{
			colorIndex = (int) ((surfaceArray[x][y].getsurfacefinalHeight() - lowestBar) / interpolation);
			if(colorIndex < 0)
				{
				colorIndex = 0;
				}
			if(colorIndex > 1022)
				{
				colorIndex = 1022;
				}
		        ecanv.setDataHeight(x, y, (float) surfaceArray[x][y].getsurfacefinalHeight());		    
			ecanv.setDataColor(x, y, colors.getColor1(colorIndex), colors.getColor2(colorIndex), colors.getColor3(colorIndex));

			//set the snapshots at their corresponding times
			if ((gv.ITERATIONCOUNTER == 0) || (gv.ITERATIONCOUNTER == gv.ENDTIME / 4))
				{
		        	ecanv25p.setDataHeight(x, y, (float) surfaceArray[x][y].getsurfacefinalHeight());		    
				ecanv25p.setDataColor(x, y, colors.getColor1(colorIndex), colors.getColor2(colorIndex), colors.getColor3(colorIndex));
				}
			if ((gv.ITERATIONCOUNTER == 0) || (gv.ITERATIONCOUNTER == gv.ENDTIME / 4 * 2))
				{
		        	ecanv50p.setDataHeight(x, y, (float) surfaceArray[x][y].getsurfacefinalHeight());		    
				ecanv50p.setDataColor(x, y, colors.getColor1(colorIndex), colors.getColor2(colorIndex), colors.getColor3(colorIndex));
				}
			if ((gv.ITERATIONCOUNTER == 0) || (gv.ITERATIONCOUNTER == gv.ENDTIME / 4 * 3))
				{
		        	ecanv75p.setDataHeight(x, y, (float) surfaceArray[x][y].getsurfacefinalHeight());		    
				ecanv75p.setDataColor(x, y, colors.getColor1(colorIndex), colors.getColor2(colorIndex), colors.getColor3(colorIndex));
				}
			if ((gv.ITERATIONCOUNTER == 0) || (gv.ITERATIONCOUNTER < gv.ENDTIME - 5))
				{
	        		ecanv100p.setDataHeight(x, y, (float) surfaceArray[x][y].getsurfacefinalHeight());		    
				ecanv100p.setDataColor(x, y, colors.getColor1(colorIndex), colors.getColor2(colorIndex), colors.getColor3(colorIndex));
				}
			}	
		}
    	}//end setColors

/***********************************************************************************************
    to reset the snapshot images everytime an iteration starts
***********************************************************************************************/
    private void resetSnapshots(ErosionCanvas ecsnapshot)
    	{
	ecsnapshot.setGridSize(gv.ROWS, gv.COLUMNS);
	ecsnapshot.setViewHeight(BARMINHEIGHT);
	ecsnapshot.setVisible(false);
    	}//end of reset snapshots

/***********************************************************************************************
    to draw corresponding snapshot
***********************************************************************************************/
    private void setSnapshot(ErosionCanvas ecsnapshot)
    	{
    	ecsnapshot.setVisible(true);
    	ecsnapshot.setWallColor(200, 180, 100);
    	ecsnapshot.redraw();	
    	}// end of set snapshot


/***********************************************************************************************
    to start fresh
***********************************************************************************************/   
    public void cleanup()
	{
	for(int i = 0; i < 9; i++)
		{
		xcoordArray[i] = -1;
		ycoordArray[i] = -1;
		}
	for(int i = 0; i < 4; i++)
		{
		xcoordDiffusionArray[i] = -1;
		ycoordDiffusionArray[i] = -1;	
		}
    	gv.SEDIMENT = 0.0;			
	}
    }//end of ErosionSim

/***********************************************************************************************
CLASS:	      SurfaceBar

FUNCTION:     This class represents the single unit that makes the topographic grid.
	      The class contains:
	      -Constructor: 
    	      *SurfaceBar() = default constructor
    	      *SurfaceBar(double x, double y, double x1, double y1, double slope, 
	       	int color1, int color2, int color3) = constructor usually called
	      -Helping functions: 
    		*double calculateRoughness() = calculate bar roughness
    		*double getRoughness() = get bar roughness
     		*double getSlope() = returns the slope of the bar
     		*void setSlope(double newSlope) = sets the height of the bar
     		*void setSediment(double sediment) = sets the sediment
     		*double getSediment() = returns the sediment
     		*void setErosion(double erosion) = sets the erosion
     		*double getErosion() = returns the erosion
     		*void setTectonics(double tectonicvalue) = sets the tectonic value
     		*double getTectonics() = gets the tectonic value
     		*double getsurfacefinalHeight() = returns value after erosion and 
    			sediment are applied
     		*double gety() = returns the width of the bar
     		*void sety(double newy) = sets the width of the bar
     		*double getx1() = returns the xbasePosition of the bar
     		*void setx1(double newx1) = sets the xbasePosition of the bar
     		*double gety1() = returns the ybasePosition of the bar 
    			(difference between y and y1 which gives the actual height)
     		*void sety1(double newy1) = sets the ybasePosition of the bar
    		*void setColor(int color1, int color2, int color3) = allows caller to set the color of the bar
    		*int getColor1() = allows caller to get color1
    		*int getColor2() = allows caller to get color2
    		*int getColor3() = allows caller to get color3

INPUT:        Nothing.                                                        

OUTPUT:       It allows for the creation of a rectangular object that will be used to create a
	      graph.
***********************************************************************************************/
//Begin SurfaceBar
class SurfaceBar
	{
    	double x, y, x1, y1, roughness, slope;
    	double width, erosion, sediment, tectonicvalue, singleSediment, singleErosion;
    	int barColor[] = new int[3];
    	double finalHeight = 0;
	
    	//default constructor
    	SurfaceBar()
    		{
		x = 1;
		y = 1;
		x1 = 1;
		y1 = 1;
		slope = 1;
		roughness = calculateRoughness();
		barColor[0] = 0;
		barColor[1] = 0;
		barColor[2] = 0;
		width = SharedParameters.BARWIDTH;
		erosion = sediment = tectonicvalue = 0;
    		}
	
    	//will be the constructor used most often, allows the caller to
    	//set the height, width, and color of the bar
    	SurfaceBar(double x, double y, double x1, double y1, double slope, 
	       int color1, int color2, int color3)
    		{
		this.roughness = calculateRoughness();
		this.slope = slope;
		this.x = x;
		this.y = y;
		this.x1 = x1;
		this.y1 = y1;
		if(color1 > 255)
	    		{
			barColor[0] = 245;
	    		}
		else
	    		{
			barColor[0] = color1;
	    		}
		if(color2 > 255)
	    		{
			barColor[1] = 150;
	    		}
		else
	    		{
			barColor[1] = color2;
	    		}
		if(color3 > 255)
	    		{
			barColor[2] = 0;
	    		}
		else
	    		{
			barColor[2] = color3;
	    		}
		width = SharedParameters.BARWIDTH;
		erosion = sediment = tectonicvalue = 0;
		finalHeight = y1;
		}
	
    	//calculate bar roughness
    	 double calculateRoughness()
    		{
		return -0.000005 + Math.random() * 0.000005;	
    		}
    
    	//get bar roughness
    	 double getRoughness()
    		{
		return roughness;	
    		}
    
    	//returns the slope of the bar
    	 double getSlope()
    		{
		return slope;			
    		}
    
    	//sets the height of the bar
    	 void setSlope(double newSlope)
    		{
		slope = newSlope;
    		}
    
    	//sets the sediment
    	 void setSediment(double sediment)
    		{
		singleSediment = sediment;
		this.sediment += sediment;
    		}
    
    	//returns the sediment
    	 double getSediment()
    		{
		return sediment;			
    		}
    
    	//returns the sediment
    	 double getsingleSediment()
    		{
		return singleSediment;			
    		}

    	//sets the erosion
    	 void setErosion(double erosion)
    		{
		singleErosion = erosion;
		this.erosion += erosion;
    		}
    
    	//returns the erosion
    	 double getErosion()
    		{
		return erosion;			
    		}
    
    	//returns the erosion
    	 double getsingleErosion()
    		{
		return singleErosion;			
    		}
    	//sets the tectonic value
    	 void setTectonics(double tectonicvalue)
		{
		this.tectonicvalue += tectonicvalue;
		}

    	//gets the tectonic value
    	 double getTectonics()
		{
		return tectonicvalue;
		}

    	 double getBarMin()
    		{
		return gety1();	
    		}
    	//returns basic height
    	 double getbasicHeight()
    		{
		return gety1() + getSlope() + getRoughness();	
    		}

    	//returns basic height
    	 double getBedrock()
    		{
		if(getErosion() > getSediment())
			{
			return getsurfacefinalHeight() - getErosion() + getSediment();	
			}
		else
		   	{
		   	return getbasicHeight();
			}	
    		}

	 //set final height
	 void setfinalHeight(int newHeight)
	 	{
		finalHeight = newHeight;			 	
		}

	 //set final height
	 void setfinalHeight()
	 	{
		finalHeight = gety1() + getSlope() + getRoughness() + getSediment() - getErosion() + getTectonics();			 	
		}

    	//returns value after erosion and sediment are applied
    	 double getsurfacefinalHeight()
    		{
		return finalHeight;
    		}
    
    	//returns the width of the bar
    	 double gety()
    		{
		return y;			
    		}
    
    	//sets the width of the bar
    	 void sety(double newy)
    		{
		y = newy;
    		}
    
    	//returns the xbasePosition of the bar
    	 double getx1()
    		{
		return x1;			
    		}
    
    	//sets the xbasePosition of the bar
    	 void setx1(double newx1)
    		{
		x1 = newx1;
    		}
    
    	//returns the ybasePosition of the bar (difference between y and y1 which gives the actual height)
    	 double gety1()
    		{
		return y1;			
    		}
    
    	//sets the ybasePosition of the bar
    	 void sety1(double newy1)
    		{
		y1= newy1;
    		}
    
    	//allows caller to set the color of the bar
    	void setColor(int color1, int color2, int color3)
    		{
		barColor[0] = color1;
		barColor[1] = color2;
		barColor[2] = color3;
    		}
    
    	//allows caller to get color1
    	int getColor1()
    		{
		return barColor[0];
    		}
    
    	//allows caller to get color2
    	int getColor2()
    		{
		return barColor[1];
    		}
    
    	//allows caller to get color3
    	int getColor3()
    		{
		return barColor[2];
    		}
}//end SurfaceBar class
	
