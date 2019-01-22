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
package com.datastax.oss.driver.internal.mapper.processor.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Ensures the unicity of field and method names in generated code: if a name clashes with an
 * existing member, an incrementing index is appended to it.
 */
public class NameIndex {

  private Map<String, Integer> fieldIndices = new HashMap<>();
  private Map<String, Integer> methodIndices = new HashMap<>();

  public String uniqueField(String baseName) {
    return uniqueName(baseName, fieldIndices);
  }

  public String uniqueMethod(String baseName) {
    return uniqueName(baseName, methodIndices);
  }

  private String uniqueName(String baseName, Map<String, Integer> indices) {
    Integer index = indices.compute(baseName, (k, v) -> (v == null) ? 0 : v + 1);
    return (index == 0) ? baseName : baseName + index;
  }
}
