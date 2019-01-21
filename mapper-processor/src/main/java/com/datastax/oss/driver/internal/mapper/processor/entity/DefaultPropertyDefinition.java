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

import com.squareup.javapoet.TypeName;

public class DefaultPropertyDefinition implements PropertyDefinition {

  private final String cqlName;
  private final String getterName;
  private final String setterName;
  private final TypeName type;

  public DefaultPropertyDefinition(
      String cqlName, String getterName, String setterName, TypeName type) {
    this.cqlName = cqlName;
    this.getterName = getterName;
    this.setterName = setterName;
    this.type = type;
  }

  @Override
  public String getCqlName() {
    return cqlName;
  }

  @Override
  public String getGetterName() {
    return getterName;
  }

  @Override
  public String getSetterName() {
    return setterName;
  }

  @Override
  public TypeName getType() {
    return type;
  }

  public static class Builder {
    private final String cqlName;
    private final TypeName type;
    private String getterName;
    private String setterName;

    public Builder(String cqlName, TypeName type) {
      this.cqlName = cqlName;
      this.type = type;
    }

    public Builder withGetterName(String getterName) {
      this.getterName = getterName;
      return this;
    }

    public Builder withSetterName(String setterName) {
      this.setterName = setterName;
      return this;
    }

    public TypeName getType() {
      return type;
    }

    public boolean hasGetterAndSetter() {
      return getterName != null && setterName != null;
    }

    DefaultPropertyDefinition build() {
      return new DefaultPropertyDefinition(cqlName, getterName, setterName, type);
    }
  }
}
