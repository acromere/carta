package com.acromere.cartesia.data;

import com.acromere.cartesia.ParseUtil;
import com.acromere.cartesia.math.CadGeometry;
import com.acromere.cartesia.math.CadTransform;
import com.acromere.transaction.Txn;
import com.acromere.transaction.TxnException;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import lombok.CustomLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CustomLog
public class DesignLine extends DesignShape {

	public static final String LINE = "line";

	public static final String POINT = "point";

	public static final String LENGTH = "length";

	public DesignLine() {
		this( null, null );
	}

	public DesignLine( double x1, double y1, double x2, double y2 ) {
		this( new Point3D( x1, y1, 0 ), new Point3D( x2, y2, 0 ) );
	}

	public DesignLine( double x1, double y1, double z1, double x2, double y2, double z2 ) {
		this( new Point3D( x1, y1, z1 ), new Point3D( x2, y2, z2 ) );
	}

	public DesignLine( Point3D origin, Point3D point ) {
		super( origin );
		addModifyingKeys( POINT );
		setPoint( point );
	}

	@Override
	public DesignShape.Type getType() {
		return DesignShape.Type.LINE;
	}

	public Point3D getPoint() {
		return getValue( POINT );
	}

	public DesignShape setPoint( Point3D point ) {
		setValue( POINT, point );
		return this;
	}

	@Override
	protected Bounds computeGeometricBounds() {
		if( getOrigin() == null || getPoint() == null ) return null;
		return CadGeometry.getBounds( getOrigin(), getPoint() );
	}

	@Override
	public List<Point3D> getReferencePoints() {
		if( getOrigin() == null || getPoint() == null ) return List.of();
		return List.of( getOrigin(), getPoint() );
	}

	@Override
	public double distanceTo( Point3D point ) {
		if( getOrigin() == null || getPoint() == null || point == null ) return Double.NaN;
		return CadGeometry.pointSegmentDistance( getOrigin(), getPoint(), point );
	}

	@Override
	public double pathLength() {
		if( getOrigin() == null || getPoint() == null ) return Double.NaN;
		return getPoint().distance( getOrigin() );
	}

	@Override
	public Map<String, Object> getInformation() {
		Map<String, Object> info = new HashMap<>();
		if( getOrigin() != null ) info.put( ORIGIN, getOrigin() );
		if( getPoint() != null ) info.put( POINT, getPoint() );
		info.put( LENGTH, pathLength() );
		return info;
	}

	@Override
	public DesignLine cloneShape() {
		return new DesignLine().copyFrom( this, true );
	}

	@Override
	public void apply( CadTransform transform ) {
		if( getOrigin() == null || getPoint() == null ) return;

		try( Txn ignored = Txn.create() ) {
			setOrigin( transform.apply( getOrigin() ) );
			setPoint( transform.apply( getPoint() ) );
		} catch( TxnException exception ) {
			log.atWarn().log( "Unable to apply transform" );
		}
	}

	protected Map<String, Object> asMap() {
		Map<String, Object> map = super.asMap();
		map.put( SHAPE, LINE );
		map.putAll( asMap( POINT ) );
		return map;
	}

	public DesignLine updateFrom( Map<String, Object> map ) {
		super.updateFrom( map );
		if( map.containsKey( POINT ) ) {
			Object point = map.get( POINT );
			if( point instanceof Point3D ) setPoint( (Point3D)point );
			else if( point instanceof String ) setPoint( ParseUtil.parsePoint3D( (String)point ) );
		}
		return this;
	}

	@Override
	public DesignShape updateFrom( DesignShape shape ) {
		super.updateFrom( shape );
		if( !(shape instanceof DesignLine line) ) return this;

		try( Txn ignore = Txn.create() ) {
			this.setPoint( line.getPoint() );
		} catch( TxnException exception ) {
			log.atWarn().log( "Unable to update line" );
		}

		return this;
	}

	public void moveEndpoint( Point3D source, Point3D target ) {
		if( source == null || target == null || getOrigin() == null || getPoint() == null ) return;

		if( CadGeometry.areSamePoint( getOrigin(), source ) ) {
			setOrigin( target );
		} else if( CadGeometry.areSamePoint( getPoint(), source ) ) {
			setPoint( target );
		}
	}

	@Override
	public String toString() {
		return super.toString( ORIGIN, POINT );
	}

}
