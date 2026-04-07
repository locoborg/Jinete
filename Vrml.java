/////////////////////////////////////////////////////////////////////
// Vrml.java  (1.1)                Copyright 1996 Dario Laverde
//
// Description:
// A simple java applet to parse, display, and navigate VRML (.wrl)
// in wire frame display on polyhedrons only.
//
// nifty feature not found in other inline vrml browsers: autospin
// (vrml model initially spinning - great for logos)
//
// Note: Requires ./Matrix3D.java for Matrix3D.class
//
// revision history:
// 1.0 - original released 1/96
// 1.1 - added ability to spin and scale simultaneously 10/96
//
// comments: dario@escape.com
/////////////////////////////////////////////////////////////////////

import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Event;
import java.awt.Image;
import java.io.StreamTokenizer;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;


public class Vrml extends Applet implements Runnable {

Model3D md;
Matrix3D amat = new Matrix3D();
Matrix3D tmat = new Matrix3D();

int prevx, prevy, prev2x, prev2y;
int Width,Height;
float xtheta, ytheta;
float initscale = 1;
float xfac;

String wrl_name = null;
String message = null;

boolean painted = true;
boolean spinmode = true;

private Image Imgoff;
private Graphics GCoff;
float spinRateY, spinRateX;
float Xpos,Ypos;


public String getAppletInfo() {
	return "the vrml applet 1.1  dario@escape.com";
}   


public String[][] getParameterInfo() {

	String[][] info = {
		{"file", "string","filename.wrl (A VRML file)"},
		{"spinx","float" ,"initial spin around the x-axis (0.0 - 100.0 recommended)"},
		{"spiny","float" ,"initial spin around the y-axis (0.0 - 100.0 recommended)"},
		{"scale","float" ,"initial scale (0.0 - 20.0 recommended)"}
    };
    return info;
}


public void init() {

	setBackground(Color.black);

	Width = size().width;
	Height = size().height;

	wrl_name = getParameter("file");

	if (wrl_name == null)
	    wrl_name = "java.wrl";

	spinRateY = 1.0f;
	spinRateX = 0.0f;
	Xpos = Width/2;
	Ypos = Height/2;

	try {
	    initscale = Float.valueOf(getParameter("scale")).floatValue();
	}catch(Exception e){};

	try {
	    spinRateY = Float.valueOf(getParameter("spiny")).floatValue();
	}catch(Exception e){};

	try {
	    spinRateX = Float.valueOf(getParameter("spinx")).floatValue();
	}catch(Exception e){};


	Imgoff = createImage(size().width,size().height);
	GCoff  = Imgoff.getGraphics();
}


public void run() {
	
	InputStream is = null;
	
	try {
	    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	    is = new URL(getDocumentBase(), wrl_name).openStream();
	    Model3D m = new Model3D (is,Width,Height);
	    md = m;
	    m.findBB();
	    m.compress();
	    float xw = m.xmax - m.xmin;
	    float yw = m.ymax - m.ymin;
	    float zw = m.zmax - m.zmin;
	    if (yw > xw)
		xw = yw;
	    if (zw > xw)
		xw = zw;
	    float f1 = Width / xw;
	    float f2 = Height / xw;
	    xfac = 0.7f * (f1 < f2 ? f1 : f2) * initscale;
	} //try
	catch(Exception e) {
	    md = null;
	    message = e.toString();
	}
	
	try {
	    if (is != null)
		is.close();
	} 
	catch(Exception e) {
	}

	repaint();

	while(true) {

//		if(spinmode) 
			spin();

	} //while

} // run


private synchronized void spin() {

	tmat.unit();
	tmat.xrot(spinRateX);
	tmat.yrot(spinRateY);
	amat.mult(tmat);

	if (painted) {
		painted = false;
		repaint();
	}
}


public void start() {
	if (md == null && message == null)
	    new Thread(this).start();
}


public void stop() {
}


public synchronized boolean mouseDown(Event e, int x, int y) {

	prevx = x;
	prevy = y;

	if(e.modifiers==e.META_MASK) 
		spinmode=true;  //rightbutton
	else 
		spinmode=false;	//leftbutton

	if (spinmode) {
		spinRateY = 0.0f;
		spinRateX = 0.0f;
	}

	return true;
}


public synchronized boolean mouseUp(Event e, int x, int y) {

	if (spinmode) {
		if(x-prev2x==0) 
			spinRateY = 0.0f;
		else
			spinRateY = (x - prev2x) * 360.0f / Width;

		if(y-prev2y==0) 
			spinRateX = 0.0f;
		else
			spinRateX = (prev2y - y) * 360.0f / Height;
	}

	return true;
}


public synchronized boolean mouseDrag(Event e, int x, int y) {

	if (spinmode) {
		tmat.unit();
		float xtheta = (prevy - y) * 360.0f / Height;
		float ytheta = (x - prevx) * 360.0f / Width;
		tmat.xrot(xtheta);
		tmat.yrot(ytheta);
		amat.mult(tmat);
		prev2x = prevx;
		prev2y = prevy;
		prevx = x;
		prevy = y;
	}
	else { // "walk" mode
	    if(prevy>y)
                xfac = xfac + (prevy-y)* 0.1f; 
         else if(prevy<y)
		      if ((xfac - (y-prevy)*0.1f) > 0 )
                  xfac = xfac - (y-prevy)*0.1f;
		  
            Xpos=Xpos+(x-prevx);
            prevx=x;
            prevy=y;
	}

	if (painted) {
	    painted = false;
	    repaint();
	}
	return true;
}


public void update(Graphics g) {

	paint(g);
}


public void paint(Graphics g) {

	if (md != null) {
	    md.mat.unit();
	    md.mat.translate(-(md.xmin + md.xmax) / 2,
			     -(md.ymin + md.ymax) / 2,
			     -(md.zmin + md.zmax) / 2);
	    md.mat.mult(amat);
	    md.mat.scale(xfac, -xfac, 16 * xfac / Width);
	    md.mat.translate(Xpos, Ypos, 8);

	    md.transformed = false;

		md.paint(GCoff);
        g.drawImage(Imgoff,0,0,this);
		setPainted();

	} 
	else if (message != null) {
	    g.drawString("Error in model:", 3, 20);
	    g.drawString(message, 10, 40);
	}
}


private synchronized void setPainted() {
	
	painted = true;
	notifyAll();
}


} // class Vrml



