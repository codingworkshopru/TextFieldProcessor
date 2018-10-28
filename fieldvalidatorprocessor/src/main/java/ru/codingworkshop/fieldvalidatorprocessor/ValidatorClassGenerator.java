package ru.codingworkshop.fieldvalidatorprocessor;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import ru.codingworkshop.fieldvalidatorlibrary.TextFieldValidator;
import ru.codingworkshop.fieldvalidatorlibrary.TextFieldViewAdapter;
import ru.codingworkshop.fieldvalidatorlibrary.Validator;

@SupportedAnnotationTypes("ru.codingworkshop.fieldvalidatorlibrary.TextFieldValidator")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ValidatorClassGenerator extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        return proceedTextFieldValidator(set.stream()
                .filter(annotation -> annotation.getQualifiedName().contentEquals(TextFieldValidator.class.getCanonicalName()))
                .findFirst()
                .orElse(null), roundEnvironment);
    }

    private boolean proceedTextFieldValidator(TypeElement textFieldValidatorAnnotation, RoundEnvironment roundEnvironment) {
        if (textFieldValidatorAnnotation != null) {
            Set<? extends Element> annotatedFields = roundEnvironment.getElementsAnnotatedWith(textFieldValidatorAnnotation);

            Map<Element, List<Element>> enclosingClassWithTypes = annotatedFields.stream()
                    .collect(Collectors.groupingBy(Element::getEnclosingElement));

            enclosingClassWithTypes.keySet()
                    .forEach(enclosingType -> {
                        ClassName validatorTypeName = ClassName.get(processingEnv.getElementUtils().getPackageOf(enclosingType).toString(), enclosingType.getSimpleName() + "Validator");
                        final String fieldContainerName = toCamelCase(enclosingType.getSimpleName().toString());
                        TypeSpec.Builder enclosingTypeSpecBuilder = TypeSpec.classBuilder(validatorTypeName)
                                .addField(TypeName.get(enclosingType.asType()), fieldContainerName, Modifier.PRIVATE);


                        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .addParameter(TypeName.get(enclosingType.asType()), fieldContainerName)
                                .addStatement("this.$N = $N", fieldContainerName, fieldContainerName)
                                .build();

                        MethodSpec initSpec = MethodSpec.methodBuilder("init")
                                .returns(validatorTypeName)
                                .addModifiers(Modifier.STATIC)
                                .addParameter(TypeName.get(enclosingType.asType()), fieldContainerName)
                                .addStatement("return new $T($N)", validatorTypeName, fieldContainerName)
                                .build();

                        TypeVariableName typeVariableName = TypeVariableName.get("T");
                        MethodSpec validateFieldMethodSpec = MethodSpec.methodBuilder("validateField")
                                .returns(boolean.class)
                                .addTypeVariable(typeVariableName)
                                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                .addParameter(typeVariableName, "field")
                                .addParameter(ParameterizedTypeName.get(ClassName.get(TextFieldViewAdapter.class), typeVariableName), "adapter")
                                .addParameter(Validator[].class, "validators")
                                .varargs()
                                .addStatement("$T text = adapter.getText(field)", String.class)
                                .beginControlFlow("for ($T validator : validators)", Validator.class)
                                .beginControlFlow("if (validator.validate(text))")
                                .addStatement("adapter.clearError(field)")
                                .nextControlFlow("else")
                                .addStatement("adapter.setError(field, validator.getErrorText())")
                                .addStatement("return false")
                                .endControlFlow()
                                .endControlFlow()
                                .addStatement("return true")
                                .build();

                        List<MethodSpec> methods = new LinkedList<>(Arrays.asList(constructorSpec, initSpec, validateFieldMethodSpec));

                        List<MethodSpec> validateMethods = enclosingClassWithTypes.get(enclosingType)
                                .stream()
                                .map(field ->
                                        {
                                            String fieldName = field.getSimpleName().toString();
                                            TypeMirror adapter;
                                            List<TypeMirror> validators;
                                            try {
                                                adapter = (TypeMirror) getAnnotationParameter(TextFieldValidator.class.getMethod("adapter").getName(), field);
                                                validators = Stream.of(getAnnotationParameter(TextFieldValidator.class.getMethod("validator").getName(), field))
                                                        .map(obj -> (Collection<?>) obj)
                                                        .flatMap(Collection::stream)
                                                        .map(annotationValue -> (AnnotationValue) annotationValue)
                                                        .map(annotationValue -> (TypeMirror) annotationValue.getValue())
                                                        .collect(Collectors.toList());
                                            } catch (NoSuchMethodException e) {
                                                e.printStackTrace();
                                                printError("It's impossible", field);
                                                return null;
                                            }

                                            CodeBlock.Builder builder = CodeBlock.builder()
                                                    .add("return validateField($L.$L, new $T()", fieldContainerName, fieldName, adapter);
                                            for (TypeMirror v : validators) {
                                                builder.add(", new $T()", v);
                                            }
                                            builder.add(")");

                                            return MethodSpec.methodBuilder(toCamelCase("validate", fieldName))
                                                    .returns(boolean.class)
                                                    .addStatement(builder.build())
                                                    .build();
                                        }
                                )
                                .collect(Collectors.toList());

                        final MethodSpec.Builder validateAllMethodSpec = MethodSpec.methodBuilder("validateAll")
                                .addStatement("return " + validateMethods.stream().map(methodSpec -> methodSpec.name+"()").collect(Collectors.joining(" && ")))
                                .returns(boolean.class);

                        methods.addAll(validateMethods);
                        methods.add(validateAllMethodSpec.build());

                        enclosingTypeSpecBuilder.addMethods(methods);

                        JavaFile jf = JavaFile.builder(processingEnv.getElementUtils().getPackageOf(enclosingType).getQualifiedName().toString(), enclosingTypeSpecBuilder.build())
                                .build();

                        try {
                            jf.writeTo(processingEnv.getFiler());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
        return false;
    }

    private Object getAnnotationParameter(String parameterName, Element field) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = getAnnotationFrom(field).getElementValues();
        Optional<? extends ExecutableElement> parameterElement = elementValues.keySet()
                .stream()
                .filter(executable -> executable.getSimpleName().toString().equals(parameterName))
                .findAny();

        if (parameterElement.isPresent()) {
            return elementValues.get(parameterElement.get()).getValue();
        } else {
            printError("Annotation parameter not found", field);
        }
        throw new RuntimeException();
    }

    private AnnotationMirror getAnnotationFrom(Element field) {
        if (field.getKind().equals(ElementKind.FIELD)) {
            final String canonicalName = TextFieldValidator.class.getCanonicalName();
            Optional<? extends AnnotationMirror> any = field.getAnnotationMirrors()
                    .stream()
                    .filter(mirror -> mirror.getAnnotationType().toString().equals(canonicalName))
                    .findAny();

            if (any.isPresent()) {
                return any.get();
            } else {
                printError("WTF! Field isn't annotated with " + canonicalName, field);
            }
        } else {
            printError("Not a field", field);
        }
        throw new RuntimeException();
    }

    private void printError(String error, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, error, element);
    }

    private String toCamelCase(String... args) {
        String result = Stream.of(args)
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1))
                .collect(Collectors.joining());

        return result.substring(0, 1).toLowerCase() + result.substring(1);

    }
}
