package com.pdg.adventure.server.annotation;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.server.support.MapperSupporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Enhanced BeanPostProcessor that automatically registers mappers annotated with @AutoRegisterMapper.
 * This version automatically detects the generic types from the Mapper interface implementation.
 */
@Component
@Order(1001) // Process after the basic MapperRegistrationProcessor
public class AutoMapperRegistrationProcessor implements BeanPostProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoMapperRegistrationProcessor.class);
    
    private final MapperSupporter mapperSupporter;
    private final List<PendingAutoRegistration> pendingRegistrations = new ArrayList<>();
    
    public AutoMapperRegistrationProcessor(MapperSupporter mapperSupporter) {
        this.mapperSupporter = mapperSupporter;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        AutoRegisterMapper annotation = AnnotationUtils.findAnnotation(bean.getClass(), AutoRegisterMapper.class);
        
        if (annotation != null && bean instanceof Mapper) {
            Class<?>[] genericTypes = resolveMapperGenericTypes(bean.getClass());
            
            if (genericTypes != null && genericTypes.length == 2) {
                pendingRegistrations.add(new PendingAutoRegistration(
                    genericTypes[0], // DO (Data Object)
                    genericTypes[1], // BO (Business Object)
                    (Mapper<?, ?>) bean,
                    annotation.priority(),
                    beanName,
                    annotation.description()
                ));
                
                logger.debug("Queued auto-mapper {} for registration: {} <-> {} (priority: {})", 
                    beanName, genericTypes[0].getSimpleName(), genericTypes[1].getSimpleName(), annotation.priority());
            } else {
                logger.warn("Could not resolve generic types for mapper {}", beanName);
            }
        }
        
        return bean;
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // Check if this is the MapperSupporter being fully initialized
        if (bean instanceof MapperSupporter && "mapperSupporter".equals(beanName)) {
            registerAllPendingAutoMappers();
        }
        
        return bean;
    }
    
    private Class<?>[] resolveMapperGenericTypes(Class<?> mapperClass) {
        // Try using Spring's GenericTypeResolver first
        Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(mapperClass, Mapper.class);
        
        if (genericTypes != null) {
            return genericTypes;
        }
        
        // Fallback: manual resolution
        return resolveGenericTypesManually(mapperClass);
    }
    
    private Class<?>[] resolveGenericTypesManually(Class<?> mapperClass) {
        Type[] genericInterfaces = mapperClass.getGenericInterfaces();
        
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericInterface;
                
                if (paramType.getRawType().equals(Mapper.class)) {
                    Type[] typeArguments = paramType.getActualTypeArguments();
                    
                    if (typeArguments.length == 2) {
                        Class<?>[] result = new Class<?>[2];
                        result[0] = (Class<?>) typeArguments[0]; // DO
                        result[1] = (Class<?>) typeArguments[1]; // BO
                        return result;
                    }
                }
            }
        }
        
        // Check superclass if not found in interfaces
        Class<?> superclass = mapperClass.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class)) {
            return resolveGenericTypesManually(superclass);
        }
        
        return null;
    }
    
    private void registerAllPendingAutoMappers() {
        if (pendingRegistrations.isEmpty()) {
            return;
        }
        
        // Sort by priority (lower numbers first)
        pendingRegistrations.sort(Comparator.comparingInt(reg -> reg.priority));
        
        logger.info("Auto-registering {} mappers...", pendingRegistrations.size());
        
        for (PendingAutoRegistration registration : pendingRegistrations) {
            try {
                mapperSupporter.registerMapper(
                    registration.dataObjectClass,
                    registration.businessObjectClass,
                    registration.mapper
                );
                
                String description = !registration.description.isEmpty() ? 
                    " (" + registration.description + ")" : "";
                
                logger.debug("Auto-registered mapper {} for {} -> {}{}",
                    registration.beanName,
                    registration.dataObjectClass.getSimpleName(),
                    registration.businessObjectClass.getSimpleName(),
                    description
                );
                
            } catch (Exception e) {
                logger.error("Failed to auto-register mapper {}: {}", registration.beanName, e.getMessage(), e);
            }
        }
        
        pendingRegistrations.clear();
        logger.info("Completed automatic mapper auto-registration");
    }
    
    private static class PendingAutoRegistration {
        final Class<?> dataObjectClass;
        final Class<?> businessObjectClass;
        final Mapper<?, ?> mapper;
        final int priority;
        final String beanName;
        final String description;
        
        PendingAutoRegistration(Class<?> dataObjectClass, Class<?> businessObjectClass, 
                               Mapper<?, ?> mapper, int priority, String beanName, String description) {
            this.dataObjectClass = dataObjectClass;
            this.businessObjectClass = businessObjectClass;
            this.mapper = mapper;
            this.priority = priority;
            this.beanName = beanName;
            this.description = description;
        }
    }
}