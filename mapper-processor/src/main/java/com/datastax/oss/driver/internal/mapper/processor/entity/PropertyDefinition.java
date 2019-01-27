/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.mapper.processor.entity;

import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.squareup.javapoet.TypeName;
import javax.lang.model.element.TypeElement;

public interface PropertyDefinition {

  String getCqlName();

  String getGetterName();

  String getSetterName();

  TypeName getType();

  /**
   * If the {@linkplain #getType() type} of this property is another {@link Entity}-annotated class,
   * the definition of that class. Otherwise null.
   */
  TypeElement getEntityElement();
}