/////////////////////////////////////////////////////////////////////////
class Model3D {

float vert[];
int tvert[];
int nvert, maxvert, polyvert;
int con[];
int ncon, maxcon;
int Width, Height;
boolean transformed;
Matrix3D mat;
float xmin, xmax, ymin, ymax, zmin, zmax;


Model3D () {

	mat = new Matrix3D ();
}


// Create a 3D model by parsing an input stream
Model3D (InputStream is, int width, int height) throws IOException {

	this();
	Width=width;
	Height=height;
	StreamTokenizer st = new StreamTokenizer(is);
	st.whitespaceChars(32,44); // includes ','
	st.eolIsSignificant(true);
	st.commentChar('#');
	nvert=0;
    
    scan:
	while (true) {
	    switch (st.nextToken()) {
		case StreamTokenizer.TT_EOL:
			break;
		case StreamTokenizer.TT_EOF:
			break scan;
	    case StreamTokenizer.TT_WORD:
		if ("point".equals(st.sval)) {
			st.nextToken();	  // '['
			st.eolIsSignificant(false);
			double x = 0, y = 0, z = 0;
			polyvert =0;
			st.nextToken();
			while (st.ttype != StreamTokenizer.TT_WORD) { // ']'
		    	if (st.ttype == StreamTokenizer.TT_NUMBER) {
					x = st.nval;
					if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
			    		y = st.nval;
			    		if (st.nextToken() == StreamTokenizer.TT_NUMBER)
							z = st.nval;
					}
		    	}
		    	addVert((float) x, (float) y, (float) z);
				polyvert++;
				st.nextToken();
			} //while
			st.eolIsSignificant(true);
			while (st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF)
				st.nextToken();

		} 
		else if ("coordIndex".equals(st.sval)) {
			st.nextToken();	// '['
		    int n=-1;
		    int prev=-1;
			boolean endface=true;
			st.eolIsSignificant(false);
		    while (true)
				if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
			    	if(st.nval != -1) {
			    		n = (int) st.nval+(nvert-polyvert)+1;
						if(!endface)
							add(prev - 1, n - 1);
						else 
							endface=false;
			    		prev = n;
					} 
					else 
						endface=true;
				}
				else 
					if (st.ttype == StreamTokenizer.TT_WORD) { // ']'
			    		break; }
				
					else
			    		break;
		    	
			st.eolIsSignificant(true);
			while (st.ttype != StreamTokenizer.TT_EOL && st.ttype != StreamTokenizer.TT_EOF)
				st.nextToken();
		} 
		else {
		    while (st.nextToken() != StreamTokenizer.TT_EOL
			    && st.ttype != StreamTokenizer.TT_EOF);

		} // if TT_WORD
	
		default:
			break;
		} //switch
	} // while
	is.close();

} // Model3D(InputStream is)


