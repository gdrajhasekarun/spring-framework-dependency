package com.home.learning.poc.springlib.service;

import com.home.learning.poc.springlib.annotation.TestDataKeys;
import com.home.learning.poc.springlib.model.Action;
import com.home.learning.poc.springlib.model.Keyword;
import com.home.learning.poc.springlib.model.Steps;
import com.home.learning.poc.springlib.testdata.TestDataInterceptorASM;
import com.home.learning.poc.springlib.testdata.TestDataProvider;
import com.home.learning.poc.springlib.testdata.TestStepInterceptorASM;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

@Service
public class TestCaseExecutor {

    TestDataProvider testDataProvider;
    TestDataInterceptorASM testDataInterceptorASM;
    TestStepInterceptorASM testStepInterceptorASM;

    public TestCaseExecutor(TestDataProvider testDataProvider, TestDataInterceptorASM testDataInterceptorASM, TestStepInterceptorASM testStepInterceptorASM) {
        this.testDataProvider = testDataProvider;
        this.testDataInterceptorASM = testDataInterceptorASM;
        this.testStepInterceptorASM = testStepInterceptorASM;
    }

    public void executeTestcases(List<String> keywordList, Map<String, String> testDataMap) throws Exception {
        Set<Class<?>> keywordClasses = getAllClassesInKeywordPackage("application");
        for (String keyword: keywordList){
            Class<?> testClass = keywordClasses.stream().filter(keywordClass -> this.isMethodFound(keywordClass, keyword)).findFirst().orElse(null);
            if(testClass == null){
                throw new Exception("Unable to Find the keyword");
            }
            this.testDataProvider.setDataProvider(testDataMap);
            Object instance = testClass.getDeclaredConstructor(TestDataProvider.class).newInstance(testDataProvider);
            Method method = testClass.getMethod(keyword);
            method.setAccessible(true);
            method.invoke(instance);
        }
    }

    public List<Keyword> getAllKeywords() throws Exception {
        Set<Class<?>> keywordClasses = getAllClassesInKeywordPackage("application");
        List<Keyword> keywords = new ArrayList<>();
        for(Class<?> keywordClass: keywordClasses){
            Arrays.stream(keywordClass.getMethods())
                    .filter(method -> !method.getDeclaringClass().equals(Object.class))
                    .forEach(method -> {
                Keyword keyword = new Keyword(method.getName());
                if(method.isAnnotationPresent(TestDataKeys.class)){
                    keyword.setTestData(new HashSet<>(Arrays.asList(method.getAnnotation(TestDataKeys.class).value())));
                }
                keywords.add(keyword);
            });
        }
        return keywords;
    }

    public List<Keyword> getAllKeywordsWithoutAnnotation() throws Exception {
        Set<Class<?>> keywordClasses = getAllClassesInKeywordPackage("application");
        List<Keyword> keywordList = new ArrayList<>();
        for(Class<?> keywordClass: keywordClasses){
            this.testDataInterceptorASM.extractTestDataKeys(keywordClass, keywordList);
        }
        return keywordList;
    }

    public List<Steps> getAllKeywordsWithTestSteps() throws Exception {
        Set<Class<?>> keywordClasses = getAllClassesInKeywordPackage("teststeps");
        List<Steps> testSteps = new ArrayList<>();
        for(Class<?> keywordClass: keywordClasses) {
            this.testStepInterceptorASM.analyzeTestSteps(keywordClass, testSteps);
        }
        return testSteps;
    }

    public List<Action> getAllActions(List<String> applications) throws Exception {
        Set<Class<?>> keywordClasses = filterClassName(applications, getAllClassesInKeywordPackage("interactions"));
        List<Action> testSteps = new ArrayList<>();
        for(Class<?> keywordClass: keywordClasses) {
            this.testDataInterceptorASM.extractActionKeys(keywordClass, "interactions", testSteps);
        }
        return testSteps;
    }

    private Set<Class<?>> filterClassName(List<String> applications, Set<Class<?>> classes){
        if(applications == null)
            return classes.stream().filter(clone -> !clone.getName().toLowerCase().contains("springlib")).collect(Collectors.toSet());
        Set<Class<?>> appClasses = new HashSet<>();
        for(String application: applications){
            Set<Class<?>> targetClasses = classes.stream().filter(clsName -> {
//                return applications.contains(clsName.getName().substring(clsName.getName().lastIndexOf(".") + 1 ));
                return clsName.getName().toLowerCase().endsWith(application.toLowerCase());
            }).collect(Collectors.toSet());
            appClasses.addAll(!targetClasses.isEmpty() ? targetClasses:
                    classes.stream().filter(clone -> clone.getName().toLowerCase().contains("springlib")).collect(Collectors.toSet()));
        }
        return appClasses;

    }

