package ru.codingworkshop.fieldvalidatorprocessor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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
                        TypeSpec.Builder enclosingTypeSpecBuilder = TypeSpec.classBuilder(validatorTypeName);


                        MethodSpec constructorSpec = MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PRIVATE)
                                .build();

                        MethodSpec initSpec = MethodSpec.methodBuilder("init")
                                .returns(validatorTypeName)
                                .addModifiers(Modifier.STATIC)
                                .addParameter(validatorTypeName, validatorTypeName.simpleName())
                                .addStatement("return new $N()", validatorTypeName.simpleName())
                                .build();

                        List<MethodSpec> methods = new LinkedList<>(Arrays.asList(constructorSpec, initSpec));

                        methods.addAll(
                                enclosingClassWithTypes.get(enclosingType)
                                        .stream()
                                        .map(field ->
                                                MethodSpec.methodBuilder("validate" + field.getSimpleName().toString().substring(0, 1).toUpperCase() + field.getSimpleName())
                                                        .returns(void.class)
                                                        .build()
                                        )
                                        .collect(Collectors.toList())
                        );
                        enclosingTypeSpecBuilder.addMethods(methods);

                        JavaFile jf = JavaFile.builder(processingEnv.getElementUtils().getPackageOf(enclosingType).getQualifiedName().toString(), enclosingTypeSpecBuilder.build())
                                .build();

                        try {
                            jf.writeTo(processingEnv.getFiler());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            annotatedFields.forEach(field -> {
                try {
                    TypeMirror adapter = (TypeMirror) getAnnotationParameter(TextFieldValidator.class.getMethod("adapter").getName(), field);
                    List<TypeMirror> validators = Stream.of(getAnnotationParameter(TextFieldValidator.class.getMethod("validator").getName(), field))
                            .map(obj -> (Collection<?>) obj)
                            .flatMap(Collection::stream)
                            .map(annotationValue -> (AnnotationValue) annotationValue)
                            .map(annotationValue -> (TypeMirror) annotationValue.getValue())
                            .collect(Collectors.toList());

                    PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(processingEnv.getTypeUtils().asElement(adapter));
                    validators.clear();
                } catch (NoSuchMethodException e) {
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
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Annotation parameter not found", field);
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
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "WTF! Field isn't annotated with " + canonicalName, field);
            }
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Not a field", field);
        }
        throw new RuntimeException();
    }
}
