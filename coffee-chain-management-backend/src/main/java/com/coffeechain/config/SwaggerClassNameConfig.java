package com.coffeechain.config;

import io.swagger.v3.oas.models.Operation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class SwaggerClassNameConfig {

  @Bean
  public OperationCustomizer appendRequestResponseClassNames() {
    return (Operation operation, HandlerMethod handlerMethod) -> {
      String requestClass = findRequestClass(handlerMethod);
      String responseClass = resolveTypeName(handlerMethod.getMethod().getGenericReturnType());
      String classInfo = buildClassInfo(requestClass, responseClass);

      String currentDescription = operation.getDescription();
      if (currentDescription == null || currentDescription.isBlank()) {
        operation.setDescription(classInfo);
      } else if (!currentDescription.contains("Request class:")) {
        operation.setDescription(currentDescription.stripTrailing() + "\n\n" + classInfo);
      }

      return operation;
    };
  }

  private String findRequestClass(HandlerMethod handlerMethod) {
    for (Parameter parameter : handlerMethod.getMethod().getParameters()) {
      if (parameter.isAnnotationPresent(RequestBody.class)) {
        return resolveTypeName(parameter.getParameterizedType());
      }
    }
    return "Không có request body";
  }

  private String buildClassInfo(String requestClass, String responseClass) {
    return "Request class: `"
        + requestClass
        + "`\n"
        + "Response class: `"
        + responseClass
        + "`\n"
        + "Ghi chú: Xem phần Schemas bên dưới Swagger để đọc đầy đủ field, kiểu dữ liệu và ví dụ của từng class.";
  }

  private String resolveTypeName(Type type) {
    if (type instanceof Class<?> clazz) {
      return clazz.getSimpleName();
    }

    if (type instanceof ParameterizedType parameterizedType) {
      Type rawType = parameterizedType.getRawType();
      Type[] arguments = parameterizedType.getActualTypeArguments();

      if (rawType instanceof Class<?> rawClass
          && ResponseEntity.class.isAssignableFrom(rawClass)
          && arguments.length == 1) {
        return resolveTypeName(arguments[0]);
      }

      String rawName =
          rawType instanceof Class<?> rawClass ? rawClass.getSimpleName() : rawType.getTypeName();
      String argumentNames =
          Arrays.stream(arguments).map(this::resolveTypeName).collect(Collectors.joining(", "));
      return rawName + "<" + argumentNames + ">";
    }

    String typeName = type.getTypeName();
    int lastDot = typeName.lastIndexOf('.');
    return lastDot >= 0 ? typeName.substring(lastDot + 1) : typeName;
  }
}
