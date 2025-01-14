/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.jaxb.mapping.marshall;

import jakarta.persistence.DiscriminatorType;

/**
 * JAXB marshalling for {@link DiscriminatorType}
 *
 * @author Steve Ebersole
 */
public class DiscriminatorTypeMarshalling {
	public static DiscriminatorType fromXml(String name) {
		return DiscriminatorType.valueOf( name );
	}

	public static String toXml(DiscriminatorType discriminatorType) {
		return discriminatorType.name();
	}
}
