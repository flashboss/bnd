package test;

import java.io.*;
import java.lang.reflect.*;

import junit.framework.*;
import aQute.bnd.compatibility.*;

public class TestSignatures extends TestCase {
	Signatures s = new Signatures();
	
	
	public void testScopes() throws Exception {
		assertEquals( "<E:Ljava/lang/Object;>(TE;TC;TD;)V", s.getSignature( Z.V.class.getMethod("fooLCO", Object.class, Object.class, Object.class)));
	}
	
	public void testClass() throws Exception {
		assertEquals( "<C:Ljava/lang/Object;>Ltest/X<Ljava/lang/String;>;Ltest/Y<Ljava/lang/Integer;>;", s.getSignature(Z.class));
		assertEquals( "<D:Ljava/lang/Object;>Ljava/lang/Object;", s.getSignature(Z.V.class));
	}
	
	public void testMethodDeclarations() throws Exception {	
		assertEquals( "<E:Ljava/lang/Object;>(TE;)V", s.getSignature( Z.class.getMethod("foo", Object.class)));
		assertEquals( "<E:Ljava/io/InputStream;>(TE;)V", s.getSignature( Z.class.getMethod("fooC", InputStream.class)));
		assertEquals( "<E:Ljava/io/InputStream;:Ljava/lang/Cloneable;>(TE;)V", s.getSignature( Z.class.getMethod("fooCI", InputStream.class)));
		assertEquals( "<E:Ljava/io/InputStream;:Ljava/lang/Cloneable;:Ljava/io/Serializable;>(TE;)V", s.getSignature( Z.class.getMethod("fooCII", InputStream.class)));
		assertEquals( "<E::Ljava/io/Serializable;:Ljava/lang/Cloneable;>(TE;)V", s.getSignature( Z.class.getMethod("fooII", Serializable.class)));
		assertEquals( "<E::Ljava/lang/Cloneable;>(TE;)V", s.getSignature( Z.class.getMethod("fooI", Cloneable.class)));
		assertEquals( "<E:TC;>(TE;)V", s.getSignature( Z.class.getMethod("fooP", Object.class)));		
		assertEquals( "<E:Ljava/lang/Object;F:Ljava/lang/Object;>(TE;TF;)V", s.getSignature( Z.class.getMethod("foo", Object.class, Object.class)));		
	}
	
	public void testFields() throws Exception {
		// Z<Long>.V<Integer>
		assertEquals( "Ltest/Z<Ljava/lang/Long;>.V<Ljava/lang/Integer;>;", s.getSignature(Z.class.getField("referenceToNestedClass")));
		
		// 
		assertEquals( "Ltest/Z<TC;>.V<TC;>;", s.getSignature(Z.class.getField("vc")));
		assertEquals( "<C:Ljava/lang/Object;>Ltest/X<Ljava/lang/String;>;Ltest/Y<Ljava/lang/Integer;>;", s.getSignature(Z.class));
		assertEquals( "<D:Ljava/lang/Object;>Ljava/lang/Object;", s.getSignature(Z.V.class));
		assertEquals( "Ltest/X<Ltest/Y<TC;>;>;", s.getSignature( Z.class.getField("field")));
	}
	
	
	public void testWildcards() throws Exception {
		assertEquals( "Ljava/util/Collection<*>;", s.getSignature( Z.class.getField("wildcard_001")));
		assertEquals( "Ljava/util/Collection<+Ljava/lang/Cloneable;>;", s.getSignature( Z.class.getField("wildcard_002")));
		assertEquals( "Ljava/util/Collection<-Ljava/lang/Cloneable;>;", s.getSignature( Z.class.getField("wildcard_003")));
		assertEquals( "Ljava/util/Collection<+TC;>;", s.getSignature( Z.class.getField("wildcard_004")));
		assertEquals( "Ljava/util/Collection<-TC;>;", s.getSignature( Z.class.getField("wildcard_005")));
		assertEquals( "Ljava/util/Collection<+Ltest/Z<TC;>.V<Ljava/lang/Integer;>;>;", s.getSignature( Z.class.getField("wildcard_006")));
		assertEquals( "Ljava/util/Collection<-Ltest/Z<TC;>.V<Ljava/lang/Integer;>;>;", s.getSignature( Z.class.getField("wildcard_007")));
	}
	
	public void testNormalize() {
		assertEquals( "Ltest/Z<Ljava/lang/Long;>.V<Ljava/lang/Integer;>;", s.normalize("Ltest/Z<Ljava/lang/Long;>.V<Ljava/lang/Integer;>;"));
		assertEquals( s.normalize("<A:Ljava/lang/Object;>(TA;)V"), s.normalize("<E:Ljava/lang/Object;>(TE;)V"));		
		assertEquals( s.normalize("<A:Ljava/lang/Object;>(TA;TB;)V"), s.normalize("<E:Ljava/lang/Object;>(TE;TC;)V"));
		
		// we use (A,A) and test against (A,B)
		assertFalse( s.normalize("<A:Ljava/lang/Object;>(TA;TA;)V").equals(s.normalize("<E:Ljava/lang/Object;>(TE;TC;)V")));		
		assertEquals( "<_0:Ljava/lang/Object;>(T_0;)V", s.normalize("<E:Ljava/lang/Object;>(TE;)V"));		
		assertEquals( "<_0:Ljava/lang/Object;>(T_0;T_1;T_2;)V", s.normalize("<E:Ljava/lang/Object;>(TE;TC;TD;)V"));
		assertEquals( "<_0:Ljava/lang/Object;>Ljava/lang/Object;", s.normalize("<_0:Ljava/lang/Object;>Ljava/lang/Object;"));
	}
	
	public void testCompatibility() throws Exception {
		String _001 =ns(Z.class.getMethod("compatibility_001"));
		String _002 =ns(Z.class.getMethod("compatibility_002"));
		String _003 =ns(Z.class.getMethod("compatibility_003"));
		assertEquals( _001, _002);
		assertTrue( _001.equals(_002));
		assertFalse( _001.equals(_003));
	}

	private String ns(Method method) throws Exception {
		String sig = s.getSignature(method);
		return s.normalize(sig);
	}
}
