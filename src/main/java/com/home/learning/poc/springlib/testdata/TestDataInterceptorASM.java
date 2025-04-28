package com.home.learning.poc.springlib.testdata;

import com.home.learning.poc.springlib.model.Action;
import com.home.learning.poc.springlib.model.Keyword;
import com.home.learning.poc.springlib.model.SubMethod;
import org.objectweb.asm.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TestDataInterceptorASM {

    public void extractActionKeys(Class<?> clazz, String packageName, List<Action> actionList) throws IOException {
        System.out.println("Extracting methods from "+ clazz.getName());
        for (Method method : clazz.getMethods()) {
            if(!(method.getReturnType().equals(void.class) && method.getModifiers()== Modifier.PUBLIC))
                continue;
            Set<String> testDataKeys = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.toSet());
            Action action = new Action(clazz.getName(), method.getName());
            if (!testDataKeys.isEmpty()) {
                action.setTestData(testDataKeys);
            }
            actionList.add(action);
        }

    }

    public void extractTestDataKeys(Class<?> clazz, List<Keyword> keywordList) throws IOException {

        for (Method method : clazz.getDeclaredMethods()) {
            if(!(method.getReturnType().toString().equals("void") && method.getModifiers()== Modifier.PUBLIC))
                continue;
            Set<String> testDataKeys = analyzeMethod(clazz, method.getName());
            Keyword keyword = new Keyword(method.getName());
            if (!testDataKeys.isEmpty()) {
                keyword.setTestData(testDataKeys);
            }
            keywordList.add(keyword);
        }

    }

    public Set<String> analyzeMethod(Class<?> clazz, String methodName) throws IOException {
        Set<String> testDataKeys = new HashSet<>();

        ClassReader classReader = new ClassReader(clazz.getName());
        classReader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (!name.equals(methodName)) return null;

                return new MethodVisitor(Opcodes.ASM9) {
                    private Deque<String> argumentQueue = new ArrayDeque<>();

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String) {
                            argumentQueue.add((String) value);
                        }
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (name.equals("getData") && descriptor.equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")) {
                            if (argumentQueue.size() >= 2) {
                                argumentQueue.poll();
                                testDataKeys.add(argumentQueue.poll());
                            }
                        }
                    }
                };
            }
        }, 0);

        return testDataKeys;
    }

    public List<SubMethod> extractMethodCalls(String methodName, Class<?> clazz, String packageName) throws IOException {
        List<SubMethod> methodCalls = new ArrayList<>();

        ClassReader reader = new ClassReader(clazz.getName());
        reader.accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                if (name.equals(methodName)) {
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name,
                                                    String descriptor, boolean isInterface) {

                            if (owner.startsWith(packageName) &&
                                    !name.equals(methodName) &&
                                    !name.startsWith("access$") &&
                                    opcode == Opcodes.INVOKEVIRTUAL) {
                                try {
                                    methodCalls.add(new SubMethod(Class.forName(owner.replace('/', '.')), name));
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    };
                }
                return null;
            }
        }, 0);

        return methodCalls;
    }

}