// Add a vertex to this model
int addVert(float x, float y, float z) {

	int i = nvert;
	if (i >= maxvert)
	    if (vert == null) {
			maxvert = 100;
			vert = new float[maxvert * 3];
	    }
	    else {
			maxvert *= 2;
			float nv[] = new float[maxvert * 3];
			System.arraycopy(vert, 0, nv, 0, vert.length);
			vert = nv;
	    }
	i *= 3;
	vert[i] = x;
	vert[i + 1] = y;
	vert[i + 2] = z;
	return nvert++;
}


// Add a line from vertex p1 to vertex p2
void add(int p1, int p2) {

	int i = ncon;
	if (p1 >= nvert || p2 >= nvert)
	    return;
	if (i >= maxcon)
	    if (con == null) {
			maxcon = 100;
			con = new int[maxcon];
	    } 
	    else {
			maxcon *= 2;
			int nv[] = new int[maxcon];
			System.arraycopy(con, 0, nv, 0, con.length);
			con = nv;
	    }
	if (p1 > p2) {
	    int t = p1;
	    p1 = p2;
	    p2 = t;
	}
	con[i] = (p1 << 16) | p2;
	ncon = i + 1;
}


// Transform all the points in this model
void transform() {

	if (transformed || nvert <= 0)
	    return;
	if (tvert == null || tvert.length < nvert * 3)
	    tvert = new int[nvert*3];
	mat.transform(vert, tvert, nvert);
	transformed = true;
}


private void sort(int lo0, int hi0) {

    int a[] = con;
	int lo = lo0;
	int hi = hi0;
	if (lo >= hi)
	    return;
	int mid = a[(lo + hi) / 2];
	while (lo < hi) {
	    while (lo < hi && a[lo] < mid) {
		lo++;
	    }
	    while (lo < hi && a[hi] >= mid) {
		hi--;
	    }
	    if (lo < hi) {
		int T = a[lo];
		a[lo] = a[hi];
		a[hi] = T;
	    }
	}
	if (hi < lo) {
	    int T = hi;
	    hi = lo;
	    lo = T;
	}
	sort(lo0, lo);
	sort(lo == lo0 ? lo + 1 : lo, hi0);
}


// eliminate duplicate lines
void compress() {

	int limit = ncon;
	int c[] = con;
	sort(0, ncon - 1);
	int d = 0;
	int pp1 = -1;
	for (int i = 0; i < limit; i++) {
	    int p1 = c[i];
	    if (pp1 != p1) {
		c[d] = p1;
		d++;
	    }
	    pp1 = p1;
	}
	ncon = d;
}


void paint(Graphics g) {

	if (vert == null || nvert <= 0)
	    return;

	transform();

	g.setColor(Color.black);
	g.fillRect(0,0,Width,Height);

	int lim = ncon;
	int c[] = con;
	int v[] = tvert;
	if (lim <= 0 || nvert <= 0)
	    return;

	for (int i = 0; i < lim; i++) {
	    int T = c[i];
	    int p1 = ((T >> 16) & 0xFFFF) * 3;
	    int p2 = (T & 0xFFFF) * 3;

		g.setColor(Color.white);

	    g.drawLine(v[p1], v[p1 + 1],
		       v[p2], v[p2 + 1]);
	} //for

} //paint


// Find the bounding box of this model
void findBB() {

	if (nvert <= 0)
	    return;
	float v[] = vert;
	float xmin = v[0], xmax = xmin;
	float ymin = v[1], ymax = ymin;
	float zmin = v[2], zmax = zmin;
	for (int i = nvert * 3; (i -= 3) > 0;) {
	    float x = v[i];
	    if (x < xmin)
		xmin = x;
	    if (x > xmax)
		xmax = x;
	    float y = v[i + 1];
	    if (y < ymin)
		ymin = y;
	    if (y > ymax)
		ymax = y;
	    float z = v[i + 2];
	    if (z < zmin)
		zmin = z;
	    if (z > zmax)
		zmax = z;
	}
	this.xmax = xmax;
	this.xmin = xmin;
	this.ymax = ymax;
	this.ymin = ymin;
	this.zmax = zmax;
	this.zmin = zmin;
}


} // class Model3D
/////////////////////////////////////////////////////////////////////////
