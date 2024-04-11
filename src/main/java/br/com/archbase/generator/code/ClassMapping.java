package br.com.archbase.generator.code;

public class ClassMapping {
    public String baseClass;
    public String targetPackageSuffix;

    public boolean generateDTO = true;
    public boolean generateRepository = true;
    public boolean generateMapper = true;
    public boolean generateAdapter = true;
    public boolean generateService = true;
    public boolean generateController = true;
    public boolean generateDomainTypescript = true;
}