    private boolean isMethodFound(Class<?>keywordClass, String method){
        return Arrays.stream(keywordClass.getMethods()).filter(method1 -> method1.getName().equals(method)).findFirst().orElse(null)!=null;
    }

//    private Set<Class<?>> getAllClassesInKeywordPackage() {
//        Reflections reflections = new Reflections( "com.home.learning.poc.cutomannotationpoc.application", Scanners.SubTypes.filterResultsBy( s -> true));
//        return reflections.get( Scanners.SubTypes.of( Object.class).asClass());
//    }


    private Set<Class<?>> getAllClassesInKeywordPackage(String targetPackageName) throws Exception {
        Set<String> packages = getAllPackageNamesFromClasspath();
        Set<String> packageNames = packages.stream().filter(pkg -> pkg.endsWith(targetPackageName)).collect(Collectors.toSet());
        if(packageNames.isEmpty())
            throw new Exception("No Package ends with the name" + targetPackageName);
        Set<Class<?>> classes = new HashSet<>();
        try{
            for (String packageName : packageNames) {
                String path = packageName.replace('.', '/');
                Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);

                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    if (resource.getProtocol().equals("file")) {
                        classes.addAll(findClassesInDirectory(resource.getPath(), packageName));
                    } else if (resource.getProtocol().equals("jar")) {
                        String jarPath = resource.getFile().split("!")[0].substring(5);
                        classes.addAll(findClassesInJar(jarPath, path));
                    }
                }
            }

        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }

    private List<Class<?>> findClassesInDirectory(String directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        java.io.File dir = new java.io.File(directory);
        if (!dir.exists()) return classes;

        for (String file : Objects.requireNonNull(dir.list())) {
            if (file.endsWith(".class")) {
                String className = packageName + '.' + file.substring(0, file.length() - 6);
//                if(className.endsWith("Steps"))
                    classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    private List<Class<?>> findClassesInJar(String jarPath, String packagePath) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(packagePath) && entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    classes.add(Class.forName(className));
                }
            }
        }
        return classes;
    }

    private Set<String> getAllPackageNamesFromClasspath() {
        Set<String> packageNames = new HashSet<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources("");

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    File rootDir = new File(resource.toURI());
                    findPackagesInDirectory(rootDir, "", packageNames);
//                } else if ("jar".equals(protocol)) {
//                    String jarPath = resource.getPath().replaceFirst("^file:", "").replaceAll("!.*$", "");
//                    try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
//                        for (JarEntry entry : Collections.list(jarFile.entries())) {
//                            if (entry.getName().endsWith(".class")) {
//                                String packageName = getPackageNameFromClassPath(entry.getName());
//                                if (packageName != null) {
//                                    packageNames.add(packageName);
//                                }
//                            }
//                        }
//                    }
                }
            }
            String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
            for (String entry : classpathEntries) {
                File file = new File(entry);
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    try (JarFile jarFile = new JarFile(file)) {
                        for (JarEntry jarEntry : Collections.list(jarFile.entries())) {
                            if (jarEntry.getName().endsWith(".class")) {
                                String packageName = getPackageNameFromClassPath(jarEntry.getName());
                                if (packageName != null) {
                                    packageNames.add(packageName);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading classpath packages", e);
        }

        return packageNames;
    }

    private void findPackagesInDirectory(File directory, String basePackage, Set<String> packages) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                String newPackage = basePackage.isEmpty() ? file.getName() : basePackage + "." + file.getName();
                findPackagesInDirectory(file, newPackage, packages);
            } else if (file.getName().endsWith(".class")) {
                if (!basePackage.isEmpty()) {
                    packages.add(basePackage);
                }
            }
        }
    }

    private String getPackageNameFromClassPath(String classPath) {
        if (!classPath.contains("/")) return null;
        int lastSlash = classPath.lastIndexOf('/');
        return classPath.substring(0, lastSlash).replace('/', '.');
    }

}
