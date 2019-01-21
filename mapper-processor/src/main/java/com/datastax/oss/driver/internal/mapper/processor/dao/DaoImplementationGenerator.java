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
package com.datastax.oss.driver.internal.mapper.processor.dao;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.internal.core.util.concurrent.BlockingOperation;
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import com.datastax.oss.driver.internal.mapper.MapperContext;
import com.datastax.oss.driver.internal.mapper.processor.GeneratedNames;
import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.SingleFileCodeGenerator;
import com.datastax.oss.driver.internal.mapper.processor.entity.EntityDefinition;
import com.datastax.oss.driver.internal.mapper.processor.entity.PropertyDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.concurrent.CompletableFuture;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class DaoImplementationGenerator extends SingleFileCodeGenerator {

  private final TypeElement interfaceElement;
  private final ClassName implementationName;

  public DaoImplementationGenerator(TypeElement interfaceElement, ProcessorContext context) {
    super(context);
    this.interfaceElement = interfaceElement;
    implementationName = GeneratedNames.daoImplementation(interfaceElement);
  }

  @Override
  protected String getFileName() {
    return implementationName.packageName() + "." + implementationName.simpleName();
  }

  @Override
  protected JavaFile.Builder getContents() {

    TypeSpec.Builder classBuilder =
        TypeSpec.classBuilder(implementationName)
            .addJavadoc(JAVADOC_GENERATED_WARNING)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(interfaceElement))
            .addField(
                FieldSpec.builder(MapperContext.class, "context", Modifier.PRIVATE, Modifier.FINAL)
                    .build())
            .addField(
                FieldSpec.builder(
                        CqlIdentifier.class, "keyspaceId", Modifier.PRIVATE, Modifier.FINAL)
                    .build())
            .addField(
                FieldSpec.builder(CqlIdentifier.class, "tableId", Modifier.PRIVATE, Modifier.FINAL)
                    .build());

    MethodSpec.Builder initAsyncBuilder =
        MethodSpec.methodBuilder("initAsync")
            .returns(
                ParameterizedTypeName.get(
                    ClassName.get(CompletableFuture.class), ClassName.get(interfaceElement)))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(MapperContext.class, "context")
            .addParameter(CqlIdentifier.class, "keyspaceId")
            .addParameter(CqlIdentifier.class, "tableId")
            .addStatement(
                "return $T.completedFuture(new $T(context, keyspaceId, tableId))",
                CompletableFuture.class,
                implementationName);

    MethodSpec.Builder initBuilder =
        MethodSpec.methodBuilder("init")
            .returns(ClassName.get(interfaceElement))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(MapperContext.class, "context")
            .addParameter(CqlIdentifier.class, "keyspaceId")
            .addParameter(CqlIdentifier.class, "tableId")
            .addStatement("$T.checkNotDriverThread()", BlockingOperation.class)
            .addStatement(
                "return $T.getUninterruptibly(initAsync(context, keyspaceId, tableId))",
                CompletableFutures.class);

    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(MapperContext.class, "context")
            .addParameter(CqlIdentifier.class, "keyspaceId")
            .addParameter(CqlIdentifier.class, "tableId")
            .addStatement("this.context = context")
            .addStatement("this.keyspaceId = keyspaceId")
            .addStatement("this.tableId = tableId");

    classBuilder.addMethod(initAsyncBuilder.build());
    classBuilder.addMethod(initBuilder.build());
    classBuilder.addMethod(constructorBuilder.build());

    documentEntityConstants(classBuilder);

    return JavaFile.builder(implementationName.packageName(), classBuilder.build());
  }

  /**
   * If the DAO has a constant of a mapped entity, dump the definition of that entity in the DAO's
   * javadoc.
   *
   * @deprecated this is a quick and dirty test to check that entity parsing works, currently the
   *     only way because we don't use entities in generated code yet. TODO delete
   */
  @Deprecated
  private void documentEntityConstants(TypeSpec.Builder classBuilder) {
    for (Element child : interfaceElement.getEnclosedElements()) {
      if (child.getKind() == ElementKind.FIELD) {
        VariableElement field = (VariableElement) child;
        TypeMirror type = field.asType();
        if (type.getKind() == TypeKind.DECLARED) {
          Element element = ((DeclaredType) type).asElement();
          if (element.getKind() == ElementKind.CLASS
              && element.getAnnotation(Entity.class) != null) {
            EntityDefinition entityDefinition =
                context.getEntityFactory().getDefinition((TypeElement) element);

            classBuilder.addJavadoc(entityDefinition.getCqlName() + "\n");
            for (PropertyDefinition propertyDefinition : entityDefinition.getProperties()) {
              classBuilder.addJavadoc(
                  propertyDefinition.getCqlName() + " " + propertyDefinition.getType() + "\n");
            }
          }
        }
      }
    }
  }
}
