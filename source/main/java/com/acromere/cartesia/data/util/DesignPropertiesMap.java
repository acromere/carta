package com.acromere.cartesia.data.util;

import com.acromere.cartesia.RbKey;
import com.acromere.cartesia.data.*;
import com.acromere.cartesia.data.*;
import com.acromere.xenon.XenonProgramProduct;
import com.acromere.xenon.tool.settings.SettingsPage;
import com.acromere.xenon.tool.settings.SettingsPageParser;
import lombok.CustomLog;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

@CustomLog
public class DesignPropertiesMap {

	private static final String propertiesPagePath = "/com/acromere/cartesia/design/props/";

	private static final String propertiesPageExt = ".xml";

	private static final Map<Class<? extends DesignDrawable>, SettingsPage> propertiesPageCache;

	static {
		propertiesPageCache = Collections.synchronizedMap( new WeakHashMap<>() );
	}

	public DesignPropertiesMap( XenonProgramProduct product ) {
		// Layer properties
		propertiesPageCache.putIfAbsent( DesignLayer.class, loadPage( product, "layer" ) );

		// Common shape properties
		propertiesPageCache.putIfAbsent( DesignShape.class, loadPage( product, "shape" ) );

		// Specific shape properties
		propertiesPageCache.putIfAbsent( DesignBox.class, loadPage( product, "box" ) );
		propertiesPageCache.putIfAbsent( DesignLine.class, loadPage( product, "line" ) );
		propertiesPageCache.putIfAbsent( DesignEllipse.class, loadPage( product, "ellipse" ) );
		propertiesPageCache.putIfAbsent( DesignArc.class, loadPage( product, "arc" ) );
		propertiesPageCache.putIfAbsent( DesignQuad.class, loadPage( product, "curve" ) );
		propertiesPageCache.putIfAbsent( DesignCubic.class, loadPage( product, "curve" ) );
		propertiesPageCache.putIfAbsent( DesignPath.class, loadPage( product, "path" ) );
		propertiesPageCache.putIfAbsent( DesignMarker.class, loadPage( product, "marker" ) );
		propertiesPageCache.putIfAbsent( DesignText.class, loadPage( product, "text" ) );
	}

	public SettingsPage getSettingsPage( Class<? extends DesignDrawable> type ) {
		return propertiesPageCache.get( type );
	}

	private SettingsPage loadPage( XenonProgramProduct product, String key ) {
		try {
			return loadSettingsPage( product, key );
		} catch( IOException exception ) {
			log.atError().withCause( exception).log( "Unable to load settings page=%s", propertiesPagePath + key + propertiesPageExt );
		}
		return null;
	}

	private SettingsPage loadSettingsPage( XenonProgramProduct product, String key ) throws IOException {
		String pagePath = propertiesPagePath + key + propertiesPageExt;
		return SettingsPageParser.parse( product, pagePath, RbKey.PROPS ).get( key );
	}

}
