package com.acromere.cartesia.data;

import com.acromere.cartesia.ParseUtil;
import com.acromere.cartesia.math.CadGeometry;
import com.acromere.cartesia.math.CadOrientation;
import com.acromere.cartesia.math.CadTransform;
import com.acromere.curve.math.Arithmetic;
import com.acromere.curve.math.Constants;
import com.acromere.curve.math.Geometry;
import com.acromere.transaction.Txn;
import com.acromere.transaction.TxnException;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import lombok.CustomLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CustomLog
public class DesignEllipse extends DesignShape {

	public static final String CIRCLE = "circle";

	public static final String ELLIPSE = "ellipse";

	public static final String RADII = "radii";

	/**
	 * @deprecated Maintained for backward compatibility only
	 */
	@Deprecated
	@SuppressWarnings( "DeprecatedIsStillUsed" )
	private static final String X_RADIUS = "x-radius";

	/**
	 * @deprecated Maintained for backward compatibility only
	 */
	@Deprecated
	@SuppressWarnings( "DeprecatedIsStillUsed" )
	private static final String Y_RADIUS = "y-radius";

	private static final String PERIMETER = "perimeter";

	@Deprecated
	// This is not to be used publicly
	static final String RADIUS = "radius";

	public DesignEllipse() {
		this( null, 0.0 );
	}

	public DesignEllipse( double x, double y, double radius ) {
		this( new Point3D( x, y, 0 ), radius );
	}

	public DesignEllipse( Point3D origin, Double radius ) {
		this( origin, radius, radius );
	}

	public DesignEllipse( Point3D origin, Point3D radii ) {
		this( origin, radii, null );
	}

	public DesignEllipse( Point3D origin, Double xRadius, Double yRadius ) {
		this( origin, xRadius, yRadius, null );
	}

	public DesignEllipse( Point3D origin, Double xRadius, Double yRadius, Double rotate ) {
		this( origin, xRadius == null || yRadius == null ? null : new Point3D( xRadius, yRadius, 0.0 ), rotate );
	}

	public DesignEllipse( Point3D origin, Point3D radii, Double rotate ) {
		super( origin );
		addModifyingKeys( RADII );
		setRadii( radii );
		setRotate( rotate == null || rotate == 0.0 ? null : rotate.toString() );
	}

	@Override
	public Type getType() {
		return Type.ELLIPSE;
	}

	public Point3D getRadii() {
		return getValue( RADII );
	}

	@SuppressWarnings( "unchecked" )
	public <T extends DesignEllipse> T setRadii( Point3D radii ) {
		setValue( RADII, radii );
		return (T)this;
	}

	public Double getRadius() {
		Point3D radii = getRadii();
		return radii == null ? null : radii.getX();
	}

	@SuppressWarnings( "unchecked" )
	public <T extends DesignEllipse> T setRadius( Double value ) {
		setRadii( value == null ? null : new Point3D( value, value, 0 ) );
		return (T)this;
	}

	public Double getXRadius() {
		Point3D radii = getRadii();
		return radii == null ? null : radii.getX();
	}

	public Double getYRadius() {
		Point3D radii = getRadii();
		return radii == null ? null : radii.getY();
	}

	/**
	 * Test if a given point is on the ellipse.
	 *
	 * @param point
	 * @return
	 */
	public boolean isCoincident( Point3D point ) {
		if( point == null || getOrigin() == null || getRadii() == null ) return false;
		return CadGeometry.areSamePoint( point, CadGeometry.ellipsePoint360( this, CadGeometry.ellipseAngle360( this, point ) ) );
	}

	public CadTransform getRotateTransform() {
		return calcLocalTransform( getOrigin(), getXRadius(), getYRadius(), calcRotate() );
	}

	public static CadTransform calcLocalTransform( Point3D center, double xRadius, double yRadius, double rotate ) {
		return CadTransform.scale( 1, xRadius / yRadius, 0 ).combine( calcOrientation( center, rotate ).getWorldToLocalTransform() );
	}

	@Override
	protected Bounds computeGeometricBounds() {
		return CadGeometry.ellipseBounds( this );
	}

	@Override
	public List<Point3D> getReferencePoints() {
		if( getOrigin() == null || getRadii() == null ) return List.of();

		Point3D radii = getRadii();
		Point3D p1 = getOrigin();
		Point3D p2 = p1.add( radii.getX(), 0, 0 );
		Point3D p3 = p1.add( 0, radii.getY(), 0 );
		Point3D p4 = p1.add( -radii.getX(), 0, 0 );
		Point3D p5 = p1.add( 0, -radii.getY(), 0 );

		return CadGeometry.rotate360( p1, calcRotate(), p1, p2, p3, p4, p5 );
	}

	@Override
	public double distanceTo( Point3D point ) {
		return CadGeometry.pointEllipseDistance( point, this );
	}

	public boolean isCircular() {
		Point3D radii = getRadii();
		if( radii == null ) return false;
		return Geometry.areSameSize( radii.getX(), radii.getY() );
	}

