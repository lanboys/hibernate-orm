/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.Locale;
import java.util.function.Supplier;

import org.hibernate.graph.spi.GraphHelper;
import org.hibernate.metamodel.model.AttributeClassification;
import org.hibernate.metamodel.model.domain.ManagedDomainType;
import org.hibernate.metamodel.model.domain.SimpleDomainType;
import org.hibernate.metamodel.model.domain.SingularPersistentAttribute;
import org.hibernate.query.sqm.SqmPathSource;
import org.hibernate.query.sqm.produce.path.spi.SemanticPathPart;
import org.hibernate.query.sqm.produce.spi.SqmCreationState;
import org.hibernate.query.sqm.tree.domain.SqmAnyValuedSimplePath;
import org.hibernate.query.sqm.tree.domain.SqmBasicValuedSimplePath;
import org.hibernate.query.sqm.tree.domain.SqmEmbeddedValuedSimplePath;
import org.hibernate.query.sqm.tree.domain.SqmEntityValuedSimplePath;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.expression.SqmExpression;

/**
 * @author Emmanuel Bernard
 * @author Steve Ebersole
 */
public class SingularAttributeImpl<D,J>
		extends AbstractAttribute<D,J,J>
		implements SingularPersistentAttribute<D,J>, Serializable {
	private final boolean isIdentifier;
	private final boolean isVersion;
	private final boolean isOptional;

	private final SimpleDomainType<J> attributeType;

	// NOTE : delay access for timing reasons
	private final DelayedKeyTypeAccess graphKeyTypeAccess = new DelayedKeyTypeAccess();

	public SingularAttributeImpl(
			ManagedDomainType<D> declaringType,
			String name,
			AttributeClassification attributeClassification,
			SimpleDomainType<J> attributeType,
			Member member,
			boolean isIdentifier,
			boolean isVersion,
			boolean isOptional) {
		super( declaringType, name, attributeType.getJavaTypeDescriptor(), attributeClassification, attributeType, member );
		this.isIdentifier = isIdentifier;
		this.isVersion = isVersion;
		this.isOptional = isOptional;

		this.attributeType = attributeType;
	}

	@Override
	public SimpleDomainType<J> getValueGraphType() {
		return attributeType;
	}

	@Override
	public SimpleDomainType<J> getKeyGraphType() {
		return graphKeyTypeAccess.get();
	}

	@Override
	public SimpleDomainType<J> getSqmNodeType() {
		return super.getSqmNodeType();
	}

	@Override
	public SqmPathSource<?> findSubPathSource(String name) {
		switch ( getAttributeClassification() ) {
			case EMBEDDED:
			case ONE_TO_ONE:
			case MANY_TO_ONE: {
				return ( (SqmPathSource) getSqmNodeType() ).findSubPathSource( name );
			}
			default: {
				throw new UnsupportedOperationException( "Attribute does not contain sub-paths" );
			}
		}
	}

	@Override
	public SemanticPathPart resolvePathPart(
			String name,
			String currentContextKey,
			boolean isTerminal,
			SqmCreationState creationState) {
		return findSubPathSource( name );
	}

	@Override
	public SqmPath resolveIndexedAccess(
			SqmExpression selector,
			String currentContextKey,
			boolean isTerminal,
			SqmCreationState creationState) {
		throw new UnsupportedOperationException( "Singular attribute cannot be index-accessed" );
	}


	/**
	 * Subclass used to simplify instantiation of singular attributes representing an entity's
	 * identifier.
	 */
	public static class Identifier<D,J> extends SingularAttributeImpl<D,J> {
		public Identifier(
				ManagedDomainType<D> declaringType,
				String name,
				SimpleDomainType<J> attributeType,
				Member member,
				AttributeClassification attributeClassification) {
			super( declaringType, name, attributeClassification, attributeType, member, true, false, false );
		}
	}

	/**
	 * Subclass used to simply instantiation of singular attributes representing an entity's
	 * version.
	 */
	public static class Version<X,Y> extends SingularAttributeImpl<X,Y> {
		public Version(
				ManagedDomainType<X> declaringType,
				String name,
				AttributeClassification attributeClassification,
				SimpleDomainType<Y> attributeType,
				Member member) {
			super( declaringType, name, attributeClassification, attributeType, member, false, true, false );
		}
	}

	@Override
	public boolean isId() {
		return isIdentifier;
	}

	@Override
	public boolean isVersion() {
		return isVersion;
	}

	@Override
	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public SimpleDomainType<J> getType() {
		return attributeType;
	}

	@Override
	public boolean isAssociation() {
		return getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE
				|| getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.SINGULAR_ATTRIBUTE;
	}

	@Override
	public Class<J> getBindableJavaType() {
		return attributeType.getJavaType();
	}

	@Override
	public SqmPath createSqmPath(
			SqmPath lhs,
			SqmCreationState creationState) {
		switch ( getAttributeClassification() ) {
			case BASIC: {
				return new SqmBasicValuedSimplePath(  );
			}
			case EMBEDDED: {
				return new SqmEmbeddedValuedSimplePath(  );
			}
			case ANY: {
				return new SqmAnyValuedSimplePath(  );
			}
			case ONE_TO_ONE:
			case MANY_TO_ONE: {
				return new SqmEntityValuedSimplePath(  );
			}
			default: {
				throw new UnsupportedOperationException(
						String.format(
								Locale.ROOT,
								"Cannot create SqmPath from singular attribute [%s#%s] - unknown classification : %s",
								getDeclaringType().getTypeName(),
								getName(),
								getAttributeClassification()
						)
				);
			}
		}
	}

	private class DelayedKeyTypeAccess implements Supplier<SimpleDomainType<J>>, Serializable {
		private boolean resolved;
		private SimpleDomainType<J> type;

		@Override
		public SimpleDomainType<J> get() {
			if ( ! resolved ) {
				type = GraphHelper.resolveKeyTypeDescriptor( SingularAttributeImpl.this );
				resolved = true;
			}
			return type;
		}
	}
}