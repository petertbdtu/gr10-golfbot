package communication;

public class Point{
	  
	   /**
	    * returns a Point at location x,y
	    * @param x coordinate
	    * @param y coordinate
	    */
	int y;
	int x;
	
		public Point(int x, int y) {
		    this.x = x;
		    this.y = y;
		}
	/**
	      * Returns a point ad distance 1 from the origin and an angle <code>radans</code> to the x-axis
	      * @param radians 
	      */
	    public Point(float radians)
	    {
	        this.x = (int)Math.cos(radians);
	        this.y = -(int)Math.sin(radians);
	    }


	/**
	 * Returns the direction angle from this point to the Point p
	 * @param p the Point to determine the angle to
	 * @return the angle in degrees
	 */

	     /**
	   *Translates this point, at location (x, y), by dx along the x axis and
	   * dy along the y axis so that it now represents the point (x + dx, y + dy).
	   * @param dx
	   * @param dy
	   */
	    public void translate(float dx, float dy)
	    {
	      x += dx;
	      y += dy;
	    }

	    /*
	     * Copy this vector to another vector
	     */
	    public Point copyTo(Point p)
	    {
	        p.x = x;
	        p.y = y;
	        return p;
	    }
	    
	    /**
	     * returns a clone of itself
	     * @return  clone of this point
	     */

	    @Override
	    public Point clone()
	    {
	        return new Point(x, y);
	    }
	/**
	     * Returns the vector sum of <code>this</code> and other
	     * @param other the point added to <code>this</code>
	     * @return vector sum
	     */
	    public Point add(Point other)
	    {
	        return new Point(this.x + other.x, this.y + other.y);
	    }
	    
	     /**
	     *  Vector addition; add other to <code>this</code>
	     * @param other is added to <code>this</code>
	     * @return  <code>this</code> after the addition
	     */
	    public Point addWith(Point other)
	    {
	        x += other.x;
	        y += other.y;
	        return this;
	    }
	/**
	     * Makes <code>this</code> a copy of the other point
	     * @param other 
	     */
	    public void moveTo(Point other)
	    {
	        x = other.x;
	        y = other.y;
	    }
	/**
	     * Vector subtraction
	     * @param other is subtracted from <code>this</code>
	     * @return a new point; this point  is unchanged
	     */
	    public Point subtract(Point other)
	    {
	        return new Point(this.x - other.x, this.y - other.y);
	    }
	/**
	     * 
	     * Vector subtraction
	     * @param length of a copy of <code>this</code>
	     * @return a new vector, obtained b subtracting a scaled version of this point
	     */
	    public Point subtract(float length)
	    {
	        return this.subtract(this.getNormalized().multiply(length));
	    }
	/**
	     * Scalar multiplication
	     * @param scale multilies the length of this to give a new length
	     * @return a new copy of this, with length scaled
	     */
	    public Point multiply(int scale)
	    {
	        return new Point(this.x * scale , this.y * scale);
	    }


	     /**
	     * Returns a new point at the specified distance in the direction angle  from
	     * this point.
	     * @param distance the distance to the new point
	     * @param angle the angle to the new point
	     * @return the new point
	     */
	    public Point pointAt(double distance, double angle)
	    {
	      int xx = (int) (distance*(float)Math.cos(Math.toRadians(angle)) + x);
	      int yy = (int) (distance*(float)Math.sin(Math.toRadians(angle)) + y);
	      return new Point(xx,yy);
	    }
	}