	@Override
	public double pathLength() {
		if( getRadii() == null ) return Double.NaN;

		// If the ellipse is circular, then use the circle formula
		if( isCircular() ) return Constants.FULL_CIRCLE * getRadius();

		double a = getXRadius();
		double b = getYRadius();

		if( Geometry.areSameSize( a, 0.0 ) ) return 4 * b;
		if( Geometry.areSameSize( b, 0.0 ) ) return 4 * a;

		double h = ((a - b) * (a - b)) / ((a + b) * (a + b));

		double factor = 0.0;
		for( int index = 0; index < 12; index++ ) {
			factor += pathTerm( index, h );
		}

		return Math.PI * (a + b) * factor;
	}

	private double pathTerm( int iteration, double h ) {
		double b = Arithmetic.bchi( iteration );
		return b * b * Math.pow( h, iteration );
	}

	@Override
	public Map<String, Object> getInformation() {
		Map<String, Object> info = new HashMap<>();
		info.put( ORIGIN, getOrigin() );
		if( isCircular() ) {
			info.put( RADIUS, getRadius() );
		} else {
			info.put( RADII, getRadii() );
		}
		if( getRotate() != null ) info.put( ROTATE, getRotate() );
		info.put( PERIMETER, pathLength() );
		return info;
	}

	@Override
	public DesignEllipse cloneShape() {
		return new DesignEllipse().copyFrom( this, true );
	}

	@Override
	public void apply( CadTransform transform ) {
		if( getOrigin() == null || getRadii() == null ) return;

		CadTransform original = getOrientation().getLocalToWorldTransform();
		CadOrientation newPose = getOrientation().clone().transform( transform );

		// Radii
		CadTransform combined = newPose.getWorldToLocalTransform().combine( transform.combine( original ) );
		double xRadius = Math.abs( combined.apply( new Point3D( getXRadius(), 0, 0 ) ).getX() );
		double yRadius = Math.abs( combined.apply( new Point3D( 0, getYRadius(), 0 ) ).getY() );

		// Rotate
		double oldRotate = CadGeometry.angle360( getOrientation().getRotate() );
		double newRotate = CadGeometry.angle360( newPose.getRotate() );
		double dRotate = newRotate - oldRotate;

		Point3D origin = transform.apply( getOrigin() );
		Point3D radii = new Point3D( xRadius, yRadius, 0 );
		double rotate = calcRotate() + dRotate;

		try( Txn ignored = Txn.create() ) {
			setOrigin( origin );
			setRadii( radii );
			setRotate( rotate );
		} catch( TxnException exception ) {
			log.atWarn().log( "Unable to apply transform" );
		}
	}

	protected Map<String, Object> asMap() {
		Double xRadius = getXRadius();
		Double yRadius = getYRadius();

		Map<String, Object> map = super.asMap();
		map.put( SHAPE, Objects.equals( xRadius, yRadius ) ? CIRCLE : ELLIPSE );
		map.putAll( asMap( RADII ) );
		map.putAll( asMap( ROTATE ) );

		return map;
	}

	public DesignEllipse updateFrom( Map<String, Object> map ) {
		super.updateFrom( map );
		if( map.containsKey( RADII ) ) {
			Object radii = map.get( RADII );
			if( radii instanceof Point3D ) setRadii( (Point3D)radii );
			else if( radii instanceof String ) setRadii( ParseUtil.parsePoint3D( (String)radii ) );
		} else if( map.containsKey( RADIUS ) ) {
			// For backward compatibility
			Object radius = map.get( RADIUS );
			if( radius instanceof Number ) setRadius( ((Number)radius).doubleValue() );
			else if( radius instanceof String ) setRadius( Double.parseDouble( (String)radius ) );
		} else if( map.containsKey( X_RADIUS ) && map.containsKey( Y_RADIUS ) ) {
			// For backward compatibility
			Object x = map.get( X_RADIUS );
			Object y = map.get( Y_RADIUS );
			double xr = x instanceof Number ? ((Number)x).doubleValue() : x instanceof String ? Double.parseDouble( (String)x ) : 0.0;
			double yr = y instanceof Number ? ((Number)y).doubleValue() : y instanceof String ? Double.parseDouble( (String)y ) : 0.0;
			setRadii( new Point3D( xr, yr, 0.0 ) );
		}

		return this;
	}

	@Override
	public DesignShape updateFrom( DesignShape shape ) {
		super.updateFrom( shape );
		if( !(shape instanceof DesignEllipse ellipse) ) return this;

		try( Txn ignore = Txn.create() ) {
			this.setRadii( ellipse.getRadii() );
			this.setRotate( ellipse.getRotate() );
		} catch( TxnException exception ) {
			log.atWarn().log( "Unable to update ellipse" );
		}

		return this;
	}

	//	@Override
	//	protected Bounds computeGeometricBounds() {
	//		// The bounds of a non-rotated ellipse
	//		Point3D origin = getOrigin();
	//		Point3D radii = getRadii();
	//		double x = origin.getX() - radii.getX();
	//		double y = origin.getY() - radii.getY();
	//		double w = 2 * radii.getX();
	//		double h = 2 * radii.getY();
	//		return new BoundingBox( x, y, w, h );
	//	}

	@Override
	public String toString() {
		return super.toString( ORIGIN, RADII, ROTATE );
	}

}
