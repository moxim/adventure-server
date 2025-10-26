package com.pdg.adventure.view.support;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility class to provide access to Spring ApplicationContext for non-Spring-managed components.
 * This is useful for Vaadin components that are not created by Spring and need access to Spring beans.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    /**
     * Get the Spring ApplicationContext.
     * @return the ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Get a bean by type from the ApplicationContext.
     * @param beanClass the class of the bean
     * @param <T> the type of the bean
     * @return the bean instance
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * Get a bean by name and type from the ApplicationContext.
     * @param beanName the name of the bean
     * @param beanClass the class of the bean
     * @param <T> the type of the bean
     * @return the bean instance
     */
    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return context.getBean(beanName, beanClass);
    }
}
