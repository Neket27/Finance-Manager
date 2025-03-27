package app.container;

import app.config.AppProperties;
import app.util.ConfigLoader;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс SimpleIoCContainer предоставляет базовую реализацию контейнера инъекций зависимостей (IoC),
 * который поддерживает регистрацию компонентов и их автоматическое создание с учетом зависимостей.
 */
public class SimpleIoCContainer {

    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceToImplementation = new HashMap<>();

    /**
     * Автоматически регистрирует компоненты и конфигурации в контейнере на основе аннотаций.
     *
     * @param basePackage пакет, в котором производится сканирование классов
     * @throws Exception если возникает ошибка при создании экземпляров или регистрации компонентов
     */
    public void autoRegister(String basePackage) throws Exception {
        List<Class<?>> classes = ClassScanner.findAllComponentAndConfigClasses(basePackage);

        // Сначала загружаем конфиг и добавляем его в контейнер
        AppProperties appProperties = ConfigLoader.loadConfig("application.yml", AppProperties.class);
        instances.put(AppProperties.class, appProperties);
        instances.put(AppProperties.DbProperties.class, appProperties.getDb());
        instances.put(AppProperties.LiquibaseProperties.class, appProperties.getLiquibase());

        AppProperties.LiquibaseProperties k = getInstance(AppProperties.LiquibaseProperties.class);
        // Регистрация интерфейсов и их реализаций
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            if (clazz.isAnnotationPresent(app.container.Component.class)) {
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> iface : interfaces) {
                    interfaceToImplementation.put(iface, clazz);
                }
            }
        }

        // Регистрация конфигураций
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            if (clazz.isAnnotationPresent(app.container.Configuration.class)) {
                Object configInstance = createWithDependencies(clazz);

                // регистрируем сам конфигурационный класс
                register(clazz, configInstance);

                // Обрабатываем @Bean методы
                processConfiguration(configInstance);
            }
        }


        // Создание компонентов
        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            if (clazz.isAnnotationPresent(app.container.Component.class)) {
                if (!instances.containsKey(clazz)) {
                    Object instance = createWithDependencies(clazz);
                    register(clazz, instance);
                }
            }
        }
    }

    /**
     * Создает экземпляр класса с учетом его зависимостей.
     *
     * @param clazz класс, экземпляр которого нужно создать
     * @return созданный экземпляр класса
     * @throws Exception если не удается создать экземпляр класса
     */
    private Object createWithDependencies(Class<?> clazz) throws Exception {
        if(clazz.equals(AppProperties.LiquibaseProperties.class))
            System.out.println("hjkl");

        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] parameters = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                parameters[i] = getInstance(paramTypes[i]);
            }
            return constructor.newInstance(parameters);
        }
        throw new RuntimeException("No suitable constructor found for " + clazz);
    }

    /**
     * Регистрирует экземпляр класса в контейнере и связывает его с интерфейсами.
     *
     * @param clazz    класс, который нужно зарегистрировать
     * @param instance экземпляр класса
     * @throws Exception если ошибка при регистрации
     */
    public void register(Class<?> clazz, Object instance) throws Exception {
        instances.put(clazz, instance);

        // Регистрируем под интерфейсами
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> iface : interfaces) {
            interfaceToImplementation.put(iface, clazz);
            instances.put(iface, instance);
        }

        // Вызов методов с @PostConstruct
        for (var method : clazz.getDeclaredMethods()) {
            AppProperties.LiquibaseProperties k = getInstance(AppProperties.LiquibaseProperties.class);
            if (method.isAnnotationPresent(app.container.PostConstruct.class)) {
                method.setAccessible(true);
                method.invoke(instance);
            }
        }
    }

    /**
     * Получает экземпляр класса из контейнера, создавая его при необходимости.
     *
     * @param clazz класс, экземпляр которого нужно получить
     * @param <T>   тип класса
     * @return экземпляр класса
     * @throws Exception если ошибка при создании экземпляра или при его получении
     */
    public <T> T getInstance(Class<T> clazz) throws Exception {
        if (instances.containsKey(clazz)) {
            return (T) instances.get(clazz);
        }

        if (clazz.isInterface()) {
            Class<?> implClass = interfaceToImplementation.get(clazz);
            if (implClass == null) {
                throw new RuntimeException("No implementation registered for interface: " + clazz);
            }
            return getInstance((Class<T>) implClass);
        }

        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] parameters = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                parameters[i] = getInstance(paramTypes[i]);
            }
            T instance = (T) constructor.newInstance(parameters);
            register(clazz, instance);
            return instance;
        }

        throw new RuntimeException("No suitable constructor found for " + clazz);
    }

    /**
     * Обрабатывает конфигурационные классы и регистрирует их компоненты.
     *
     * @param config объект конфигурации
     * @throws Exception если ошибка при обработке конфигурации
     */
    public void processConfiguration(Object config) throws Exception {
        Class<?> clazz = config.getClass();

        for (var method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(app.container.Bean.class)) {
                Object bean = method.invoke(config);
                Class<?> returnType = method.getReturnType();
                register(returnType, bean);

                // Если бин реализует интерфейс — связываем интерфейс с реализацией
                Class<?>[] interfaces = returnType.getInterfaces();
                if (interfaces.length > 0) {
                    interfaceToImplementation.put(interfaces[0], returnType);
                }
            }
        }
    }
}
