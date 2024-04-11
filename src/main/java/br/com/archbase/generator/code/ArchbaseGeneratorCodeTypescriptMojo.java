package br.com.archbase.generator.code;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


@Mojo(name = "generate-typescript", requiresDependencyResolution = ResolutionScope.COMPILE, requiresDependencyCollection = ResolutionScope.COMPILE)
public class ArchbaseGeneratorCodeTypescriptMojo
    extends AbstractMojo
{
    @Parameter(required = true)
    private String typescriptOutputPackageBase;

    @Parameter(required = true)
    private String iocTypesPath;

    @Parameter(required = true)
    private String apiVersion;
    @Parameter(required = true)
    private List<String> entityClasses;

    @Parameter(defaultValue = "${project.compileClasspathElements}")
    private List<String> classpathElements;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    protected List<URL> createClassPath() {
        List<URL> list = new ArrayList<>();
        if (classpathElements != null) {
            for (String cpel : classpathElements) {
                try {
                    list.add(new File(cpel).toURI().toURL());
                } catch (MalformedURLException mue) {
                }
            }
        }
        return list;
    }


    public void execute()
        throws MojoExecutionException
    {
        URLClassLoader urlClassLoader = new URLClassLoader(createClassPath().toArray(new URL[] {}),
                Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(urlClassLoader);

        for (String classMapping : entityClasses) {
            Class<?> sourceClass = null;
            try {
                sourceClass = urlClassLoader.loadClass(classMapping);
                generateDomainTypescript(sourceClass);
                generateServiceTypescript(sourceClass);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void generateDomainTypescript(Class<?> sourceClass) throws IOException, MojoExecutionException {
        String tsClassCode = TypeScriptDomainGenerator.generateTypeScriptClass(sourceClass);
        writeDomainTypeScriptClassToFile(sourceClass.getSimpleName(), tsClassCode);
    }

    private void generateServiceTypescript(Class<?> sourceClass) throws IOException, MojoExecutionException {
        String tsClassCode = TypeScriptServiceGenerator.generateTypeScriptClass(sourceClass, apiVersion, iocTypesPath);
        writeServiceTypeScriptClassToFile(sourceClass.getSimpleName()+"RemoteService", tsClassCode);
    }

    private void createOutputDirIfNeeded(String outputDirectory) {
        File outputFolder = new File(outputDirectory);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
    }

    private void writeDomainTypeScriptClassToFile(String className, String tsClassContent) throws MojoExecutionException {
        createOutputDirIfNeeded(typescriptOutputPackageBase);
        File outputFolder = new File(typescriptOutputPackageBase);
        String filePath = outputFolder.getPath() + File.separator + className + ".ts";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(tsClassContent);
        } catch (IOException e) {
            throw new MojoExecutionException("Erro ao escrever o arquivo Domain TypeScript", e);
        }
    }

    private void writeServiceTypeScriptClassToFile(String className, String tsClassContent) throws MojoExecutionException {
        createOutputDirIfNeeded(typescriptOutputPackageBase+ File.separator+"service");
        File outputFolder = new File(typescriptOutputPackageBase);
        String filePath = outputFolder.getPath() + File.separator+"service"+File.separator + className + ".ts";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(tsClassContent);
        } catch (IOException e) {
            throw new MojoExecutionException("Erro ao escrever o arquivo Service TypeScript", e);
        }
    }

}
