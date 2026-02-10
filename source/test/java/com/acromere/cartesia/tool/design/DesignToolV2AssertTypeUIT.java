package com.acromere.cartesia.tool.design;

import com.acromere.cartesia.Design2dResourceType;
import lombok.CustomLog;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Getter
@CustomLog
public class DesignToolV2AssertTypeUIT extends DesignToolV2BaseUIT {

	@Test
	void assetTypeResolvesCorrectly() {
		assertThat( getResource().getType() ).isInstanceOf( Design2dResourceType.class );
	}

}
