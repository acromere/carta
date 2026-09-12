package com.acromere.cartesia.tool.design.binding;

import com.acromere.data.DataNode;
import javafx.beans.value.ObservableValue;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DesignStringBindingTest {

	private static class MockNode extends DataNode {

		static final String KEY = "name";

		static final String OTHER = "other";

		String getName() {
			return getValue( KEY );
		}

		void setName( String name ) {
			setValue( KEY, name );
		}

		void setOther( String other ) {
			setValue( OTHER, other );
		}

	}

	@Test
	void testInitialValue() {
		MockNode node = new MockNode();
		node.setName( "initial" );

		DesignStringBinding binding = new DesignStringBinding( node, MockNode.KEY, MockNode::getName );

		assertThat( binding.get() ).isEqualTo( "initial" );
		assertThat( binding.getValue() ).isEqualTo( "initial" );
	}

	@Test
	void testInitialNullValue() {
		MockNode node = new MockNode();

		DesignStringBinding binding = new DesignStringBinding( node, MockNode.KEY, MockNode::getName );

		assertThat( binding.get() ).isNull();
	}

	@Test
	void testUpdateValueOnPropertyChange() {
		MockNode node = new MockNode();
		node.setName( "first" );

		DesignStringBinding binding = new DesignStringBinding( node, MockNode.KEY, MockNode::getName );
		assertThat( binding.get() ).isEqualTo( "first" );

		node.setName( "second" );
		assertThat( binding.get() ).isEqualTo( "second" );

		node.setName( null );
		assertThat( binding.get() ).isNull();

		node.setName( "third" );
		assertThat( binding.get() ).isEqualTo( "third" );
	}

	@Test
	void testUnrelatedPropertyChangeDoesNotTriggerUpdate() {
		MockNode node = new MockNode();
		node.setName( "first" );

		DesignStringBinding binding = new DesignStringBinding( node, MockNode.KEY, MockNode::getName );

		AtomicInteger listenerCallCount = new AtomicInteger( 0 );
		binding.addListener( ( observable, oldValue, newValue ) -> listenerCallCount.incrementAndGet() );

		node.setOther( "unrelated" );

		assertThat( listenerCallCount.get() ).isZero();
		assertThat( binding.get() ).isEqualTo( "first" );
	}

	@Test
	void testChangeListenerNotified() {
		MockNode node = new MockNode();
		node.setName( "first" );

		DesignStringBinding binding = new DesignStringBinding( node, MockNode.KEY, MockNode::getName );

		AtomicInteger callCount = new AtomicInteger( 0 );
		binding.addListener( ( ObservableValue<? extends String> obs, String oldValue, String newValue ) -> {
			assertThat( oldValue ).isEqualTo( "first" );
			assertThat( newValue ).isEqualTo( "second" );
			callCount.incrementAndGet();
		} );

		node.setName( "second" );

		assertThat( callCount.get() ).isEqualTo( 1 );
	}

	@Test
	void testGetBeanAndGetName() {
		MockNode node = new MockNode();
		DesignStringBinding binding = new DesignStringBinding( node, MockNode.KEY, MockNode::getName );

		assertThat( binding.getBean() ).isNull();
		assertThat( binding.getName() ).isNull();
	}

}
