package app.container;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс ClassScanner предоставляет методы для поиска всех классов с аннотациями {@link app.container.Component}
 * и {@link app.container.Configuration} в указанном пакете.
 */
public class ClassScanner {

    /**
     * Находит все классы с аннотациями {@link app.container.Component} и {@link app.container.Configuration} в указанном пакете.
     *
     * @param packageName имя пакета, в котором нужно выполнить поиск классов
     * @return список классов, имеющих аннотации {@link app.container.Component} или {@link app.container.Configuration}
     * @throws Exception если происходит ошибка при загрузке классов
     */
    public static List<Class<?>> findAllComponentAndConfigClasses(String packageName) throws Exception {
        String path = packageName.replace('.', '/');
        URL root = Thread.currentThread().getContextClassLoader().getResource(path);

        if (root == null) {
            throw new RuntimeException("Package " + packageName + " not found!");
        }

        List<Class<?>> classes = new ArrayList<>();
        scanDirectory(new File(root.toURI()), packageName, classes);
        return classes;
    }

    /**
     * Рекурсивно сканирует директории для поиска классов с аннотациями {@link app.container.Component}
     * и {@link app.container.Configuration}.
     *
     * @param dir директория для сканирования
     * @param packageName имя пакета для формирования полных имен классов
     * @param classes список для добавления найденных классов
     * @throws Exception если ошибка при загрузке классов
     */
    private static void scanDirectory(File dir, String packageName, List<Class<?>> classes) throws Exception {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                // Рекурсивно спускаемся в поддиректории
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(app.container.Component.class)
                        || clazz.isAnnotationPresent(app.container.Configuration.class)) {
                    classes.add(clazz);
                }
            }
        }
    }
}
