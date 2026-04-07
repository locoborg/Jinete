
import java.applet.Applet;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Event;
import java.io.StreamTokenizer;
import java.io.InputStream;
import java.io.IOException;
import java.net.*;


class FileFormatException extends Exception {
    public FileFormatException(String s) {
	super(s);
    }
}


class Model3D {
    float vert[];
    int tvert[];
    int nvert, maxvert;
    int con[];
    int ncon, maxcon;
    boolean transformed;
    Matrix3D mat;
    int wdist=1;

    float xmin, xmax, ymin, ymax, zmin, zmax;

    Model3D () {
	mat = new Matrix3D ();
	mat.xrot(20);
	mat.yrot(30);
    }

    Model3D (InputStream is) throws IOException, FileFormatException {
	this();
	StreamTokenizer st = new StreamTokenizer(is);
	st.eolIsSignificant(true);
	st.commentChar('#');
scan:
	while (true) {
	    switch (st.nextToken()) {
	      default:
		break scan;
	      case StreamTokenizer.TT_EOL:
		break;
	      case StreamTokenizer.TT_WORD:
		if ("v".equals(st.sval)) {
		    double x = 0, y = 0, z = 0;
		    if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
			x = st.nval;
			if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
			    y = st.nval;
			    if (st.nextToken() == StreamTokenizer.TT_NUMBER)
				z = st.nval;
			}
		    }
		    addVert((float) x, (float) y, (float) z);
		    while (st.ttype != StreamTokenizer.TT_EOL &&
			    st.ttype != StreamTokenizer.TT_EOF)
			st.nextToken();
		} else if ("f".equals(st.sval) || "fo".equals(st.sval) || "l".equals(st.sval)) {
		    int start = -1;
		    int prev = -1;
		    int n = -1;
		    while (true)
			if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
			    n = (int) st.nval;
			    if (prev >= 0)
				add(prev - 1, n - 1);
			    if (start < 0)
				start = n;
			    prev = n;
			} else if (st.ttype == '/')
    		    st.nextToken();
            else
	            break;
		    if (start >= 0)
			add(start - 1, prev - 1);
			if (st.ttype != StreamTokenizer.TT_EOL)
			break scan;
		} else {
		    while (st.nextToken() != StreamTokenizer.TT_EOL
			    && st.ttype != StreamTokenizer.TT_EOF);
		}
	    }
	}
	is.close();
	if (st.ttype != StreamTokenizer.TT_EOF)
	    throw new FileFormatException(st.toString());
    }


    int addVert(float x, float y, float z) {
	int i = nvert;
	if (i >= maxvert)
	    if (vert == null) {
		maxvert = 100;
		vert = new float[maxvert * 3];
	    } else {
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


    void add(int p1, int p2) {
	int i = ncon;
	if (p1 >= nvert || p2 >= nvert)
	    return;
	if (i >= maxcon)
	    if (con == null) {
		maxcon = 100;
		con = new int[maxcon];
	    } else {
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


    void transform() {
	if (transformed || nvert <= 0)
	    return;
	if (tvert == null || tvert.length < nvert * 3)
	    tvert = new int[nvert*3];
	mat.transform(vert, tvert, nvert);
	transformed = true;
    }



   private void quickSort(int a[], int left, int right)
   {
      int leftIndex = left;
      int rightIndex = right;
      int partionElement;
      if ( right > left)
      {

         /* Arbitrarily establishing partition element as the midpoint of
          * the array.
          */
         partionElement = a[ ( left + right ) / 2 ];

         // loop through the array until indices cross
         while( leftIndex <= rightIndex )
         {
            /* find the first element that is greater than or equal to
             * the partionElement starting from the leftIndex.
             */
            while( ( leftIndex < right ) && ( a[leftIndex] < partionElement ) )
               ++leftIndex;

            /* find an element that is smaller than or equal to
             * the partionElement starting from the rightIndex.
             */
            while( ( rightIndex > left ) &&
                   ( a[rightIndex] > partionElement ) )
               --rightIndex;

            // if the indexes have not crossed, swap
            if( leftIndex <= rightIndex )
            {
               swap(a, leftIndex, rightIndex);
               ++leftIndex;
               --rightIndex;
            }
         }

         /* If the right index has not reached the left side of array
          * must now sort the left partition.
          */
         if( left < rightIndex )
            quickSort( a, left, rightIndex );

         /* If the left index has not reached the right side of array
          * must now sort the right partition.
          */
         if( leftIndex < right )
            quickSort( a, leftIndex, right );

      }
   }

   private void swap(int a[], int i, int j)
   {
      int T;
      T = a[i];
      a[i] = a[j];
      a[j] = T;
   }


    public void setDist(int d) {
        wdist=d;
    }



    /** eliminate duplicate lines */
    void compress() {
	int limit = ncon;
	int c[] = con;
	quickSort(con, 0, ncon - 1);
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

    static Color gr[];

    /** Paint this model to a graphics context.  It uses the matrix associated
	with this model to map from model space to screen space.
	The next version of the browser should have double buffering,
	which will make this *much* nicer */
    void paint(Graphics g, int ox, int oy) {
	if (vert == null || nvert <= 0)
	    return;
	transform();
	if (gr == null) {
	    gr = new Color[16];
	    for (int i = 0; i < 16; i++) {
		int grey = (int) (170*(1-Math.pow(i/15.0, 2.3)));
		gr[i] = new Color(grey, grey, grey);
	    }
	}
	int lg = 0;
	int lim = ncon;
	int c[] = con;
	int v[] = tvert;
	if (lim <= 0 || nvert <= 0)
	    return;
	for (int i = 0; i < lim; i++) {
	    int T = c[i];
	    int p1 = ((T >> 16) & 0xFFFF) * 3;
	    int p2 = (T & 0xFFFF) * 3;
	    int grey = v[p1 + 2] + v[p2 + 2];
	    if (grey < 0)
		grey = 15;
	    if (grey > 15)
		grey = 0;
	    if (grey != lg) {
		lg = grey;
		g.setColor(gr[grey]);
	    }
	    g.setColor(Color.white);
	    if(v[p1+2]>=0&&v[p2+2]>=0){
	        double a=Math.pow(0.9999,v[p1+2]);
        	double b=Math.pow(0.9999,v[p2+2]);
    	    g.drawLine((int) (ox+v[p1]*a), (int) (oy+v[p1+1]*a),
		       (int) (oy+v[p2]*b), (int) (oy+v[p2+1]*b));
		}
	}
    }

    /** Find the bounding box of this model */
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
}

public class ics {
    int esux,esuy,esuz;
    int ipx,ipy,ipz,ipt;

    ics(){
        String addr=getHostAddress();
        int oc,c=0;

        oc=addr.indexOf(".",c);
        ipx=new Integer(addr.subtring(c,oc)).intValue();
        c=oc+1;

        oc=addr.indexOf(".",c);
        ipy=new Integer(addr.subtring(c,oc)).intValue();
        c=oc+1;

        oc=addr.indexOf(".",c);
        ipz=new Integer(addr.subtring(c,oc)).intValue();
        c=oc+1;

        oc=addr.indexOf(".",c);
        ipt=new Integer(addr.subtring(c)).intValue();
    }

    ics(int x, int y, int z){
        esux=x;esuy=y;esuz=z;
        ipx=esux/8;
        ipy=esuy/4;
        ipz=esuz/8;
        ipt=(esux%8)+(esuy%4)*64+(esuz%8)*8;
    }

    ics(int x, int y, int z, int t){
        ipx=x;ipy=y;ipz=z;ipt=t;
        esux=ipx*8+(ipt%8);
        esuy=ipy*4+(ipt/64);
        esuz=ipz*8+((ipt%64)/8);
    }
}

/** An applet to put a 3D model into a page */
public class ThreeD extends Applet implements Runnable {
    Model3D[] md;
    boolean painted = true;
    float xfac;
    int prevx, prevy;
    float xtheta, ytheta;
    float scalefudge = 1;
    Matrix3D amat = new Matrix3D(), tmat = new Matrix3D();
    String mdname = null;
    String message = null;
    Thread galopa;
    int angx,angy;
    ics mycoord;

    public void init() {
	mdname = getParameter("model");
	try {
	    scalefudge = Float.valueOf(getParameter("scale")).floatValue();
	}catch(Exception e){};
	if (mdname == null)
	    mdname = "models/cube.obj";

	mycoord=new ics();

	scan();
	galopa=new Thread(this);
	galopa.start();
	galopa.suspend();
	setBackground(Color.black);
    }
/*
    public void scan(){
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        md=new Model3D[255];
        InputStream is = null;

    	try {
    	    for(int n=0; n<256; n++){
    	        String addr="addr";
	            Socket t= new Socket("129.15.22.33", p);
                is = new URL(new URL(), mdname).openStream();
        	    Model3D m = new Model3D (is);
        	    md[n] = m;
        	    m.findBB();
    	        m.compress();

                md.mat.scale(10, 10, 10);
	            md.mat.translate(0,0,1000);
	        }

    	} catch(Exception e) {
    	    md = null;
    	    message = e.toString();
    	}
    	try {
    	    if (is != null)
    		is.close();
    	} catch(Exception e) {
	    }

    	repaint();
    }

*/
    public void run() {
        while(true){
    	    md.mat.translate(0,0,-10);
	        repaint();
	    }
    }


    public boolean mouseMove(Event e, int x, int y) {

        angx=0; angy=0;
        if(x<size().width/6) {
            angx=20;
        }else if(x<size().width/3){
            angx=10;
        }else if(x>5*size().width/6){
            angx=-20;
        }else if(x>2*size().width/3){
            angx=-10;
        }

        if(y<size().height/6) {
            angy=20;
        }else if(y<size().height/3){
            angy=10;
        }else if(y>5*size().height/6){
            angy=-20;
        }else if(y>2*size().height/3){
            angy=-10;
        }

        if(angx!=0||angy!=0){
            md.mat.yrot(angx);
            md.mat.xrot(angy);
            repaint();
        }
        return true;
    }


    public boolean mouseDown(Event e, int x, int y) {
/*	prevx = x;
	prevy = y;
	md.mat.translate(0,0,-2);
	repaint();
*/
    galopa.resume();
	return true;
    }

    public boolean mouseUp(Event e, int x, int y) {
        galopa.suspend();
    	return true;
    }


/*
    public boolean mouseDrag(Event e, int x, int y) {
	tmat.unit();
	double xtheta=Math.atan((double)((x-prevx)/2));
	double ytheta=Math.atan((double)((prevy-y)/2));
	tmat.yrot(xtheta);
	tmat.xrot(ytheta);
	amat.mult(tmat);
	if (painted) {
	    painted = false;
	    repaint();
	}
	prevx = x;
	prevy = y;
	return true;
    }

*/
    public void paint(Graphics g) {
	if (md != null) {
	    md.mat.mult(amat);
	    md.transformed = false;
	    md.paint(g, size().width/2, size().height/2);
	    setPainted();
	} else if (message != null) {
	    g.drawString("Error in model:", 3, 20);
	    g.drawString(message, 10, 40);
	}
    }


/*
    public ??? scanSpace(int radius){
        for(int p=2;p<1000;p++){
        try {
			System.out.println("Port: "+p);
	            Socket t= new Socket("129.15.22.33", p);
	            BufferedReader info = new BufferedReader(new InputStreamReader(t.getInputStream()));
			DataOutputStream sendinfo = new DataOutputStream(t.getOutputStream());
			String data, senddata="d";
			while (senddata!="q"){
				System.out.println("Send Str:");
				senddata="GET / HTTP/1.0\n\n";
				sendinfo.writeBytes(senddata);
				System.out.println(senddata+" Send");

      		      data = info.readLine();
				System.out.println(data);
				t.close();
				while (data!=null){
	      		      if (data != null)
      		      	    System.out.println(data);
			            else
	      		          System.out.println("Nada");
					data = info.readLine();
				}
				senddata="q";
			}

        }
        catch(IOException e) {
		System.out.println("Error: "+e);
	  }
	}
    }
*/
    private synchronized void setPainted() {
	painted = true;
	notifyAll();
    }
//    private synchronized void waitPainted() {
//	while (!painted)
//	    wait();
//	painted = false;
//    }
}
