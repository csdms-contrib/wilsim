/***********************************************************************************************
CLASS:	      ErosionColors

FUNCTION:     This class helps with the grid colors according to its height.                                                           
	      The class contains:
		-Constructor:
		 *ErosionColors()
		-Helping Functions:
		 *int getColor1(int index)
		 *int getColor2(int index)
		 *int getColor3(int index)
		 *int getSize()
		
	              
DATE CREATED: August 2002
***********************************************************************************************/
public class ErosionColors
	{
	static final int ARRAYINDEX = 1023;
	int colorArray[][] = new int[ARRAYINDEX][3];
	int bluetocyan = 0;
	int cyantogreen = 0;
	int greentoyellow = 0;
	int yellowtored = 0;
			
    	//default constructor
    	ErosionColors()
    		{
    		for (int i = 0; i < ARRAYINDEX; i++)
    			{
			if(i < 255)
				{
				colorArray[i][0] = 0;
				colorArray[i][1] = 0 + bluetocyan;
				colorArray[i][2] = 255;
				bluetocyan++;
				}
			if(i >= 255 && i < 511)
				{
				colorArray[i][0] = 0;
				colorArray[i][1] = 255;
				colorArray[i][2] = 255 - cyantogreen;
				cyantogreen += 1;
				}
			if(i >= 511 && i < 767)
				{
				colorArray[i][0] = 0 + greentoyellow;
				colorArray[i][1] = 255;
				colorArray[i][2] = 0;
				greentoyellow += 1;
				}
			if(i >= 767 && i < 1023)
				{
				colorArray[i][0] = 255;
				colorArray[i][1] = 255 - yellowtored;
				colorArray[i][2] = 0;
				if((i % 4) == 0)
					{
					yellowtored += 1;
					}
				}
    			}
    		}//end of constructor
	
	int getColor1(int index)
		{
		return colorArray[index][0];
		}
	int getColor2(int index)
		{
		return colorArray[index][1];
		}
	int getColor3(int index)
		{
		return colorArray[index][2];
		}
	int getSize()
		{
		return ARRAYINDEX;
		}
		
	}//end ErosionColors