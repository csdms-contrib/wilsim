/***********************************************************************************************
CLASS:	      Wilsim

FUNCTION:     Wilsim declares and instantiates objects from the ErosionUI, ErosionSim,     
	      ErosionCanvas, ErosionIntervals, ErosionHypsometric and SharedParameters classes.                                    
	      The class contains:
	      -init()
	       It creates objects from the classes metioned and calls starts running the 
	       threads for the ErosionSim and ErosionCanvas objects.
	      -start()
	       To get the user interface started after all other threads have been started
	              
DATE CREATED: August 2002
***********************************************************************************************/
import java.applet.*;
import java.awt.*;

public class Wilsim extends Applet
{
    ErosionUI eui; 		// User Interface
    ErosionSim esim;		// Simulation Code
    ErosionCanvas ecanv, ecanv25p, ecanv50p, ecanv75p, ecanv100p;	// Simulation Images
    ErosionIntervals eintervals, eintsediment, ecolumn, ecolsediment, erow, erowsediment; 	// Profiles
    ErosionHypsometric eh;	// Hypsometric curve
    SharedParameters sparam;  	// The parameters driving the simulation
    int type1 = 1; 		// For the type of interval
    int type2 = 2; 		// For the type of interval

    public void init()
    {
	sparam = new SharedParameters();

	ecanv = new ErosionCanvas();
	ecanv.start();

	ecanv25p = new ErosionCanvas();
	ecanv25p.start();
	ecanv50p = new ErosionCanvas();
	ecanv50p.start();
	ecanv75p = new ErosionCanvas();
	ecanv75p.start();
	ecanv100p = new ErosionCanvas();
	ecanv100p.start();

	eh = new ErosionHypsometric(sparam);
	
	eintervals = new ErosionIntervals(sparam, "Average", type2, "Rows");
	eintsediment = new ErosionIntervals(sparam, "Average Sediment", type1, "Rows");
	ecolumn = new ErosionIntervals(sparam, "Column", type2, "Rows");
	ecolsediment = new ErosionIntervals(sparam, "Column Sediment", type1, "Rows");
	erow = new ErosionIntervals(sparam, "Row", type2, "Columns");
	erowsediment = new ErosionIntervals(sparam, "Row Sediment", type1, "Columns");

	esim = new ErosionSim(sparam, ecanv, ecanv25p, ecanv50p, ecanv75p, ecanv100p, eintervals, eintsediment, ecolumn, ecolsediment, erow, erowsediment, eh);
	esim.start();

	eui = new ErosionUI(this, esim, sparam, ecanv, ecanv25p, ecanv50p, ecanv75p, ecanv100p, eintervals, eintsediment, ecolumn, ecolsediment, erow, erowsediment, eh);
    }

    public void start()
    {
	eui.start();  // It would be nice to get rid of this.
    }
}
