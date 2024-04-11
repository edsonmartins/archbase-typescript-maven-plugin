package br.com.archbase.generator.code;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TypeScriptDomainGenerator {

    private static final Map<Class<?>, String> typeMappings = new HashMap<>();

    static {
        typeMappings.put(String.class, "string");
        typeMappings.put(Long.class, "number");
        typeMappings.put(Boolean.class, "boolean");
        typeMappings.put(boolean.class, "boolean");
        typeMappings.put(byte[].class, "string");
        typeMappings.put(Date.class, "string");
        typeMappings.put(LocalDateTime.class, "string");
        typeMappings.put(LocalDate.class, "string");
        typeMappings.put(Integer.class, "number");
        typeMappings.put(Byte[].class, "string");
    }

    public static String generateTypeScriptClass(Class<?> dtoClass) {
        StringBuilder tsClass = new StringBuilder();
        tsClass.append("export class ").append(dtoClass.getSimpleName()).append(" {\n");

        // Campos
        for (Field field : dtoClass.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            String tsType = typeMappings.getOrDefault(fieldType, "any");
            if (Collection.class.isAssignableFrom(fieldType)) {
                Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                Class<?> genericClass = (Class<?>) genericType;
                String genericTypeName = genericClass.getSimpleName();
                tsType = "Array<" + (genericClass.isEnum() ? "string" : genericTypeName) + ">";
            }  else if (fieldType.getSimpleName().contains("Dto")) {
                tsType = fieldType.getSimpleName();
            } else if (fieldType.isEnum()){
                tsType = fieldType.getSimpleName();
            }
            tsClass.append("  ").append(field.getName()).append(": ").append(tsType).append(";\n");
        }

        // Construtor
        tsClass.append("\n  constructor(data: any) {\n");
        for (Field field : dtoClass.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            String fieldName = field.getName();
            if (Collection.class.isAssignableFrom(fieldType)) {
                String[] name = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName().split("\\.");
                tsClass.append("    this.").append(fieldName).append(" = data.").append(fieldName)
                        .append(" ? data.").append(fieldName).append(".map((item: any) => new ")
                        .append(name[name.length-1])
                        .append("(item)) : [];\n");
            } else if (fieldType.getSimpleName().contains("Dto")) {
                tsClass.append("    if (data.").append(fieldName).append(") {\n");
                tsClass.append("      this.").append(fieldName).append(" = new ").append(fieldType.getSimpleName())
                        .append("(data.").append(fieldName).append(");\n");
                tsClass.append("    }\n");
            } else {
                tsClass.append("    this.").append(fieldName).append(" = data.").append(fieldName).append(";\n");
            }
        }
        tsClass.append("  }\n");

        tsClass.append("}\n");

        return tsClass.toString();
    }
}
